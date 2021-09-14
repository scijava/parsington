package org.scijava.parsington.parser;

import org.scijava.parsington.ParserChain;
import org.scijava.parsington.Position;

import java.util.ArrayList;
import java.util.List;

public class ParserChainImpl implements ParserChain {

    List<Parser> parserChain = new ArrayList<Parser>();

    private int positionInChain = 0;

    @Override
    public Object doParser(CharSequence s, Position pos) {
        if (positionInChain < parserChain.size()) {
            Parser nextParser = parserChain.get(positionInChain);
            positionInChain++;
            Object result = nextParser.doParse(s, pos, this);
            return result;
        }

        return null;
    }

    private void reset() {
        positionInChain = 0;
    }

    public ParserChainImpl then(Parser parser) {
        parserChain.add(parser);
        return this;
    }

    @Override
    public ParserChain fromStart() {
        reset();
        return this;
    }

}
