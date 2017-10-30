package ch.nerdin.minecraft.plugin.sync;

import fi.iki.elonen.NanoWSD;
import org.eclipse.jgit.api.ApplyCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Websocket server that receives file diffs and applies them to a folder, thus keeping files in sync.
 */
class FileSyncWSD extends NanoWSD {
  private final File folder;

  FileSyncWSD(int port, File folder) {
    super(port);
    this.folder = folder;
  }

  @Override
  protected WebSocket openWebSocket(IHTTPSession handshake) {
    return new SyncWebSocket(handshake, folder);
  }

  private static class SyncWebSocket extends WebSocket {
    private Git git;
    private final File folder;
    SyncWebSocket(IHTTPSession handshake, File folder) {
      super(handshake);
      this.folder = folder;
    }

    @Override
    protected void onOpen() {
      try {
        git = Git.open(folder);
      } catch (IOException e) {
        throw new RuntimeException("could not sync with folder", e);
      }
    }

    @Override
    protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
      git.close();
    }

    @Override
    protected void onMessage(WebSocketFrame message) {
      revertExistingChanges();
      String diff = message.getTextPayload();
      ApplyCommand apply = git.apply();
      apply.setPatch(new ByteArrayInputStream(diff.getBytes()));
      try {
        apply.call();
        send("applied");
        close(WebSocketFrame.CloseCode.NormalClosure, "where done here", false);
      } catch (Exception e) {
        onException(new IOException(e));
      }
    }

    private void revertExistingChanges() {
      try {
        Status status = git.status().call();
        CheckoutCommand checkoutCommand = git.checkout().setForce(true);
        for (String modified : status.getModified()) {
          checkoutCommand.addPath(modified);
        }
        checkoutCommand.call();
      } catch (GitAPIException e) {
        onException(new IOException(e));
      }
    }

    @Override
    protected void onPong(WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {
      exception.printStackTrace();
    }
  }
}
