package lite.ast;

import lite.Interpreter;
import lite.LiteNode;

/**
 * a = 1
 * '@a = 1
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteAssignment extends LiteNode {
    /**
     * The identifier a = 1 @a = 1
     */
    public LiteIdentifier ident;

    /**
     * The expr a = System.gc
     */
    public LiteNode value;

    /**
     * Blank constructor
     */
    public LiteAssignment() {
    }

    /**
     * The quick constructor
     *
     * @param identifier identifier
     * @param val        value node
     */
    public LiteAssignment(LiteIdentifier identifier, LiteNode val) {
        this.ident = identifier;
        value = val;
    }

    /**
     * Sets the identifier value
     *
     * @param context lite interpreter context
     * @return rhs
     */
    @Override
    public Object eval(Interpreter context) {
        Object rhs = value.eval(context);
        ident.set(context, rhs);
        return rhs;
    }

    /**
     * identifier = value
     */
    @Override
    public String toString() {
        return ident + " = " + value;
    }
}
