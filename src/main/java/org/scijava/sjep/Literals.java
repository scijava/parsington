/*
 * #%L
 * SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 Board of Regents of the University of
 * Wisconsin-Madison.
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

package org.scijava.sjep;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for parsing literals from strings. These methods largely
 * conform to the Java specification's ideas of what constitutes a numeric or
 * string literal.
 *
 * @author Curtis Rueden
 */
public final class Literals {

	private static final Pattern HEX = Pattern
		.compile("((-?)0[Xx]([0-9a-fA-F]+)([Ll]?)).*");

	private static final Pattern BINARY = Pattern
		.compile("((-?)0[Bb]([01]+)([Ll]?)).*");

	private static final Pattern OCTAL = Pattern
		.compile("((-?)0([0-7]+)([Ll]?)).*");

	private static final Pattern DECIMAL = Pattern
		.compile("((-?[0-9]+(\\.[0-9]*)?([Ee][0-9]+)?)([Dd]|[Ff]|[Ll])?).*");

	private Literals() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Parses a hexidecimal literal (e.g., {@code 0xfedcba9876543210}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}.
	 */
	public static Number parseHex(final CharSequence s) {
		return parseHex(s, new Position());
	}

	/**
	 * Parses a binary literal (e.g., {@code 0b010101000011}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}.
	 */
	public static Number parseBinary(final CharSequence s) {
		return parseBinary(s, new Position());
	}

	/**
	 * Parses an octal literal (e.g., {@code 01234567}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}.
	 */
	public static Number parseOctal(final CharSequence s) {
		return parseOctal(s, new Position());
	}

	/**
	 * Parses a decimal literal (integer or otherwise; e.g., {@code 1234567890},
	 * {@code 1234.0987} or {@code 1.2e34}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseDecimal(final CharSequence s) {
		return parseDecimal(s, new Position());
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
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseNumber(final CharSequence s) {
		return parseNumber(s, new Position());
	}

	/**
	 * Parses a hexidecimal literal (e.g., {@code 0xfedcba9876543210}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}; or {@code null} if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseHex(final CharSequence s, final Position pos) {
		return parseInteger(HEX, s, pos, 16);
	}

	/**
	 * Parses a binary literal (e.g., {@code 0b010101000011}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}; or {@code null} if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseBinary(final CharSequence s, final Position pos) {
		return parseInteger(BINARY, s, pos, 2);
	}

	/**
	 * Parses an octal literal (e.g., {@code 01234567}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long}; or {@code null} if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseOctal(final CharSequence s, final Position pos) {
		return parseInteger(OCTAL, s, pos, 8);
	}

	/**
	 * Parses a decimal literal (e.g., {@code 1234.0987} or {@code 1.2e34}).
	 * 
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseDecimal(final CharSequence s, final Position pos) {
		if (!isNumberSyntax(s, pos)) return null;

		final Matcher m = matcher(DECIMAL, s, pos);
		if (!m.matches()) return null;
		final String number = m.group(2);
		final String force = m.group(5);
		final boolean forceLong = "l".equalsIgnoreCase(force);
		final boolean forceFloat = "f".equalsIgnoreCase(force);
		final boolean forceDouble = "d".equalsIgnoreCase(force);
		Number result = null;
		if (!forceFloat && !forceDouble) {
			result = parseInteger(number, forceLong, 10);
		}
		if (result == null && !forceLong) {
			result = parseDecimal(number, forceFloat, forceDouble);
		}
		return verifyResult(result, m, pos);
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
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseNumber(final CharSequence s, final Position pos) {
		final Number hex = parseHex(s, pos);
		if (hex != null) return hex;

		final Number binary = parseBinary(s, pos);
		if (binary != null) return binary;

		final Number octal = parseOctal(s, pos);
		if (octal != null) return octal;

		final Number decimal = parseDecimal(s, pos);
		if (decimal != null) return decimal;

		return null;
	}

	// -- Helper methods --

	private static boolean
		isNumberSyntax(final CharSequence s, final Position pos)
	{
		final int i = pos.get();
		final boolean negative = s.charAt(i) == '-';
		final char digit = s.charAt(negative ? i + 1 : i);
		return digit >= '0' && digit <= '9';
	}

	private static Number parseInteger(final Pattern p, final CharSequence s,
		final Position pos, final int base)
	{
		if (!isNumberSyntax(s, pos)) return null;

		final Matcher m = matcher(p, s, pos);
		if (!m.matches()) return null;
		String sign = m.group(2);
		if (sign == null) sign = "";
		final String number = sign + m.group(3);
		final boolean forceLong = !m.group(4).isEmpty();
		final Number result = parseInteger(number, forceLong, base);
		return verifyResult(result, m, pos);
	}

	private static Number parseInteger(final String number,
		final boolean forceLong, final int base)
	{
		if (!forceLong) {
			// Try to fit it into an int.
			try {
				return Integer.parseInt(number, base);
			}
			catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		// Try to fit it into a long.
		try {
			return Long.parseLong(number, base);
		}
		catch (final NumberFormatException exc) {
			// NB: No action needed.
		}

		if (!forceLong) {
			// Try to treat it as a BigInteger.
			try {
				return new BigInteger(number, base);
			}
			catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		return null;
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

	private static Matcher matcher(final Pattern p, final CharSequence s,
		final Position pos)
	{
		return p.matcher(sub(s, pos));
	}

	private static CharSequence sub(final CharSequence s, final Position pos) {
		return pos.get() == 0 ? s : new SubSequence(s, pos.get());
	}

	private static Number verifyResult(final Number result, final Matcher m,
		final Position pos)
	{
		if (result == null) pos.die("Illegal numeric literal");
		pos.inc(m.group(1).length());
		return result;
	}

}
