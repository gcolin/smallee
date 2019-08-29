package net.gcolin.json.test.json;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Assert;
import org.junit.Test;

public class ReadWhiteTest {

	@Test
	public void test() throws Exception {
        Reader rfc6901Reader = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("/white.json"));
        JsonReader reader = Json.createReader(rfc6901Reader);
        JsonObject value = reader.readObject();
        reader.close();
        Assert.assertTrue(value.containsKey(" "));
    }
	
}
