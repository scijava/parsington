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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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

	private final Set<Operator> operators;

	/** Creates an expression parser with the default set of operators. */
	public ExpressionParser() {
		this(Operators.standardList());
	}

	/**
	 * Creates an expression parser with the given set of operators.
	 * 
	 * @param operators The collection of operators available to expressions.
	 */
	public ExpressionParser(final Collection<Operator> operators) {
		this.operators = new HashSet<Operator>(operators);
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

		/**
		 * Whether the next token may legally be a unary prefix operator.
		 *
		 * @see #state
		 */
		private static final int PREFIX_OK = 1;

		/**
		 * Whether the next token may legally be a unary postfix operator.
		 *
		 * @see #state
		 */
		private static final int POSTFIX_OK = 2;

		/**
		 * Whether the next token may legally be a binary infix operator.
		 *
		 * @see #state
		 */
		private static final int INFIX_OK = 4;

		private final String expression;
		private final Position pos = new Position();
		private final Deque<Object> stack = new ArrayDeque<Object>();
		private final LinkedList<Object> outputQueue = new LinkedList<Object>();

		/**
		 * State flags for operator context.
		 *
		 * @see #PREFIX_OK
		 * @see #POSTFIX_OK
		 * @see #INFIX_OK
		 */
		private int state = PREFIX_OK;

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
				parseWhitespace();

				// If next token is a number, add it to the output queue.
				final Number num = parseNumber();
				if (num != null) {
					outputQueue.add(num);
					// Update the state flags.
					state = POSTFIX_OK | INFIX_OK;
					continue;
				}

				// If next token is a function, push it onto the stack.
				final Function func = parseFunction();
				if (func != null) {
					stack.push(func);
					// Update the state flags.
					state = PREFIX_OK;
					continue;
				}

				// If next token is a function argument separator (i.e., a comma)...
				final Character separator = parseComma();
				if (separator != null) {
					// Pop from stack to output queue until function found.
					while (true) {
						if (stack.isEmpty()) {
							pos.die("Misplaced separator or mismatched parentheses");
						}
						if (Tokens.isFunction(stack.peek())) {
							// Count the completed function argument in the function's arity.
							((Function) stack.peek()).incArity();
							break;
						}
						outputQueue.add(stack.pop());
					}
					// Update the state flags.
					state = PREFIX_OK;
					continue;
				}

				// If the token is an operator...
				final Operator o1 = parseOperator();
				if (o1 != null) {
					// While there is an operator token, o2, at the top of the stack...
					final double p1 = o1.getPrecedence();
					while (!stack.isEmpty() && Tokens.isOperator(stack.peek())) {
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
					stack.push(o1);
					// Update the state flags.
					if (o1.isPrefix() || o1.isInfix()) state = PREFIX_OK;
					else if (o1.isPostfix()) state = POSTFIX_OK | INFIX_OK;
					else pos.fail("Impenetrable operator '" + o1 + "'");
					continue;
				}

				// If the token is a left parenthesis, then push it onto the stack.
				final Character leftParen = parseLeftParen();
				if (leftParen != null) {
					stack.push(leftParen);
					// Update the state flags.
					state = PREFIX_OK;
					continue;
				}

				// If the token is a right parenthesis...
				final Character rightParen = parseRightParen();
				if (rightParen != null) {
					// Pop from stack to output queue until left parenthesis found.
					while (true) {
						if (stack.isEmpty()) {
							// No left parenthesis found: mismatched parentheses!
							pos.die("Mismatched parentheses");
						}
						if (Tokens.isLeftParen(stack.peek())) {
							// Pop the left parenthesis, but not onto the output queue.
							stack.pop();
							break;
						}
						// If token is a function, it implicitly has a left parenthesis.
						if (Tokens.isFunction(stack.peek())) {
							// Count the completed function argument in the function's arity.
							((Function) stack.peek()).incArity();
							// Pop the function onto the output queue.
							outputQueue.add(stack.pop());
							break;
						}
						outputQueue.add(stack.pop());
					}
					// Update the state flags.
					state = POSTFIX_OK | INFIX_OK;
					continue;
				}

				// If next token is a variable, add it to the output queue.
				final Variable variable = parseVariable();
				if (variable != null) {
					outputQueue.add(variable);
					// Update the state flags.
					state = POSTFIX_OK | INFIX_OK;
					continue;
				}

				pos.die("Invalid character");
			}
			// No more tokens to read!

			// While there are still operator tokens in the stack...
			while (!stack.isEmpty()) {
				final Object token = stack.pop();
				// There shouldn't be any parentheses left.
				if (Tokens.isParen(token)) pos.die("Mismatched parentheses");
				// Pop the operator onto the output queue.
				outputQueue.add(token);
			}

			return outputQueue;
		}

		public char currentChar() {
			return futureChar(0);
		}

		public char futureChar(final int offset) {
			final int index = pos.get() + offset;
			return index >= expression.length() ? '\0' : expression.charAt(index);
		}

		// -- State methods --

		public boolean isPrefixOK() {
			return (state & PREFIX_OK) != 0;
		}

		public boolean isPostfixOK() {
			return (state & POSTFIX_OK) != 0;
		}

		public boolean isInfixOK() {
			return (state & INFIX_OK) != 0;
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
		 * @return The parsed number, or null if the next token is not one.
		 */
		public Number parseNumber() {
			// Only accept a numeric literal in the appropriate context.
			// This avoids confusing the negative sign with binary minus operator.
			if (!isPrefixOK()) return null;

			return Literals.parseNumber(expression, pos);
		}

		/**
		 * Attempts to parse a function.
		 *
		 * @return The parsed function name, or null if the next token is not one.
		 */
		public Function parseFunction() {
			final int length = parseIdentifier();
			if (length == 0) return null;

			// Skip any intervening whitespace.
			int offset = length;
			while (Character.isWhitespace(futureChar(offset)))
				offset++;

			// NB: Token is considered a function _iff_ it is
			// an identifier followed by a left parenthesis.
			if (!Tokens.isLeftParen(futureChar(offset))) return null;

			// Consume the function token.
			final String token = parseToken(length);

			// Consume the following whitespace and associated left parenthesis.
			parseWhitespace();
			pos.assertThat(Tokens.isLeftParen(parseLeftParen()),
				"Left parenthesis expected after function");

			return new Function(token);
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
			for (final Operator operator : operators) {
				final String symbol = operator.getToken();
				if (!expression.startsWith(symbol, pos.get())) continue;
				// Ensure the operator is appropriate to the current context.
				if (isPrefixOK() && operator.isPrefix() || //
					isPostfixOK() && operator.isPostfix() || //
					isInfixOK() && operator.isInfix())
				{
					pos.inc(symbol.length());
					return operator;
				}
			}
			return null;
		}

		/**
		 * Attempts to parse a comma character.
		 *
		 * @return The comma, or null if the next token is not one.
		 */
		public Character parseComma() {
			return parseChar(',');
		}

		/**
		 * Attempts to parse a left parenthesis character.
		 *
		 * @return The left parenthesis, or null if the next token is not one.
		 */
		public Character parseLeftParen() {
			return parseChar('(');
		}

		/**
		 * Attempts to parse a right parenthesis character.
		 *
		 * @return The right parenthesis, or null if the next token is not one.
		 */
		public Character parseRightParen() {
			return parseChar(')');
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

	}

}
