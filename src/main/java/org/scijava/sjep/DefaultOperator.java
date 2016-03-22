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

package org.scijava.sjep;

/**
 * Default implementation of {@link Operator}.
 *
 * @author Curtis Rueden
 */
public class DefaultOperator extends AbstractToken implements Operator {

	private final int arity;
	private final Associativity associativity;
	private final double precedence;

	public DefaultOperator(final String symbol, final int arity,
		final Associativity associativity, final double precedence)
	{
		super(symbol);
		this.arity = arity;
		this.associativity = associativity;
		this.precedence = precedence;
	}

	// -- Operator methods --

	@Override
	public Associativity getAssociativity() {
		return associativity;
	}

	@Override
	public boolean isLeftAssociative() {
		final Associativity a = getAssociativity();
		return a == Associativity.LEFT || a == Associativity.EITHER;
	}

	@Override
	public boolean isRightAssociative() {
		final Associativity a = getAssociativity();
		return a == Associativity.RIGHT || a == Associativity.EITHER;
	}

	@Override
	public boolean isInfix() {
		return getArity() > 1;
	}

	@Override
	public boolean isPrefix() {
		return getArity() == 1 && isRightAssociative();
	}

	@Override
	public boolean isPostfix() {
		return getArity() == 1 && isLeftAssociative();
	}

	@Override
	public double getPrecedence() {
		return precedence;
	}

	// -- Verb methods --

	@Override
	public int getArity() {
		return arity;
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Operator that) {
		final double thisP = getPrecedence();
		final double thatP = that.getPrecedence();
		if (thisP == thatP) return 0;
		return thisP < thatP ? -1 : 1;
	}

}
