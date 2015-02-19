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

import static org.scijava.sjep.Operator.Associativity.LEFT;
import static org.scijava.sjep.Operator.Associativity.RIGHT;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.sjep.Operator.Associativity;

/**
 * A collection of standard {@link Operator}s.
 *
 * @author Curtis Rueden
 */
public final class Operators {

	// -- unary --

	public static final Operator POS = op("+", 1, RIGHT, 2);
	public static final Operator NEG = op("-", 1, RIGHT, 2);

	// -- multiplicative --

	public static final Operator MUL = op("*", 2, LEFT, 3);
	public static final Operator DIV = op("/", 2, LEFT, 3);

	// -- additive --

	public static final Operator ADD = op("+", 2, LEFT, 2);
	public static final Operator SUB = op("-", 2, LEFT, 2);

	// -- extra --

	public static final Operator POW = op("^", 2, RIGHT, 4);

	private Operators() {
		// NB: Prevent instantiation of utility class.
	}

	/** Gets the standard list of operators. */
	public static List<Operator> standardList() {
		// Build the standard list from all available Operator constants.
		final ArrayList<Operator> ops = new ArrayList<Operator>();
		for (final Field f : Operators.class.getFields()) {
			if (!isOperator(f)) continue;
			try {
				ops.add((Operator) f.get(null));
			}
			catch (final IllegalAccessException exc) {
				// This should never happen.
				throw new IllegalStateException(exc);
			}
		}
		return ops;
	}

	// -- Helper methods --

	private static Operator op(final String symbol, final int arity,
		final Associativity associativity, final double precedence)
	{
		return new DefaultOperator(symbol, arity, associativity, precedence);
	}

	private static boolean isOperator(final Field f) {
		final int mods = f.getModifiers();
		return Modifier.isStatic(mods) && Modifier.isFinal(mods) &&
			f.getType() == Operator.class;
	}

}
