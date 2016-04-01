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

import java.util.LinkedList;
import java.util.Map;

import org.scijava.sjep.ExpressionParser;
import org.scijava.sjep.SyntaxTree;
import org.scijava.sjep.Variable;

/**
 * Interface for expression evaluators.
 *
 * @author Curtis Rueden
 */
public interface Evaluator {

	/** Gets the parser used when evaluating expressions. */
	ExpressionParser getParser();

	/** Evaluates the given infix expression, returning the result. */
	Object evaluate(final String expression);

	/** Evaluates the given postfix token queue, returning the result. */
	Object evaluate(final LinkedList<Object> queue);

	/** Evaluates the given syntax tree, returning the result. */
	Object evaluate(final SyntaxTree syntaxTree);

	/**
	 * Gets the value of the given token. For variables, returns the value of the
	 * variable, throwing an exception if the variable is not set. For literals,
	 * returns the token itself.
	 */
	Object value(Object token);

	/** Gets the value of the given variable. */
	Object get(Variable v);

	/** Sets the value of the given variable. */
	void set(Variable v, Object value);

	/** Assigns variables en masse. */
	void setAll(Map<? extends String, ? extends Object> map);

}
