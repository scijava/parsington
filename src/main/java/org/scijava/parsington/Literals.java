/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2026 Parsington developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.parsington;

/**
 * Utility methods for parsing literals from strings. These methods largely
 * conform to the Java specification's ideas of what constitutes a numeric or
 * string literal.
 *
 * @author Curtis Rueden
 */
public final class Literals {

	private Literals() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Parses a boolean literal (i.e., true and false).
	 *
	 * @param s The string from which the boolean literal should be parsed.
	 * @return The parsed boolean value&mdash;either {@link Boolean#TRUE} or
	 *         {@link Boolean#FALSE}&mdash; or null if the string does not begin
	 *         with a boolean literal.
	 */
	public static Boolean parseBoolean(final CharSequence s) {
		return parseBoolean(s, new Position());
	}

	/**
	 * Parses a string literal which is enclosed in single or double quotes.
	 * <p>
	 * For literals in double quotes, this parsing mechanism is intended to be as
	 * close as possible to the numeric literals supported by the Java programming
	 * language itself. Literals in single quotes are completely verbatim, with no
	 * escaping performed.
	 * </p>
	 *
	 * @param s The string from which the string literal should be parsed.
	 * @return The parsed string value, unescaped according to Java conventions.
	 *         Returns null if the string does not begin with a single or double
	 *         quote.
	 */
	public static String parseString(final CharSequence s) {
		return parseString(s, new Position());
	}

	/**
	 * Parses a literal of any known type (booleans, strings and numbers).
	 *
	 * @param s The string from which the literal should be parsed.
	 * @return The parsed value, of a type consistent with Java's support for
	 *         literals: either {@link Boolean}, {@link String} or a concrete
	 *         {@link Number} subclass. Returns null if the string does
	 *         not match the syntax of a known literal.
	 * @see #parseBoolean(CharSequence)
	 * @see #parseString(CharSequence)
	 * @see ParseNumber#parseAllNumbers(String)
	 */
	public static Object parseLiteral(final String s) {
		return parseLiteral(s, new Position());
	}

	/**
	 * Parses a boolean literal (i.e., true and false).
	 *
	 * @param s The string from which the boolean literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed boolean value&mdash;either {@link Boolean#TRUE} or
	 *         {@link Boolean#FALSE}&mdash; or null if the string does not begin
	 *         with a boolean literal.
	 */
	public static Boolean parseBoolean(final CharSequence s, final Position pos) {
		if (isWord(s, pos, "true")) {
			pos.inc(4);
			return Boolean.TRUE;
		}
		if (isWord(s, pos, "false")) {
			pos.inc(5);
			return Boolean.FALSE;
		}
		return null;
	}

	/**
	 * Parses a string literal which is enclosed in single or double quotes.
	 * <p>
	 * For literals in double quotes, this parsing mechanism is intended to be as
	 * close as possible to the numeric literals supported by the Java programming
	 * language itself. Literals in single quotes are completely verbatim, with no
	 * escaping performed.
	 * </p>
	 *
	 * @param s The string from which the string literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed string value, unescaped according to Java conventions.
	 *         Returns null if the string does not begin with a single or double
	 *         quote.
	 */
	public static String parseString(final CharSequence s, final Position pos) {
		final char quote = pos.ch(s);
		if (quote != '"' && quote != '\'') return null;
		int index = pos.get() + 1;

		boolean escaped = false;
		final StringBuilder sb = new StringBuilder();
		while (true) {
			if (index >= s.length()) pos.die("Unclosed string literal");
			final char c = s.charAt(index);

			if (escaped) {
				escaped = false;
				if (isOctal(c)) { // octal sequence
					String octal = "" + c;
					final char c1 = pos.ch(s, index + 1);
					if (isOctal(c1)) {
						octal += c1;
						if (c >= '0' && c <= '3') {
							final char c2 = pos.ch(s, index + 2);
							if (isOctal(c2)) octal += c2;
						}
					}
					sb.append((char) Integer.parseInt(octal, 8));
					index += octal.length();
					continue;
				}
				switch (c) {
					case 'b': // backspace
						sb.append('\b');
						break;
					case 't': // tab
						sb.append('\t');
						break;
					case 'n': // linefeed
						sb.append('\n');
						break;
					case 'f': // form feed
						sb.append('\f');
						break;
					case 'r': // carriage return
						sb.append('\r');
						break;
					case '"': // double quote
						sb.append('"');
						break;
					case '\\': // backslash
						sb.append('\\');
						break;
					case 'u': // unicode sequence
						final char u1 = hex(s, pos, index + 1);
						final char u2 = hex(s, pos, index + 2);
						final char u3 = hex(s, pos, index + 3);
						final char u4 = hex(s, pos, index + 4);
						sb.append((char) Integer.parseInt("" + u1 + u2 + u3 + u4, 16));
						index += 4;
						break;
					default: // invalid escape
						pos.die("Invalid escape sequence");
				}
			}
			else if (c == '\\' && quote == '"') escaped = true;
			else if (c == quote) break;
			else sb.append(c);
			index++;
		}
		pos.set(index + 1);
		return sb.toString();
	}

	/**
	 * Parses a literal of any known type (booleans, strings and numbers).
	 *
	 * @param s The string from which the literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed value, of a type consistent with Java's support for
	 *         literals: either {@link Boolean}, {@link String} or a concrete
	 *         {@link Number} subclass. Returns null if the string does
	 *         not match the syntax of a known literal.
	 * @see #parseBoolean(CharSequence, Position)
	 * @see #parseString(CharSequence, Position)
	 * @see ParseNumber#parseAllNumbers(String, Position)
	 */
	public static Object parseLiteral(final String s, final Position pos) {
		final Boolean bool = parseBoolean(s, pos);
		if (bool != null) return bool;

		final String str = parseString(s, pos);
		if (str != null) return str;

		return ParseNumber.parseAllNumbers(s,pos);
	}

	// -- Helper methods --

	private static boolean isOctal(final char c) {
		return c >= '0' && c <= '7';
	}

	private static char hex(final CharSequence s, final Position pos,
		final int index)
	{
		final char c = pos.ch(s, index);
		if (c >= '0' && c <= '9') return c;
		if (c >= 'a' && c <= 'f') return c;
		if (c >= 'A' && c <= 'F') return c;
		pos.die("Invalid unicode sequence");
		return '\0'; // NB: Unreachable.
	}

	private static boolean isWord(final CharSequence s, final Position pos,
		final String word)
	{
		// not needed since pos.ch will return 0 for out of bounds requests. if (s.length() - pos.get() < word.length()) return false;
		for (int i=0; i<word.length(); i++) {
			if (pos.ch(s, i) != word.charAt(i)) return false;
		}
		final char next = pos.ch(s, word.length());
		if (next >= 'a' && next <= 'z') return false;
		if (next >= 'A' && next <= 'Z') return false;
		if (next >= '0' && next <= '9') return false;
        return next != '_';
    }

}
