package lite.ast.unop;

import lite.Interpreter;
import lite.LiteNode;
import lite.ast.LiteIdentifier;

/**
 * Lite ++ --
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIncDec extends LiteNode {
    /**
     * Operator operand
     */
    public LiteIdentifier ident;

    /**
     * is --?
     */
    public boolean isDec;

    /**
     * Blank constructor
     */
    public LiteIncDec() {
    }

    /**
     * Quick constructor
     *
     * @param identifier identifier
     * @param dec        decrease?
     */
    public LiteIncDec(LiteIdentifier identifier, boolean dec) {
        ident = identifier;
        isDec = dec;
    }

    /**
     * Preform inc/dec operation
     *
     * @param context lite interpreter context
     * @return operation result
     */
    @Override
    public Object eval(Interpreter context) {
        Object val = ident.eval(context);
        if (val instanceof Integer)
            ident.set(context, isDec ? (Integer) val - 1 : (Integer) val + 1);
        return val;
    }

    /**
     * @return ident++/ident--
     */
    @Override
    public String toString() {
        return ident.toString() + (isDec ? "-" : "+");
    }
}
