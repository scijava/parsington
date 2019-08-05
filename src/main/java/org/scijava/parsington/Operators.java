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

package org.scijava.parsington;

import static org.scijava.parsington.Operator.Associativity.LEFT;
import static org.scijava.parsington.Operator.Associativity.RIGHT;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.parsington.Operator.Associativity;

/**
 * A collection of standard {@link Operator}s. This set of operators was
 * synthesized by combining <a href=
 * "http://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html"
 * >Java standard operators</a> and <a href=
 * "http://www.mathworks.com/help/matlab/matlab_prog/operator-precedence.html"
 * >MATLAB standard operators</a>.
 * <p>
 * Note that the {@code ^} operator is assigned MATLAB's meaning of
 * exponentiation, rather than Java's meaning of bitwise XOR. And the {@code :}
 * operator&mdash;which in MATLAB resides at a precedence between the shift and
 * relational operators&mdash;is instead assigned a lower precedence to
 * facilitate support for a Java-like ternary {@code ?:} operation.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Operators {

	// -- dot --

	public static final Operator DOT = op(".", 2, LEFT, 16);

	// -- groups --

	public static final Group PARENS = group("(", ")", 16);
	public static final Group BRACKETS = group("[", "]", 16);
	public static final Group BRACES = group("{", "}", 16);

	// -- transpose, power --

	public static final Operator TRANSPOSE = op("'", 1, LEFT, 15);
	public static final Operator DOT_TRANSPOSE = op(".'", 1, LEFT, 15);
	public static final Operator POW = op("^", 2, RIGHT, 15);
	public static final Operator DOT_POW = op(".^", 2, RIGHT, 15);

	// -- postfix --

	public static final Operator POST_INC = op("++", 1, LEFT, 14);
	public static final Operator POST_DEC = op("--", 1, LEFT, 14);

	// -- unary --

	public static final Operator PRE_INC = op("++", 1, RIGHT, 13);
	public static final Operator PRE_DEC = op("--", 1, RIGHT, 13);
	public static final Operator POS = op("+", 1, RIGHT, 13);
	public static final Operator NEG = op("-", 1, RIGHT, 13);
	public static final Operator COMPLEMENT = op("~", 1, RIGHT, 13);
	public static final Operator NOT = op("!", 1, RIGHT, 13);

	// -- multiplicative --

	public static final Operator MUL = op("*", 2, LEFT, 12);
	public static final Operator DIV = op("/", 2, LEFT, 12);
	public static final Operator MOD = op("%", 2, LEFT, 12);
	public static final Operator RIGHT_DIV = op("\\", 2, LEFT, 12);
	public static final Operator DOT_MUL = op(".*", 2, LEFT, 12);
	public static final Operator DOT_DIV = op("./", 2, LEFT, 12);
	public static final Operator DOT_RIGHT_DIV = op(".\\", 2, LEFT, 12);

	// -- additive --

	public static final Operator ADD = op("+", 2, LEFT, 11);
	public static final Operator SUB = op("-", 2, LEFT, 11);

	// -- shift --

	public static final Operator LEFT_SHIFT = op("<<", 2, LEFT, 10);
	public static final Operator RIGHT_SHIFT = op(">>", 2, LEFT, 10);
	public static final Operator UNSIGNED_RIGHT_SHIFT = op(">>>", 2, LEFT, 10);

	// -- relational --

	public static final Operator LESS_THAN = op("<", 2, LEFT, 8);
	public static final Operator GREATER_THAN = op(">", 2, LEFT, 8);
	public static final Operator LESS_THAN_OR_EQUAL = op("<=", 2, LEFT, 8);
	public static final Operator GREATER_THAN_OR_EQUAL = op(">=", 2, LEFT, 8);
	public static final Operator INSTANCEOF = op("instanceof", 2, LEFT, 8);

	// -- equality --

	public static final Operator EQUAL = op("==", 2, LEFT, 7);
	public static final Operator NOT_EQUAL = op("!=", 2, LEFT, 7);

	// -- bitwise AND --

	public static final Operator BITWISE_AND = op("&", 2, LEFT, 6);

	// -- bitwise exclusive OR --

	// NB: No bitwise XOR operator, because '^' is reserved for POW above.
	//public static final Operator BITWISE_XOR = op("^", 2, LEFT, 5);

	// -- bitwise inclusive OR --

	public static final Operator BITWISE_OR = op("|", 2, LEFT, 4);

	// -- logical AND --

	public static final Operator LOGICAL_AND = op("&&", 2, LEFT, 3);

	// -- logical OR --

	public static final Operator LOGICAL_OR = op("||", 2, LEFT, 2);

	// -- ternary --

	// NB: These will not be parsed as true ternary operators.
	// But the behavior can be simulated at evaluation time.
	public static final Operator QUESTION = op("?", 2, LEFT, 1);
	public static final Operator COLON = op(":", 2, LEFT, 1.5);

	// -- assignment --

	public static final Operator ASSIGN = op("=", 2, RIGHT, 0);
	public static final Operator POW_ASSIGN = op("^=", 2, RIGHT, 0);
	public static final Operator DOT_POW_ASSIGN = op(".^=", 2, RIGHT, 0);
	public static final Operator MUL_ASSIGN = op("*=", 2, RIGHT, 0);
	public static final Operator DIV_ASSIGN = op("/=", 2, RIGHT, 0);
	public static final Operator MOD_ASSIGN = op("%=", 2, RIGHT, 0);
	public static final Operator RIGHT_DIV_ASSIGN = op("\\=", 2, RIGHT, 0);
	public static final Operator DOT_DIV_ASSIGN = op("./=", 2, RIGHT, 0);
	public static final Operator DOT_RIGHT_DIV_ASSIGN = op(".\\=", 2, RIGHT, 0);
	public static final Operator ADD_ASSIGN = op("+=", 2, RIGHT, 0);
	public static final Operator SUB_ASSIGN = op("-=", 2, RIGHT, 0);
	public static final Operator AND_ASSIGN = op("&=", 2, RIGHT, 0);
	public static final Operator OR_ASSIGN = op("|=", 2, RIGHT, 0);
	public static final Operator LEFT_SHIFT_ASSIGN = op("<<=", 2, RIGHT, 0);
	public static final Operator RIGHT_SHIFT_ASSIGN = op(">>=", 2, RIGHT, 0);
	public static final Operator UNSIGNED_RIGHT_SHIFT_ASSIGN = op(">>>=", 2,
		RIGHT, 0);

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
		return new Operator(symbol, arity, associativity, precedence);
	}

	private static Group group(final String leftSymbol,
		final String rightSymbol, final double precedence)
	{
		return new Group(leftSymbol, rightSymbol, precedence);
	}

	private static boolean isOperator(final Field f) {
		final int mods = f.getModifiers();
		return Modifier.isStatic(mods) && Modifier.isFinal(mods) &&
			Operator.class.isAssignableFrom(f.getType());
	}

}
