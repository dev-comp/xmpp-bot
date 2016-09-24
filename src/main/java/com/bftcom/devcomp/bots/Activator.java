package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.adapters.XmppAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Activator implements BundleActivator, ServiceListener {
  Thread botThread;
  @SuppressWarnings("PackageAccessibility")
  public static final Logger logger = LoggerFactory.getLogger(Activator.class);
  private XmppAdapter xmppAdapter;

  public void start(BundleContext bundleContext) throws Exception {
    logger.info("bundle fucking " + bundleContext.getBundle().getSymbolicName() + "started");
    xmppAdapter = new XmppAdapter();
  }

  public void stop(BundleContext bundleContext) throws Exception {
    xmppAdapter.stopAllBotSessions();
    logger.info("bundle " + bundleContext.getBundle().getSymbolicName() + " stopped");
  }

  @Override
  public void serviceChanged(ServiceEvent event) {

  }
}
