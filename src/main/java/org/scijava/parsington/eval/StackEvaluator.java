/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2022 Board of Regents of the University of
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import org.scijava.parsington.Group;
import org.scijava.parsington.Operator;
import org.scijava.parsington.SyntaxTree;
import org.scijava.parsington.Tokens;

/**
 * Interface for stack-based expression evaluators, operating on postfix queues.
 *
 * @author Curtis Rueden
 */
public interface StackEvaluator extends Evaluator {

	/**
	 * Executes an {@link Operator operation} with the specified value stack.
	 * 
	 * @param op The operator to execute.
	 * @param stack The value stack containing the arguments to pass.
	 * @return The result of the operation.
	 */
	Object execute(final Operator op, final Deque<Object> stack);

	// -- Evaluator methods --

	@Override
	default Object evaluate(final String expression) {
		// Convert the expression to postfix.
		return evaluate(getParser().parsePostfix(expression));
	}

	@Override
	default Object evaluate(final SyntaxTree syntaxTree) {
		// Convert the syntax tree to postfix.
		return evaluate(syntaxTree.postfix());
	}

	@Override
	default Object evaluate(final LinkedList<Object> queue) {
		// Process the postfix token queue.
		final Deque<Object> stack = new ArrayDeque<>();
		while (!queue.isEmpty()) {
			final Object token = queue.removeFirst();
			final Object result;
			if (Tokens.isOperator(token)) {
				result = execute((Operator) token, stack);
			}
			else {
				// Token is a variable or a literal.
				result = token;
			}
			if (result == null) {
				// Throw an informative exception.
				final StringBuilder message = new StringBuilder("Unsupported");
				if (token instanceof Operator) {
					final int arity = ((Operator) token).getArity();
					final String[] aryNames = { "nullary", "unary", "binary", "ternary",
						"quaternary", "quinary", "senary", "septenary", "octary",
						"nonary" };
					final String aryName = arity < aryNames.length ? aryNames[arity]
						: arity + "-ary";
					message.append(" " + aryName);
				}
				final String type;
				if (token instanceof Function) type = "function";
				else if (token instanceof Group) type = "group";
				else if (token instanceof Operator) type = "operator";
				else type = "token";
				message.append(" " + type + ": " + token);
				throw new IllegalArgumentException(message.toString());
			}
			stack.push(result);
		}

		if (stack.isEmpty()) return null;
		if (stack.size() == 1) return stack.pop();

		final LinkedList<Object> resultList = new LinkedList<>();
		while (!stack.isEmpty()) {
			resultList.addFirst(stack.pop());
		}
		return resultList;
	}

}
