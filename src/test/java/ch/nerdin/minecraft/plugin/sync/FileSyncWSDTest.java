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
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by edewit on 9/5/17.
 */
public class FileSyncWSDTest {
  private static NanoWSD server;

  @Before
  public void setUp() throws Exception {
    server = new FileSyncWSD(9191, new File("."));
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void shouldSyncFiles() throws Exception {
    // given
    URI uri = new URI("ws://localhost:9191");

    WebSocketClient client = new WebSocketClient();
    TestSocket socket = new TestSocket();

    Git git = Git.open(new File("."));
    git.diff().setOutputStream( System.out ).call();

    socket.getToSendMessages().add("Hello");
    socket.getToSendMessages().add("Thanks for the conversation.");

    try {
      client.start();
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      client.connect(socket, uri, request);
      System.out.printf("Connecting to : %s%n", uri);
      socket.awaitClose(5, TimeUnit.SECONDS);
    } finally {
      try {
        client.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    assertEquals("Hello", socket.getReceivedMessages().get(0));
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