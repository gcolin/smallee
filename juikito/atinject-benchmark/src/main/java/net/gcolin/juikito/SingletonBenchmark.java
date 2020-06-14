package net.gcolin.juikito;

import javax.inject.Singleton;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
public class SingletonBenchmark extends AbstractBenchmark {

	@Singleton
	public static class A {

	}

	private void test(Getter getter) {
		A a1 = getter.get(A.class);
		A a2 = getter.get(A.class);
		assert a1 == a2 && a1 != null;
	}

	@Benchmark
	public void testAtinject() {
		test(atinject(A.class));
	}

	@Benchmark
	public void testGuice() {
		test(guice(A.class));
	}

	@Benchmark
	public void testWeld() {
		test(weld(A.class));
	}

}
