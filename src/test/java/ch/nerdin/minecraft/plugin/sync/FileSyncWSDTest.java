package ch.nerdin.minecraft.plugin.sync;

import fi.iki.elonen.NanoWSD;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by edewit on 9/5/17.
 */
public class FileSyncWSDTest {
  private static NanoWSD server;
  private File repoLocation = new File(".");

  @Before
  public void setUp() throws Exception {
    server = new FileSyncWSD(9191, repoLocation);
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
    Git git = Git.open(repoLocation);
    git.checkout().setForce(true).addPath(repoLocation.getPath()).call();
    git.close();
  }

  @Test
  public void shouldSyncFiles() throws Exception {
    // given
    URI uri = new URI("ws://localhost:9191");

    WebSocketClient client = new WebSocketClient();
    TestSocket socket = new TestSocket();

    String patch = convertToString("/test.patch");
    socket.getToSendMessages().add(patch);

    try {
      client.start();
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      client.connect(socket, uri, request);
      socket.awaitClose(5, TimeUnit.SECONDS);
    } finally {
      client.stop();
    }

    assertEquals("applied", socket.getReceivedMessages().get(0));
    try (InputStream is = Paths.get("src/test/resources/file.txt").toUri().toURL().openConnection().getInputStream()) {
      assertEquals("This is a test file and has been changed.", convertToString(is));
    }
  }

  private String convertToString(String resource) {
    return convertToString(getClass().getResourceAsStream(resource));
  }

  private String convertToString(InputStream diffStream) {
    return new BufferedReader(new InputStreamReader(diffStream))
        .lines().collect(Collectors.joining("\n"));
  }


  @WebSocket(maxTextMessageSize = 64 * 1024)
  public class TestSocket {
    private final List<String> receivedMessages = new ArrayList<String>();

    private final List<String> toSendMessages = new ArrayList<String>();

    private final CountDownLatch closeLatch;

    public TestSocket() {
      this.closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
      return this.closeLatch.await(duration, unit);
    }

    public List<String> getReceivedMessages() {
      return this.receivedMessages;
    }

    public List<String> getToSendMessages() {
      return this.toSendMessages;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
      System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
      this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
      System.out.printf("Got connect: %s%n", session);
      try {
        Future<Void> fut;

        for (String message : this.toSendMessages) {
          fut = session.getRemote().sendStringByFuture(message);
          fut.get(5, TimeUnit.SECONDS);
        }
        session.close(StatusCode.NORMAL, "I'm done");
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
      System.out.printf("Got msg: %s%n", msg);
      this.receivedMessages.add(msg);
    }
  }

}