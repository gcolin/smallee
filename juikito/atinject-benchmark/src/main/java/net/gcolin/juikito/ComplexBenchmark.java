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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations=5)
@Measurement(iterations=5)
@Fork(1)
public class ComplexBenchmark extends AbstractBenchmark {

  public static class A {
    
    @Inject
    Provider<D> d;
    
  }
  
  public static class B {
    
  }
  
  @Singleton
  public static class C {
    
    @Inject
    Provider<B> b;
    
  }
  
  @Singleton
  public static class D {
    
    @Inject
    C c;
    
  }
  
  @Setup
  public void setup() {
    init(A.class, B.class, C.class, D.class);
  }
  
  private void test(Getter getter) {
    A a = getter.get(A.class);
    assert a != null;
    assert a.d.get() != null && a.d.get() == a.d.get();
    
    D d = a.d.get();
    assert d.c != null;
    
    assert d.c.b.get() != null && d.c.b.get() != d.c.b.get();
  }

  @Benchmark
  public void testAtinject() {
    test(atinject);
  }

  @Benchmark
  public void testGuice() {
    test(guice);
  }


  @Benchmark
  public void testWeld() {
    test(weld);
  }
  
  public static void main(String[] args) {
    ComplexBenchmark c = new ComplexBenchmark();
    c.test(c.atinject(A.class, B.class, C.class, D.class));
    c.test(c.guice(A.class, B.class, C.class, D.class));
    c.test(c.weld(A.class, B.class, C.class, D.class));
  }

}
