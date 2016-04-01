/*
 * #%L
 * SciJava mathematical expression parser.
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

package org.scijava.sjep.eval;

import java.util.HashMap;
import java.util.Map;

import org.scijava.sjep.ExpressionParser;
import org.scijava.sjep.SyntaxTree;
import org.scijava.sjep.Tokens;
import org.scijava.sjep.Variable;

/**
 * Base class for {@link Evaluator} implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractEvaluator implements Evaluator {

	private final HashMap<String, Object> vars = new HashMap<String, Object>();
	private final ExpressionParser parser;

	public AbstractEvaluator() {
		this(new ExpressionParser());
	}

	public AbstractEvaluator(final ExpressionParser parser) {
		this.parser = parser;
	}

	// -- Evaluator methods --

	@Override
	public ExpressionParser getParser() {
		return parser;
	}

	@Override
	public Object evaluate(final SyntaxTree syntaxTree) {
		// Convert the syntax tree to postfix.
		return evaluate(syntaxTree.postfix());
	}

	@Override
	public Object evaluate(final String expression) {
		// Convert the expression to postfix.
		return evaluate(parser.parsePostfix(expression));
	}

	@Override
	public Object value(final Object token) {
		return Tokens.isVariable(token) ? get((Variable) token) : token;
	}

	@Override
	public Object get(final Variable v) {
		final String name = v.getToken();
		if (vars.containsKey(name)) return vars.get(name);
		throw new IllegalArgumentException("Unknown variable: " + name);
	}

	@Override
	public void set(final Variable v, final Object value) {
		vars.put(v.getToken(), value);
	}

	@Override
	public void setAll(final Map<? extends String, ? extends Object> map) {
		vars.putAll(map);
	}

}
