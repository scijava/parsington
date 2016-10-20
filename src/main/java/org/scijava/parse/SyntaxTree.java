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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <a href="https://en.wikipedia.org/wiki/Abstract_syntax_tree">syntax
 * tree</a> corresponding to an expression.
 *
 * @author Curtis Rueden
 */
public class SyntaxTree implements Iterable<SyntaxTree> {

	private final Object token;
	private SyntaxTree[] children;

	/**
	 * Creates a syntax tree built from the given <a
	 * href="https://en.wikipedia.org/wiki/Reverse_Polish_notation">postfix</a>
	 * token queue. <em>This process will consume the entire queue.</em>
	 *
	 * @param tokens The token queue, in postfix order.
	 */
	public SyntaxTree(final LinkedList<Object> tokens) {
		token = tokens.removeLast();
		if (Tokens.isOperator(token)) {
			final Operator op = (Operator) token;
			final int arity = op.getArity();
			if (arity > 0) children = new SyntaxTree[arity];
			for (int i = children.length - 1; i >= 0; i--) {
				children[i] = new SyntaxTree(tokens);
			}
		}
	}

	public Object token() {
		return token;
	}

	public SyntaxTree child(final int index) {
		return children[index];
	}

	public int count() {
		return children == null ? 0 : children.length;
	}

	/** Converts the syntax tree into a token queue in postfix order. */
	public LinkedList<Object> postfix() {
		final LinkedList<Object> queue = new LinkedList<Object>();
		postfix(queue);
		return queue;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SyntaxTree)) return false;
		final SyntaxTree tree = (SyntaxTree) o;
		return token.equals(tree.token) && Arrays.equals(children, tree.children);
	}

	@Override
	public int hashCode() {
		return token.hashCode() ^ children.hashCode();
	}

	// -- Iterable methods --

	@Override
	public Iterator<SyntaxTree> iterator() {
		return new Iterator<SyntaxTree>() {

			private int index;

			@Override
			public boolean hasNext() {
				return index < count();
			}

			@Override
			public SyntaxTree next() {
				return child(index++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// -- Helper methods --

	private void postfix(final LinkedList<Object> queue) {
		for (final SyntaxTree child : this) {
			child.postfix(queue);
		}
		queue.add(token());
	}

	private String toString(final String prefix) {
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix + " '" + token + "'\n");
		final String deeperPrefix = " " + prefix + "-";
		for (int i = 0; i < count(); i++) {
			sb.append(child(i).toString(deeperPrefix));
		}
		return sb.toString();
	}

}
