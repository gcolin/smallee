package net.gcolin.juikito;

import javax.inject.Singleton;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
public class SingletonBenchmarkSetup extends AbstractBenchmark {

	@Singleton
	public static class A {

	}

	@Setup
	public void setup() {
		init(A.class);
	}

	private void test(Getter getter) {
		A a1 = getter.get(A.class);
		A a2 = getter.get(A.class);
		assert a1 == a2 && a1 != null;
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

}
