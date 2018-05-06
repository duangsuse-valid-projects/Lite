package lite.ast;

import lite.Interpreter;
import lite.LiteNode;

/**
 * Lite identifier
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIdentifier extends LiteNode {
    /**
     * is local? (starts with @)
     */
    public boolean isLocalIdentifier;

    /**
     * Identifier string
     */
    public String identifier;

    /**
     * Blank constructor
     */
    public LiteIdentifier() {
    }

    /**
     * Fast constructor
     *
     * @param isLocal is local variable
     * @param ident   string ident
     */
    public LiteIdentifier(boolean isLocal, String ident) {
        this.isLocalIdentifier = isLocal;
        this.identifier = ident;
    }

    /**
     * Resolve to object (getter)
     *
     * @param context lite interpreter context
     * @return global if not local identifier, or java class
     */
    @Override
    public Object eval(Interpreter context) {
        if (isLocalIdentifier)
            return context.scopeGet(identifier);
        Object tmp = context.get(identifier);
        if (tmp != null)
            return tmp;
        else // is java class
            return context.getJava(identifier);
    }

    /**
     * Set value using this identifier
     *
     * @param context interpreter
     * @param value   the value
     */
    public void set(Interpreter context, Object value) {
        if (isLocalIdentifier)
            context.scopePut(identifier, value);
        else
            context.set(identifier, value);
    }

    /**
     * '@local' global
     *
     * @return '@identifier' if is local
     */
    @Override
    public String toString() {
        return isLocalIdentifier ? '@' + identifier : identifier;
    }
}
