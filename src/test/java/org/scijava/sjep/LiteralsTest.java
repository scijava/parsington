/*
 * #%L
 * SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2016 Board of Regents of the University of
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.math.BigInteger;

import org.junit.Test;

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
		// Test position
		final Position pos = new Position();
		pos.set(2);
		assertEquals("cde", Literals.parseString("ab'cde'fg", pos));
		assertEquals(7, pos.get());
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
	public void testParseHex() {
		assertNumber(0x123, Literals.parseHex("0x123"));
		// Test explicit long.
		assertNumber(0x123L, Literals.parseHex("0x123L"));
		// Test implicit long.
		assertNumber(0x123456789abcdefL, Literals.parseHex("0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = Literals.parseHex("0x" + big);
		assertNumber(new BigInteger(big, 16), bigNum);
	}

	@Test
	public void testParseHexNegative() {
		assertNumber(-0x123, Literals.parseHex("-0x123"));
		// Test explicit long.
		assertNumber(-0x123L, Literals.parseHex("-0x123L"));
		// Test implicit long.
		assertNumber(-0x123456789abcdefL, Literals.parseHex("-0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = Literals.parseHex("-0x" + big);
		assertNumber(new BigInteger("-" + big, 16), bigNum);
	}

	@Test
	public void testParseBinary() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(33, Literals.parseBinary("0b100001"));
		// Test explicit long.
		assertNumber(33L, Literals.parseBinary("0b100001L"));
		// Test implicit long.
		// TODO
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = Literals.parseBinary("0b" + big);
		assertNumber(new BigInteger(big, 2), bigNum);
	}

	@Test
	public void testParseBinaryNegative() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(-33, Literals.parseBinary("-0b100001"));
		// Test explicit long.
		assertNumber(-33L, Literals.parseBinary("-0b100001L"));
		// Test implicit long.
		// TODO
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = Literals.parseBinary("-0b" + big);
		assertNumber(new BigInteger("-" + big, 2), bigNum);
	}

	@Test
	public void testParseOctal() {
		assertNumber(01234567, Literals.parseOctal("01234567"));
		// Test explicit long.
		assertNumber(01234567L, Literals.parseOctal("01234567L"));
		// Test implicit long.
		assertNumber(012345677654321L, Literals.parseOctal("012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = Literals.parseOctal("0" + big);
		assertNumber(new BigInteger(big, 8), bigNum);
	}

	@Test
	public void testParseOctalNegative() {
		assertNumber(-01234567, Literals.parseOctal("-01234567"));
		// Test explicit long.
		assertNumber(-01234567L, Literals.parseOctal("-01234567L"));
		// Test implicit long.
		assertNumber(-012345677654321L, Literals.parseOctal("-012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = Literals.parseOctal("-0" + big);
		assertNumber(new BigInteger("-" + big, 8), bigNum);
	}

	@Test
	public void testParseDecimal() {
		assertNumber(123456789, Literals.parseDecimal("123456789"));
		// Test explicit long.
		assertNumber(123456789L, Literals.parseDecimal("123456789L"));
		// Test implicit long.
		assertNumber(123456787654321L, Literals.parseDecimal("123456787654321"));
		// Test BigInteger.
		final String bigI = "1234567898765432123456789";
		final Number bigInt = Literals.parseDecimal(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(1f, Literals.parseDecimal("1f"));
		// Test explicit double.
		assertNumber(1d, Literals.parseDecimal("1d"));
		// Test implicit double.
		assertNumber(1.0, Literals.parseDecimal("1.0"));
		assertNumber(1., Literals.parseDecimal("1."));
		// Test scientific notation.
		assertNumber(1e2, Literals.parseDecimal("1e2"));
		assertNumber(1.2e3, Literals.parseDecimal("1.2e3"));
		assertNumber(1.2e3f, Literals.parseDecimal("1.2e3f"));
	}

	@Test
	public void testParseDecimalNegative() {
		assertNumber(-123456789, Literals.parseDecimal("-123456789"));
		// Test explicit long.
		assertNumber(-123456789L, Literals.parseDecimal("-123456789L"));
		// Test implicit long.
		assertNumber(-123456787654321L, Literals.parseDecimal("-123456787654321"));
		// Test BigInteger.
		final String bigI = "-1234567898765432123456789";
		final Number bigInt = Literals.parseDecimal(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(-1f, Literals.parseDecimal("-1f"));
		// Test explicit double.
		assertNumber(-1d, Literals.parseDecimal("-1d"));
		// Test implicit double.
		assertNumber(-1.0, Literals.parseDecimal("-1.0"));
		assertNumber(-1., Literals.parseDecimal("-1."));
		// Test scientific notation.
		assertNumber(-1e2, Literals.parseDecimal("-1e2"));
		assertNumber(-1.2e3, Literals.parseDecimal("-1.2e3"));
		assertNumber(-1.2e3f, Literals.parseDecimal("-1.2e3f"));
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
	}

	@Test
	public void testParseLiteral() {
		assertSame(Boolean.FALSE, Literals.parseLiteral("false"));
		assertSame(Boolean.TRUE, Literals.parseLiteral("true"));

		assertEquals("fubar", Literals.parseLiteral("'fubar'"));

		assertNumber(0, Literals.parseLiteral("0"));
	}

}
