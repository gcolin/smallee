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
