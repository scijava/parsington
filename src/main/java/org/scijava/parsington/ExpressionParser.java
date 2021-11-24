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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A parser for mathematical expressions, using Dijkstra's famous <a
 * href="https://en.wikipedia.org/wiki/Shunting-yard_algorithm">shunting-yard
 * algorithm</a>.
 * <p>
 * It is important to note that this parser does not attempt to
 * <em>evaluate</em> the expression in any way; rather, it only provides the
 * parsed tree according to the desired operators.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ExpressionParser {

	private final List<Operator> operators;
	private final String elementSeparator;
	private final String statementSeparator;

	/**
	 * Creates an expression parser with the standard set of operators and default
	 * separator symbols ({@code ,} for group elements, {@code ;} for statements).
	 * 
	 * @see Operators#standardList()
	 */
	public ExpressionParser() {
		this(Operators.standardList());
	}

	/**
	 * Creates an expression parser with custom operators and default separator
	 * symbols ({@code ,} for group elements, {@code ;} for statements).
	 * 
	 * @param operators The collection of operators available to expressions.
	 */
	public ExpressionParser(final Collection<? extends Operator> operators) {
		this(operators, ",", ";");
	}

	/**
	 * Creates an expression parser with custom operators and separator symbols.
	 *
	 * @param operators The collection of operators available to expressions.
	 * @param elementSeparator The symbol to use for separating group elements.
	 * @param statementSeparator The symbol to use for separating statements.
	 */
	public ExpressionParser(final Collection<? extends Operator> operators,
		final String elementSeparator, final String statementSeparator)
	{
		final List<Operator> operatorsList = new ArrayList<>(operators);

		// NB: Ensure operators with longer symbols come first.
		// This prevents e.g. '-' from being matched before '-=' and '--'.
		Collections.sort(operatorsList, (o1, o2) -> {
			final String t1 = o1.getToken();
			final String t2 = o2.getToken();
			final int len1 = t1.length();
			final int len2 = t2.length();
			if (len1 > len2) return -1; // o1 is longer, so o1 comes first.
			if (len1 < len2) return 1; // o2 is longer, so o2 comes first.
			return t1.compareTo(t2);
		});

		this.operators = Collections.unmodifiableList(operatorsList);
		this.elementSeparator = elementSeparator;
		this.statementSeparator = statementSeparator;
	}

	// -- ExpressionParser methods --

	/**
	 * Parses the given mathematical expression into a syntax tree.
	 *
	 * @param expression The mathematical expression to parse.
	 * @return Parsed hierarchy of tokens.
	 * @throws IllegalArgumentException if the syntax of the expression is
	 *           incorrect.
	 */
	public SyntaxTree parseTree(final String expression) {
		return new SyntaxTree(parsePostfix(expression));
	}

	/**
	 * Parses the given mathematical expression into a queue in <a
	 * href="https://en.wikipedia.org/wiki/Reverse_Polish_notation">Reverse Polish
	 * notation</a> (i.e., postfix notation).
	 *
	 * @param expression The mathematical expression to parse.
	 * @return Parsed queue of tokens in postfix notation.
	 * @throws IllegalArgumentException if the syntax of the expression is
	 *           incorrect.
	 */
	public LinkedList<Object> parsePostfix(final String expression) {
		return new ParseOperation(this, expression).parsePostfix();
	}

	/**
	 * Gets the list of operators available to expressions.
	 * 
	 * @return A read-only view of the operators list.
	 */
	public List<Operator> operators() {
		return operators;
	}

	/**
	 * Separator symbol between group elements. The default symbol is comma
	 * ({@code ,}). Example: {@code f(1, 2)}
	 * 
	 * @see ExpressionParser#ExpressionParser(Collection, String, String)
	 */
	public String elementSeparator() {
		return elementSeparator;
	}

	/**
	 * Separator symbol between statements. The default symbol is semicolon
	 * ({@code ;}). Example: {@code x = 1; y = 2; z = x + y}
	 * 
	 * @see ExpressionParser#ExpressionParser(Collection, String, String)
	 */
	public String statementSeparator() {
		return statementSeparator;
	}

}
