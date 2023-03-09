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

package org.scijava.parsington;

/**
 * A mutable parse position. Similar to {@link java.text.ParsePosition}, but
 * less complex.
 *
 * @author Curtis Rueden
 */
public class Position {

	private int index;

	public int get() {
		return index;
	}

	public void set(final int index) {
		this.index = index;
	}

	public void inc() {
		inc(1);
	}

	public void inc(final int count) {
		index += count;
	}

	public char ch(final CharSequence s) {
		return ch(s, 0);
	}

	public char ch(final CharSequence s, final int offset) {
		final int i = get() + offset;
		return i < s.length() ? s.charAt(i) : '\0';
	}

	/**
	 * Throws an exception with an informative message. Called by the parsing
	 * infrastructure when syntax is incorrect.
	 * 
	 * @param message The text to use as a basis for the error message.
	 * @throws IllegalArgumentException Always.
	 */
	public void die(final String message) {
		throw new IllegalArgumentException(messageWithDetails(message));
	}

	/**
	 * Calls {@link #fail(String)} if a condition is not met. Called by the
	 * parsing infrastructure to assert that things are going OK.
	 * 
	 * @param condition If false, throw the exception.
	 * @param message The text to use as a basis for the error message.
	 * @throws IllegalStateException If the condition is not met.
	 */
	public void assertThat(final boolean condition, final String message) {
		if (condition) return;
		fail(message);
	}

	/**
	 * Throws an exception. Called by the parsing infrastructure when something
	 * goes wrong.
	 * 
	 * @param message The text to use as a basis for the error message.
	 * @throws IllegalStateException Always.
	 */
	public void fail(final String message) {
		throw new IllegalStateException(messageWithDetails(message));
	}

	// -- Object methods --

	@Override
	public String toString() {
		return "" + get();
	}

	// -- Helper methods --

	private String messageWithDetails(final String message) {
		return message + " at index " + get();
	}

}
