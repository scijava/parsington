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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.scijava.parsington.Variable;

/**
 * Performance benchmark for the {@code lessThan} dispatch path in
 * {@link AbstractStandardEvaluator}.
 *
 * <p>
 * Skipped during normal test runs; enable by setting the system property:
 * <pre>mvn test -Dparsington.benchmark=true</pre>
 *
 * <p>
 * The benchmark isolates the dispatch cost of {@code lessThan(Object, Object)}
 * when called with {@link Variable} arguments, so every {@code is*()} check
 * pays a {@code value()} call to resolve the variable. This is the code path
 * affected by the proposed optimization of resolving variables once at the top
 * of each operation rather than once per type check.
 */
public class LessThanBenchmarkTest {

	/** Number of calls used to warm up the JIT before measuring. */
	private static final int WARMUP = 200_000;

	/** Number of calls timed in the measurement phase. */
	private static final int MEASURE = 2_000_000;

	@Test
	public void benchmarkStackEvaluator() {
		Assumptions.assumeTrue(Boolean.getBoolean("parsington.benchmark"),
			"Skipping benchmark (set -Dparsington.benchmark=true to run)");
		runBenchmark(new DefaultStackEvaluator(), "DefaultStackEvaluator");
	}

	@Test
	public void benchmarkTreeEvaluator() {
		Assumptions.assumeTrue(Boolean.getBoolean("parsington.benchmark"),
			"Skipping benchmark (set -Dparsington.benchmark=true to run)");
		runBenchmark(new DefaultTreeEvaluator(), "DefaultTreeEvaluator");
	}

	// -- Helper methods --

	private void runBenchmark(final StandardEvaluator e, final String label) {
		e.setStrict(false);
		final Variable va = new Variable("a");
		final Variable vb = new Variable("b");

		final String[] typeLabels = {
			"Boolean", "String", "Integer", "Long", "Float",
			"Double", "BigInteger", "BigDecimal"
		};
		final Object[][] pairs = {
			{ Boolean.FALSE, Boolean.TRUE },
			{ "apple", "banana" },
			{ 42, 43 },
			{ 42L, 43L },
			{ 1.5f, 2.5f },
			{ 1.5, 2.5 },
			{ new BigInteger("123"), new BigInteger("456") },
			{ new BigDecimal("1.23"), new BigDecimal("4.56") },
		};

		System.out.println();
		System.out.println("=== " + label + " - lessThan(Variable, Variable) ===");
		System.out.printf("  Warmup: %,d calls | Measure: %,d calls%n",
			WARMUP, MEASURE);
		System.out.printf("  %-12s  %10s  %15s%n", "Type", "ns/op", "ops/sec");
		System.out.println("  ------------------------------------------");

		for (int i = 0; i < typeLabels.length; i++) {
			e.set("a", pairs[i][0]);
			e.set("b", pairs[i][1]);

			for (int w = 0; w < WARMUP; w++) {
				e.lessThan(va, vb);
			}

			final long start = System.nanoTime();
			for (int m = 0; m < MEASURE; m++) {
				e.lessThan(va, vb);
			}
			final long elapsed = System.nanoTime() - start;

			final double nsPerOp = (double) elapsed / MEASURE;
			System.out.printf("  %-12s  %10.1f  %,15.0f%n",
				typeLabels[i], nsPerOp, 1e9 / nsPerOp);
		}
	}
}
