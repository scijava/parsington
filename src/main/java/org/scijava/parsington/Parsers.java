package org.scijava.parsington;

import org.scijava.parsington.parser.BooleanParser;
import org.scijava.parsington.parser.NumberParser;
import org.scijava.parsington.parser.ParserChainImpl;
import org.scijava.parsington.parser.StringParser;

public class Parsers {

    public static ParserChain standardChain() {

        ParserChainImpl parserChain = new ParserChainImpl();
        parserChain.
                then(new BooleanParser()).
                then(new StringParser()).
                then(new NumberParser());
        return parserChain;
    }

}
