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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ExpressionCompiler} and {@link CompiledExpression}.
 *
 * @author Curtis Rueden
 */
public class ExpressionCompilerTest {

	private final ExpressionCompiler compiler = new ExpressionCompiler();

	@Test
	public void testLiterals() {
		assertResult(true, "true");
		assertResult(42L, "42");
		assertResult(42L, "42L");
		assertResult(1.5, "1.5");
		assertResult(1.5, "1.5f");
	}

	@Test
	public void testArithmetic() {
		assertResult(7L, "1 + 2 * 3");
		assertResult(9L, "(1 + 2) * 3");
		assertResult(3L, "7 / 2");
		assertResult(-3L, "-7 / 2");
		assertResult(1L, "7 % 3");
		assertResult(3.5, "7.0 / 2");
		assertResult(1.5, "7.5 % 2");
		assertResult(-4L, "-(1 + 3)");
		assertResult(4L, "+4");
		assertThrows(ArithmeticException.class, () -> compiler.compile("1 / 0")
			.evaluate());
		assertResult(Double.POSITIVE_INFINITY, "1.0 / 0");
	}

	@Test
	public void testPromotion() {
		assertResult(3.5, "1 + 2.5");
		assertResult(3.5, "2.5 + 1");
		assertResult(8.0, "2 ^ 3");
		assertResult(1.0, "true ? 1 : 2.5");
		assertResult(2.5, "false ? 1 : 2.5");
		assertResult(true, "1 == 1.0");
	}

	@Test
	public void testLongSemantics() {
		// NB: Integers are longs, so these differ from the eval package's results.
		assertResult(2147483648L, "2147483647 + 1");
		assertResult(4294967296L, "1 << 32");
		assertResult(-1L >>> 28, "-1 >>> 28");
		assertResult(Long.MIN_VALUE, "9223372036854775807 + 1");
	}

	@Test
	public void testBitwise() {
		assertResult(1024L, "1 << 10");
		assertResult(-4L, "-16 >> 2");
		assertResult(1L, "5 & 3");
		assertResult(7L, "5 | 3");
		assertResult(-6L, "~5");
		assertResult(false, "true & false");
		assertResult(true, "true | false");
	}

	@Test
	public void testComparison() {
		assertResult(true, "3 < 4.5");
		assertResult(false, "3 > 4.5");
		assertResult(true, "3 <= 3");
		assertResult(true, "3 >= 3.0");
		assertResult(true, "2 != 3");
		assertResult(true, "false < true");
		assertResult(true, "true == true");
		assertResult(true, "1 < 2 == 3 < 4");
	}

	@Test
	public void testLogic() {
		assertResult(true, "3<5 && 2>=2 || 1==0");
		assertResult(false, "!(1 < 2)");
		assertResult(true, "!false && !!true");
		assertResult(4L, "1 < 2 ? 4 : 5");
		assertResult(5L, "1 > 2 ? 4 : 5");
		assertResult(6L, "false ? 4 : (true ? 6 : 7)");
	}

	@Test
	public void testShortCircuit() {
		final CompiledExpression and = compile("false && (++n > 0)", "n", Type.LONG);
		assertEquals(false, and.evaluate());
		assertEquals(0L, and.get("n"));

		final CompiledExpression or = compile("true || (++n > 0)", "n", Type.LONG);
		assertEquals(true, or.evaluate());
		assertEquals(0L, or.get("n"));

		final CompiledExpression ternary = compile("n > 0 ? n++ : n--", "n",
			Type.LONG);
		ternary.set("n", 5);
		assertEquals(5L, ternary.evaluate());
		assertEquals(6L, ternary.get("n"));
		ternary.set("n", -5);
		assertEquals(-5L, ternary.evaluate());
		assertEquals(-6L, ternary.get("n"));
	}

	@Test
	public void testVariables() {
		final Map<String, Type> vars = new LinkedHashMap<>();
		vars.put("a", Type.DOUBLE);
		vars.put("b", Type.LONG);
		vars.put("c", Type.BOOLEAN);
		final CompiledExpression expr = compiler.compile("c ? a * b : b", vars);
		assertSame(Type.DOUBLE, expr.type());
		assertEquals(0, expr.slot("a"));
		assertEquals(1, expr.slot("b"));
		assertEquals(-1, expr.slot("d"));

		expr.set("a", 1.5);
		expr.set("b", 4);
		expr.set("c", true);
		assertEquals(6.0, expr.evaluate());
		expr.setBoolean(expr.slot("c"), false);
		assertEquals(4.0, expr.evaluateDouble());
		expr.setLong(expr.slot("a"), 3); // NB: Promoted to double.
		expr.setBoolean(expr.slot("c"), true);
		assertEquals(12.0, expr.evaluate());

		assertThrows(IllegalArgumentException.class, () -> expr.set("b", 1.5));
		assertThrows(IllegalArgumentException.class, () -> expr.set("c", 1));
		assertThrows(IllegalArgumentException.class, () -> expr.set("d", 1));
		assertThrows(IllegalArgumentException.class, () -> expr.setDouble(1, 1));
	}

	@Test
	public void testAssignment() {
		// NB: Assignment declares a variable, and yields the assigned value.
		final CompiledExpression expr = compiler.compile("(y = 2) * y");
		assertEquals(4L, expr.evaluate());
		assertEquals(2L, expr.get("y"));

		final CompiledExpression late = compiler.compile(
			"(x = 7) + (x += 3) + (x -= 2) + (x <<= 1) + (x >>= 2) + (x | 3) + (x & 5)");
		assertEquals(56L, late.evaluate());
		assertEquals(4L, late.get("x"));

		final CompiledExpression d = compile("d += 1", "d", Type.DOUBLE);
		d.set("d", 0.5);
		assertEquals(1.5, d.evaluate());
		assertEquals(2.5, d.evaluate());

		final CompiledExpression pow = compile("d ^= 2", "d", Type.DOUBLE);
		pow.set("d", 3);
		assertEquals(9.0, pow.evaluate());

		final CompiledExpression steps = compile("n++ + ++n + n-- + --n", "n",
			Type.LONG);
		steps.set("n", 10);
		// 10 + 12 + 12 + 10
		assertEquals(44L, steps.evaluate());
		assertEquals(10L, steps.get("n"));
	}

	@Test
	public void testTypedEvaluation() {
		assertEquals(3L, compiler.compile("1 + 2").evaluateLong());
		assertEquals(3.0, compiler.compile("1 + 2").evaluateDouble());
		assertEquals(true, compiler.compile("1 < 2").evaluateBoolean());
		assertThrows(IllegalStateException.class, () -> compiler.compile("1.5")
			.evaluateLong());
		assertThrows(IllegalStateException.class, () -> compiler.compile("true")
			.evaluateDouble());
	}

	@Test
	public void testUnsupported() {
		assertInvalid("'hello'"); // string literal
		assertInvalid("123456789012345678901234567890"); // BigInteger literal
		assertInvalid("sqrt(4)"); // function
		assertInvalid("[1, 2]"); // list
		assertInvalid("(1, 2)"); // tuple
		assertInvalid("q + 1"); // undeclared variable
		assertInvalid("1 instanceof 2"); // unsupported operator
		assertInvalid("true + 1");
		assertInvalid("1.5 << 2");
		assertInvalid("1 & true");
		assertInvalid("1.5 & 2.5");
		assertInvalid("!5");
		assertInvalid("~1.5");
		assertInvalid("-true");
		assertInvalid("1 && true");
		assertInvalid("1 ? 2 : 3");
		assertInvalid("true ? 1 : false");
		assertInvalid("1 == true");
		assertInvalid("3 = 4"); // assignment to non-variable
		assertInvalid("(n = 1) + (n = 1.5)"); // assignment changing type
		assertInvalid("(n = 1) + (n += 1.5)"); // compound assignment changing type
		assertInvalid("(n = 1) + (n ^= 2)"); // pow always yields a double
		assertInvalid("(b = true) + (b++)");
	}

	// -- Helper methods --

	private CompiledExpression compile(final String expression,
		final String var, final Type type)
	{
		final Map<String, Type> vars = new LinkedHashMap<>();
		vars.put(var, type);
		return compiler.compile(expression, vars);
	}

	private void assertResult(final Object expected, final String expression) {
		final CompiledExpression expr = compiler.compile(expression);
		final Object actual = expr.evaluate();
		assertEquals(expected, actual, expression);
		assertSame(Type.of(expected), expr.type(), expression);
	}

	private void assertInvalid(final String expression) {
		assertThrows(IllegalArgumentException.class, () -> compiler.compile(
			expression), expression);
	}
}
