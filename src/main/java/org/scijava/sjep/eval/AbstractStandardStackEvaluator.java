/*
 * #%L
 * SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 Board of Regents of the University of
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

import java.util.Deque;

import org.scijava.sjep.ExpressionParser;
import org.scijava.sjep.Operators;
import org.scijava.sjep.Tokens;
import org.scijava.sjep.Variable;
import org.scijava.sjep.Verb;

/**
 * Base class for stack-based evaluators which support the standard operators.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractStandardStackEvaluator extends
	AbstractStackEvaluator implements StandardEvaluator
{

	public AbstractStandardStackEvaluator() {
		super();
	}

	public AbstractStandardStackEvaluator(final ExpressionParser parser) {
		super(parser);
	}

	// -- StandardEvaluator methods --

	// -- postfix --

	@Override
	public Object postInc(final Object a) {
		final Variable v = var(a);
		final Object value = value(v);
		if (value == null) return null;
		set(v, add(a, 1));
		return value;
	}

	@Override
	public Object postDec(final Object a) {
		final Variable v = var(a);
		final Object value = value(v);
		if (value == null) return null;
		set(v, sub(a, 1));
		return value;
	}

	// -- unary --

	@Override
	public Object preInc(final Object a) {
		final Variable v = var(a);
		final Object result = add(a, 1);
		set(v, result);
		return result;
	}

	@Override
	public Object preDec(final Object a) {
		final Variable v = var(a);
		final Object result = sub(a, 1);
		set(v, result);
		return result;
	}

	// -- assignment --

	@Override
	public Object assign(final Object a, final Object b) {
		final Variable v = var(a);
		set(v, value(b));
		return v;
	}

	@Override
	public Object powAssign(final Object a, final Object b) {
		return assign(a, pow(a, b));
	}

	@Override
	public Object dotPowAssign(final Object a, final Object b) {
		return assign(a, dotPow(a, b));
	}

	@Override
	public Object mulAssign(final Object a, final Object b) {
		return assign(a, mul(a, b));
	}

	@Override
	public Object divAssign(final Object a, final Object b) {
		return assign(a, div(a, b));
	}

	@Override
	public Object modAssign(final Object a, final Object b) {
		return assign(a, mod(a, b));
	}

	@Override
	public Object rightDivAssign(final Object a, final Object b) {
		return assign(a, rightDiv(a, b));
	}

	@Override
	public Object dotDivAssign(final Object a, final Object b) {
		return assign(a, dotDiv(a, b));
	}

	@Override
	public Object dotRightDivAssign(final Object a, final Object b) {
		return assign(a, dotRightDiv(a, b));
	}

	@Override
	public Object addAssign(final Object a, final Object b) {
		return assign(a, add(a, b));
	}

	@Override
	public Object subAssign(final Object a, final Object b) {
		return assign(a, sub(a, b));
	}

	@Override
	public Object andAssign(final Object a, final Object b) {
		return assign(a, bitwiseAnd(a, b));
	}

	@Override
	public Object orAssign(final Object a, final Object b) {
		return assign(a, bitwiseOr(a, b));
	}

	@Override
	public Object leftShiftAssign(final Object a, final Object b) {
		return assign(a, leftShift(a, b));
	}

	@Override
	public Object rightShiftAssign(final Object a, final Object b) {
		return assign(a, rightShift(a, b));
	}

	@Override
	public Object unsignedRightShiftAssign(final Object a, final Object b) {
		return assign(a, unsignedRightShift(a, b));
	}

	// -- StackEvaluator methods --

	@Override
	public Object execute(final Verb verb, final Deque<Object> stack) {
		// Pop the arguments.
		final int arity = verb.getArity();
		final Object[] args = new Object[arity];
		for (int i = args.length - 1; i >= 0; i--) {
			args[i] = stack.pop();
		}
		final Object a = args.length > 0 ? args[0] : null;
		final Object b = args.length > 1 ? args[1] : null;

		// Let the case logic begin!
		if (verb == Operators.DOT) return dot(a, b);
		if (verb == Operators.TRANSPOSE) return transpose(a);
		if (verb == Operators.DOT_TRANSPOSE) return dotTranspose(a);
		if (verb == Operators.POW) return pow(a, b);
		if (verb == Operators.DOT_POW) return dotPow(a, b);
		if (verb == Operators.POST_INC) return postInc(a);
		if (verb == Operators.POST_DEC) return postDec(a);
		if (verb == Operators.PRE_INC) return preInc(a);
		if (verb == Operators.PRE_DEC) return preDec(a);
		if (verb == Operators.POS) return pos(a);
		if (verb == Operators.NEG) return neg(a);
		if (verb == Operators.COMPLEMENT) return complement(a);
		if (verb == Operators.NOT) return not(a);
		if (verb == Operators.MUL) return mul(a, b);
		if (verb == Operators.DIV) return div(a, b);
		if (verb == Operators.MOD) return mod(a, b);
		if (verb == Operators.RIGHT_DIV) return rightDiv(a, b);
		if (verb == Operators.DOT_DIV) return dotDiv(a, b);
		if (verb == Operators.DOT_RIGHT_DIV) return dotRightDiv(a, b);
		if (verb == Operators.ADD) return add(a, b);
		if (verb == Operators.SUB) return sub(a, b);
		if (verb == Operators.LEFT_SHIFT) return leftShift(a, b);
		if (verb == Operators.RIGHT_SHIFT) return rightShift(a, b);
		if (verb == Operators.UNSIGNED_RIGHT_SHIFT) return unsignedRightShift(a, b);
		if (verb == Operators.COLON) return colon(a, b);
		if (verb == Operators.LESS_THAN) return lessThan(a, b);
		if (verb == Operators.GREATER_THAN) return greaterThan(a, b);
		if (verb == Operators.LESS_THAN_OR_EQUAL) return lessThanOrEqual(a, b);
		if (verb == Operators.GREATER_THAN_OR_EQUAL) return greaterThanOrEqual(a, b);
		if (verb == Operators.INSTANCEOF) return instanceOf(a, b);
		if (verb == Operators.EQUAL) return equal(a, b);
		if (verb == Operators.NOT_EQUAL) return notEqual(a, b);
		if (verb == Operators.BITWISE_AND) return bitwiseAnd(a, b);
		if (verb == Operators.BITWISE_OR) return bitwiseOr(a, b);
		if (verb == Operators.LOGICAL_AND) return logicalAnd(a, b);
		if (verb == Operators.LOGICAL_OR) return logicalOr(a, b);
		if (verb == Operators.ASSIGN) return assign(a, b);
		if (verb == Operators.POW_ASSIGN) return powAssign(a, b);
		if (verb == Operators.DOT_POW_ASSIGN) return dotPowAssign(a, b);
		if (verb == Operators.MUL_ASSIGN) return mulAssign(a, b);
		if (verb == Operators.DIV_ASSIGN) return divAssign(a, b);
		if (verb == Operators.MOD_ASSIGN) return modAssign(a, b);
		if (verb == Operators.RIGHT_DIV_ASSIGN) return rightDivAssign(a, b);
		if (verb == Operators.DOT_DIV_ASSIGN) return dotDivAssign(a, b);
		if (verb == Operators.DOT_RIGHT_DIV_ASSIGN) return dotRightDivAssign(a, b);
		if (verb == Operators.ADD_ASSIGN) return addAssign(a, b);
		if (verb == Operators.SUB_ASSIGN) return subAssign(a, b);
		if (verb == Operators.AND_ASSIGN) return andAssign(a, b);
		if (verb == Operators.OR_ASSIGN) return orAssign(a, b);
		if (verb == Operators.LEFT_SHIFT_ASSIGN) return leftShiftAssign(a, b);
		if (verb == Operators.RIGHT_SHIFT_ASSIGN) return rightShiftAssign(a, b);
		if (verb == Operators.UNSIGNED_RIGHT_SHIFT_ASSIGN) return unsignedRightShiftAssign(a, b);

		// Unknown verb.
		return null;
	}

	// -- Helper methods --

	/** Casts the given token to a variable. */
	private Variable var(final Object token) {
		if (Tokens.isVariable(token)) return (Variable) token;
		throw new IllegalArgumentException("Not a variable: " + token);
	}

}
