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

import org.scijava.parsington.Function;
import org.scijava.parsington.Operators;

/**
 * Interface for expression evaluators which support the {@link Operators
 * standard operators}.
 *
 * @author Curtis Rueden
 */
public interface StandardEvaluator extends Evaluator {

	// -- function --

	/** Applies the {@link Function} operator. */
	Object function(Object a, Object b);

	// -- dot --

	/** Applies the {@link Operators#DOT} operator. */
	Object dot(Object a, Object b);

	// -- groups --

	/** Applies the {@link Operators#PARENS} operator. */
	Object parens(Object[] args);

	/** Applies the {@link Operators#BRACKETS} operator. */
	Object brackets(Object[] args);

	/** Applies the {@link Operators#BRACES} operator. */
	Object braces(Object[] args);

	// -- transpose, power --

	/** Applies the {@link Operators#TRANSPOSE} operator. */
	Object transpose(Object a);

	/** Applies the {@link Operators#DOT_TRANSPOSE} operator. */
	Object dotTranspose(Object a);

	/** Applies the {@link Operators#POW} operator. */
	Object pow(Object a, Object b);

	/** Applies the {@link Operators#DOT_POW} operator. */
	Object dotPow(Object a, Object b);

	// -- postfix --

	/** Applies the {@link Operators#POST_INC} operator. */
	Object postInc(Object a);

	/** Applies the {@link Operators#POST_DEC} operator. */
	Object postDec(Object a);

	// -- unary --

	/** Applies the {@link Operators#PRE_INC} operator. */
	Object preInc(Object a);

	/** Applies the {@link Operators#PRE_DEC} operator. */
	Object preDec(Object a);

	/** Applies the {@link Operators#POS} operator. */
	Object pos(Object a);

	/** Applies the {@link Operators#NEG} operator. */
	Object neg(Object a);

	/** Applies the {@link Operators#COMPLEMENT} operator. */
	Object complement(Object a);

	/** Applies the {@link Operators#NOT} operator. */
	Object not(Object a);

	// -- multiplicative --

	/** Applies the {@link Operators#MUL} operator. */
	Object mul(Object a, Object b);

	/** Applies the {@link Operators#DIV} operator. */
	Object div(Object a, Object b);

	/** Applies the {@link Operators#MOD} operator. */
	Object mod(Object a, Object b);

	/** Applies the {@link Operators#RIGHT_DIV} operator. */
	Object rightDiv(Object a, Object b);

	/** Applies the {@link Operators#DOT_MUL} operator. */
	Object dotMul(Object a, Object b);

	/** Applies the {@link Operators#DOT_DIV} operator. */
	Object dotDiv(Object a, Object b);

	/** Applies the {@link Operators#DOT_RIGHT_DIV} operator. */
	Object dotRightDiv(Object a, Object b);

	// -- additive --

	/** Applies the {@link Operators#ADD} operator. */
	Object add(Object a, Object b);

	/** Applies the {@link Operators#SUB} operator. */
	Object sub(Object a, Object b);

	// -- shift --

	/** Applies the {@link Operators#LEFT_SHIFT} operator. */
	Object leftShift(Object a, Object b);

	/** Applies the {@link Operators#RIGHT_SHIFT} operator. */
	Object rightShift(Object a, Object b);

	/** Applies the {@link Operators#UNSIGNED_RIGHT_SHIFT} operator. */
	Object unsignedRightShift(Object a, Object b);

	// -- relational --

	/** Applies the {@link Operators#LESS_THAN} operator. */
	Object lessThan(Object a, Object b);

	/** Applies the {@link Operators#GREATER_THAN} operator. */
	Object greaterThan(Object a, Object b);

	/** Applies the {@link Operators#LESS_THAN_OR_EQUAL} operator. */
	Object lessThanOrEqual(Object a, Object b);

	/** Applies the {@link Operators#GREATER_THAN_OR_EQUAL} operator. */
	Object greaterThanOrEqual(Object a, Object b);

	/** Applies the {@link Operators#INSTANCEOF} operator. */
	Object instanceOf(Object a, Object b);

	// -- equality --

	/** Applies the {@link Operators#EQUAL} operator. */
	Object equal(Object a, Object b);

	/** Applies the {@link Operators#NOT_EQUAL} operator. */
	Object notEqual(Object a, Object b);

	// -- bitwise --

	/** Applies the {@link Operators#BITWISE_AND} operator. */
	Object bitwiseAnd(Object a, Object b);

	/** Applies the {@link Operators#BITWISE_OR} operator. */
	Object bitwiseOr(Object a, Object b);

	// -- logical --

	/** Applies the {@link Operators#LOGICAL_AND} operator. */
	Object logicalAnd(Object a, Object b);

	/** Applies the {@link Operators#LOGICAL_OR} operator. */
	Object logicalOr(Object a, Object b);

	// -- ternary --

	/** Applies the {@link Operators#QUESTION} operator. */
	Object question(Object a, Object b);

	/** Applies the {@link Operators#COLON} operator. */
	Object colon(Object a, Object b);

	// -- assignment --

	/** Applies the {@link Operators#ASSIGN} operator. */
	Object assign(Object a, Object b);

	/** Applies the {@link Operators#POW_ASSIGN} operator. */
	Object powAssign(Object a, Object b);

	/** Applies the {@link Operators#DOT_POW_ASSIGN} operator. */
	Object dotPowAssign(Object a, Object b);

	/** Applies the {@link Operators#MUL_ASSIGN} operator. */
	Object mulAssign(Object a, Object b);

	/** Applies the {@link Operators#DIV_ASSIGN} operator. */
	Object divAssign(Object a, Object b);

	/** Applies the {@link Operators#MOD_ASSIGN} operator. */
	Object modAssign(Object a, Object b);

	/** Applies the {@link Operators#RIGHT_DIV_ASSIGN} operator. */
	Object rightDivAssign(Object a, Object b);

	/** Applies the {@link Operators#DOT_DIV_ASSIGN} operator. */
	Object dotDivAssign(Object a, Object b);

	/** Applies the {@link Operators#DOT_RIGHT_DIV_ASSIGN} operator. */
	Object dotRightDivAssign(Object a, Object b);

	/** Applies the {@link Operators#ADD_ASSIGN} operator. */
	Object addAssign(Object a, Object b);

	/** Applies the {@link Operators#SUB_ASSIGN} operator. */
	Object subAssign(Object a, Object b);

	/** Applies the {@link Operators#AND_ASSIGN} operator. */
	Object andAssign(Object a, Object b);

	/** Applies the {@link Operators#OR_ASSIGN} operator. */
	Object orAssign(Object a, Object b);

	/** Applies the {@link Operators#LEFT_SHIFT_ASSIGN} operator. */
	Object leftShiftAssign(Object a, Object b);

	/** Applies the {@link Operators#RIGHT_SHIFT_ASSIGN} operator. */
	Object rightShiftAssign(Object a, Object b);

	/** Applies the {@link Operators#UNSIGNED_RIGHT_SHIFT_ASSIGN} operator. */
	Object unsignedRightShiftAssign(Object a, Object b);

}
