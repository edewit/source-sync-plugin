package ch.nerdin.minecraft.plugin.sync;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import fi.iki.elonen.NanoWSD;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spongepowered.api.event.EventListener;

import java.io.File;

public class Activator implements BundleActivator {
  private NanoWSD server = new FileSyncWSD(7791, new File("."));

  @Override
  public void start(BundleContext context) throws Exception {
    server.start();
    context.registerService(CommandRegistration.class, new HelloWorldCommand(), null);
    context.registerService(EventListener.class, new ExampleEventListener(), null);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    server.stop();
  }

}

