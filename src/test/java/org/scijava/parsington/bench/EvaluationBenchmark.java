/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2026 Board of Regents of the University of
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

package org.scijava.parsington.bench;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.scijava.parsington.SyntaxTree;
import org.scijava.parsington.eval.DefaultStackEvaluator;
import org.scijava.parsington.eval.DefaultTreeEvaluator;
import org.scijava.parsington.eval.Evaluator;

/**
 * Benchmarks the evaluation half of Parsington, across both evaluator
 * implementations ({@link DefaultTreeEvaluator} and
 * {@link DefaultStackEvaluator}).
 * <p>
 * Two modes are measured:
 * </p>
 * <ul>
 * <li>{@link Mode#PARSE_AND_EVAL} &mdash; {@code evaluate(String)}: the common
 * user entry point, which re-parses on every call. This mirrors the workload of
 * evaluating many <em>different</em> expressions read from a file.</li>
 * <li>{@link Mode#EVAL_ONLY} &mdash; evaluation of a pre-parsed input, isolating
 * evaluation cost from parsing. This mirrors evaluating the <em>same</em>
 * expression many times. Note that stack evaluation drains its postfix queue,
 * so this mode copies the queue on each invocation; that copy is included in
 * the measurement for the stack strategy.</li>
 * </ul>
 *
 * @author Curtis Rueden
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EvaluationBenchmark {

	/** Variables referenced by variable-driven expressions in the corpus. */
	static final Map<String, Object> VARS = new LinkedHashMap<>();
	static {
		VARS.put("a", 2.5);
		VARS.put("b", 3.5);
		VARS.put("c", 4.5);
		VARS.put("d", 5.5);
		VARS.put("e", 6.5);
	}

	public enum Strategy {
			TREE, STACK
	}

	public enum Mode {
			PARSE_AND_EVAL, EVAL_ONLY
	}

	@Param
	public Expressions.Eval expression;

	@Param
	public Strategy strategy;

	@Param
	public Mode mode;

	private Evaluator evaluator;
	private String expr;

	/** Pre-parsed syntax tree, reused across invocations (read-only eval). */
	private SyntaxTree treeInput;

	/** Pre-parsed postfix queue template, copied per invocation (drained). */
	private LinkedList<Object> queueTemplate;

	@Setup(Level.Trial)
	public void setUp() {
		evaluator = strategy == Strategy.TREE ? new DefaultTreeEvaluator()
			: new DefaultStackEvaluator();
		for (final Map.Entry<String, Object> var : VARS.entrySet()) {
			evaluator.set(var.getKey(), var.getValue());
		}
		expr = expression.expression();

		if (mode == Mode.EVAL_ONLY) {
			if (strategy == Strategy.TREE) {
				treeInput = evaluator.getParser().parseTree(expr);
			}
			else {
				queueTemplate = evaluator.getParser().parsePostfix(expr);
			}
		}
	}

	@Benchmark
	public Object evaluate() {
		if (mode == Mode.PARSE_AND_EVAL) return evaluator.evaluate(expr);
		// EVAL_ONLY
		if (strategy == Strategy.TREE) return evaluator.evaluate(treeInput);
		// Stack evaluation consumes its queue, so supply a fresh copy.
		return evaluator.evaluate(new LinkedList<>(queueTemplate));
	}
}
