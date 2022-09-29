/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2022 Board of Regents of the University of
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.scijava.parsington.Operator.Associativity;
import org.scijava.parsington.eval.DefaultTreeEvaluator;
import org.scijava.parsington.eval.Evaluator;

/**
 * Working examples of Parsington in action.
 *
 * @author Curtis Rueden
 */
public class TestExamples extends AbstractTest {

	/**
	 * An example of the expression parser with the standard operators. See also
	 * {@link ExpressionParserTest} for many more simple examples.
	 */
	@Test
	public void standardOperators() {
		final ExpressionParser parser = new ExpressionParser();
		final String expression = "(-b + sqrt(b^2 - 4*a*c)) / (2*a)";
		final SyntaxTree quadraticFormula = parser.parseTree(expression);

		// Traverse the syntax tree to see if the nodes match what we expect.

		// The top level of the tree is a fraction.
		assertSame(Operators.DIV, quadraticFormula.token());
		assertBinary(quadraticFormula);
		final SyntaxTree numer = quadraticFormula.child(0);
		final SyntaxTree denom = quadraticFormula.child(1);

		// Because the numerator is in parentheses, it is parsed as a unary group.
		assertUnary(numer);
		assertGroup(Operators.PARENS, 1, numer.token());

		// The numerator (inside the parentheses) is the sum of two expressions.
		final SyntaxTree innerNumer = numer.child(0);
		assertSame(Operators.ADD, innerNumer.token());
		assertBinary(innerNumer);

		// The numerator's first expression, -b, is a unary negation.
		final SyntaxTree minusB = innerNumer.child(0);
		assertSame(Operators.NEG, minusB.token());
		assertUnary(minusB);
		assertVariable("b", minusB.child(0).token());

		// The numerator's second expression, sqrt(b^2 - 4*a*c), is a function call.
		final SyntaxTree sqrtFn = innerNumer.child(1);
		assertFunction(sqrtFn.token());
		assertBinary(sqrtFn);
		final SyntaxTree sqrtVar = sqrtFn.child(0);
		assertVariable("sqrt", sqrtVar.token());

		// The sqrt function is applied to a unary group.
		final SyntaxTree sqrtTarget = sqrtFn.child(1);
		assertUnary(sqrtTarget);
		assertGroup(Operators.PARENS, 1, sqrtTarget.token());

		// The expression inside the sqrt function has two terms, subtracted.
		final SyntaxTree sqrtExpr = sqrtTarget.child(0);
		assertSame(Operators.SUB, sqrtExpr.token());
		assertBinary(sqrtExpr);

		// The first term, b^2, is exponentiation.
		final SyntaxTree bSquared = sqrtExpr.child(0);
		assertSame(Operators.POW, bSquared.token());
		assertBinary(bSquared);
		assertVariable("b", bSquared.child(0).token());
		assertEquals(2, bSquared.child(1).token());

		// The second term, 4*a*c, goes one level deeper.
		final SyntaxTree fourAC = sqrtExpr.child(1);
		assertSame(Operators.MUL, fourAC.token());
		assertBinary(fourAC);
		final SyntaxTree fourA = fourAC.child(0);
		assertSame(Operators.MUL, fourA.token());
		assertBinary(fourA);
		assertEquals(4, fourA.child(0).token());
		assertVariable("a", fourA.child(1).token());
		assertVariable("c", fourAC.child(1).token());

		// Because the denominator is in parentheses, it is parsed as a unary group.
		assertUnary(denom);
		assertGroup(Operators.PARENS, 1, denom.token());

		// The denominator (inside the parentheses) is a product of two terms.
		final SyntaxTree innerDenom = denom.child(0);
		assertBinary(innerDenom);
		assertSame(Operators.MUL, innerDenom.token());
		assertEquals(2, innerDenom.child(0).token());
		assertVariable("a", innerDenom.child(1).token());

		// Whew! That's the whole tree!
	}

	// -- Customizing the parser --

	/** An example of custom operators approximating some POSIX shell syntax. */
	@Test
	public void posixShellSyntax() {
		final Operator substringLeft = new Operator("%", 2, Associativity.LEFT, 10);
		final Operator substringRight = new Operator("#", 2, Associativity.LEFT, 10);
		final Operator substringLeftGreedy = new Operator("%%", 2, Associativity.LEFT, 10);
		final Operator substringRightGreedy = new Operator("##", 2, Associativity.LEFT, 10);
		final List<Operator> operators = Arrays.asList(Operators.ASSIGN, Operators.BRACES,
			substringLeft, substringRight, substringLeftGreedy, substringRightGreedy);

		final ExpressionParser parser = new ExpressionParser(operators);
		final LinkedList<Object> queue = parser.parsePostfix(
			"logpath='/var/log/syslog'; dir={logpath%'/*'}; name={logpath##'*/'}");

		// logpath "/var/log/syslog" = dir logpath "/*" % {1} = name logpath "*/" ## {1} =
		assertNotNull(queue);
		assertEquals(15, queue.size());
		assertVariable("logpath", queue.pop());
		assertString("/var/log/syslog", queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("dir", queue.pop());
		assertVariable("logpath", queue.pop());
		assertString("/*", queue.pop());
		assertSame(substringLeft, queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("name", queue.pop());
		assertVariable("logpath", queue.pop());
		assertString("*/", queue.pop());
		assertSame(substringRightGreedy, queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
	}

	/** An example which parses all literals as {@link String}s. */
	@Test
	public void parseLiteralsAsStrings() {
		final ExpressionParser parser = new ExpressionParser( //
			(p, expression) -> new ParseOperation(p, expression)
			{
				@Override
				protected Object parseLiteral() {
					// No variables! Treat all identifiers as literal strings.
					final int length = parseIdentifier();
					return length == 0 ? null : parseToken(length);
				}
			});

		final LinkedList<Object> queue = parser.parsePostfix(
			"quick && brown && fox || lazy && dog");

		// quick brown && fox && lazy dog && ||
		assertNotNull(queue);
		assertEquals(Arrays.asList("quick", "brown", Operators.LOGICAL_AND, "fox",
			Operators.LOGICAL_AND, "lazy", "dog", Operators.LOGICAL_AND,
			Operators.LOGICAL_OR), queue);
	}

	@Test
	public void allowDotsInVariableNames() {
		// Create a parser that allows dot characters as part of identifiers.
		//
		// NB: In this example:
		// - The dot character is allowed WITHIN an identifier, but not STARTING it.
		// - Multiple dot characters in a row are allowed. To allow only single dot
		//   characters in a row, you would need to override the parseIdentifier
		//   method instead to be more intelligent. A regex might come in handy.
		final ExpressionParser parser = new ExpressionParser( //
			(p, expression) -> new ParseOperation(p, expression)
			{
				@Override
				protected boolean isIdentifierPart(char c) {
					return c == '.' || super.isIdentifierPart(c);
				}
			});

		final LinkedList<Object> queue = parser.parsePostfix(
			"shape.length * shape.width");

		// shape.length shape.width *
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertVariable("shape.length", queue.pop());
		assertVariable("shape.width", queue.pop());
		assertSame(Operators.MUL, queue.pop());
	}

	@Test
	public void allowLeadingUnderscoreInVariableNames() {
		// Create a parser that allows variables to start with an underscore.
		final ExpressionParser parser = new ExpressionParser( //
			(p, expression) -> new ParseOperation(p, expression)
			{
				@Override
				protected boolean isIdentifierStart(char c) {
					return c == '_' || super.isIdentifierStart(c);
				}
			});

		final LinkedList<Object> queue = parser.parsePostfix("_x / _y");

		// _x _y *
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertVariable("_x", queue.pop());
		assertVariable("_y", queue.pop());
		assertSame(Operators.DIV, queue.pop());
	}

	@Test
	public void expressionsEnclosedInBracketsAreVariables() {
		// Create a parser that treats anything in square brackets as a variable.

		// Create the list of available operators... but WITHOUT square brackets.
		// Otherwise, the square bracket operator will influence the parsing.
		final List<Operator> operators = Operators.standardList();
		operators.remove(Operators.BRACKETS);

		// Now create the parser, with overridden identifier parsing logic.
		final ExpressionParser parser = new ExpressionParser(operators, ",", ";", //
			(p, expression) -> new ParseOperation(p, expression)
			{
				@Override
				protected int parseIdentifier() {
					// Only accept an identifier in the appropriate context.
					if (infix) return 0;

					// Check for special bracketed variable syntax.
					// A variable can be *anything* enclosed in brackets!
					if (currentChar() == '[') {
						int length = 1;
						while (true) {
							final char next = futureChar(length++);
							if (next == '\0') return 0;
							if (next == ']') return length;
						}
					}

					// Check for the usual identifier syntax.
					return super.parseIdentifier();
				}
			});

		final LinkedList<Object> queue = parser.parsePostfix(
			"2 * [check it out: aa.bb + cc.dd] / ([z-y-x] + x)");

		// 2 "[check it out: aa.bb + cc.dd]" * "[z-y-x]" x + (1) /
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertEquals(2, queue.pop());
		assertVariable("[check it out: aa.bb + cc.dd]", queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertVariable("[z-y-x]", queue.pop());
		assertVariable("x", queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.DIV, queue.pop());
	}

	// -- Customizing the evaluator --

	@Test
	public void dotOperatorJoinsVariables() {
		// Create an evaluator whose dot operator implementation joins variables
		// into a longer-named variable.
		final Evaluator e = new DefaultTreeEvaluator() {
			@Override
			public Object dot(Object a, Object b) {
				if (a instanceof Variable && b instanceof Variable) {
					Variable va = (Variable) a;
					Variable vb = (Variable) b;
					return new Variable(va.getToken() + "." + vb.getToken());
				}
				return super.dot(a, b);
			}
		};

		// Evaluate an expression with dotted variable names.
		e.set(new Variable("shape.length"), 13);
		e.set(new Variable("shape.width"), 17);
		final Object result = e.evaluate("shape.length * shape.width");
		final int expected = 13 * 17;
		assertEquals(expected, result);
	}

	@Test
	public void dollarSignPrefixedVariablesAreSpecial() {
		// Create an expression parser with an additional $ unary operator.
		final List<Operator> operators = new ArrayList<>();
		operators.addAll(Operators.standardList());
		final Operator dollar = new Operator("$", 1, Associativity.RIGHT, 100);
		operators.add(dollar);
		final ExpressionParser parser = new ExpressionParser(operators);

		// Create an evaluator that replaces $-prefixed variables
		// with environment variables from the system.
		final Evaluator e = new DefaultTreeEvaluator(parser) {
			@Override
			public Object execute(final Operator op, final SyntaxTree tree) {
				if (op == dollar) {
					assert tree.count() == 1;
					final Variable v = var(evaluate(tree.child(0)));
					final String value = System.getenv(v.getToken());
					return value == null ? "" : value; // Treat undefined as empty string.
				}
				return super.execute(op, tree);
			}
		};

		// Evaluate an expression using the ubiquitous $PATH.
		final Object result = e.evaluate("'/etc:' + $PATH + ':/var/tmp'");
		final String expected = "/etc:" + System.getenv("PATH") + ":/var/tmp";
		assertEquals(expected, result);
	}

}
