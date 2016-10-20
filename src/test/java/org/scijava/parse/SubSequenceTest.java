/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
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

package org.scijava.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test {@link SubSequence}.
 *
 * @author Curtis Rueden
 */
public class SubSequenceTest {

	private static String PHRASE =
		"The quick brown fox jumped over the lazy dogs.";

	@Test
	public void testLength() {
		for (int off = 0; off < PHRASE.length(); off++) {
			for (int len = 0; len < PHRASE.length() - off; len++) {
				assertEquals(len, sub(off, len).length());
			}
		}
	}

	@Test
	public void testCharAt() {
		for (int off = 0; off < PHRASE.length(); off++) {
			for (int len = 0; len < PHRASE.length() - off; len++) {
				final SubSequence sub = sub(off, len);
				for (int c = 0; c < sub.length(); c++) {
					assertEquals(PHRASE.charAt(off + c), sub.charAt(c));
				}
			}
		}
	}

	@Test
	public void testSubSequence() {
		final SubSequence sub = sub(4, 15); // quick brown fox
		final SubSequence subSub = sub.subSequence(6, 11); // brown
		assertEquals(5, subSub.length());
		assertEquals('b', subSub.charAt(0));
		assertEquals('r', subSub.charAt(1));
		assertEquals('o', subSub.charAt(2));
		assertEquals('w', subSub.charAt(3));
		assertEquals('n', subSub.charAt(4));
	}

	@Test
	public void testToString() {
		assertEquals("quick brown fox", sub(4, 15).toString());
	}

	@Test
	public void testNegativeOffset() {
		try {
			sub(-1, 1);
			fail("Expected IndexOutOfBoundsException");
		}
		catch (final IndexOutOfBoundsException exc) {
			assertEquals("Offset -1 < 0", exc.getMessage());
		}
	}

	@Test
	public void testNegativeLength() {
		try {
			sub(1, -1);
			fail("Expected IndexOutOfBoundsException");
		}
		catch (final IndexOutOfBoundsException exc) {
			assertEquals("Length -1 < 0", exc.getMessage());
		}
	}

	@Test
	public void testOffsetTooLarge() {
		final int len = PHRASE.length();
		final int off = len + 1;
		try {
			sub(off, 1);
			fail("Expected IndexOutOfBoundsException");
		}
		catch (final IndexOutOfBoundsException exc) {
			assertEquals("Offset " + off + " > " + len, exc.getMessage());
		}
	}

	@Test
	public void testLengthTooLong() {
		final int len = PHRASE.length();
		try {
			sub(1, len);
			fail("Expected IndexOutOfBoundsException");
		}
		catch (final IndexOutOfBoundsException exc) {
			assertEquals("Offset 1 + length " + len + " > " + len, exc.getMessage());
		}
	}

	// -- Helper methods --

	private SubSequence sub(final int offset, final int length) {
		return new SubSequence(PHRASE, offset, length);
	}

}
