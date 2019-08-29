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

package net.gcolin.di.atinject.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.event.EventExtension;
import net.gcolin.di.atinject.producer.ProducerExtension;
import net.gcolin.di.atinject.web.RequestScoped;
import net.gcolin.di.atinject.web.WebExtension;

/**
 * An extension for enabling some CDI features:
 * ApplicationScoped, RequestScoped, SessionScoped,
 * Disposes, Produces, Event, Observes
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CDIExtension implements Extension {
	
  @Override
  public void doStart(Environment env) {
    for (Extension extension : env.getExtensions()) {
      if (extension instanceof WebExtension) {
        WebExtension web = (WebExtension) extension;
        web.getApplicationAnnotations().add(ApplicationScoped.class);
        web.getRequestAnnotations().add(RequestScoped.class);
        web.getSessionAnnotations().add(SessionScoped.class);
      } else if (extension instanceof ProducerExtension) {
        ProducerExtension producer = (ProducerExtension) extension;
        producer.getDisposesAnnotations().add(Disposes.class);
        producer.getProducesAnnotations().add(Produces.class);
      } else if (extension instanceof EventExtension) {
        EventExtension event = (EventExtension) extension;
        event.getObservesType().add(Observes.class);
        env.addResolver(new EventResolver(event.getEvents(), env));
      }
    }
  }
  
  @Override
  public int priority() {
    return 0;
  }

}
