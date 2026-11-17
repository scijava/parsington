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

import java.util.LinkedHashMap;
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
import org.scijava.parsington.compiler.CompiledExpression;
import org.scijava.parsington.compiler.ExpressionCompiler;
import org.scijava.parsington.compiler.Type;

/**
 * Benchmarks the {@link ExpressionCompiler}, on the same expressions and
 * variables as {@link EvaluationBenchmark}, for direct comparison.
 * <p>
 * Two modes are measured:
 * </p>
 * <ul>
 * <li>{@link Mode#COMPILE_AND_EVAL} &mdash; parsing, compiling, binding
 * variables and evaluating on every call, comparable to
 * {@link EvaluationBenchmark.Mode#PARSE_AND_EVAL}.</li>
 * <li>{@link Mode#EVAL_ONLY} &mdash; evaluation of a precompiled expression,
 * comparable to {@link EvaluationBenchmark.Mode#EVAL_ONLY}.</li>
 * </ul>
 *
 * @author Curtis Rueden
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class CompiledEvaluationBenchmark {

	public enum Mode {
			COMPILE_AND_EVAL, EVAL_ONLY
	}

	@Param
	public Expressions.Eval expression;

	@Param
	public Mode mode;

	private ExpressionCompiler compiler;
	private Map<String, Type> types;
	private String expr;

	/** Precompiled expression, reused across invocations. */
	private CompiledExpression compiled;

	@Setup(Level.Trial)
	public void setUp() {
		compiler = new ExpressionCompiler();
		types = new LinkedHashMap<>();
		for (final Map.Entry<String, Object> var : EvaluationBenchmark.VARS
			.entrySet())
		{
			types.put(var.getKey(), Type.of(var.getValue()));
		}
		expr = expression.expression();
		compiled = compile();
	}

	@Benchmark
	public Object evaluate() {
		if (mode == Mode.COMPILE_AND_EVAL) return compile().evaluate();
		return compiled.evaluate();
	}

	private CompiledExpression compile() {
		final CompiledExpression result = compiler.compile(expr, types);
		for (final Map.Entry<String, Object> var : EvaluationBenchmark.VARS
			.entrySet())
		{
			result.set(var.getKey(), var.getValue());
		}
		return result;
	}
}
