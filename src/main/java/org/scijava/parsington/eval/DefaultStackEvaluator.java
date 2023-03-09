/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2023 Board of Regents of the University of
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
import org.scijava.parsington.Operators;

/**
 * An expression evaluator for most {@link Operators standard operators} with
 * common built-in types (i.e.: {@link Boolean}s, {@link String}s and
 * {@link Number}s). Does not handle short circuiting of ternary expressions.
 * <p>
 * It is recommended to use {@link DefaultTreeEvaluator} instead, unless your
 * expression's syntax tree is so deep it exceeds the maximum recursion depth.
 * </p>
 *
 * @author Curtis Rueden
 * @see DefaultTreeEvaluator For an evaluator that supports ternary
 *      short-circuiting.
 */
public class DefaultStackEvaluator extends AbstractStandardEvaluator implements
	StandardStackEvaluator
{

	public DefaultStackEvaluator() {
		super();
	}

	public DefaultStackEvaluator(final ExpressionParser parser) {
		super(parser);
	}

}
