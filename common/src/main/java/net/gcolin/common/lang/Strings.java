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

package net.gcolin.common.lang;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code Strings} class provides various helper method for manipulating
 * String.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public final class Strings {

	private static final Logger LOG = LoggerFactory.getLogger(Strings.class);
	public static final String FMT_LOCALE = "javax.servlet.jsp.jstl.fmt.locale.session";
	private static final String MISSING_SEP = "!!!";
	private static final int BLANK_SIZE = 33;
	private static final boolean[] BLANK = new boolean[BLANK_SIZE];
	private static final String[] JSON_ENCODING = new String[93];
	private static final String[] JAVA_ENCODING = new String[93];
	private static final String[] XML_ENCODING = new String[63];
	private static final String[] XML_ATTR_ENCODING = new String[63];
	private static final Map<String, String> XML_TABLE = new HashMap<>();
	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>',
			'|', '\"', ':' };

	static {
		JSON_ENCODING['"'] = "\\\"";
		JSON_ENCODING['\\'] = "\\\\";
		JSON_ENCODING['/'] = "\\/";

		JAVA_ENCODING['"'] = "\\\"";
		JAVA_ENCODING['\\'] = "\\\\";
		JAVA_ENCODING['\n'] = "\\n";

		XML_ENCODING['<'] = XML_ATTR_ENCODING['<'] = "&lt;";
		XML_ENCODING['>'] = XML_ATTR_ENCODING['>'] = "&gt;";
		XML_ENCODING['&'] = XML_ATTR_ENCODING['&'] = "&amp;";
		XML_ATTR_ENCODING['"'] = "&quot;";
		XML_TABLE.put("lt", "<");
		XML_TABLE.put("gt", ">");
		XML_TABLE.put("amp", "&");
		XML_TABLE.put("quot", "\"");

		BLANK[' '] = true;
		BLANK['\r'] = true;
		BLANK['\n'] = true;
		BLANK['\t'] = true;
	}

	private Strings() {
	}

	/**
	 * Remove quotes and trim.
	 * 
	 * @param in A string
	 * @return A string without quotes and space at the end and at the beginning
	 */
	public static String unquoteAndTrim(String in) {
		return unquoteAndTrim(in, 0, in.length());
	}

	/**
	 * Remove quotes and trim.
	 * 
	 * @param in    A string
	 * @param start The string offset
	 * @param end   The end offset
	 * @return A string without quotes and space at the end and at the beginning
	 */
	public static String unquoteAndTrim(String in, int start, int end) {
		char ch;
		int st = start;
		int en = end;
		while (en > st && (isBlank(ch = in.charAt(st)) || ch == '"')) {
			st++;
		}
		while (en > st && (isBlank(ch = in.charAt(en - 1)) || ch == '"')) {
			en--;
		}
		return st != 0 || en != in.length() ? in.substring(st, en) : in;
	}

	/**
	 * Lower the case of the first character.
	 * 
	 * @param str A string
	 * @return A string uncapitalized
	 */
	public static String uncapitalize(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char ch = str.charAt(0);
		if (Character.isLowerCase(ch)) {
			// already uncapitalized
			return str;
		}
		return new StringBuilder(str.length()).append(Character.toLowerCase(ch)).append(str.substring(1)).toString();
	}

	/**
	 * Upper the case of the first character.
	 * 
	 * @param str A string
	 * @return A string capitalized
	 */
	public static String capitalize(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char ch = str.charAt(0);
		if (Character.isUpperCase(ch)) {
			// already uncapitalized
			return str;
		}
		return new StringBuilder(str.length()).append(Character.toUpperCase(ch)).append(str.substring(1)).toString();
	}

	/**
	 * Is a string null or empty (length == 0).
	 * 
	 * @param str A string
	 * @return true if the string is null or its length is 0
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Check if the char is a blank character (space, new line, chariot,
	 * tabulation).
	 * 
	 * @param ch A char
	 * @return true if the char is blank
	 */
	public static boolean isBlank(char ch) {
		return ch < BLANK_SIZE && BLANK[ch];
	}

	/**
	 * Check if the string is only composed of blank character (space, new line,
	 * chariot, tabulation).
	 * 
	 * @param str A string
	 * @return true if the string is blank
	 */
	public static boolean isBlank(String str) {
		if (isNullOrEmpty(str)) {
			return true;
		}
		for (int i = 0, l = str.length(); i < l; i++) {
			if (!isBlank(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fast XML encoding.
	 * 
	 * @param input A string to encode
	 * @param attr  Is the string in an attribute
	 * @return An XML encoded string
	 */
	public static String encodeXml(String input, boolean attr) {
		return encode(input, attr ? XML_ATTR_ENCODING : XML_ENCODING);
	}

	/**
	 * Fast JSON encoding.
	 * 
	 * @param input A string to encode
	 * @return An JSON encoded string
	 */
	public static String encodeJson(String input) {
		return encode(input, JSON_ENCODING);
	}

	/**
	 * Fast Java encoding.
	 * 
	 * @param input A string to encode
	 * @return An Java encoded string
	 */
	public static String encodeJava(String input) {
		return encode(input, JAVA_ENCODING);
	}

	private static String encode(String input, String[] check) {
		StringBuilder out = null;
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (ch < check.length && check[ch] != null) {
				if (out == null) {
					out = new StringBuilder(input.length() + 16);
					if (i != 0) {
						out.append(input, 0, i);
					}
				}
				out.append(check[ch]);
			} else if (out != null) {
				out.append(ch);
			}
		}

		if (out != null) {
			return out.toString();
		}
		return input;
	}

	/**
	 * Fast XML decoding.
	 * 
	 * @param input A string to decode
	 * @return An XML encoded string
	 */
	public static String decodeXml(String input) {
		StringBuilder out = null;
		StringBuilder tmp = null;

		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (ch == '&') {
				if (tmp == null || out == null) {
					tmp = new StringBuilder();
					out = new StringBuilder();
					if (i != 0) {
						out.append(input, 0, i);
					}
				} else {
					tmp.setLength(0);
				}
				while ((ch = input.charAt(++i)) != ';') {
					tmp.append(ch);
				}
				out.append(XML_TABLE.get(tmp.toString()));
			} else if (out != null) {
				out.append(ch);
			}
		}
		if (out != null) {
			return out.toString();
		}
		return input;
	}

	/**
	 * Fast URL decoding.
	 * 
	 * @param input A string to decode
	 * @return An URL encoded string
	 */
	public static String decodeUrl(String input) {
		return decodeUrl(input, 0, input.length());
	}

	/**
	 * Fast URL decoding.
	 * 
	 * @param input A string to decode
	 * @param start Start offset
	 * @param end   End offset
	 * @return An URL encoded string
	 */
	public static String decodeUrl(String input, int start, int end) {
		StringBuilder out = null;

		for (int i = start; i < end; i++) {
			char ch = input.charAt(i);
			if (ch == '%') {
				if (out == null) {
					out = new StringBuilder(end - start);
					if (i != start) {
						out.append(input, start, i);
					}
				}
				if (i + 3 > input.length()) {
					throw new IllegalArgumentException("bad input " + input.substring(start, end));
				}
				i++;
				int rn = nextInt(input, i);

				if ((rn & 0xF0) == 0xF0) {
					if (i + 10 < input.length()) {
						i += 3;
						int i1 = nextInt(input, i);
						i += 3;
						int i2 = nextInt(input, i);
						i += 3;
						int i3 = nextInt(input, i);

						int nb = ((rn ^ 0xF0) << 18) | ((i1 ^ 0x80) << 12) | ((i2 ^ 0x80) << 6) | (i3 ^ 0x80);
						out.append(Character.highSurrogate(nb));
						out.append(Character.lowSurrogate(nb));
					}
				} else if ((rn & 0xE0) == 0xE0) {
					if (i + 7 < input.length()) {
						i += 3;
						int i1 = nextInt(input, i);
						i += 3;
						int i2 = nextInt(input, i);
						out.append((char) (((rn ^ 0xE0) << 12) | ((i1 ^ 0x80) << 6) | (i2 ^ 0x80)));
					}
				} else if ((rn & 0xC0) == 0xC0) {
					if (i + 4 < input.length()) {
						i += 3;
						int i1 = nextInt(input, i);
						out.append((char) (((rn ^ 0xC0) << 6) | (i1 ^ 0x80)));
					}
				} else {
					out.append((char) rn);
				}

				i++;
			} else if (ch == '+') {
				if (out == null) {
					out = new StringBuilder(end - start);
					if (i != start) {
						out.append(input, start, i);
					}
				}
				out.append(' ');
			} else if (out != null) {
				out.append(ch);
			}
		}

		if (out != null) {
			return out.toString();
		}
		if (start == 0 && end == input.length()) {
			return input;
		}
		return input.substring(start, end);
	}

	private static int nextInt(String input, int index) {
		return toInt(input.charAt(index)) * 16 + toInt(input.charAt(index + 1));
	}

	private static int toInt(int nb) {
		if (nb > 'Z') {
			return nb - 87;
		} else if (nb >= 'A') {
			return nb - 55;
		} else {
			return nb - 48;
		}
	}

	/**
	 * Simplify a string by removing punctuation and accents.
	 * 
	 * @param input A string
	 * @return A simplified string
	 */
	public static String simplify(String input) {
		return Normalizer.normalize(input, Normalizer.Form.NFKD).replaceAll("([^\\p{ASCII}]|[\\p{Punct}])", "")
				.toLowerCase();
	}

	/**
	 * Get a message from a resource bundle without throwing exception.
	 * 
	 * @param rb  The resource bundle
	 * @param key The key
	 * @return The value or !!!key!!!
	 */
	public static String msg(ResourceBundle rb, String key) {
		if (rb == null || key == null) {
			return "";
		}
		try {
			return rb.getString(key);
		} catch (MissingResourceException ex) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(key + " is missing in " + rb, ex);
			}
			return MISSING_SEP + key + MISSING_SEP;
		}
	}

	/**
	 * To string of blank
	 * 
	 * @param obj An object
	 * @return blank if the object is null else object.toString()
	 */
	public static String toString(Object obj) {
		if (obj == null) {
			return "";
		} else {
			return obj.toString();
		}
	}

	/**
	 * Check if a string is an IPv4 address.
	 * 
	 * @param sourceStr A string
	 * @return true if the string is an IPv4
	 */
	public static boolean isIp(String sourceStr) {
		return sourceStr.contains(":") || sourceStr.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
	}

	/**
	 * Substring and trim.
	 * 
	 * @param str   A string
	 * @param start Start offset
	 * @param end   End offset
	 * @return string reduced and trimmed
	 */
	public static String substringTrimed(String str, int start, int end) {
		int st = start;
		int len = end;
		while (st < len && str.charAt(st) <= ' ') {
			st++;
		}
		while (st < len && str.charAt(len - 1) <= ' ') {
			len--;
		}
		if (st == len) {
			return "";
		}
		if (len - st > 1 && (str.charAt(st) == '"' && str.charAt(len - 1) == '"'
				|| str.charAt(st) == '\'' && str.charAt(len - 1) == '\'')) {
			st++;
			len--;
		}
		return str.substring(st, len);
	}

	public static boolean isChecked(String value) {
		return Boolean.valueOf(value) || "on".equalsIgnoreCase(value);
	}

	// Misc
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Find the Levenshtein distance between two Strings.
	 * </p>
	 *
	 * <p>
	 * This is the number of changes needed to change one String into another, where
	 * each change is a single character modification (deletion, insertion or
	 * substitution).
	 * </p>
	 *
	 * <p>
	 * The previous implementation of the Levenshtein distance algorithm was from
	 * <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com
	 * /ld.htm</a>
	 * </p>
	 *
	 * <p>
	 * Chas Emerick has written an implementation in Java, which avoids an
	 * OutOfMemoryError which can occur when my Java implementation is used with
	 * very large strings.<br>
	 * This implementation of the Levenshtein distance algorithm is from
	 * <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark. com/
	 * ldjava.htm</a>
	 * </p>
	 *
	 * <pre>
	 * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance("","")               = 0
	 * StringUtils.getLevenshteinDistance("","a")              = 1
	 * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
	 * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
	 * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
	 * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
	 * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
	 * </pre>
	 *
	 * @param s1 the first String, must not be null
	 * @param s2 the second String, must not be null
	 * @return result distance
	 * @throws IllegalArgumentException if either String input {@code null}
	 * @since 3.0 Changed signature from getLevenshteinDistance(String, String) to
	 *        getLevenshteinDistance(CharSequence, CharSequence)
	 */
	public static int getLevenshteinDistance(CharSequence s1, CharSequence s2) {
		CharSequence seq1 = s1;
		CharSequence seq2 = s2;
		if (seq1 == null || seq2 == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		 * The difference between this impl. and the previous is that, rather than
		 * creating and retaining a matrix of size s.length() + 1 by t.length() + 1, we
		 * maintain two single-dimensional arrays of length s.length() + 1. The first,
		 * d, is the 'current working' distance array that maintains the newest distance
		 * cost counts as we iterate through the characters of String s. Each time we
		 * increment the index of String t we are comparing, d is copied to p, the
		 * second int[]. Doing so allows us to retain the previous cost counts as
		 * required by the algorithm (taking the minimum of the cost count to the left,
		 * up one, and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing a
		 * System.arraycopy() each time through the outer loop.)
		 * 
		 * Effectively, the difference between the two implementations is this one does
		 * not cause an out of memory condition when calculating the LD over two very
		 * large strings.
		 */

		// length of s
		int slen = seq1.length();
		// length of t
		int tlen = seq2.length();

		if (slen == 0) {
			return tlen;
		} else if (tlen == 0) {
			return slen;
		}

		if (slen > tlen) {
			// swap the input strings to consume less memory
			CharSequence tmp = seq1;
			seq1 = seq2;
			seq2 = tmp;
			slen = tlen;
			tlen = seq2.length();
		}

		// 'previous' cost array, horizontally
		int[] pa = new int[slen + 1];
		// cost array, horizontally
		int[] da = new int[slen + 1];
		// placeholder to assist in swapping p and d
		int[] tmp;

		// indexes into strings s and tç

		// jth character of t
		char jth;

		// cost
		int cost;

		for (int i = 0; i <= slen; i++) {
			pa[i] = i;
		}

		for (int j = 1; j <= tlen; j++) {
			jth = seq2.charAt(j - 1);
			da[0] = j;

			for (int i = 1; i <= slen; i++) {
				cost = seq1.charAt(i - 1) == jth ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				da[i] = Math.min(Math.min(da[i - 1] + 1, pa[i] + 1), pa[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			tmp = pa;
			pa = da;
			da = tmp;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return pa[slen];
	}

	public static boolean isQuoted(String str) {
		return str != null && str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"';
	}

	/**
	 * Check if a file name is valid.
	 * 
	 * @param fileName a file name
	 * @return {@code true} if the file name is valid
	 */
	public static boolean isFileNameValid(String fileName) {
		if (isNullOrEmpty(fileName)) {
			return false;
		}
		for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++) {
			if (fileName.indexOf(ILLEGAL_CHARACTERS[i]) != -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the number of a char in a string.
	 * 
	 * @param str a string
	 * @param ch  a char
	 * @return a number
	 */
	public static int getNbIndexOf(String str, char ch) {
		int sum = 0;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (str.charAt(i) == ch) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * Convert a collection to string an join them with a delimiter.
	 * 
	 * @param <E>        the collection element type
	 * @param collection a collection
	 * @param transform  a transformer to string
	 * @param delimiter  a delimiter
	 * @return a string
	 */
	public static <E> String join(Iterable<E> collection, Function<E, String> transform, String delimiter) {
		StringBuilder str = new StringBuilder();
		for (E e : collection) {
			if (str.length() > 0) {
				str.append(delimiter);
			}
			str.append(transform.apply(e));
		}
		return str.toString();
	}

	/**
	 * Convert an array to string an join them with a delimiter.
	 * 
	 * @param <E>        the array element type
	 * @param collection a collection
	 * @param transform  a transformer to string
	 * @param delimiter  a delimiter
	 * @return a string
	 */
	public static <E> String join(E[] collection, Function<E, String> transform, String delimiter) {
		StringBuilder str = new StringBuilder();
		for (E e : collection) {
			if (str.length() > 0) {
				str.append(delimiter);
			}
			str.append(transform.apply(e));
		}
		return str.toString();
	}

	/**
	 * Create a char iterator from a string
	 * 
	 * @param str a string
	 * @param off the start offset in the data.
	 * @param len the number of char to use.
	 * @return a {@code CharIterator}
	 */
	public static CharIterator iterator(String str, int off, int len) {
		return new CharIterator() {

			int index = off;
			int end = Math.min(off + len, str.length());

			@Override
			public char next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return str.charAt(index++);
			}

			@Override
			public boolean hasNext() {
				return index < end;
			}
		};
	}

	/**
	 * Create a char iterator from a char array
	 * 
	 * @param str a char array
	 * @param off the start offset in the data.
	 * @param len the number of char to use.
	 * @return a {@code CharIterator}
	 */
	public static CharIterator iterator(char[] str, int off, int len) {
		return new CharIterator() {

			int index = off;
			int end = Math.min(off + len, str.length);

			@Override
			public char next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return str[index++];
			}

			@Override
			public boolean hasNext() {
				return index < end;
			}
		};
	}

	public static String stripAccents(String input) {
		if (input == null) {
			return null;
		}
		final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");//$NON-NLS-1$
		final StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
		for (int i = 0; i < decomposed.length(); i++) {
			if (decomposed.charAt(i) == '\u0141') {
				decomposed.deleteCharAt(i);
				decomposed.insert(i, 'L');
			} else if (decomposed.charAt(i) == '\u0142') {
				decomposed.deleteCharAt(i);
				decomposed.insert(i, 'l');
			}
		}

		// Note that this doesn't correctly remove ligatures...
		return pattern.matcher(decomposed).replaceAll("");
	}

}
