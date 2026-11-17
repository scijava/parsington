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

package org.scijava.parsington.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.scijava.parsington.eval.DefaultTreeEvaluator;
import org.scijava.parsington.eval.Evaluator;

/**
 * Tests that {@link CompiledExpression}s compute the same results as the
 * {@link DefaultTreeEvaluator}, for expressions whose semantics agree.
 * <p>
 * Excluded are expressions whose results legitimately differ: those which
 * overflow 32-bit integers or shift by 32 or more bits, those which use the
 * result of an assignment, and equality comparisons between different
 * numeric types.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CompiledExpressionParityTest {

	private static final String[] EXPRESSIONS = { //
		// Arithmetic.
		"1+2*3-4+5*6-7", "1.5+2.5*3.5-4.5+5.5*6.5", "6.5*7.8^2.3", "2^10",
		"7/2", "-7/2", "7%3", "-7%3", "7.5%2", "7.0/2", "-(1+3)", "+4", "1+2.5",
		"10-0.25", "3*0.5", "1.0/3",
		// Bitwise.
		"1<<10", "-16>>2", "16>>>2", "5&3", "5|3", "~5",
		// Comparison and logic.
		"3<5 && 2>=2 || 1==0", "3<4.5", "3>4.5", "3<=3", "3>=3.0", "2!=3",
		"false<true", "true==true", "!(1<2)", "!false && !!true",
		"1<2 ? 4 : 5", "1>2 ? 4 : 5", "1<2 ? 1.5 : 2.5",
		// Variables.
		"a*b + c*d - a/b + c", "(a + 2.5) * (b - 1.5) / 2", "i*j - k", "i/j",
		"i%j + k", "i<<j", "a < b && i > j", "flag ? a : b", "!flag || i == 3",
		"a*i + b*j", "-a + -i", "a^i", };

	@Test
	public void testParity() {
		final Map<String, Object> vars = new LinkedHashMap<>();
		vars.put("a", 2.5);
		vars.put("b", 3.5);
		vars.put("c", 4.5);
		vars.put("d", 5.5);
		vars.put("i", 7);
		vars.put("j", 2);
		vars.put("k", -3L);
		vars.put("flag", true);

		final Evaluator evaluator = new DefaultTreeEvaluator();
		final Map<String, Type> types = new LinkedHashMap<>();
		for (final Map.Entry<String, Object> var : vars.entrySet()) {
			evaluator.set(var.getKey(), var.getValue());
			types.put(var.getKey(), Type.of(var.getValue()));
		}
		final ExpressionCompiler compiler = new ExpressionCompiler();

		for (final String expression : EXPRESSIONS) {
			final Object expected = evaluator.value(evaluator.evaluate(expression));
			final CompiledExpression compiled = compiler.compile(expression, types);
			for (final Map.Entry<String, Object> var : vars.entrySet()) {
				compiled.set(var.getKey(), var.getValue());
			}
			assertSameValue(expected, compiled.evaluate(), expression);
		}
	}

	/**
	 * Asserts that two results represent the same value, regardless of their
	 * exact types: {@link Integer} versus {@link Long}, or {@link Float} versus
	 * {@link Double}.
	 */
	private void assertSameValue(final Object expected, final Object actual,
		final String expression)
	{
		if (expected instanceof Boolean) {
			assertEquals(expected, actual, expression);
			return;
		}
		assertTrue(expected instanceof Number, expression);
		assertTrue(actual instanceof Number, expression);
		final Type expectedType = Type.of(expected), actualType = Type.of(actual);
		assertEquals(expectedType, actualType, expression);
		if (expectedType == Type.LONG) {
			assertEquals(((Number) expected).longValue(), ((Number) actual)
				.longValue(), expression);
		}
		else {
			assertEquals(((Number) expected).doubleValue(), ((Number) actual)
				.doubleValue(), expression);
		}
	}
}
