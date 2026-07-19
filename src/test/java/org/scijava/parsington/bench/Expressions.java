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

package org.scijava.parsington.bench;

/**
 * Shared expression corpora for the Parsington JMH benchmarks.
 * <p>
 * Each enum names a representative expression, chosen to exercise a distinct
 * parser or evaluator code path rather than merely to vary in size. The
 * benchmarks drive these via {@code @Param} so that results are broken down per
 * expression.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Expressions {

	private Expressions() {
		// prevent instantiation of utility class
	}

	/** Builds a long flat sum {@code a0+a1+...+a(count-1)}. */
	static String flatSum(final int count) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (i > 0) sb.append("+");
			sb.append("a").append(i);
		}
		return sb.toString();
	}

	/**
	 * Expressions for the <em>parsing</em> benchmarks. These need not be
	 * evaluable, so they may include function calls, unary operators and
	 * unresolved variables.
	 */
	public enum Parse {
			/** Single literal: the fixed-overhead floor. */
			CONSTANT("1"),
			/** Single variable: bare-token floor. */
			VARIABLE("a"),
			/** Flat operator chain, no functions or groups. */
			ARITHMETIC("1+2+3+4+5+6+7+8"),
			/** The README's quadratic formula: unary ops, functions, groups. */
			QUADRATIC("(-b + sqrt(b^2 - 4*a*c)) / (2*a)"),
			/** Deeply nested parentheses. */
			NESTED_GROUPS("((((((1+2))))))"),
			/** Nested function calls with argument groups. */
			FUNCTIONS("f(g(h(1, 2), 3), 4)"),
			/** Long flat sum; catches accidental super-linear scaling. */
			LONG_SUM(flatSum(50)),
			/**
			 * Dominated by the most common operators ({@code = + - *}), which sit
			 * near the end of the standard operator list. Stresses the operator
			 * matcher on its hot path.
			 */
			OPERATORS_COMMON("a = b*2 + c*3 - d*4 + e*5 + f*6 - g*7"),
			/**
			 * Uses rarer, multi-character operators (shifts, bitwise, logical).
			 * Contrasts with {@link #OPERATORS_COMMON} for operator-matching cost.
			 */
			OPERATORS_RARE("a << b >> c >>> d & e | f && g || h"),
			/** All-integer literal list: the literal fast path. */
			INT_LIST("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"),
			/**
			 * All-decimal literal list: the issue #24 hot path, where each decimal
			 * previously paid for a thrown-and-swallowed exception.
			 */
			DOUBLE_LIST("[1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5]"),
			/** Heterogeneous literal list spanning many literal types. */
			MIXED_LIST("[1, 2f, 3d, 4., 5L, 6.02e23, 0x1F, 0b1010, " +
				"99987654321234567899, 'text']");

		private final String expression;

		Parse(final String expression) {
			this.expression = expression;
		}

		public String expression() {
			return expression;
		}
	}

	/**
	 * Expressions for the <em>evaluation</em> benchmarks. These use only
	 * operators and literals of types the default evaluator understands
	 * (no function calls), and any variables are registered by the benchmark
	 * before measurement. See {@link EvaluationBenchmark#VARS}.
	 */
	public enum Eval {
			/** Integer arithmetic with mixed precedence. */
			ARITH_INT("1+2*3-4+5*6-7"),
			/** Double arithmetic; also parses several decimal literals. */
			ARITH_DOUBLE("1.5+2.5*3.5-4.5+5.5*6.5"),
			/** Power operator on doubles (README example). */
			POWER("6.5*7.8^2.3"),
			/** Variable-driven arithmetic; variables preset by the benchmark. */
			VARIABLES("a*b + c*d - a/b + c"),
			/** Boolean comparison and logic. */
			BOOLEAN("3<5 && 2>=2 || 1==0"),
			/** Mix of variables, decimals, groups and division. */
			MIXED("(a + 2.5) * (b - 1.5) / 2");

		private final String expression;

		Eval(final String expression) {
			this.expression = expression;
		}

		public String expression() {
			return expression;
		}
	}
}
