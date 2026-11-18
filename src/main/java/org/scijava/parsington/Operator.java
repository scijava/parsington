/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2026 Parsington developers.
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

package org.scijava.parsington;

/**
 * A mathematical operator is a special symbol or "verb" which defines a
 * relation between "nouns" (i.e.: literals and variables). For binary
 * operators, it is infix (between the nouns); for unary operators, it is either
 * prefix (preceding the noun) or postfix (following the noun).
 *
 * @author Curtis Rueden
 * @see StandardOperator
 */
public interface Operator extends Token {

	static Operator create(final String symbol, final int arity,
		final Associativity associativity, final double precedence)
	{
		return new Operator() {
			@Override public String getToken() { return symbol; }
			@Override public int getArity() { return arity; }
			@Override public Associativity getAssociativity() { return associativity; }
			@Override public double getPrecedence() { return precedence; }
			@Override public String toString() { return getToken(); }
		};
	}

	public enum Associativity {
		EITHER, LEFT, RIGHT, NONE
	}

	// -- Operator methods --

	/**
	 * Gets the operator's arity.
	 * 
	 * @return The arity of the operator: 1 for unary, 2 for binary, etc.
	 */
	int getArity();

	/**
	 * Gets the operator's associativity.
	 * 
	 * @return One of {@link Associativity#EITHER}, {@link Associativity#LEFT},
	 *         {@link Associativity#RIGHT}, or {@link Associativity#NONE}.
	 */
	Associativity getAssociativity();

	/**
	 * Gets whether the operator is left associative.
	 * 
	 * @return True iff the operator's associativity is {@link Associativity#LEFT}
	 *         or {@link Associativity#EITHER}.
	 */
	default boolean isLeftAssociative() {
		final Associativity a = getAssociativity();
		return a == Associativity.LEFT || a == Associativity.EITHER;
	}

	/**
	 * Gets whether the operator is right associative.
	 * 
	 * @return True iff the operator's associativity is
	 *         {@link Associativity#RIGHT} or {@link Associativity#EITHER}.
	 */
	default boolean isRightAssociative() {
		final Associativity a = getAssociativity();
		return a == Associativity.RIGHT || a == Associativity.EITHER;
	}

	/**
	 * Gets whether the operator is an infix operator (e.g., {@code a-b}).
	 * 
	 * @return True iff the operator is an infix operator.
	 */
	default boolean isInfix() {
		return getArity() > 1;
	}

	/**
	 * Gets whether the operator is a prefix operator (e.g., {@code -a}).
	 * 
	 * @return True iff the operator is a prefix operator.
	 */
	default boolean isPrefix() {
		return getArity() == 1 && isRightAssociative();
	}

	/**
	 * Gets whether the operator is a postfix operator (e.g., {@code a'}).
	 * 
	 * @return True iff the operator is a postfix operator.
	 */
	default boolean isPostfix() {
		return getArity() == 1 && isLeftAssociative();
	}

	/**
	 * Gets the operator precedence. Larger is higher.
	 * 
	 * @return The operator precedence.
	 */
	double getPrecedence();

	/**
	 * Gets an instance of the operator, using this one as a template.
	 * <p>
	 * For stateless operators, no copy will be made. But for operators with state
	 * (e.g. {@link Group}), a new instance will be returned.
	 * </p>
	 * 
	 * @return {@code this} or a new instance, depending on the type of operator.
	 */
	default Operator instance() {
		// NB: Properties are immutable, so instance can be reused.
		return this;
	}

}
