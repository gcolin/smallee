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

package net.gcolin.common.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.gcolin.common.collection.ConcurrentQueue;

/**
 * Utility class for I/O Contain a pool for byte array and a pool for char array
 * The pool state is visible through JMX
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Io {

	private static final String CREATED = "{0} created";
	public static final int BUFFER_SIZE = 8 * 1024;
	public static final int POOL_SIZE = 100;
	private static final Queue<byte[]> BYTES_POOL = new ConcurrentQueue<>(POOL_SIZE);
	private static final Queue<char[]> CHAR_POOL = new ConcurrentQueue<>(POOL_SIZE);
	private static final Logger LOG = Logger.getLogger(Io.class.getName());

	private Io() {
	}

	public static int getBytePoolSize() {
		return BYTES_POOL.size();
	}

	public static int getCharPoolSize() {
		return CHAR_POOL.size();
	}

	public static void clearBytePool() {
		BYTES_POOL.clear();
	}

	public static void clearCharPool() {
		CHAR_POOL.clear();
	}

	/**
	 * Get a byte array from the byte array pool or create a new one.
	 * 
	 * @return a byte array
	 */
	public static byte[] takeBytes() {
		byte[] ba = BYTES_POOL.poll();
		if (ba == null) {
			ba = new byte[BUFFER_SIZE];
		}
		return ba;
	}

	/**
	 * Recycle a byte array to the byte array pool if the pool is not full.
	 * 
	 * @param ba a byte array
	 */
	public static void recycleBytes(byte[] ba) {
		BYTES_POOL.offer(ba);
	}

	/**
	 * Get a char array from the char array pool or create a new one.
	 * 
	 * @return a char array
	 */
	public static char[] takeChars() {
		char[] ba = CHAR_POOL.poll();
		if (ba == null) {
			ba = new char[BUFFER_SIZE];
		}
		return ba;
	}

	/**
	 * Recycle a char array to the char array pool if the pool is not full.
	 * 
	 * @param ba a char array
	 */
	public static void recycleChars(char[] ba) {
		CHAR_POOL.offer(ba);
	}

	/**
	 * Read a stream to a byte array.
	 * 
	 * @param in a stream
	 * @return a byte array
	 * @throws IOException if an I/O error occurs.
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream bout = null;
		try {
			bout = new ByteArrayOutputStream();
			copy(in, bout);
			return bout.toByteArray();
		} finally {
			if (bout != null) {
				bout.release();
			}
		}
	}

	/**
	 * Consume a stream.
	 * 
	 * @param in a stream
	 * @throws IOException if an I/O error occurs.
	 */
	public static void consume(InputStream in) throws IOException {
		byte[] buf = takeBytes();
		try {
			while (true) {
				if (in.read(buf) > 0) {
					break;
				}
			}
		} finally {
			recycleBytes(buf);
		}
	}

	/**
	 * Copy a stream to another.
	 * 
	 * @param in     an input stream
	 * @param out    an output stream
	 * @param buffer buffer
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
		int count;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
	}

	/**
	 * Copy a stream to another.
	 * 
	 * @param in  an input stream
	 * @param out an output stream
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = takeBytes();
		try {
			int count;
			while ((count = in.read(buf)) != -1) {
				out.write(buf, 0, count);
			}
		} finally {
			recycleBytes(buf);
		}
	}

	/**
	 * Copy a stream to another.
	 * 
	 * @param in  an input stream
	 * @param out an output stream
	 * @param len the size of the copy
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(InputStream in, OutputStream out, int len) throws IOException {
		byte[] buf = takeBytes();
		try {
			int count;
			int rem = len;
			while ((count = in.read(buf, 0, Math.min(rem, buf.length))) != -1) {
				rem -= count;
				out.write(buf, 0, count);
			}
		} finally {
			recycleBytes(buf);
		}
	}

	/**
	 * Copy a stream to another.
	 * 
	 * @param in  an input stream
	 * @param out an output stream
	 * @param len the size of the copy
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(RandomAccessFile in, OutputStream out, int len) throws IOException {
		byte[] buf = takeBytes();
		try {
			int count;
			int rem = len;
			while ((count = in.read(buf, 0, Math.min(rem, buf.length))) != -1) {
				rem -= count;
				out.write(buf, 0, count);
			}
		} finally {
			recycleBytes(buf);
		}
	}

	/**
	 * Copy a reader to a writer.
	 * 
	 * @param reader a reader
	 * @param writer a writer
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(Reader reader, Writer writer) throws IOException {
		char[] cbuf = takeChars();
		try {
			int count;
			while ((count = reader.read(cbuf)) != -1) {
				writer.write(cbuf, 0, count);
			}
		} finally {
			recycleChars(cbuf);
		}
	}

	/**
	 * Copy a directory to another.
	 * 
	 * @param directory a source directory
	 * @param dest      a destination directory
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(Path directory, Path dest) throws IOException {
		if (Files.exists(directory)) {
			if (dest.toFile().mkdirs()) {
				LOG.log(Level.FINE, CREATED, dest);
			}
			Files.walkFileTree(directory, new CopyFileVisitor(directory, dest));
		}
	}

	/**
	 * Digest a stream.
	 * 
	 * @param in     an input stream
	 * @param digest a message digest
	 * @throws IOException if an I/O error occurs.
	 */
	public static void digest(InputStream in, MessageDigest digest) throws IOException {
		byte[] buf = takeBytes();
		try {
			int count;
			while ((count = in.read(buf)) != -1) {
				digest.update(buf, 0, count);
			}
		} finally {
			recycleBytes(buf);
		}
	}

	/**
	 * Close an URL connection.
	 * 
	 * @param cl a connection
	 */
	public static void close(URLConnection cl) {
		if (cl != null) {
			try {
				close(cl.getInputStream());
			} catch (IOException ex) {
				LOG.log(Level.FINE, "cannot close url", ex);
			}
		}
	}

	/**
	 * Close a stream.
	 * 
	 * @param cl a stream
	 */
	public static void close(Closeable cl) {
		if (cl != null) {
			try {
				cl.close();
			} catch (IOException ex) {
				LOG.log(Level.FINE, "cannot close", ex);
			}
		}
	}

	/**
	 * Close an auto closeable.
	 * 
	 * @param cl an auto closeable.
	 */
	public static void close(AutoCloseable cl) {
		if (cl != null) {
			try {
				cl.close();
			} catch (Exception ex) {
				LOG.log(Level.FINE, "cannot close", ex);
			}
		}
	}

	/**
	 * Create a reader from an input stream.
	 * 
	 * <p>
	 * The reader will decode the BOM
	 * </p>
	 * 
	 * @param in input stream
	 * @return a reader
	 */
	public static Reader reader(InputStream in) {
		return new FastInputStreamReader(in);
	}

	/**
	 * Create a reader from an input stream.
	 * 
	 * @param in      an input stream
	 * @param charset an encoding
	 * @return a reader
	 */
	public static Reader reader(InputStream in, String charset) {
		return charset == null || Decoder.has(charset) ? new FastInputStreamReader(in, charset)
				: new InputStreamReader(in, Charset.forName(charset));
	}

	/**
	 * Create a writer from an output stream.
	 * 
	 * @param out     an output stream
	 * @param charset an encoding
	 * @return a writer
	 */
	public static Writer writer(OutputStream out, String charset) {
		return FastOutputStreamWriter.isCompatible(charset) ? new FastOutputStreamWriter(out, charset)
				: new OutputStreamWriter(out, Charset.forName(charset));
	}

	/**
	 * Read a reader and return a string.
	 * 
	 * @param reader a reader
	 * @return a string
	 * @throws IOException if an I/O error occurs.
	 */
	public static String toString(Reader reader) throws IOException {
		return toString(reader, true);
	}

	/**
	 * Read a reader and return a string.
	 * 
	 * @param reader a reader
	 * @param close  close the reader
	 * @return a string
	 * @throws IOException if an I/O error occurs.
	 */
	public static String toString(Reader reader, boolean close) throws IOException {
		StringWriter bout = new StringWriter();
		try {
			copy(reader, bout);
			return bout.toString();
		} finally {
			if (close) {
				close(reader);
			} else if (reader instanceof FastInputStreamReader) {
				((FastInputStreamReader) reader).release();
			}
			close(bout);
		}
	}

	/**
	 * Read an input stream and return a string.
	 * 
	 * @param in an input stream
	 * @return a string
	 * @throws IOException if an I/O error occurs.
	 */
	public static String toString(InputStream in) throws IOException {
		return toString(in, true);
	}

	/**
	 * Read an input stream and return a string.
	 * 
	 * @param in    an input stream
	 * @param close close the reader
	 * @return a string
	 * @throws IOException if an I/O error occurs.
	 */
	public static String toString(InputStream in, boolean close) throws IOException {
		return toString(reader(in), close);
	}

	private static class CopyFileVisitor extends SimpleFileVisitor<Path> {

		private Path directory;
		private Path dest;

		public CopyFileVisitor(Path directory, Path dest) {
			this.directory = directory;
			this.dest = dest;
		}

		private Path buildPath(Path file) {
			return dest.resolve(directory.relativize(file));
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.copy(file, buildPath(file), StandardCopyOption.REPLACE_EXISTING);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Path targetPath = buildPath(dir);
			if (!Files.exists(targetPath)) {
				Files.createDirectory(targetPath);
			}
			return FileVisitResult.CONTINUE;
		}

	}

	/**
	 * Delete a directory.
	 * 
	 * @param directory a directory
	 * @throws IOException if an I/O error occurs.
	 */
	public static void deleteDir(Path directory) throws IOException {
		if (directory.toFile().exists()) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		}
	}

	/**
	 * Compress a directory
	 * 
	 * @param zipfile the output file
	 * @param dir     the input directory
	 * @throws IOException if an I/O error occurs.
	 */
	public static void zip(File zipfile, File dir) throws IOException {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(new FileOutputStream(zipfile));
			String basePath = dir.getAbsolutePath();
			basePath += File.separator;
			int offset = basePath.length();
			final ZipOutputStream zipfinal = zip;
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					ZipEntry zipEntry = new ZipEntry(file.toFile().getAbsolutePath().substring(offset));
					zipfinal.putNextEntry(zipEntry);
					InputStream in = null;
					try {
						in = new FileInputStream(file.toFile());
						Io.copy(in, zipfinal);
					} finally {
						Io.close(in);
					}
					zipfinal.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		} finally {
			close(zip);
		}
	}

	/**
	 * Uncompress a compressed file
	 * 
	 * @param zipfile       the input file
	 * @param extractFolder the output directory
	 * @throws IOException if an I/O error occurs.
	 */
	public static void unzip(File zipfile, File extractFolder) throws IOException {
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipfile);
			Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(extractFolder, currentEntry);
				if (destFile.getParentFile().mkdirs()) {
					LOG.log(Level.FINE, CREATED, destFile.getParentFile());
				}
				if (!entry.isDirectory()) {
					unzipEntry(zip, entry, destFile);
				}
			}
		} finally {
			close(zip);
		}
	}

	private static void unzipEntry(ZipFile zip, ZipEntry entry, File destFile) throws IOException {
		OutputStream out = null;
		InputStream in = null;
		try {
			if (destFile.getParentFile().mkdirs()) {
				LOG.log(Level.FINE, CREATED, destFile.getParentFile());
			}
			out = new FileOutputStream(destFile);
			in = zip.getInputStream(entry);
			Io.copy(in, out);
		} finally {
			close(out);
			close(in);
		}
	}

	/**
	 * Read a file from its URL and transform its lines to objects.
	 * 
	 * @param <T>       the object type
	 * @param url       an URL
	 * @param type      the object type
	 * @param transform a factory
	 * @return an array of object types
	 */
	public static <T> T[] readLines(URL url, Class<T> type, Function<String, T> transform) {
		return readLines(url, type, transform, null);
	}

	/**
	 * Read a file from its URL and transform its lines to objects.
	 * 
	 * @param <T>       the object type
	 * @param url       an URL
	 * @param type      the object type
	 * @param transform a factory
	 * @param charset   an encoding
	 * @return an array of object types
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] readLines(URL url, Class<T> type, Function<String, T> transform, String charset) {
		try {
			return readLines(url == null ? null : url.openStream(), type, transform, charset);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "cannot load " + url, ex);
			return (T[]) Array.newInstance(type, 0);
		}
	}

	/**
	 * Read a file from its stream and transform its lines to objects.
	 * 
	 * @param <T>       the object type
	 * @param input     an input stream
	 * @param type      the object type
	 * @param transform a factory
	 * @param charset   an encoding
	 * @return an array of object types
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] readLines(InputStream input, Class<T> type, Function<String, T> transform, String charset) {
		if (input == null) {
			return (T[]) Array.newInstance(type, 0);
		}
		List<T> list = new ArrayList<>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(reader(input, charset));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				list.add(transform.apply(line));
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "cannot load " + in, ex);
		} finally {
			close(in);
		}
		return list.toArray((T[]) Array.newInstance(type, list.size()));
	}

	public static String[] readLines(InputStream in, String charset) {
		return readLines(in, String.class, Function.identity(), charset);
	}
}
