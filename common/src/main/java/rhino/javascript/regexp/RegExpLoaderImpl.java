package rhino.javascript.regexp;

import rhino.javascript.RegExpLoader;
import rhino.javascript.RegExpProxy;

/** This class loads the default RegExp implementation. */
public class RegExpLoaderImpl implements RegExpLoader {
    @Override
    public RegExpProxy newProxy() {
        return new RegExpImpl();
    }
}
