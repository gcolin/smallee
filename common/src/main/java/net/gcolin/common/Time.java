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

package net.gcolin.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small utility class to test speed.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Time {

  private static long init;
  private static boolean mute;
  private static final Logger LOG = LoggerFactory.getLogger(Time.class);

  private Time() {}

  /**
   * Start the time in milliseconds.
   */
  public static void tick() {
    init = System.currentTimeMillis();
  }

  /**
   * Mute the log.
   * 
   * @param mute : true for hidding log messages
   */
  public static void mute(boolean mute) {
    Time.mute = mute;
  }

  /**
   * Stop the timer in milliseconds.
   * 
   * @param msg : message to display
   * @return the delta time.
   */
  public static long tock(String msg) {
    if (LOG.isInfoEnabled()) {
      long time = System.currentTimeMillis() - init;
      if (!mute) {
        LOG.info(time + " : " + msg + " in " + time + " ms");
      }
      return time;
    }
    return 0L;
  }

  /**
   * Start the time in nanoseconds.
   */
  public static void ticknano() {
    init = System.nanoTime();
  }

  /**
   * Stop the timer in nanoseconds.
   * 
   * @param msg : message to display
   * @return the delta time.
   */
  public static long tocknano(String msg) {
    if (LOG.isInfoEnabled()) {
      long time = System.nanoTime() - init;
      if (!mute) {
        LOG.info(time + " : " + msg + " in " + time + " nano");
      }
      return time;
    }
    return 0L;
  }
}
