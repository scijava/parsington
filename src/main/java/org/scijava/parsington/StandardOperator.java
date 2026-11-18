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

import static org.scijava.parsington.Operator.Associativity.LEFT;
import static org.scijava.parsington.Operator.Associativity.RIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The standard {@link Operator}s. This set of operators was
 * synthesized by combining <a href=
 * "https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html"
 * >Java standard operators</a> and <a href=
 * "https://www.mathworks.com/help/matlab/matlab_prog/operator-precedence.html"
 * >MATLAB standard operators</a>.
 * <p>
 * Note that the {@code ^} operator is assigned MATLAB's meaning of
 * exponentiation, rather than Java's meaning of bitwise XOR. And the {@code :}
 * operator&mdash;which in MATLAB resides at a precedence between the shift and
 * relational operators&mdash;is instead assigned a lower precedence to
 * facilitate support for a Java-like ternary {@code ?:} operation.
 * </p>
 * <p>
 * The {@link #PARENS}, {@link #BRACKETS} and {@link #BRACES} groups are also
 * standard operators, but they are not constants of this enum, because each
 * {@link Group} {@link Group#instance() instance} keeps track of its own arity.
 * </p>
 *
 * @author Curtis Rueden
 */
public enum StandardOperator implements Operator {

	// -- dot --

	DOT(".", 2, LEFT, 16),

	// -- transpose, power --

	TRANSPOSE("'", 1, LEFT, 15),
	DOT_TRANSPOSE(".'", 1, LEFT, 15),
	POW("^", 2, RIGHT, 15),
	DOT_POW(".^", 2, RIGHT, 15),

	// -- postfix --

	POST_INC("++", 1, LEFT, 14),
	POST_DEC("--", 1, LEFT, 14),

	// -- unary --

	PRE_INC("++", 1, RIGHT, 13),
	PRE_DEC("--", 1, RIGHT, 13),
	POS("+", 1, RIGHT, 13),
	NEG("-", 1, RIGHT, 13),
	COMPLEMENT("~", 1, RIGHT, 13),
	NOT("!", 1, RIGHT, 13),

	// -- multiplicative --

	MUL("*", 2, LEFT, 12),
	DIV("/", 2, LEFT, 12),
	MOD("%", 2, LEFT, 12),
	RIGHT_DIV("\\", 2, LEFT, 12),
	DOT_MUL(".*", 2, LEFT, 12),
	DOT_DIV("./", 2, LEFT, 12),
	DOT_RIGHT_DIV(".\\", 2, LEFT, 12),

	// -- additive --

	ADD("+", 2, LEFT, 11),
	SUB("-", 2, LEFT, 11),

	// -- shift --

	LEFT_SHIFT("<<", 2, LEFT, 10),
	RIGHT_SHIFT(">>", 2, LEFT, 10),
	UNSIGNED_RIGHT_SHIFT(">>>", 2, LEFT, 10),

	// -- relational --

	LESS_THAN("<", 2, LEFT, 8),
	GREATER_THAN(">", 2, LEFT, 8),
	LESS_THAN_OR_EQUAL("<=", 2, LEFT, 8),
	GREATER_THAN_OR_EQUAL(">=", 2, LEFT, 8),
	INSTANCEOF("instanceof", 2, LEFT, 8),

	// -- equality --

	EQUAL("==", 2, LEFT, 7),
	NOT_EQUAL("!=", 2, LEFT, 7),

	// -- bitwise AND --

	BITWISE_AND("&", 2, LEFT, 6),

	// -- bitwise exclusive OR --

	// NB: No bitwise XOR operator, because '^' is reserved for POW above.
	//BITWISE_XOR("^", 2, LEFT, 5),

	// -- bitwise inclusive OR --

	BITWISE_OR("|", 2, LEFT, 4),

	// -- logical AND --

	LOGICAL_AND("&&", 2, LEFT, 3),

	// -- logical OR --

	LOGICAL_OR("||", 2, LEFT, 2),

	// -- ternary --

	// NB: These will not be parsed as true ternary operators.
	// But the behavior can be simulated at evaluation time.
	QUESTION("?", 2, LEFT, 1),
	COLON(":", 2, LEFT, 1.5),

	// -- assignment --

	ASSIGN("=", 2, RIGHT, 0),
	POW_ASSIGN("^=", 2, RIGHT, 0),
	DOT_POW_ASSIGN(".^=", 2, RIGHT, 0),
	MUL_ASSIGN("*=", 2, RIGHT, 0),
	DIV_ASSIGN("/=", 2, RIGHT, 0),
	MOD_ASSIGN("%=", 2, RIGHT, 0),
	RIGHT_DIV_ASSIGN("\\=", 2, RIGHT, 0),
	DOT_DIV_ASSIGN("./=", 2, RIGHT, 0),
	DOT_RIGHT_DIV_ASSIGN(".\\=", 2, RIGHT, 0),
	ADD_ASSIGN("+=", 2, RIGHT, 0),
	SUB_ASSIGN("-=", 2, RIGHT, 0),
	AND_ASSIGN("&=", 2, RIGHT, 0),
	OR_ASSIGN("|=", 2, RIGHT, 0),
	LEFT_SHIFT_ASSIGN("<<=", 2, RIGHT, 0),
	RIGHT_SHIFT_ASSIGN(">>=", 2, RIGHT, 0),
	UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>=", 2, RIGHT, 0);

	// -- groups --

	public static final Group PARENS = new Group("(", ")", 16);
	public static final Group BRACKETS = new Group("[", "]", 16);
	public static final Group BRACES = new Group("{", "}", 16);

	private final String symbol;
	private final int arity;
	private final Associativity associativity;
	private final double precedence;

	private StandardOperator(final String symbol, final int arity,
		final Associativity associativity, final double precedence)
	{
		this.symbol = symbol;
		this.arity = arity;
		this.associativity = associativity;
		this.precedence = precedence;
	}

	/**
	 * Gets the standard list of operators.
	 * 
	 * @return A new list containing each constant of this enum, in declaration
	 *         order, followed by the {@link #PARENS}, {@link #BRACKETS} and
	 *         {@link #BRACES} groups.
	 */
	public static List<Operator> standardList() {
		final List<Operator> list = new ArrayList<>(Arrays.asList(values()));
		list.add(PARENS);
		list.add(BRACKETS);
		list.add(BRACES);
		return list;
	}

	// -- Token methods --

	@Override
	public String getToken() {
		return symbol;
	}

	// -- Operator methods --

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public Associativity getAssociativity() {
		return associativity;
	}

	@Override
	public double getPrecedence() {
		return precedence;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getToken();
	}

}
