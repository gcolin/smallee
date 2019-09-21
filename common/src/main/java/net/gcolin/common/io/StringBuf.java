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

import java.util.ArrayList;
import java.util.List;

/**
 * StringBuffer with char array from pool.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringBuf implements AutoCloseable {

	private List<char[]> all = new ArrayList<>();
	private char[] current;
	private int index;

	public StringBuf() {
		enlarge();
	}

	private void enlarge() {
		current = Io.takeChars();
		all.add(current);
		index = 0;
	}

	public void write(int ch) {
		if (current.length == index) {
			enlarge();
		}
		current[index++] = (char) ch;
	}

	public void write(String cbuf) {
		if (current.length - index < cbuf.length()) {
			int rem = cbuf.length();
			int offset = 0;
			while (rem > 0) {
				int todo = Math.min(current.length - index, rem);
				cbuf.getChars(offset, todo + offset, current, index);
				offset += todo;
				rem -= todo;
				index += todo;
				if (rem > 0) {
					enlarge();
				}
			}
		} else {
			cbuf.getChars(0, cbuf.length(), current, index);
			index += cbuf.length();
		}
	}

	public int getSize() {
		return (all.size() - 1) * Io.BUFFER_SIZE + index;
	}

	public String toString() {
		char[] ba = new char[getSize()];
		int off = 0;
		for (int i = 0; i < all.size() - 1; i++, off += Io.BUFFER_SIZE) {
			System.arraycopy(all.get(i), 0, ba, off, Io.BUFFER_SIZE);
		}
		System.arraycopy(current, 0, ba, off, index);
		return new String(ba);
	}

	public void close() {
		for (int i = 0; i < all.size(); i++) {
			Io.recycleChars(all.get(i));
		}
		all.clear();
		current = null;
	}

}
