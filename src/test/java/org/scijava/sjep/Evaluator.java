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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A toy expression evaluator using primitive doubles.
 *
 * @author Curtis Rueden
 */
public class Evaluator {

	private static final String PROMPT = "> ";

	private static final String[] ARY = { "nullary", "unary", "binary",
		"ternary", "quaternary", "quinary", "senary", "septenary", "octary",
		"nonary" };

	private static String ary(final int arity) {
		return arity < ARY.length ? ARY[arity] : arity + "-ary";
	}

	private final HashMap<String, Double> vars = new HashMap<String, Double>();
	private final ExpressionParser parser;

	public Evaluator() {
		this(new ExpressionParser());
	}

	public Evaluator(final ExpressionParser parser) {
		this.parser = parser;
	}

	public double evaluate(final String line) {
		// Parse "var = expression" syntax.
		final int equals = line.indexOf("=");
		final String var, expression;
		if (equals >= 0) {
			var = line.substring(0, equals).trim();
			expression = line.substring(equals + 1);
		}
		else {
			var = "";
			expression = line;
		}

		// Convert the expression to postfix.
		final LinkedList<Object> queue = parser.parsePostfix(expression);

		// Process the postfix token queue.
		final Deque<Double> stack = new ArrayDeque<Double>();
		while (!queue.isEmpty()) {
			final Object token = queue.removeFirst();
			if (Tokens.isOperator(token)) {
				execute((Operator) token, stack);
			}
			else if (Tokens.isFunction(token)) {
				throw new IllegalArgumentException("Unsupported function: " + token);
			}
			else if (Tokens.isNumber(token)) {
				final double value = ((Number) token).doubleValue();
				stack.push(value);
			}
			else if (Tokens.isVariable(token)) {
				final String tokenName = ((Variable) token).getToken();
				if (!vars.containsKey(tokenName)) {
					throw new IllegalArgumentException("Unknown variable: " + tokenName);
				}
				stack.push(vars.get(tokenName));
			}
			else {
				throw new IllegalArgumentException("Unknown token type: " + token +
					" [" + token.getClass().getName() + "]");
			}
		}

		if (stack.size() != 1) {
			throw new IllegalArgumentException("Expected one result but got " +
				stack.size());
		}

		final double result = stack.pop();
		vars.put(var, result);
		return result;
	}

	private void execute(final Operator operator, final Deque<Double> stack) {
		final int arity = operator.getArity();

		Double result = null;
		if (arity == 1) {
			final double a = stack.pop();
			if (operator == Operators.NEG) result = -a;
		}
		else if (arity == 2) {
			final double b = stack.pop();
			final double a = stack.pop();
			if (operator == Operators.ADD) result = a + b;
			else if (operator == Operators.SUB) result = a - b;
			else if (operator == Operators.MUL) result = a * b;
			else if (operator == Operators.DIV) result = a / b;
			else if (operator == Operators.POW) result = Math.pow(a, b);
		}
		if (result == null) {
			final String opType = operator.getClass().getSimpleName().toLowerCase();
			throw new IllegalArgumentException("Unsupported " + ary(arity) + " " +
				opType + ": " + operator.getToken());
		}
		stack.push(result);
	}

	public static void main(final String[] args) throws IOException {
		final Evaluator evaluator = new Evaluator();
		final BufferedReader in =
			new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print(PROMPT);
			final String line = in.readLine();
			if (line == null) break;
			try {
				print(evaluator.evaluate(line));
			}
			catch (final RuntimeException exc) {
				final String msg = exc.getMessage();
				if (msg == null) throw exc; // Probably a serious exception.
				final Matcher m = Pattern.compile(".* at index (\\d+)").matcher(msg);
				if (m.matches()) {
					// Show a helpful caret to indicate where the problem is.
					final int index = Integer.parseInt(m.group(1));
					print(caret(index));
				}
				print(msg);
			}
		}
	}

	private static String caret(final int index) {
		final StringBuilder sb = new StringBuilder();
		final int count = PROMPT.length() + index;
		for (int i = 0; i < count; i++) {
			sb.append(" ");
		}
		sb.append("^");
		return sb.toString();
	}

	private static void print(final Object o) {
		System.out.println(o);
	}

}
