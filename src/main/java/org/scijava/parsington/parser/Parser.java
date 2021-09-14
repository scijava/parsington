package org.scijava.parsington.parser;

import org.scijava.parsington.ParserChain;
import org.scijava.parsington.Position;

public interface Parser {

    Object doParse(final CharSequence s, final Position pos, ParserChain parserChain);

}
