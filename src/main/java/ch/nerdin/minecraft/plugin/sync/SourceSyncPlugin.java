package ch.nerdin.minecraft.plugin.sync;

import com.google.inject.Inject;
import fi.iki.elonen.NanoWSD;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Plugin(id = "source-sync-plugin", name = "Source Sync Plugin",
    description = "Make sure your current changes are running", version = "1.0")
public class SourceSyncPlugin {
  @Inject
  private Logger logger;
  private NanoWSD server = new FileSyncWSD(PORT, new File("src"));

  private static final int PORT = 7791;

  @Listener
  public void onServerStart(GameStartedServerEvent event) throws IOException {
    logger.info("started sync server on port '{}'", PORT);
    server.start();
  }

  @Listener
  public void onServerStop(GameStoppedServerEvent event) throws IOException {
    server.stop();
  }
}

