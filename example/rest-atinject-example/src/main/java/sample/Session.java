package sample;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import net.gcolin.di.atinject.web.SessionScoped;

@SessionScoped
public class Session implements Serializable {

  private static final long serialVersionUID = 3357429008958007526L;
  
  private AtomicInteger counter = new AtomicInteger(0);
  
  public int next() {
    return counter.incrementAndGet();
  }
  
}
