package net.gcolin.juikito;

@FunctionalInterface
public interface Getter {

  <T> T get(Class<T> type);
  
}
