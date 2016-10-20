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

/**
 * A {@link CharSequence} which is a by-reference subsequence of another
 * {@link CharSequence}. This is particularly useful for
 * {@link java.util.regex.Pattern regex} matching without excessive string
 * copying.
 * <p>
 * Surprisingly, core Java does not seem to have this capability (apart from
 * {@link javax.swing.text.Segment}, which seems misplaced in the Swing
 * library); all of {@link String#subSequence}, {@link StringBuffer#subSequence}
 * and {@link StringBuilder#subSequence} internally copy the requested string
 * segment.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SubSequence implements CharSequence {

	private final CharSequence seq;
	private final int offset;
	private final int length;

	public SubSequence(final CharSequence seq, final int offset) {
		this(seq, offset, seq.length() - offset);
	}

	private static void outOfBounds(final String message) {
		throw new IndexOutOfBoundsException(message);
	}

	public SubSequence(final CharSequence seq, final int offset, final int length)
	{
		if (offset < 0) outOfBounds("Offset " + offset + " < 0");
		if (offset > seq.length()) {
			outOfBounds("Offset " + offset + " > " + seq.length());
		}
		if (length < 0) outOfBounds("Length " + length + " < 0");
		if (offset + length > seq.length()) {
			outOfBounds("Offset " + offset + " + length " + length + " > " +
				seq.length());
		}
		this.seq = seq;
		this.offset = offset;
		this.length = length;
	}

	// -- CharSequence methods --

	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(final int index) {
		return seq.charAt(offset + index);
	}

	@Override
	public SubSequence subSequence(final int start, final int end) {
		return new SubSequence(seq, offset + start, end - start);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return seq.subSequence(offset, offset + length).toString();
	}

}
