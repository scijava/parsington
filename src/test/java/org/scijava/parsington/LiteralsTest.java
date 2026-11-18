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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Literals}.
 *
 * @author Curtis Rueden
 */
public class LiteralsTest extends AbstractTest {

	@Test
	public void testParseBoolean() {
		assertSame(Boolean.FALSE, Literals.parseBoolean("false"));
		assertSame(Boolean.TRUE, Literals.parseBoolean("true"));

		assertNull(Literals.parseBoolean("zfalse"));
		assertNull(Literals.parseBoolean("zfalsez"));
		assertNull(Literals.parseBoolean("ztrue"));
		assertNull(Literals.parseBoolean("ztruez"));

		final Position pos = new Position();
		pos.set(0);
		assertSame(Boolean.FALSE, Literals.parseBoolean("false-", pos));
		assertEquals(5, pos.get());
		pos.set(0);
		assertSame(Boolean.TRUE, Literals.parseBoolean("true-", pos));
		assertEquals(4, pos.get());
	}

	@Test
	public void testParseString() {
		assertEquals("hello world", Literals.parseString("'hello world'"));
		// Test escape sequences.
		assertEquals("a\b\t\n\f\r\"\\z", Literals
			.parseString("\"a\\b\\t\\n\\f\\r\\\"\\\\z\""));
		assertEquals("\t\\\t\\\\\t", Literals
			.parseString("\"\\t\\\\\\t\\\\\\\\\\t\""));
		// Test Unicode escape sequences.
		assertEquals("\u9654", Literals.parseString("\"\u9654\""));
		assertEquals("xyz\u9654abc", Literals.parseString("\"xyz\\u9654abc\""));
		// Test octal escape sequences.
		assertEquals("\0", Literals.parseString("\"\\0\""));
		assertEquals("\00", Literals.parseString("\"\\00\""));
		assertEquals("\000", Literals.parseString("\"\\000\""));
		assertEquals("\12", Literals.parseString("\"\\12\""));
		assertEquals("\123", Literals.parseString("\"\\123\""));
		assertEquals("\377", Literals.parseString("\"\\377\""));
		assertEquals("\1234", Literals.parseString("\"\\1234\""));
		// Test escape sequences adjacent to the closing quote.
		assertEquals("ab\"", Literals.parseString("\"ab\\\"\""));
		assertEquals("ab\\", Literals.parseString("\"ab\\\\\""));
		assertEquals("ab\u9654", Literals.parseString("\"ab\\u9654\""));
		assertEquals("ab\12", Literals.parseString("\"ab\\12\""));
		// Test consecutive escape sequences with surrounding text.
		assertEquals("\"\n\u9654\0x", Literals.parseString("\"\\\"\\n\\u9654\\0x\""));
		assertEquals("a\12b\1c", Literals.parseString("\"a\\12b\\1c\""));
		// Test empty strings.
		assertEquals("", Literals.parseString("''"));
		assertEquals("", Literals.parseString("\"\""));
		// Test that single-quoted strings are verbatim.
		assertEquals("a\\nb\\", Literals.parseString("'a\\nb\\'"));
		assertEquals("a\"b", Literals.parseString("'a\"b'"));
		assertEquals("a'b", Literals.parseString("\"a'b\""));
		// Test position
		final Position pos = new Position();
		pos.set(2);
		assertEquals("cde", Literals.parseString("ab'cde'fg", pos));
		assertEquals(7, pos.get());
		pos.set(2);
		assertEquals("c\"d\te", Literals.parseString("ab\"c\\\"d\\te\"fg", pos));
		assertEquals(11, pos.get());
	}

	@Test
	public void testParseStringInvalid() {
		// Test non-string tokens.
		assertNull(Literals.parseString(""));
		assertNull(Literals.parseString("1234"));
		assertNull(Literals.parseString("foo"));
		assertNull(Literals.parseString("a'b'c"));
		// Test malformed string literals.
		try {
			Literals.parseString("'");
			fail("IllegalArgumentException expected");
		}
		catch (final IllegalArgumentException exc) {
			assertEquals("Unclosed string literal at index 0", exc.getMessage());
		}
	}

	@Test
	public void testParseHexInteger() {
		assertNumber(0x123, Literals.parseNumber("0x123"));
		// Test explicit long.
		assertNumber(0x123L, Literals.parseNumber("0x123L"));
		// Test implicit long.
		assertNumber(0x123456789abcdefL, Literals.parseNumber("0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = Literals.parseNumber("0x" + big);
		assertNumber(new BigInteger(big, 16), bigNum);
	}

	@Test
	public void testParseHexNegativeInteger() {
		assertNumber(-0x123, Literals.parseNumber("-0x123"));
		// Test explicit long.
		assertNumber(-0x123L, Literals.parseNumber("-0x123L"));
		// Test implicit long.
		assertNumber(-0x123456789abcdefL, Literals.parseNumber("-0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = Literals.parseNumber("-0x" + big);
		assertNumber(new BigInteger("-" + big, 16), bigNum);
	}

	@Test
	public void testParseHexFloat() {
		assertNumber(0xfedcba.98765432P-10f, //
			Literals.parseNumber("0xfedcba.98765432P-10f"));
		assertNumber(0x1.fffffffffffffP+1023, //
			Literals.parseNumber("0x1.fffffffffffffP+1023"));
		assertNumber(0xff.fP0F, Literals.parseNumber("0xff.fP0F"));
		assertNumber(0x1P+1023, Literals.parseNumber("0x1P+1023"));
		assertNumber(0xfP102, Literals.parseNumber("0xfP102"));
		assertNumber(0xfP-102, Literals.parseNumber("0xfP-102"));
		assertNumber(0xffP-102, Literals.parseNumber("0xffP-102"));
		assertNumber(0x123.456P1, Literals.parseNumber("0x123.456P1"));
		assertNumber(0x123.456P0f, Literals.parseNumber("0x123.456P0f"));
		assertNumber(0x123.456P0d, Literals.parseNumber("0x123.456P0d"));
		assertNumber(0x123.456P-10d, Literals.parseNumber("0x123.456P-10d"));
		assertNumber(0x123.456P+009, Literals.parseNumber("0x123.456P+009"));
	}

	@Test
	public void testParseHexNegativeFloat() {
		assertNumber(-0xfedcba.98765432P-10f, //
			Literals.parseNumber("-0xfedcba.98765432P-10f"));
		assertNumber(-0x1.fffffffffffffP+1023, //
			Literals.parseNumber("-0x1.fffffffffffffP+1023"));
		assertNumber(-0xff.fP0F, Literals.parseNumber("-0xff.fP0F"));
		assertNumber(-0x1P+1023, Literals.parseNumber("-0x1P+1023"));
		assertNumber(-0xfP102, Literals.parseNumber("-0xfP102"));
		assertNumber(-0xfP-102, Literals.parseNumber("-0xfP-102"));
		assertNumber(-0xffP-102, Literals.parseNumber("-0xffP-102"));
		assertNumber(-0x123.456P1, Literals.parseNumber("-0x123.456P1"));
		assertNumber(-0x123.456P0f, Literals.parseNumber("-0x123.456P0f"));
		assertNumber(-0x123.456P0d, Literals.parseNumber("-0x123.456P0d"));
		assertNumber(-0x123.456P-10d, Literals.parseNumber("-0x123.456P-10d"));
		assertNumber(-0x123.456P+009, Literals.parseNumber("-0x123.456P+009"));
	}

	@Test
	public void testParseBinary() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(33, Literals.parseNumber("0b100001"));
		// Test explicit long.
		assertNumber(33L, Literals.parseNumber("0b100001L"));
		// Test implicit long.
		assertNumber(194588677707L, Literals.parseNumber(
			"0b10110101001110011000111001011001001011"));
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = Literals.parseNumber("0b" + big);
		assertNumber(new BigInteger(big, 2), bigNum);
	}

	@Test
	public void testParseBinaryNegative() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(-33, Literals.parseNumber("-0b100001"));
		// Test explicit long.
		assertNumber(-33L, Literals.parseNumber("-0b100001L"));
		// Test implicit long.
		assertNumber(-194588677707L, Literals.parseNumber(
			"-0b10110101001110011000111001011001001011"));
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = Literals.parseNumber("-0b" + big);
		assertNumber(new BigInteger("-" + big, 2), bigNum);
	}

	@Test
	public void testParseOctal() {
		assertNumber(01234567, Literals.parseNumber("01234567"));
		// Test explicit long.
		assertNumber(01234567L, Literals.parseNumber("01234567L"));
		// Test implicit long.
		assertNumber(012345677654321L, Literals.parseNumber("012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = Literals.parseNumber("0" + big);
		assertNumber(new BigInteger(big, 8), bigNum);
	}

	@Test
	public void testParseOctalNegative() {
		assertNumber(-01234567, Literals.parseNumber("-01234567"));
		// Test explicit long.
		assertNumber(-01234567L, Literals.parseNumber("-01234567L"));
		// Test implicit long.
		assertNumber(-012345677654321L, Literals.parseNumber("-012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = Literals.parseNumber("-0" + big);
		assertNumber(new BigInteger("-" + big, 8), bigNum);
	}

	@Test
	public void testParseDecimal() {
		assertNumber(123456789, Literals.parseNumber("123456789"));
		// Test explicit long.
		assertNumber(123456789L, Literals.parseNumber("123456789L"));
		// Test implicit long.
		assertNumber(123456787654321L, Literals.parseNumber("123456787654321"));
		// Test BigInteger.
		final String bigI = "1234567898765432123456789";
		final Number bigInt = Literals.parseNumber(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(1f, Literals.parseNumber("1f"));
		// Test explicit double.
		assertNumber(1d, Literals.parseNumber("1d"));
		// Test implicit double.
		assertNumber(1.0, Literals.parseNumber("1.0"));
		assertNumber(1., Literals.parseNumber("1."));
		// Test scientific notation.
		assertNumber(1e2, Literals.parseNumber("1e2"));
		assertNumber(1.2e3, Literals.parseNumber("1.2e3"));
		assertNumber(4.5e-6, Literals.parseNumber("4.5e-6"));
		assertNumber(1.2e3f, Literals.parseNumber("1.2e3f"));
		assertNumber(4.5e-6f, Literals.parseNumber("4.5e-6f"));
	}

	@Test
	public void testParseDecimalNegative() {
		assertNumber(-123456789, Literals.parseNumber("-123456789"));
		// Test explicit long.
		assertNumber(-123456789L, Literals.parseNumber("-123456789L"));
		// Test implicit long.
		assertNumber(-123456787654321L, Literals.parseNumber("-123456787654321"));
		// Test BigInteger.
		final String bigI = "-1234567898765432123456789";
		final Number bigInt = Literals.parseNumber(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(-1f, Literals.parseNumber("-1f"));
		// Test explicit double.
		assertNumber(-1d, Literals.parseNumber("-1d"));
		// Test implicit double.
		assertNumber(-1.0, Literals.parseNumber("-1.0"));
		assertNumber(-1., Literals.parseNumber("-1."));
		// Test scientific notation.
		assertNumber(-1e2, Literals.parseNumber("-1e2"));
		assertNumber(-1.2e3, Literals.parseNumber("-1.2e3"));
		assertNumber(-4.5e-6, Literals.parseNumber("-4.5e-6"));
		assertNumber(-1.2e3f, Literals.parseNumber("-1.2e3f"));
		assertNumber(-4.5e-6f, Literals.parseNumber("-4.5e-6f"));
	}

	@Test
	public void testParseNumber() {
		final Position pos = new Position();

		assertNumber(0, Literals.parseNumber("0", pos));
		assertEquals(1, pos.get());

		pos.set(1);
		assertNumber(5.7, Literals.parseNumber("a5.7a", pos));
		assertEquals(4, pos.get());

		pos.set(2);
		assertNumber(-11, Literals.parseNumber("bb-11bb", pos));
		assertEquals(5, pos.get());

		pos.set(3);
		assertNumber(0x123L, Literals.parseNumber("ccc0x123Lccc", pos));
		assertEquals(9, pos.get());

		pos.set(1);
		assertNumber(0b101, Literals.parseNumber("d0b101d", pos));
		assertEquals(6, pos.get());

		pos.set(0);
		assertNumber(0755, Literals.parseNumber("0755+1", pos));
		assertEquals(4, pos.get());
	}

	@Test
	public void testParseNumberEdgeCases() {
		// Octal only applies to integer literals, as in Java.
		assertNumber(7.5, Literals.parseNumber("07.5"));
		assertNumber(7e1, Literals.parseNumber("07e1"));
		assertNumber(7f, Literals.parseNumber("07f"));
		assertNumber(07L, Literals.parseNumber("07L"));
		assertNumber(0, Literals.parseNumber("00"));
		// Leading zero with non-octal digits falls back to decimal.
		assertNumber(89, Literals.parseNumber("089"));

		// Integer/long/BigInteger boundaries.
		assertNumber(Integer.MAX_VALUE, Literals.parseNumber("2147483647"));
		assertNumber(Integer.MIN_VALUE, Literals.parseNumber("-2147483648"));
		assertNumber(2147483648L, Literals.parseNumber("2147483648"));
		assertNumber(Long.MAX_VALUE, Literals.parseNumber("9223372036854775807"));
		assertNumber(Long.MIN_VALUE, Literals.parseNumber("-9223372036854775808"));
		assertNumber(new BigInteger("9223372036854775808"),
			Literals.parseNumber("9223372036854775808"));
		assertNumber(new BigInteger("-9223372036854775809"),
			Literals.parseNumber("-9223372036854775809"));
		assertNumber(0x7fffffffffffffffL, Literals.parseNumber("0x7fffffffffffffff"));
		assertNumber(new BigInteger("8000000000000000", 16),
			Literals.parseNumber("0x8000000000000000"));

		final Position pos = new Position();

		// An exponent marker without digits is not part of the literal.
		assertNumber(1, Literals.parseNumber("1e", pos));
		assertEquals(1, pos.get());
		pos.set(0);
		assertNumber(1, Literals.parseNumber("1e+x", pos));
		assertEquals(1, pos.get());

		// A prefix without digits is not part of the literal.
		pos.set(0);
		assertNumber(0, Literals.parseNumber("0x", pos));
		assertEquals(1, pos.get());
		pos.set(0);
		assertNumber(0, Literals.parseNumber("0b2", pos));
		assertEquals(1, pos.get());

		// Hex floating point requires a binary exponent.
		pos.set(0);
		assertNumber(1, Literals.parseNumber("0x1.8", pos));
		assertEquals(3, pos.get());
		pos.set(0);
		assertNumber(0x1.8p1, Literals.parseNumber("0x1.8p1L", pos));
		assertEquals(7, pos.get());

		// Binary literals accept only an L suffix.
		pos.set(0);
		assertNumber(1, Literals.parseNumber("0b1f", pos));
		assertEquals(3, pos.get());

		// Only ASCII digits are allowed.
		assertNull(Literals.parseNumber("٣"));
		pos.set(0);
		assertNumber(1, Literals.parseNumber("1٣", pos));
		assertEquals(1, pos.get());

		// A leading decimal point is allowed, as in Java.
		assertNumber(.5, Literals.parseNumber(".5"));
		assertNumber(-.5, Literals.parseNumber("-.5"));
		assertNumber(+.5, Literals.parseNumber("+.5"));
		assertNumber(.5f, Literals.parseNumber(".5f"));
		assertNumber(.5d, Literals.parseNumber(".5d"));
		assertNumber(.5e2, Literals.parseNumber(".5e2"));
		pos.set(0);
		assertNumber(.25, Literals.parseNumber(".25.5", pos));
		assertEquals(3, pos.get());

		// Not numbers.
		assertNull(Literals.parseNumber("x1"));
		assertNull(Literals.parseNumber("-x"));
		assertNull(Literals.parseNumber("-"));
		assertNull(Literals.parseNumber("."));
		assertNull(Literals.parseNumber("-."));
		assertNull(Literals.parseNumber(".e5"));
		assertNull(Literals.parseNumber(".f"));
	}

	@Test
	public void testParseNumberInvalid() {
		// Too large for a long, but forced to be one.
		assertThrows(IllegalArgumentException.class,
			() -> Literals.parseNumber("9223372036854775808L"));
		assertThrows(IllegalArgumentException.class,
			() -> Literals.parseNumber("0x10000000000000000L"));
		// A decimal point cannot be combined with a long suffix.
		assertThrows(IllegalArgumentException.class,
			() -> Literals.parseNumber("1.5L"));
		assertThrows(IllegalArgumentException.class,
			() -> Literals.parseNumber(".5L"));
	}

	@Test
	public void testParseLiteral() {
		assertSame(Boolean.FALSE, Literals.parseLiteral("false"));
		assertSame(Boolean.TRUE, Literals.parseLiteral("true"));

		assertEquals("fubar", Literals.parseLiteral("'fubar'"));

		assertNumber(0, Literals.parseLiteral("0"));
	}

}
