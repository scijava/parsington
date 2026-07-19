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

package org.scijava.parsington.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.scijava.parsington.AbstractTest;

/**
 * Abstract base class for testing {@link Evaluator} implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractEvaluatorTest extends AbstractTest {

	public abstract Evaluator createEvaluator();

	/**
	 * Tests {@link Evaluator#get(String)} and
	 * {@link Evaluator#set(String, Object)}.
	 */
	@Test
	public void testGetSet() {
		final Evaluator e = createEvaluator();
		// In strict mode (the default), getting an unset variable throws.
		assertThrows(IllegalArgumentException.class, () -> e.get("x"));
		e.set("x", 42);
		assertEquals(42, e.get("x"));
		e.set("x", "hello");
		assertEquals("hello", e.get("x"));
	}

	/** Tests {@link Evaluator#remove(String)}. */
	@Test
	public void testRemove() {
		final Evaluator e = createEvaluator();
		// Removing an absent variable returns null.
		assertNull(e.remove("x"));
		e.set("x", 99);
		assertEquals(99, e.remove("x"));
		// Variable is gone after removal.
		assertThrows(IllegalArgumentException.class, () -> e.get("x"));
	}

	/** Tests {@link Evaluator#clear()}. */
	@Test
	public void testClear() {
		final Evaluator e = createEvaluator();
		e.set("a", 1);
		e.set("b", 2);
		assertEquals(2, e.vars().size());
		e.clear();
		assertEquals(0, e.vars().size());
		assertThrows(IllegalArgumentException.class, () -> e.get("a"));
		assertThrows(IllegalArgumentException.class, () -> e.get("b"));
	}

	/** Tests {@link Evaluator#vars()} and {@link Evaluator#setAll(Map)}. */
	@Test
	public void testVarsSetAll() {
		final Evaluator e = createEvaluator();
		assertEquals(new HashMap<>(), e.vars());

		final Map<String, Object> vars = new HashMap<>();
		vars.put("a", 1);
		vars.put("b", "two");
		vars.put("c", 3.0);
		e.setAll(vars);

		assertEquals(vars, e.vars());

		// Verify individual get still works after setAll.
		assertEquals(1, e.get("a"));
		assertEquals("two", e.get("b"));
		assertEquals(3.0, e.get("c"));

		// Verify variables created at evaluation are accessible and correct.
		e.evaluate("d=a+c");
		assertTrue(e.vars().containsKey("d"));
		final Object dVal = e.get("d");
		assertEquals(4.0, dVal);
		assertEquals(dVal, e.vars().get("d"));
	}
}
