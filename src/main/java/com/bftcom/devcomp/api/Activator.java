package com.bftcom.devcomp.api;

import org.osgi.framework.*;

/**
 * todo@shapoval add class description
 * <p>
 * date: 24.09.2016
 *
 * @author p.shapoval
 */
public class Activator implements  BundleActivator, ServiceListener {
  
  @Override
  public void serviceChanged(ServiceEvent event) {
      
  }

  @Override
  public void start(BundleContext context) throws Exception {
    System.out.println("bundle started");
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    System.out.println("bundle stopped");
  }
}
