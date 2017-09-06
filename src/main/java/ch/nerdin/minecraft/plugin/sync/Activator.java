package ch.nerdin.minecraft.plugin.sync;

import fi.iki.elonen.NanoWSD;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.File;

public class Activator implements BundleActivator {
  private NanoWSD server = new FileSyncWSD(7791, new File("."));

  @Override
  public void start(BundleContext context) throws Exception {
    server.start();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    server.stop();
  }

}

