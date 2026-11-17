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

import java.util.Collections;
import java.util.Map;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Operator;
import org.scijava.parsington.SyntaxTree;

/**
 * Compiles expressions into {@link CompiledExpression}s, which can be
 * evaluated repeatedly with much less overhead than the evaluators of the
 * {@code eval} package.
 * <p>
 * Compiled expressions are statically typed: every variable has a fixed
 * {@link Type}, declared up front or inferred from its first assignment.
 * Supported are boolean, integer and floating point literals and variables,
 * with all integers represented as {@code long} and all floating point values
 * as {@code double}. The semantics otherwise follow Java, which differs from
 * the {@code eval} package's evaluators in a few respects:
 * </p>
 * <ul>
 * <li>Integer arithmetic overflows at 64 bits, not 32.</li>
 * <li>Equality compares numeric values, so {@code 1 == 1.0} is true.</li>
 * <li>Assignment yields the assigned value, rather than the variable.</li>
 * <li>Variables cannot change type.</li>
 * </ul>
 * <p>
 * Expressions using any other operators, types or functions are rejected at
 * compile time with an {@link IllegalArgumentException}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ExpressionCompiler {

	private final ExpressionParser parser;
	private final Map<Operator, Rule> rules;

	/** Creates an expression compiler using the default expression parser. */
	public ExpressionCompiler() {
		this(new ExpressionParser());
	}

	/**
	 * Creates an expression compiler using the given expression parser.
	 *
	 * @param parser The parser to use for compiling expression strings. Only
	 *          the {@link org.scijava.parsington.Operators standard operators}
	 *          are supported, regardless of which operators the parser knows.
	 */
	public ExpressionCompiler(final ExpressionParser parser) {
		this.parser = parser;
		this.rules = StandardRules.create();
	}

	/** Gets the parser used to parse expression strings. */
	public ExpressionParser getParser() {
		return parser;
	}

	/**
	 * Compiles the given expression, which must not reference any variables
	 * before assigning them.
	 *
	 * @param expression The expression to compile.
	 * @return The compiled expression.
	 * @throws IllegalArgumentException If the expression cannot be compiled.
	 */
	public CompiledExpression compile(final String expression) {
		return compile(expression, Collections.emptyMap());
	}

	/**
	 * Compiles the given expression.
	 *
	 * @param expression The expression to compile.
	 * @param variables The names and types of variables referenced by the
	 *          expression. Their values are set afterward, via
	 *          {@link CompiledExpression#set(String, Object)}.
	 * @return The compiled expression.
	 * @throws IllegalArgumentException If the expression cannot be compiled.
	 */
	public CompiledExpression compile(final String expression,
		final Map<String, Type> variables)
	{
		return compile(parser.parseTree(expression), variables);
	}

	/**
	 * Compiles the given syntax tree.
	 *
	 * @param tree The syntax tree to compile.
	 * @param variables The names and types of variables referenced by the
	 *          expression. Their values are set afterward, via
	 *          {@link CompiledExpression#set(String, Object)}.
	 * @return The compiled expression.
	 * @throws IllegalArgumentException If the expression cannot be compiled.
	 */
	public CompiledExpression compile(final SyntaxTree tree,
		final Map<String, Type> variables)
	{
		return new Compilation(rules, variables).compile(tree);
	}
}
