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

package net.gcolin.di.atinject;

/**
 * The extension interface for plugins.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public interface Extension extends HasPriority {
  
  /**
   * Load classes, add custom providers and builders
   * 
   * @param environment environment
   */
  default void doStart(Environment environment) {};

  /**
   * Called after the environment is configured. You can create instances here.
   * 
   * @param environment environment
   */
  default void doStarted(Environment environment) {};
  
  /**
   * Before the environment is stopped.
   * 
   * @param environment environment
   */
  default void doStop(Environment environment) {};
  
  /**
   * After the environment is stopped.
   * 
   * @param environment environment
   */
  default void doStopped(Environment environment) {};
  
}
