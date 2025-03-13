package rhino.javascript.xml;

import rhino.javascript.ScriptableObject;

/** This interface is used to load the XML implementation using the ServiceLoader pattern. */
public interface XMLLoader {
    void load(ScriptableObject scope, boolean sealed);

    XMLLib.Factory getFactory();
}
