package org.scijava.parsington;

public interface ParserChain {

    Object doParser(CharSequence s, Position pos);

    ParserChain fromStart();
}
