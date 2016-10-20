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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
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

	/** Creates an expression parser with the default set of operators. */
	public ExpressionParser() {
		this(Operators.standardList());
	}

	/**
	 * Creates an expression parser with the given set of operators.
	 *
	 * @param operators The collection of operators available to expressions.
	 */
	public ExpressionParser(final Collection<? extends Operator> operators) {
		this.operators = new ArrayList<Operator>(operators);

		// NB: Ensure operators with longer symbols come first.
		// This prevents e.g. '-' from being matched before '-=' and '--'.
		Collections.sort(this.operators, new Comparator<Operator>() {

			@Override
			public int compare(final Operator o1, final Operator o2) {
				final String t1 = o1.getToken();
				final String t2 = o2.getToken();
				final int len1 = t1.length();
				final int len2 = t2.length();
				if (len1 > len2) return -1; // o1 is longer, so o1 comes first.
				if (len1 < len2) return 1; // o2 is longer, so o2 comes first.
				return t1.compareTo(t2);
			}
		});
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
		return new ParseOperation(expression).parseTree();
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
		return new ParseOperation(expression).parsePostfix();
	}

	// -- Helper classes --

	/** A stateful parsing operation. */
	private class ParseOperation {

		private final String expression;
		private final Position pos = new Position();
		private final Deque<Object> stack = new ArrayDeque<Object>();
		private final LinkedList<Object> outputQueue = new LinkedList<Object>();

		/**
		 * State flag for parsing context.
		 * <ul>
		 * <li>If true, we are expecting an infix or postfix operator next.</li>
		 * <li>If false, we are expecting a prefix operator or "noun" token (e.g.,
		 * variable or literal) next.</li>
		 * </ul>
		 */
		private boolean infix;

		public ParseOperation(final String expression) {
			this.expression = expression;
		}

		/** Parses the expression into a syntax tree. */
		public SyntaxTree parseTree() {
			return new SyntaxTree(parsePostfix());
		}

		/**
		 * Parses the expression into an output queue in <a
		 * href="https://en.wikipedia.org/wiki/Reverse_Polish_notation">Reverse
		 * Polish notation</a> (i.e., postfix notation).
		 */
		public LinkedList<Object> parsePostfix() {
			// While there are tokens to be read...
			while (pos.get() < expression.length()) {
				// PROTIP: Put a breakpoint here, and watch the expression
				// parser do its thing piece by piece in your debugger!
				parseWhitespace();

				// If next token is a literal, add it to the output queue.
				final Object literal = parseLiteral();
				if (literal != null) {
					outputQueue.add(literal);
					// Update the state flag.
					infix = true;
					continue;
				}

				// If next token is a function argument separator (i.e., a comma)...
				final Character separator = parseComma();
				if (separator != null) {
					handleGroupSeparator();
					continue;
				}

				// If next token is a statement separator (i.e., a semicolon)...
				final Character semicolon = parseSemicolon();
				if (semicolon != null) {
					// Flush the stack and begin a new statement.
					flushStack();
					infix = false;
					continue;
				}

				// If the token is an operator...
				final Operator o1 = parseOperator();
				if (o1 != null) {
					if (Tokens.isGroup(o1) && infix) {
						// NB: Group initiator symbol following a "noun" token;
						// we infer an implicit function operator between them.
						handleOperator(new Function(o1.getPrecedence()));
					}
					handleOperator(o1);
					continue;
				}

				// If the token is a group terminator...
				final Group group = parseGroupTerminator();
				if (group != null) {
					handleGroupTerminator(group);
					continue;
				}

				// If next token is a variable, add it to the output queue.
				final Variable variable = parseVariable();
				if (variable != null) {
					outputQueue.add(variable);
					// Update the state flag.
					infix = true;
					continue;
				}

				pos.die("Invalid character");
			}
			// No more tokens to read!

			flushStack();

			return outputQueue;
		}

		public char currentChar() {
			return futureChar(0);
		}

		public char futureChar(final int offset) {
			return pos.ch(expression, offset);
		}

		// -- State methods --

		public boolean isPrefixOK() {
			return !infix;
		}

		public boolean isPostfixOK() {
			return infix;
		}

		public boolean isInfixOK() {
			return infix;
		}

		// -- Parsing methods --

		/** Skips past any whitespace to the next interesting character. */
		public void parseWhitespace() {
			while (Character.isWhitespace(currentChar()))
				pos.inc();
		}

		/**
		 * Attempts to parse a numeric literal.
		 *
		 * @return The parsed literal, or null if the next token is not one.
		 * @see Literals#parseLiteral
		 */
		public Object parseLiteral() {
			// Only accept a literal in the appropriate context.
			// This avoids confusing e.g. the unary and binary minus
			// operators, or a quoted string with a quote operator.
			if (infix) return null;

			return Literals.parseLiteral(expression, pos);
		}

		/**
		 * Attempts to parse a variable.
		 *
		 * @return The parsed variable name, or null if the next token is not one.
		 */
		public Variable parseVariable() {
			final int length = parseIdentifier();
			if (length == 0) return null;
			return new Variable(parseToken(length));
		}

		/**
		 * Attempts to parse an identifier, as defined by
		 * {@link Character#isUnicodeIdentifierStart(char)} and
		 * {@link Character#isUnicodeIdentifierPart(char)}.
		 *
		 * @return The <em>length</em> of the parsed identifier, or 0 if the next
		 *         token is not one.
		 */
		public int parseIdentifier() {
			// Only accept an identifier in the appropriate context.
			if (infix) return 0;

			if (!Character.isUnicodeIdentifierStart(currentChar())) return 0;
			int length = 0;
			while (true) {
				final char next = futureChar(length);
				if (next == '\0') break;
				if (!Character.isUnicodeIdentifierPart(next)) break;
				length++;
			}
			return length;
		}

		/**
		 * Attempts to parse an operator.
		 *
		 * @return The parsed operator, or null if the next token is not one.
		 */
		public Operator parseOperator() {
			for (final Operator op : operators) {
				final String symbol = op.getToken();
				if (operatorMatches(op, symbol)) return op;
			}
			return null;
		}

		/**
		 * Attempts to parse a group terminator symbol.
		 *
		 * @return The group, or null if the next token is not a group terminator.
		 */
		public Group parseGroupTerminator() {
			for (final Operator op : operators) {
				if (!(op instanceof Group)) continue;
				final Group group = (Group) op;
				final String symbol = group.getTerminator();
				if (operatorMatches(op, symbol)) return group;
			}
			return null;
		}

		/**
		 * Attempts to parse a comma character.
		 *
		 * @return The comma, or null if the next token is not one.
		 */
		public Character parseComma() {
			// Only accept a comma in the appropriate context.
			if (!infix) return null;

			return parseChar(',');
		}

		/**
		 * Attempts to parse a semicolon character.
		 *
		 * @return The semicolon, or null if the next token is not one.
		 */
		public Character parseSemicolon() {
			return parseChar(';');
		}

		/**
		 * Attempts to parse the given character.
		 *
		 * @return The character, or null if the next token is not that character.
		 */
		public Character parseChar(final char c) {
			if (currentChar() == c) {
				pos.inc();
				return c;
			}
			return null;
		}

		/**
		 * Parses a token of the given length.
		 *
		 * @return The parsed token.
		 */
		public String parseToken(final int length) {
			final int offset = pos.get();
			final String token = expression.substring(offset, offset + length);
			pos.inc(length);
			return token;
		}

		// -- Object methods --

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(expression);
			sb.append("\n");
			for (int i = 0; i < pos.get(); i++) {
				sb.append(" ");
			}
			sb.append("^");
			return sb.toString();
		}

		// -- Helper methods --

		private void handleOperator(final Operator o1) {
			// While there is an operator token, o2, at the top of the stack...
			final double p1 = o1.getPrecedence();
			while (!stack.isEmpty() && Tokens.isOperator(stack.peek()) &&
				!Tokens.isGroup(stack.peek()))
			{
				final Operator o2 = (Operator) stack.peek();
				final double p2 = o2.getPrecedence();
				// ...and o1 has lower precedence than o2...
				if (o1.isLeftAssociative() && p1 <= p2 || //
					o1.isRightAssociative() && p1 < p2)
				{
					// Pop o2 off the stack, onto the output queue.
					outputQueue.add(stack.pop());
				}
				else break;
			}
			// Push o1 onto the stack.
			stack.push(o1.instance());
			// Update the state flag.
			if (o1.isPrefix() || o1.isInfix()) infix = false;
			else if (o1.isPostfix()) infix = true;
			else pos.fail("Impenetrable operator '" + o1 + "'");
		}

		private void handleGroupSeparator() {
			// Pop from stack to output queue until function found.
			while (true) {
				if (stack.isEmpty()) {
					pos.die("Misplaced separator or mismatched groups");
				}
				if (Tokens.isGroup(stack.peek())) {
					// Count the completed argument in the group's arity.
					((Group) stack.peek()).incArity();
					break;
				}
				outputQueue.add(stack.pop());
			}
			// Update the state flag.
			infix = false;
		}

		private void handleGroupTerminator(final Group group) {
			// Pop from stack to output queue until matching group found.
			while (true) {
				if (stack.isEmpty()) {
					// No group found: mismatched group symbols!
					pos.die("Mismatched group terminator '" + group.getTerminator() +
						"'");
				}
				// If token is a group...
				if (Tokens.isMatchingGroup(stack.peek(), group)) {
					// Count the completed argument in the group's arity.
					if (infix) ((Group) stack.peek()).incArity();
					// Pop the group onto the output queue.
					outputQueue.add(stack.pop());
					break;
				}
				outputQueue.add(stack.pop());
			}
			// Update the state flag.
			infix = true;
		}

		private void flushStack() {
			// While there are still operator tokens in the stack...
			while (!stack.isEmpty()) {
				final Object token = stack.pop();
				// There shouldn't be any groups left.
				if (Tokens.isGroup(token)) pos.die("Mismatched groups");
				// Pop the operator onto the output queue.
				outputQueue.add(token);
			}
		}

		private boolean operatorMatches(final Operator op, final String symbol) {
			if (!expression.startsWith(symbol, pos.get())) return false;
			// Ensure the operator is appropriate to the current context.
			if (isPrefixOK() && op.isPrefix() || //
				isPostfixOK() && op.isPostfix() || //
				isInfixOK() && op.isInfix())
			{
				pos.inc(symbol.length());
				return true;
			}
			return false;
		}

	}

}
