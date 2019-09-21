package net.gcolin.json.test.json;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.json.Json;

import org.junit.Assert;
import org.junit.Test;

public class TestEscapeChar {
	@Test
	public void test() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Json.createGenerator(bout).writeStartObject().write("a", "D:\\hello\\world.txt").writeEnd().close();
		Assert.assertEquals("{\"a\":\"D:\\\\hello\\\\world.txt\"}", new String(bout.toByteArray(), StandardCharsets.UTF_8));
	}

}
