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

import java.math.BigDecimal;
import java.math.BigInteger;

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
	 * Parses a numeric literal of any known type.
	 * <p>
	 * This parsing mechanism is intended to be as close as possible to the
	 * numeric literals supported by the Java programming language itself.
	 * </p>
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit (optionally preceded by a
	 *         decimal point) with optional leading sign.
	 */
	public static Number parseNumber(final CharSequence s) {
		return parseNumber(s, new Position());
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
	 * @see #parseNumber(CharSequence)
	 */
	public static Object parseLiteral(final CharSequence s) {
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
		// NB: Unescaped characters are appended in bulk, one segment at a time.
		int start = index;
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
					start = index;
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
				start = index + 1;
			}
			else if (c == '\\' && quote == '"') {
				sb.append(s, start, index); // verbatim chars before the escape
				escaped = true;
			}
			else if (c == quote) {
				sb.append(s, start, index); // verbatim chars before the quote
				break;
			}
			index++;
		}
		pos.set(index + 1);
		return sb.toString();
	}

	/**
	 * Parses a numeric literal of any known type.
	 * <p>
	 * This parsing mechanism is intended to be as close as possible to the
	 * numeric literals supported by the Java programming language itself.
	 * </p>
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit (optionally preceded by a
	 *         decimal point) with optional leading sign.
	 */
	public static Number parseNumber(final CharSequence s, final Position pos) {
		// Conceptually, this scanner matches the following regexes, choosing
		// the longest match, with ties going to the earlier pattern:
		//
		//   hex:     [-+]?0[Xx][0-9a-fA-F]+
		//              ([Ll]|(\.[0-9a-fA-F]*)?[Pp][-+]?[0-9]+[DdFf]?)?
		//   binary:  [-+]?0[Bb][01]+[Ll]?
		//   octal:   [-+]?0[0-7]+[Ll]?
		//   decimal: [-+]?([0-9]+(\.[0-9]*)?|\.[0-9]+)([Ee][-+]?[0-9]+)?[DdFfLl]?
		//
		// E.g., 0755 is octal, but 07.5 and 07f are decimal, and 089 is decimal.

		if (!isNumberSyntax(s, pos)) return null;

		final int start = pos.get();
		int i = isSign(s.charAt(start)) ? start + 1 : start;

		// Detect the radix from the prefix, if any. A prefix without any valid
		// digits after it (e.g. 0x by itself) is not part of the literal.
		int radix = 10;
		if (s.charAt(i) == '0') {
			final char prefix = Character.toLowerCase(ch(s, i + 1));
			if (prefix == 'x' && isDigit(ch(s, i + 2), 16)) radix = 16;
			if (prefix == 'b' && isDigit(ch(s, i + 2), 2)) radix = 2;
			if (radix != 10) i += 2;
		}
		final int digitsStart = i;
		i = skipDigits(s, i, radix);
		final int digitsEnd = i;

		// Optional fractional and exponent parts. Hex floating point literals
		// (e.g. 0x1.8p3) require a binary exponent; binary literals have neither.
		boolean floating = false;
		if (radix == 10) {
			if (ch(s, i) == '.') {
				floating = true;
				i = skipDigits(s, i + 1, 10);
			}
			final int e = skipExponent(s, i, 'e');
			if (e > i) {
				floating = true;
				i = e;
			}
		}
		else if (radix == 16) {
			final int f = ch(s, i) == '.' ? skipDigits(s, i + 1, 16) : i;
			final int e = skipExponent(s, f, 'p');
			if (e > f) {
				floating = true;
				i = e;
			}
		}
		final int end = i;

		// Optional type suffix: L for integers, F or D for floating point.
		// NB: A decimal literal consumes an L suffix even after a fractional part,
		// so that e.g. 1.5L is reported as an error, rather than parsed as 1.5.
		final char suffix = Character.toLowerCase(ch(s, i));
		final boolean forceLong = suffix == 'l' && (radix == 10 || !floating);
		final boolean forceFloat = suffix == 'f' && (radix == 10 || floating);
		final boolean forceDouble = suffix == 'd' && (radix == 10 || floating);
		if (forceLong || forceFloat || forceDouble) i++;

		Number result = null;
		if (!floating && !forceFloat && !forceDouble) {
			// No fractional or exponent part. So this *might* be an integer!
			// As in Java, a leading zero followed solely by octal digits is octal.
			final boolean octal = radix == 10 && s.charAt(digitsStart) == '0' &&
				digitsEnd - digitsStart > 1 && isOctal(s, digitsStart + 1, digitsEnd);
			result = octal ? //
				parseInteger(s, start, digitsStart + 1, digitsEnd, 8, forceLong) : //
				parseInteger(s, start, digitsStart, digitsEnd, radix, forceLong);
		}
		if (result == null && !forceLong) {
			// NB: Float.parseFloat and Double.parseDouble understand hex notation.
			final String number = s.subSequence(start, end).toString();
			result = parseDecimal(number, forceFloat, forceDouble);
		}
		if (result == null) pos.die("Illegal numeric literal");
		pos.set(i);
		return result;
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
	 * @see #parseNumber(CharSequence, Position)
	 */
	public static Object parseLiteral(final CharSequence s, final Position pos) {
		final Boolean bool = parseBoolean(s, pos);
		if (bool != null) return bool;

		final String str = parseString(s, pos);
		if (str != null) return str;

		final Number num = parseNumber(s, pos);
		if (num != null) return num;

		return null;
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

	private static boolean isNumberSyntax(final CharSequence s,
		final Position pos)
	{
		final int i = pos.get();
		int d = isSign(ch(s, i)) ? i + 1 : i;
		if (ch(s, d) == '.') d++; // e.g. .5
		return isDigit(ch(s, d), 10);
	}

	/**
	 * Parses already-validated digits as an {@link Integer} if sufficiently
	 * small; or a {@link Long} if needed or if {@code forceLong} is set; or a
	 * {@link BigInteger} if the value is too large even for {@code long}, unless
	 * {@code forceLong} is set, in which case null is returned instead.
	 *
	 * @param signIndex Index of the optional sign; equal to {@code start} when
	 *          there is no sign.
	 */
	private static Number parseInteger(final CharSequence s, final int signIndex,
		final int start, final int end, final int radix, final boolean forceLong)
	{
		final boolean negative = s.charAt(signIndex) == '-';
		// NB: Accumulate negatively, as Long.parseLong does, so that
		// Long.MIN_VALUE (whose magnitude exceeds Long.MAX_VALUE) still fits.
		final long limit = negative ? Long.MIN_VALUE : -Long.MAX_VALUE;
		final long multmin = limit / radix;
		long result = 0;
		for (int i = start; i < end; i++) {
			final int digit = Character.digit(s.charAt(i), radix);
			if (result < multmin || result * radix < limit + digit) {
				// Too large for a long.
				if (forceLong) return null;
				final String digits = s.subSequence(start, end).toString();
				return new BigInteger(negative ? "-" + digits : digits, radix);
			}
			result = result * radix - digit;
		}
		final long value = negative ? result : -result;
		if (!forceLong && value == (int) value) return (int) value;
		return value;
	}

	private static Number parseDecimal(final String number,
		final boolean forceFloat, final boolean forceDouble)
	{
		if (forceFloat) {
			// Try to fit it into a flaot.
			try {
				return Float.parseFloat(number);
			}
			catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}
		else {
			// Try to fit it into a double.
			try {
				return Double.parseDouble(number);
			}
			catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		if (!forceDouble && !forceFloat) {
			// Try to treat it as a BigDecimal.
			try {
				return new BigDecimal(number);
			}
			catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		return null;
	}

	/** Gets the character at the given index, or {@code '\0'} if past the end. */
	private static char ch(final CharSequence s, final int index) {
		return index < s.length() ? s.charAt(index) : '\0';
	}

	private static boolean isSign(final char c) {
		return c == '-' || c == '+';
	}

	/** Checks for an ASCII digit of the given radix (at most 36). */
	private static boolean isDigit(final char c, final int radix) {
		if (c >= '0' && c <= '9') return c - '0' < radix;
		if (c >= 'a' && c <= 'z') return c - 'a' + 10 < radix;
		if (c >= 'A' && c <= 'Z') return c - 'A' + 10 < radix;
		return false;
	}

	private static boolean isOctal(final CharSequence s, final int start,
		final int end)
	{
		for (int i = start; i < end; i++) {
			if (!isOctal(s.charAt(i))) return false;
		}
		return true;
	}

	private static int skipDigits(final CharSequence s, int index,
		final int radix)
	{
		while (isDigit(ch(s, index), radix)) index++;
		return index;
	}

	/**
	 * Skips past the exponent part (e.g. {@code e-5}) at the given index, if
	 * there is one with at least one digit; otherwise returns the index as is.
	 */
	private static int skipExponent(final CharSequence s, final int index,
		final char marker)
	{
		if (Character.toLowerCase(ch(s, index)) != marker) return index;
		final int i = isSign(ch(s, index + 1)) ? index + 2 : index + 1;
		return isDigit(ch(s, i), 10) ? skipDigits(s, i, 10) : index;
	}

	private static boolean isWord(final CharSequence s, final Position pos,
		final String word)
	{
		if (s.length() - pos.get() < word.length()) return false;
		for (int i=0; i<word.length(); i++) {
			if (pos.ch(s, i) != word.charAt(i)) return false;
		}
		final char next = pos.ch(s, word.length());
		if (next >= 'a' && next <= 'z') return false;
		if (next >= 'A' && next <= 'Z') return false;
		if (next >= '0' && next <= '9') return false;
		if (next == '_') return false;
		return true;
	}

}
