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

package org.scijava.parsington.compiler;

import org.scijava.parsington.SyntaxTree;

/**
 * Compiles syntax trees rooted at a particular operator.
 * <p>
 * Compilation happens in two passes: first {@link #type} determines the
 * result type of every node, then {@link #emit} generates instructions.
 * </p>
 *
 * @author Curtis Rueden
 */
interface Rule {

	/**
	 * Determines the type of the given tree's result.
	 *
	 * @param c The ongoing compilation, for typing child nodes.
	 * @param tree The tree whose root token is this rule's operator.
	 * @return The result type.
	 * @throws IllegalArgumentException If the operand types are not supported.
	 */
	Type type(Compilation c, SyntaxTree tree);

	/**
	 * Emits instructions which leave the tree's result on the stack.
	 *
	 * @param c The ongoing compilation, for emitting instructions.
	 * @param tree The tree whose root token is this rule's operator.
	 * @param type The result type, as previously computed by {@link #type}.
	 */
	void emit(Compilation c, SyntaxTree tree, Type type);
}
