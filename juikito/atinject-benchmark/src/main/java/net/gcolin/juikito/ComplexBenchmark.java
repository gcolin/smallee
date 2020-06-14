package net.gcolin.juikito;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 2)
@Measurement(iterations = 2)
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

		D d1 = a.d.get();
		D d2 = a.d.get();
		assert d1 != null && d1 == d2;

		D d = a.d.get();
		assert d.c != null;

		B b1 = d.c.b.get();
		B b2 = d.c.b.get();
		assert b1 != null && b1 != b2;
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
