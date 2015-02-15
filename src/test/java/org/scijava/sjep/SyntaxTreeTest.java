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

package org.scijava.sjep;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Tests {@link SyntaxTree}.
 *
 * @author Curtis Rueden
 */
public class SyntaxTreeTest extends AbstractTest {

	@Test
	public void testSimple() {
		// infix:   a + b * c
		// postfix: [a, b, c, *, +]
		final LinkedList<Object> queue =
			queue(var("a"), var("b"), var("c"), Operators.MUL, Operators.ADD);
		final SyntaxTree tree = new SyntaxTree(queue);

		assertNotNull(tree);
		assertSame(Operators.ADD, tree.token());
		assertCount(2, tree);
		assertVariable("a", tree.child(0).token());
		assertSame(Operators.MUL, tree.child(1).token());
		assertCount(2, tree.child(1));
		assertVariable("b", tree.child(1).child(0).token());
		assertVariable("c", tree.child(1).child(1).token());
	}

	@Test
	public void testComplicated() {
		// infix: a / b * c + a ^ b ^ c - f(d, c, b, a)
		// postfix: [a, b, /, c, *, a, b, c, ^, ^, +, d, c, b, a, f, -]
		final LinkedList<Object> queue =
			queue(var("a"), var("b"), Operators.DIV, var("c"), Operators.MUL,
				var("a"), var("b"), var("c"), Operators.POW, Operators.POW,
				Operators.ADD, var("d"), var("c"), var("b"), var("a"), func("f", 4),
				Operators.SUB);
		final SyntaxTree tree = new SyntaxTree(queue);

		assertNotNull(tree);
		assertSame(Operators.SUB, token(tree));
		assertSame(Operators.ADD, token(tree, 0));
		assertSame(Operators.MUL, token(tree, 0, 0));
		assertSame(Operators.DIV, token(tree, 0, 0, 0));
		assertVariable("a", token(tree, 0, 0, 0, 0));
		assertVariable("b", token(tree, 0, 0, 0, 1));
		assertVariable("c", token(tree, 0, 0, 1));
		assertSame(Operators.POW, token(tree, 0, 1));
		assertVariable("a", token(tree, 0, 1, 0));
		assertSame(Operators.POW, token(tree, 0, 1, 1));
		assertVariable("b", token(tree, 0, 1, 1, 0));
		assertVariable("c", token(tree, 0, 1, 1, 1));
		assertFunction("f", 4, token(tree, 1));
		assertVariable("d", token(tree, 1, 0));
		assertVariable("c", token(tree, 1, 1));
		assertVariable("b", token(tree, 1, 2));
		assertVariable("a", token(tree, 1, 3));
	}

	// -- Helper methods --

	private LinkedList<Object> queue(final Object... args) {
		return new LinkedList<Object>(Arrays.asList(args));
	}

	private Variable var(final String token) {
		return new Variable(token);
	}

	private Function func(final String token, final int arity) {
		return new Function(token, arity);
	}

	private Object token(final SyntaxTree tree, int... indices) {
		if (indices.length == 0) return tree.token();
		final int[] subIndices = new int[indices.length - 1];
		System.arraycopy(indices, 1, subIndices, 0, subIndices.length);
		return token(tree.child(indices[0]), subIndices);
	}

}
