/*
 * Copyright (c) 2014, Oracle America, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.gcolin.juikito;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.gcolin.di.atinject.Environment;

@State(Scope.Benchmark)
public class AbstractBenchmark {
    
  protected Getter atinject;
  protected Getter guice;
  protected Getter weld;
  
  protected void init(Class<?>... classes) {
    atinject = atinject(classes);
    guice = guice(classes);
    weld = weld(classes);
  }

  public Getter atinject(Class<?>... classes) {
    Environment env = new Environment();
    env.add(classes);
    return new Getter() {

      @Override
      public <T> T get(Class<T> type) {
        return env.get(type);
      }
    };
  }

  public Getter guice(Class<?>... classes) {
    Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        for (Class<?> clazz : classes) {
          bind(clazz);
        }
      }

    });
    return new Getter() {

      @Override
      public <T> T get(Class<T> type) {
        return injector.getInstance(type);
      }
    };
  }

  public Getter weld(Class<?>... classes) {
    Weld weld = new Weld();
    for (Class<?> clazz : classes) {
      weld.addBeanClass(clazz);
    }
    WeldContainer container = weld.initialize();

    return new Getter() {

      @Override
      public <T> T get(Class<T> type) {
        return container.select(type).get();
      }
    };
  }
  
}
