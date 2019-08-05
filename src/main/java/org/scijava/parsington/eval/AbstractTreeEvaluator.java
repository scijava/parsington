/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2019 Board of Regents of the University of
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

import java.util.LinkedList;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Operator;
import org.scijava.parsington.SyntaxTree;
import org.scijava.parsington.Tokens;

/**
 * Base class for {@link Evaluator} implementations that evaluate
 * {@link SyntaxTree}s recursively.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractTreeEvaluator extends AbstractEvaluator implements
	TreeEvaluator
{

	public AbstractTreeEvaluator() {
		super();
	}

	public AbstractTreeEvaluator(final ExpressionParser parser) {
		super(parser);
	}

	// -- Evaluator methods --

	@Override
	public Object evaluate(final String expression) {
		// Convert the expression to a syntax tree.
		return evaluate(getParser().parseTree(expression));
	}

	@Override
	public Object evaluate(final LinkedList<Object> queue) {
		// Convert the postfix queue to a syntax tree.
		return evaluate(new SyntaxTree(queue));
	}

	@Override
	public Object evaluate(final SyntaxTree syntaxTree) {
		// Evaluate the syntax tree recursively.
		final Object token = syntaxTree.token();
		if (Tokens.isOperator(token)) {
			final Operator op = (Operator) token;
			assert op.getArity() == syntaxTree.count();
			return execute(op, syntaxTree);
		}
		// Token is a variable or a literal.
		assert syntaxTree.count() == 0;
		return token;
	}

}
