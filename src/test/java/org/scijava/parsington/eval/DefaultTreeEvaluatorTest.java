/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2022 Board of Regents of the University of
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

package org.scijava.parsington.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests {@link DefaultTreeEvaluator}. */
public class DefaultTreeEvaluatorTest extends AbstractStandardEvaluatorTest {

	@Override
	public StandardEvaluator createEvaluator() {
		return new DefaultTreeEvaluator();
	}

	@Test
	public void testShortCircuitingAnd() {
		final Object result = e.evaluate("(x = 1, (++x > 2) && (++x > 3))");
		// Both sides of the above AND expression evaluate to false.
		// But for short circuiting AND, only the left side should
		// execute, leaving the value of x afterward at 2, not 3.
		final Object v = ((List<?>) result).get(0); // First element of tuple.
		assertEquals(2, e.value(v));
	}

	@Test
	public void testShortCircuitingOr() {
		final Object result = e.evaluate("(x = 1, (++x == 2) || (++x == 3))");
		// Both sides of the above OR expression evaluate to true.
		// But for short circuiting OR, only the left side should
		// execute, leaving the value of x afterward at 2, not 3.
		final Object v = ((List<?>) result).get(0); // First element of tuple.
		assertEquals(2, e.value(v));
	}

	@Test
	public void testShortCircuitingTernary() {
		final Object result = e.evaluate("2 < 3 ? (x = 'yes') : (x += 'no')");
		assertEquals("yes", e.value(result));
	}

}
