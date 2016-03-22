/*
 * #%L
 * SciJava mathematical expression parser.
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

package org.scijava.sjep.eval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.sjep.Tokens;

/**
 * A simple console-driven expression evaluator.
 *
 * @author Curtis Rueden
 */
public class EvaluatorConsole {

	private static final String PROMPT = "> ";

	private final Evaluator evaluator;

	public EvaluatorConsole() {
		this(new DefaultEvaluator());
	}

	public EvaluatorConsole(final Evaluator evaluator) {
		this.evaluator = evaluator;
	}

	// -- EvaluatorConsole methods --

	public void showConsole() throws IOException {
		showConsole(new BufferedReader(new InputStreamReader(System.in)));
	}

	public void showConsole(final BufferedReader in) throws IOException {
		while (true) {
			print(PROMPT);
			final String line = in.readLine();
			if (line == null) break;
			try {
				final Object result = evaluator.evaluate(line);
				if (result != null) printResult(result);
			}
			catch (final IllegalArgumentException exc) {
				final String msg = exc.getMessage();
				if (msg == null) throw exc; // Probably a serious exception.
				final Matcher m = Pattern.compile(".* at index (\\d+)").matcher(msg);
				if (m.matches()) {
					// Show a helpful caret to indicate where the problem is.
					final int index = Integer.parseInt(m.group(1));
					println(caret(index));
				}
				println(msg);
			}
		}
	}

	private void printResult(final Object o) {
		if (o instanceof List) {
			for (final Object item : (List<?>) o) {
				printResult(item);
			}
		}
		else if (Tokens.isVariable(o)) {
			println(o + " = " + evaluator.value(o));
		}
		else println(o);
	}

	public void print(final Object o) {
		System.out.print(o);
	}

	public void println(final Object o) {
		System.out.println(o);
	}

	// -- Helper methods --

	private static String caret(final int index) {
		final StringBuilder sb = new StringBuilder();
		final int count = PROMPT.length() + index;
		for (int i = 0; i < count; i++) {
			sb.append(" ");
		}
		sb.append("^");
		return sb.toString();
	}

}
