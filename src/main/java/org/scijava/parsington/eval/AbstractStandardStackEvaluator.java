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

import java.util.Deque;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Operator;

/**
 * Base class for stack-based evaluators which support the standard operators.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractStandardStackEvaluator extends
	AbstractEvaluator implements StandardEvaluator, StackEvaluator
{

	public AbstractStandardStackEvaluator() {
		super();
	}

	public AbstractStandardStackEvaluator(final ExpressionParser parser) {
		super(parser);
	}

	// -- StandardEvaluator methods --

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

	// -- StackEvaluator methods --

	@Override
	public Object execute(final Operator op, final Deque<Object> stack) {
		// Pop the arguments.
		final int arity = op.getArity();
		final Object[] args = new Object[arity];
		for (int i = args.length - 1; i >= 0; i--) {
			args[i] = stack.pop();
		}

		return execute(op, args);
	}

}
