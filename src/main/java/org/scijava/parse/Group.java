/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
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

package org.scijava.parse;

/**
 * A group is a special N-ary operator delineated by a left-hand symbol and a
 * right-hand symbol, with comma-separated arguments.
 * <p>
 * Typically, these are various forms of parentheses, although in principle any
 * pair of two distinct symbols is allowed.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Group extends Operator {

	private final String terminator;

	private int arity;

	public Group(final String initiator, final String terminator,
		final double precedence)
	{
		super(initiator, 0, Associativity.NONE, precedence);
		this.terminator = terminator;
	}

	// -- Group methods --

	public String getTerminator() {
		return terminator;
	}

	public void incArity() {
		arity++;
	}

	/**
	 * Returns true iff the given group is the same as this one, in terms of token
	 * (lefthand symbol), terminator (righthand symbol) and precedence.
	 * <p>
	 * Note that this method intentionally does not compare arity; the idea is
	 * that if you have a {@link Group} and call {@link #instance} to duplicate
	 * it, that copy will match this one, even though the copy initially starts at
	 * arity 0.
	 * </p>
	 */
	public boolean matches(final Group g) {
		return getToken().equals(g.getToken()) &&
			getTerminator().equals(g.getTerminator()) &&
			getPrecedence() == g.getPrecedence();
	}

	// -- Operator methods --

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public boolean isInfix() {
		return true;
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	/**
	 * Creates an instance of a group operator, using this one as a template.
	 * <p>
	 * The created group will have the same initiator and terminator symbols, as
	 * well as the same precedence. But it will begin as a nullary group until
	 * {@link #incArity} is called.
	 * </p>
	 */
	@Override
	public Group instance() {
		return new Group(getToken(), getTerminator(), getPrecedence());
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getToken() + getArity() + getTerminator();
	}

}
