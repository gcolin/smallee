/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.common.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.io.FastInputStreamReader;
import net.gcolin.common.io.FastOutputStreamWriter;
import net.gcolin.common.io.Io;

public class IoTest {

	private static final Logger LOG = Logger.getLogger(IoTest.class.getName());

	@Test
	public void poolByteTest() {
		byte[] bt = Io.takeBytes();
		Assert.assertNotNull(bt);
		Assert.assertEquals(Io.BUFFER_SIZE, bt.length);

		List<byte[]> list = new ArrayList<>();
		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeBytes();
			Assert.assertNotNull(bt);
			Assert.assertEquals(Io.BUFFER_SIZE, bt.length);
			list.add(bt);
		}

		List<byte[]> listoverflow = new ArrayList<>();
		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeBytes();
			Assert.assertNotNull(bt);
			Assert.assertEquals(Io.BUFFER_SIZE, bt.length);
			listoverflow.add(bt);
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			Io.recycleBytes(list.get(i));
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			Io.recycleBytes(listoverflow.get(i));
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeBytes();
			Assert.assertTrue(list.contains(bt));
			Assert.assertFalse(listoverflow.contains(bt));
		}

	}

	@Test
	public void poolCharTest() {
		char[] bt = Io.takeChars();
		Assert.assertNotNull(bt);
		Assert.assertEquals(Io.BUFFER_SIZE, bt.length);

		List<char[]> list = new ArrayList<>();
		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeChars();
			Assert.assertNotNull(bt);
			Assert.assertEquals(Io.BUFFER_SIZE, bt.length);
			list.add(bt);
		}

		List<char[]> listoverflow = new ArrayList<>();
		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeChars();
			Assert.assertNotNull(bt);
			Assert.assertEquals(Io.BUFFER_SIZE, bt.length);
			listoverflow.add(bt);
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			Io.recycleChars(list.get(i));
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			Io.recycleChars(listoverflow.get(i));
		}

		for (int i = 0; i < Io.POOL_SIZE; i++) {
			bt = Io.takeChars();
			Assert.assertTrue(list.contains(bt));
			Assert.assertFalse(listoverflow.contains(bt));
		}

	}

	@Test
	public void toByteArrayTest() throws IOException {
		byte[] bt = "hello".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bin = new ByteArrayInputStream(bt);
		Assert.assertArrayEquals(bt, Io.toByteArray(bin));
	}

	@Test
	public void toStringTest() throws IOException {
		byte[] bt = "hello".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bin = new ByteArrayInputStream(bt);
		Assert.assertEquals("hello", Io.toString(bin));
		Assert.assertEquals("hello", Io.toString(new StringReader("hello")));
	}

	@Test
	public void consumeTest() throws IOException {
		byte[] bt = "hello".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bin = new ByteArrayInputStream(bt);
		Io.consume(bin);
		Assert.assertEquals(-1, bin.read());
	}

	@Test
	public void copyTest() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] bt = "hello world".getBytes(StandardCharsets.UTF_8);
		for (int i = 0; i < Io.BUFFER_SIZE; i++) {
			bout.write(bt);
		}

		bt = bout.toByteArray();
		ByteArrayInputStream bin = new ByteArrayInputStream(bt);
		bout = new ByteArrayOutputStream();
		Io.copy(bin, bout);
		Assert.assertArrayEquals(bt, bout.toByteArray());

		StringWriter wout = new StringWriter();
		String str = "hello world";
		for (int i = 0; i < Io.BUFFER_SIZE; i++) {
			wout.write(str);
		}

		str = wout.toString();
		StringReader win = new StringReader(str);
		wout = new StringWriter();
		Io.copy(win, wout);
		Assert.assertEquals(str, wout.toString());
	}

	private static class CloseableImpl implements Closeable {

		boolean closed;

		@Override
		public void close() throws IOException {
			closed = true;
		}

	}

	private static class UrlConnectionImpl extends URLConnection {

		InputStream input;

		protected UrlConnectionImpl(URL url) {
			super(url);
		}

		@Override
		public void connect() throws IOException {
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return input;
		}
	}

	private static class InputStreamImpl extends InputStream {

		boolean closed;

		@Override
		public int read() throws IOException {
			return 0;
		}

		@Override
		public void close() throws IOException {
			closed = true;
		}

	}

	@Test
	public void closeTest() throws IOException {
		Io.close((Closeable) null);
		CloseableImpl closable = new CloseableImpl();
		Assert.assertFalse(closable.closed);
		Io.close(closable);
		Assert.assertTrue(closable.closed);

		Io.close(new Closeable() {

			@Override
			public void close() throws IOException {
				throw new IOException("must be ignored");
			}
		});

		Io.close((URLConnection) null);

		UrlConnectionImpl uconn = new UrlConnectionImpl(new URL("http://github.com"));
		@SuppressWarnings("resource")
		InputStreamImpl in = new InputStreamImpl();
		uconn.input = in;
		Assert.assertFalse(in.closed);
		Io.close(uconn);
		Assert.assertTrue(in.closed);

		Io.close(new URLConnection(new URL("http://github.com")) {

			@Override
			public void connect() throws IOException {
			}

			@Override
			public InputStream getInputStream() throws IOException {
				throw new IOException("must be ignored");
			}
		});
	}

	@Test
	public void readerTest() {
		Reader reader = Io.reader(new ByteArrayInputStream(new byte[0]));
		Assert.assertTrue(reader instanceof FastInputStreamReader);

		reader = Io.reader(new ByteArrayInputStream(new byte[0]), StandardCharsets.UTF_8.name());
		Assert.assertTrue(reader instanceof FastInputStreamReader);

		reader = Io.reader(new ByteArrayInputStream(new byte[0]), "KOI8-R");
		Assert.assertTrue(reader instanceof InputStreamReader);
	}

	@Test
	public void writerTest() throws IOException {
		Writer writer = Io.writer(new ByteArrayOutputStream(), StandardCharsets.UTF_8.name());
		Assert.assertTrue(writer instanceof FastOutputStreamWriter);
		writer.flush();
		writer.close();
		writer.close();

		writer = Io.writer(new ByteArrayOutputStream(), "KOI8-R");
		Assert.assertTrue(writer instanceof OutputStreamWriter);
	}

	@Test
	public void deleteDirTest() throws IOException {
		File dir = new File(FileFinder.getBuild(), "deldir");
		if (dir.mkdirs()) {
			LOG.finer(dir + " created");
		}
		File inner = new File(dir, "inner");
		if (inner.mkdirs()) {
			LOG.finer(inner + " created");
		}
		File txt1 = new File(dir, "txt1");
		File txt2 = new File(inner, "txt2");

		Files.write(txt1.toPath(), "hello".getBytes(StandardCharsets.UTF_8));
		Files.write(txt2.toPath(), "hello2".getBytes(StandardCharsets.UTF_8));

		Assert.assertTrue(dir.exists());
		Assert.assertTrue(inner.exists());
		Assert.assertTrue(txt1.exists());
		Assert.assertTrue(txt2.exists());

		Io.copy(dir.toPath(), new File(FileFinder.getBuild(), "copydir").toPath());
		Assert.assertTrue(new File(FileFinder.getBuild(), "copydir").exists());
		Assert.assertTrue(new File(FileFinder.getBuild(), "copydir/inner").exists());
		Assert.assertTrue(new File(FileFinder.getBuild(), "copydir/txt1").exists());
		Assert.assertTrue(new File(FileFinder.getBuild(), "copydir/inner/txt2").exists());
		Io.deleteDir(new File(FileFinder.getBuild(), "copydir").toPath());
		Io.deleteDir(dir.toPath());
		Assert.assertFalse(dir.exists());
		Assert.assertFalse(inner.exists());
		Assert.assertFalse(txt1.exists());
		Assert.assertFalse(txt2.exists());

		Io.deleteDir(dir.toPath());
	}

	@Test
	public void readLineTest() throws IOException {
		File txt = new File(FileFinder.getBuild(), "readline");
		Files.write(txt.toPath(), "hello  \n  world\n  ".getBytes(StandardCharsets.UTF_8));
		String[] lines = Io.readLines(txt.toURI().toURL(), String.class, x -> x.toUpperCase());
		Assert.assertNotNull(lines);
		Assert.assertEquals(2, lines.length);
		Assert.assertEquals("HELLO", lines[0]);
		Assert.assertEquals("WORLD", lines[1]);

		lines = Io.readLines(new FileInputStream(txt), null);
		Assert.assertEquals(2, lines.length);
		Assert.assertEquals("hello", lines[0]);
		Assert.assertEquals("world", lines[1]);
		if (!txt.delete()) {
			LOG.warning("cannot delete " + txt);
		}

		lines = Io.readLines(new URL(txt.toURI().toURL() + "2"), String.class, x -> x.toUpperCase());
		Assert.assertEquals(0, lines.length);

		lines = Io.readLines((InputStream) null, String.class, x -> x.toUpperCase(), "utf8");
		Assert.assertEquals(0, lines.length);

		lines = Io.readLines(new InputStream() {

			@Override
			public int read() throws IOException {
				throw new IOException();
			}
		}, String.class, x -> x.toUpperCase(), "utf8");
		Assert.assertEquals(0, lines.length);
	}

	@Test
	public void zipTest() throws IOException {
		File dir = new File(FileFinder.getBuild(), "zipdir");
		if (dir.mkdirs()) {
			LOG.finer(dir + " created");
		}
		File inner = new File(FileFinder.getBuild(), "zipdir/inner");
		if (inner.mkdirs()) {
			LOG.finer(inner + " created");
		}
		File txt = new File(inner, "readline");
		Files.write(txt.toPath(), "hello  \n  world\n  ".getBytes(StandardCharsets.UTF_8));
		File zip = new File(FileFinder.getBuild(), "zip.zip");
		Assert.assertFalse(zip.exists());
		Io.zip(zip, dir);
		Assert.assertTrue(zip.exists());
		File dir2 = new File(FileFinder.getBuild(), "zipdir2");
		Io.unzip(zip, dir2);
		Assert.assertTrue(new File(dir2, "inner/readline").exists());

		Io.deleteDir(dir.toPath());
		Io.deleteDir(dir2.toPath());
		if (!zip.delete()) {
			LOG.finer("cannot delete " + zip);
		}
	}
}
