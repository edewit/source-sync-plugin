package ch.nerdin.minecraft.plugin.sync;

import fi.iki.elonen.NanoWSD;
import org.eclipse.jgit.api.ApplyCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.Patch;

import java.io.File;
import java.io.IOException;

/**
 * Websocket server that receives file diffs and applies them to a folder, thus keeping files in sync.
 */
class FileSyncWSD extends NanoWSD {
  FileSyncWSD(int port, File folder) {
    super(port);
  }

  @Override
  protected WebSocket openWebSocket(IHTTPSession handshake) {
    return new SyncWebSocket(handshake);
  }

  private static class SyncWebSocket extends WebSocket {
    public SyncWebSocket(IHTTPSession handshake) {
      super(handshake);
    }

    @Override
    protected void onOpen() {
    }

    @Override
    protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
    }

    @Override
    protected void onMessage(WebSocketFrame message) {
      //Patch patch = message.getTextPayload();
//      DiffEntry
//      Patch patch = new Patch();
//      patch.parse();
//      Repository repo;
//      ApplyCommand command = new ApplyCommand(repo);
//      command.setPatch()
    }

    @Override
    protected void onPong(WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {
      throw new RuntimeException(exception);
    }
  }
}
