package lite.ast.unop;

import lite.Interpreter;
import lite.LiteNode;

/**
 * Negative a object
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteNegative extends LiteNode {
    /**
     * Sub expr
     */
    public LiteNode expr;

    /**
     * The quick constructor
     *
     * @param expression expression to negative
     */
    public LiteNegative(LiteNode expression) {
        this.expr = expression;
    }

    /**
     * Eval this expression
     *
     * @param context lite interpreter context
     * @return Supported negative op (Number)
     */
    @Override
    public Object eval(Interpreter context) {
        Object value = expr.eval(context);
        if (!(value instanceof Number)) {
            context.error(expr + " Not a number in -: " + value);
            return null;
        }
        if (value instanceof Long)
            return -((Number) value).longValue();
        else if (value instanceof Integer)
            return -((Number) value).intValue();
        else if (value instanceof Float)
            return -((Number) value).floatValue();
        else if (value instanceof Byte)
            return -((Number) value).byteValue();
        else
            return -((Number) value).doubleValue();
    }

    /**
     * - $expr
     *
     * @return - expression string
     */
    @Override
    public String toString() {
        return '-' + expr.toString();
    }
}
