/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.common.jmx;

import net.gcolin.common.Logs;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/**
 * JMX helper.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Jmx {

  private static final String CANNOT_REGISTER_MBEAN = "cannot register mbean";
private static final Logger LOG = Logger.getLogger(Jmx.class.getName());

  private Jmx() {}

  /**
   * Publish a JmxBean.
   * 
   * @param <T> interface type
   * @param jmxname JMX path
   * @param implementation the implementation of type bean
   * @param mbeanInterface the interface accessible through a JMX console 
   */
  public static <T> void publish(String jmxname, T implementation, Class<T> mbeanInterface) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName(jmxname);
      if (!mbs.isRegistered(name)) {
        StandardMBean mbean = new StandardMBean(implementation, mbeanInterface);
        mbs.registerMBean(mbean, name);
      }
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException
        | MBeanRegistrationException | NotCompliantMBeanException ex) {
      LOG.log(Level.SEVERE, CANNOT_REGISTER_MBEAN, ex);
    }
  }

  /**
   * Remove a JmxBean.
   * 
   * @param jmxname JMX path
   */
  public static void unpublish(String jmxname) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName(jmxname);
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
    } catch (MalformedObjectNameException | MBeanRegistrationException
        | InstanceNotFoundException ex) {
      LOG.log(Level.SEVERE, CANNOT_REGISTER_MBEAN, ex);
    }
  }

  /**
   * Enable Common Io bean for managing the buffers.
   */
  public static void enable() {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName("net.gcolin.common:type=IO");
      StandardMBean mbean = new StandardMBean(new IoController(), IoBean.class);
      mbs.registerMBean(mbean, name);
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException
        | MBeanRegistrationException | NotCompliantMBeanException ex) {
      Logs.LOG.log(Level.SEVERE, CANNOT_REGISTER_MBEAN, ex);
    }
  }
}
