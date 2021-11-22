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

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Operator;
import org.scijava.parsington.Operators;
import org.scijava.parsington.SyntaxTree;

/**
 * Base class for tree-based evaluators which support the standard operators.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractStandardTreeEvaluator extends
	AbstractEvaluator implements StandardEvaluator, TreeEvaluator
{

	public AbstractStandardTreeEvaluator() {
		super();
	}

	public AbstractStandardTreeEvaluator(final ExpressionParser parser) {
		super(parser);
	}

	// -- StandardEvaluator methods --

	// -- ternary --

	@Override
	public Object question(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object colon(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	// -- TreeEvaluator methods --

	@Override
	public Object execute(final Operator op, final SyntaxTree tree) {
		// Handle short-circuiting operators first.
		if (op == Operators.QUESTION) {
			final SyntaxTree conditional = tree.child(0);
			final SyntaxTree consequent = tree.child(1);
			if (consequent.token() != Operators.COLON || consequent.count() != 2) {
				throw new IllegalArgumentException("Invalid ternary operator syntax");
			}
			final Object conditionalValue = value(evaluate(conditional));
			if (!(conditionalValue instanceof Boolean)) {
				throw new IllegalArgumentException("Invalid ternary operator conditional expression");
			}
			return ((Boolean) conditionalValue) ? evaluate(consequent.child(0))
				: evaluate(consequent.child(1));
		}

		// Recursively evaluate the subtrees. None of the remaining operators
		// benefit from short-circuiting, so we can evaluate subexpressions eagerly.
		final Object[] args = new Object[tree.count()];
		for (int i = 0; i < args.length; i++) {
			args[i] = evaluate(tree.child(i));
		}

		return execute(op, args);
	}

}
