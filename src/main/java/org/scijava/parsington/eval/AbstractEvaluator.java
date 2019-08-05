/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2021 Board of Regents of the University of
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

import java.util.HashMap;
import java.util.Map;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Tokens;
import org.scijava.parsington.Variable;

/**
 * Base class for {@link Evaluator} implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractEvaluator implements Evaluator {

	private final HashMap<String, Object> vars = new HashMap<String, Object>();
	private final ExpressionParser parser;

	private boolean strict = true;

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
	public boolean isStrict() {
		return strict;
	}

	@Override
	public void setStrict(final boolean strict) {
		this.strict = strict;
	}

	@Override
	public Object value(final Object token) {
		return Tokens.isVariable(token) ? get((Variable) token) : token;
	}

	@Override
	public Object get(final Variable v) {
		final String name = v.getToken();
		if (vars.containsKey(name)) return vars.get(name);
		if (strict) throw new IllegalArgumentException("Unknown variable: " + name);
		return new Unresolved(name);
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
