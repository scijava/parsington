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
import org.scijava.parsington.Function;
import org.scijava.parsington.Operator;
import org.scijava.parsington.Operators;
import org.scijava.parsington.SyntaxTree;
import org.scijava.parsington.Tokens;

/**
 * Base class for tree-based evaluators which support the standard operators.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractStandardTreeEvaluator extends
	AbstractTreeEvaluator implements StandardEvaluator
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
		final Object a = args.length > 0 ? args[0] : null;
		final Object b = args.length > 1 ? args[1] : null;

		// Let the case logic begin!
		if (op instanceof Function) return function(a, b);
		if (op == Operators.DOT) return dot(a, b);
		if (Tokens.isMatchingGroup(op, Operators.PARENS)) return parens(args);
		if (Tokens.isMatchingGroup(op, Operators.BRACKETS)) return brackets(args);
		if (Tokens.isMatchingGroup(op, Operators.BRACES)) return braces(args);
		if (op == Operators.TRANSPOSE) return transpose(a);
		if (op == Operators.DOT_TRANSPOSE) return dotTranspose(a);
		if (op == Operators.POW) return pow(a, b);
		if (op == Operators.DOT_POW) return dotPow(a, b);
		if (op == Operators.POST_INC) return postInc(a);
		if (op == Operators.POST_DEC) return postDec(a);
		if (op == Operators.PRE_INC) return preInc(a);
		if (op == Operators.PRE_DEC) return preDec(a);
		if (op == Operators.POS) return pos(a);
		if (op == Operators.NEG) return neg(a);
		if (op == Operators.COMPLEMENT) return complement(a);
		if (op == Operators.NOT) return not(a);
		if (op == Operators.MUL) return mul(a, b);
		if (op == Operators.DIV) return div(a, b);
		if (op == Operators.MOD) return mod(a, b);
		if (op == Operators.RIGHT_DIV) return rightDiv(a, b);
		if (op == Operators.DOT_MUL) return dotMul(a, b);
		if (op == Operators.DOT_DIV) return dotDiv(a, b);
		if (op == Operators.DOT_RIGHT_DIV) return dotRightDiv(a, b);
		if (op == Operators.ADD) return add(a, b);
		if (op == Operators.SUB) return sub(a, b);
		if (op == Operators.LEFT_SHIFT) return leftShift(a, b);
		if (op == Operators.RIGHT_SHIFT) return rightShift(a, b);
		if (op == Operators.UNSIGNED_RIGHT_SHIFT) return unsignedRightShift(a, b);
		if (op == Operators.LESS_THAN) return lessThan(a, b);
		if (op == Operators.GREATER_THAN) return greaterThan(a, b);
		if (op == Operators.LESS_THAN_OR_EQUAL) return lessThanOrEqual(a, b);
		if (op == Operators.GREATER_THAN_OR_EQUAL) return greaterThanOrEqual(a, b);
		if (op == Operators.INSTANCEOF) return instanceOf(a, b);
		if (op == Operators.EQUAL) return equal(a, b);
		if (op == Operators.NOT_EQUAL) return notEqual(a, b);
		if (op == Operators.BITWISE_AND) return bitwiseAnd(a, b);
		if (op == Operators.BITWISE_OR) return bitwiseOr(a, b);
		if (op == Operators.LOGICAL_AND) return logicalAnd(a, b);
		if (op == Operators.LOGICAL_OR) return logicalOr(a, b);
		if (op == Operators.QUESTION) return question(a, b);
		if (op == Operators.COLON) return colon(a, b);
		if (op == Operators.ASSIGN) return assign(a, b);
		if (op == Operators.POW_ASSIGN) return powAssign(a, b);
		if (op == Operators.DOT_POW_ASSIGN) return dotPowAssign(a, b);
		if (op == Operators.MUL_ASSIGN) return mulAssign(a, b);
		if (op == Operators.DIV_ASSIGN) return divAssign(a, b);
		if (op == Operators.MOD_ASSIGN) return modAssign(a, b);
		if (op == Operators.RIGHT_DIV_ASSIGN) return rightDivAssign(a, b);
		if (op == Operators.DOT_DIV_ASSIGN) return dotDivAssign(a, b);
		if (op == Operators.DOT_RIGHT_DIV_ASSIGN) return dotRightDivAssign(a, b);
		if (op == Operators.ADD_ASSIGN) return addAssign(a, b);
		if (op == Operators.SUB_ASSIGN) return subAssign(a, b);
		if (op == Operators.AND_ASSIGN) return andAssign(a, b);
		if (op == Operators.OR_ASSIGN) return orAssign(a, b);
		if (op == Operators.LEFT_SHIFT_ASSIGN) return leftShiftAssign(a, b);
		if (op == Operators.RIGHT_SHIFT_ASSIGN) return rightShiftAssign(a, b);
		if (op == Operators.UNSIGNED_RIGHT_SHIFT_ASSIGN) return unsignedRightShiftAssign(a, b);

		// Unknown operator.
		return null;
	}

}
