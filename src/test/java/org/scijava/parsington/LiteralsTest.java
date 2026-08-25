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
import static org.junit.jupiter.api.Assertions.fail;

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
	public void testParseNumber() {
		final Position pos = new Position();

		assertNumber(0, ParseNumber.parseAllNumbers("0", pos));
		assertEquals(1, pos.get());

		pos.set(1);
		assertNumber(5.7, ParseNumber.parseAllNumbers("a5.7a", pos));
		assertEquals(4, pos.get());

		pos.set(2);
		assertNumber(-11, ParseNumber.parseAllNumbers("bb-11bb", pos));
		assertEquals(5, pos.get());

		pos.set(3);
		assertNumber(0x123L, ParseNumber.parseAllNumbers("ccc0x123Lccc", pos));
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
