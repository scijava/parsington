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

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Tests {@link Literals}.
 *
 * @author Curtis Rueden
 */
public class ParseNumberTest extends AbstractTest {


	@Test
	public void testParseHexInteger() {
		assertNumber(0x123, ParseNumber.parseAllNumbers("0x123"));
		// Test explicit long.
		assertNumber(0x123L, ParseNumber.parseAllNumbers("0x123L"));
		// Test implicit long.
		assertNumber(0x123456789abcdefL, ParseNumber.parseAllNumbers("0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = ParseNumber.parseAllNumbers("0x" + big);
		assertNumber(new BigInteger(big, 16), bigNum);
	}

	@Test
	public void testParseHexNegativeInteger() {
		assertNumber(-0x123, ParseNumber.parseAllNumbers("-0x123"));
		// Test explicit long.
		assertNumber(-0x123L, ParseNumber.parseAllNumbers("-0x123L"));
		// Test implicit long.
		assertNumber(-0x123456789abcdefL, ParseNumber.parseAllNumbers("-0x123456789abcdef"));
		// Test BigInteger.
		final String big = "123456789abcdeffedcba987654321";
		final Number bigNum = ParseNumber.parseAllNumbers("-0x" + big);
		assertNumber(new BigInteger("-" + big, 16), bigNum);
	}

	@Test
	public void testParseHexFloat() {
		assertNumber(0xfedcba.98765432P-10f, //
			ParseNumber.parseAllNumbers("0xfedcba.98765432P-10f"));
		assertNumber(0x1.fffffffffffffP+1023, //
			ParseNumber.parseAllNumbers("0x1.fffffffffffffP+1023"));
		assertNumber(0xff.fP0F, ParseNumber.parseAllNumbers("0xff.fP0F"));
		assertNumber(0x1P+1023, ParseNumber.parseAllNumbers("0x1P+1023"));
		assertNumber(0xfP102, ParseNumber.parseAllNumbers("0xfP102"));
		assertNumber(0xfP-102, ParseNumber.parseAllNumbers("0xfP-102"));
		assertNumber(0xffP-102, ParseNumber.parseAllNumbers("0xffP-102"));
		assertNumber(0x123.456P1, ParseNumber.parseAllNumbers("0x123.456P1"));
		assertNumber(0x123.456P0f, ParseNumber.parseAllNumbers("0x123.456P0f"));
		assertNumber(0x123.456P0d, ParseNumber.parseAllNumbers("0x123.456P0d"));
		assertNumber(0x123.456P-10d, ParseNumber.parseAllNumbers("0x123.456P-10d"));
		assertNumber(0x123.456P+009, ParseNumber.parseAllNumbers("0x123.456P+009"));
	}

	@Test
	public void testParseHexNegativeFloat() {
		assertNumber(-0xfedcba.98765432P-10f, //
			ParseNumber.parseAllNumbers("-0xfedcba.98765432P-10f"));
		assertNumber(-0x1.fffffffffffffP+1023, //
			ParseNumber.parseAllNumbers("-0x1.fffffffffffffP+1023"));
		assertNumber(-0xff.fP0F, ParseNumber.parseAllNumbers("-0xff.fP0F"));
		assertNumber(-0x1P+1023, ParseNumber.parseAllNumbers("-0x1P+1023"));
		assertNumber(-0xfP102, ParseNumber.parseAllNumbers("-0xfP102"));
		assertNumber(-0xfP-102, ParseNumber.parseAllNumbers("-0xfP-102"));
		assertNumber(-0xffP-102, ParseNumber.parseAllNumbers("-0xffP-102"));
		assertNumber(-0x123.456P1, ParseNumber.parseAllNumbers("-0x123.456P1"));
		assertNumber(-0x123.456P0f, ParseNumber.parseAllNumbers("-0x123.456P0f"));
		assertNumber(-0x123.456P0d, ParseNumber.parseAllNumbers("-0x123.456P0d"));
		assertNumber(-0x123.456P-10d, ParseNumber.parseAllNumbers("-0x123.456P-10d"));
		assertNumber(-0x123.456P+009, ParseNumber.parseAllNumbers("-0x123.456P+009"));
	}

	@Test
	public void testParseBinary() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(33, ParseNumber.parseAllNumbers("0b100001"));
		// Test explicit long.
		assertNumber(33L, ParseNumber.parseAllNumbers("0b100001L"));
		// Test implicit long.
		assertNumber(194588677707L, ParseNumber.parseAllNumbers(
			"0b10110101001110011000111001011001001011"));
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = ParseNumber.parseAllNumbers("0b" + big);
		assertNumber(new BigInteger(big, 2), bigNum);
	}

	@Test
	public void testParseBinaryNegative() {
		// NB: "0b..." syntax is only supported starting with Java 7.
		assertNumber(-33, ParseNumber.parseAllNumbers("-0b100001"));
		// Test explicit long.
		assertNumber(-33L, ParseNumber.parseAllNumbers("-0b100001L"));
		// Test implicit long.
		assertNumber(-194588677707L, ParseNumber.parseAllNumbers(
			"-0b10110101001110011000111001011001001011"));
		// Test BigInteger.
		final String big =
			"10110011100011110000111110000011111100000011111110000000"
				+ "111111110000000011111111100000000011111111110000000000";
		final Number bigNum = ParseNumber.parseAllNumbers("-0b" + big);
		assertNumber(new BigInteger("-" + big, 2), bigNum);
	}

	@Test
	public void testParseOctal() {
		assertNumber(01234567, ParseNumber.parseAllNumbers("01234567"));
		// Test explicit long.
		assertNumber(01234567L, ParseNumber.parseAllNumbers("01234567L"));
		// Test implicit long.
		assertNumber(012345677654321L, ParseNumber.parseAllNumbers("012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = ParseNumber.parseAllNumbers("0" + big);
		assertNumber(new BigInteger(big, 8), bigNum);
	}

	@Test
	public void testParseOctalNegative() {
		assertNumber(-01234567, ParseNumber.parseAllNumbers("-01234567"));
		// Test explicit long.
		assertNumber(-01234567L, ParseNumber.parseAllNumbers("-01234567L"));
		// Test implicit long.
		assertNumber(-012345677654321L, ParseNumber.parseAllNumbers("-012345677654321"));
		// Test BigInteger.
		final String big = "1234567765432112345677654321";
		final Number bigNum = ParseNumber.parseAllNumbers("-0" + big);
		assertNumber(new BigInteger("-" + big, 8), bigNum);
	}

	@Test
	public void testParseDecimal() {
		assertNumber(1, ParseNumber.parseAllNumbers("1+2"));
		assertNumber(01.01, ParseNumber.parseAllNumbers("01.01"));
		assertNull(ParseNumber.parseAllNumbers("x01.01 yada")); // x
		assertNull(ParseNumber.parseAllNumbers("01.01E yada")); // needs digit after E
		assertNull(ParseNumber.parseAllNumbers("01.01EE yada")); // double E
		assertNull(ParseNumber.parseAllNumbers("01.01E1.2 yada")); // decimal after E
		assertNumber(01.01E1, ParseNumber.parseAllNumbers("01.01E1 "));
		assertNumber(01.01E+1, ParseNumber.parseAllNumbers("01.01E+1 "));
		assertNumber(01.01E-1, ParseNumber.parseAllNumbers("01.01E-1 "));
		assertNull(ParseNumber.parseAllNumbers("01.01E++1l "));
		assertNull(ParseNumber.parseAllNumbers("01.01E--1l "));
		assertNumber(01.01E-13, ParseNumber.parseAllNumbers("01.01E-13 "));
		assertNumber(01.01E-13f, ParseNumber.parseAllNumbers("01.01E-13f "));
		assertNumber(01.01E-13d, ParseNumber.parseAllNumbers("01.01E-13d "));
		assertNull(ParseNumber.parseAllNumbers("01.01E-13l ")); // will not fit in long
		assertNull(ParseNumber.parseAllNumbers("+ ya"));
		assertNull(ParseNumber.parseAllNumbers("+. ya"));
		// This seems to be valid in java but does not match re since a digit is required before the decimal.
		// assertNull(ParseNumber.parseAllNumbers("+.2 ya"));
		assertNumber(+.2, ParseNumber.parseAllNumbers("+.2 ya"));
		assertNumber(.2, ParseNumber.parseAllNumbers(".2 ya"));


		assertNumber(123456789, ParseNumber.parseAllNumbers("123456789"));
		// Test explicit long.
		assertNumber(123456789L, ParseNumber.parseAllNumbers("123456789L"));
		// Test implicit long.
		assertNumber(123456787654321L, ParseNumber.parseAllNumbers("123456787654321"));
		// Test BigInteger.
		final String bigI = "1234567898765432123456789";
		final Number bigInt = ParseNumber.parseAllNumbers(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(1f, ParseNumber.parseAllNumbers("1f"));
		// Test explicit double.
		assertNumber(1d, ParseNumber.parseAllNumbers("1d"));
		// Test implicit double.
		assertNumber(1.0, ParseNumber.parseAllNumbers("1.0"));
		assertNumber(1., ParseNumber.parseAllNumbers("1."));
		// Test scientific notation.
		assertNumber(1e2, ParseNumber.parseAllNumbers("1e2"));
		assertNumber(1.2e3, ParseNumber.parseAllNumbers("1.2e3"));
		assertNumber(4.5e-6, ParseNumber.parseAllNumbers("4.5e-6"));
		assertNumber(1.2e3f, ParseNumber.parseAllNumbers("1.2e3f"));
		assertNumber(4.5e-6f, ParseNumber.parseAllNumbers("4.5e-6f"));
	}

	@Test
	public void testParseDecimalNegative() {
		assertNumber(-123456789, ParseNumber.parseAllNumbers("-123456789"));
		// Test explicit long.
		assertNumber(-123456789L, ParseNumber.parseAllNumbers("-123456789L"));
		// Test implicit long.
		assertNumber(-123456787654321L, ParseNumber.parseAllNumbers("-123456787654321"));
		// Test BigInteger.
		final String bigI = "-1234567898765432123456789";
		final Number bigInt = ParseNumber.parseAllNumbers(bigI);
		assertNumber(new BigInteger(bigI), bigInt);
		// Test explicit float.
		assertNumber(-1f, ParseNumber.parseAllNumbers("-1f"));
		// Test explicit double.
		assertNumber(-1d, ParseNumber.parseAllNumbers("-1d"));
		// Test implicit double.
		assertNumber(-1.0, ParseNumber.parseAllNumbers("-1.0"));
		assertNumber(-1., ParseNumber.parseAllNumbers("-1."));
		// Test scientific notation.
		assertNumber(-1e2, ParseNumber.parseAllNumbers("-1e2"));
		assertNumber(-1.2e3, ParseNumber.parseAllNumbers("-1.2e3"));
		assertNumber(-4.5e-6, ParseNumber.parseAllNumbers("-4.5e-6"));
		assertNumber(-1.2e3f, ParseNumber.parseAllNumbers("-1.2e3f"));
		assertNumber(-4.5e-6f, ParseNumber.parseAllNumbers("-4.5e-6f"));
	}


}
