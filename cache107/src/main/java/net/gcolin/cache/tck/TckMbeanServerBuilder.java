/*
 * Copyright 2011-2013 Terracotta, Inc. Copyright 2011-2013 Oracle America Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.cache.tck;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * Taken from RI.
 * 
 * @author Alex Snaps
 * @since 1.0
 */
public class TckMbeanServerBuilder extends MBeanServerBuilder {

  /**
   * Empty public constructor as required.
   */
  public TckMbeanServerBuilder() {
    super();
  }

  @Override
  public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer,
      MBeanServerDelegate delegate) {
    MBeanServerDelegate decoratingDelegate = new RimBeanServerDelegate(delegate);
    return super.newMBeanServer(defaultDomain, outer, decoratingDelegate);
  }

  /**
   * A decorator around the MBeanServerDelegate which sets the mBeanServerId to the value of the
   * <code>org.jsr107.tck.management.agentId</code> system property so that the TCK can precisely
   * identify the correct MBeanServer when running tests.
   */
  public static class RimBeanServerDelegate extends MBeanServerDelegate {

    private MBeanServerDelegate delegate;

    /**
     * Constructor.
     *
     * @param delegate the provided delegate
     */
    public RimBeanServerDelegate(MBeanServerDelegate delegate) {
      this.delegate = delegate;
    }

    @Override
    public String getSpecificationName() {
      return delegate.getSpecificationName();
    }

    @Override
    public String getSpecificationVersion() {
      return delegate.getSpecificationVersion();
    }

    @Override
    public String getSpecificationVendor() {
      return delegate.getSpecificationVendor();
    }

    @Override
    public String getImplementationName() {
      return delegate.getImplementationName();
    }

    @Override
    public String getImplementationVersion() {
      return delegate.getImplementationVersion();
    }

    @Override
    public String getImplementationVendor() {
      return delegate.getImplementationVendor();
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
      return delegate.getNotificationInfo();
    }

    @Override
    public synchronized void addNotificationListener(NotificationListener listener,
        NotificationFilter filter, Object handback) {
      delegate.addNotificationListener(listener, filter, handback);
    }

    @Override
    public synchronized void removeNotificationListener(NotificationListener listener,
        NotificationFilter filter, Object handback) throws ListenerNotFoundException {
      delegate.removeNotificationListener(listener, filter, handback);
    }

    @Override
    public synchronized void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
      delegate.removeNotificationListener(listener);
    }

    @Override
    public void sendNotification(Notification notification) {
      delegate.sendNotification(notification);
    }

    @Override
    public synchronized String getMBeanServerId() {
      return System.getProperty("org.jsr107.tck.management.agentId");
    }
  }


}
