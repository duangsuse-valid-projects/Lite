package lite.ast.misc;

import lite.Interpreter;
import lite.LiteNode;

/**
 * Lite return statement
 * <p>
 * return expr
 *
 * @author duangsuse
 * @see lite.LiteBlock
 * @since 1.0
 */
public class LiteReturn extends LiteNode {
    /**
     * The returned expression
     */
    public LiteNode expr;

    /**
     * Quick constructor
     *
     * @param expression the returned expression
     */
    public LiteReturn(LiteNode expression) {
        expr = expression;
    }

    /**
     * Calculate the expression value
     *
     * @param context lite interpreter context
     * @return sub-expression value
     */
    @Override
    public Object eval(Interpreter context) {
        // just return returned expr value
        return expr.eval(context);
    }

    /**
     * Describe this expression
     *
     * @return always return $expr
     */
    @Override
    public String toString() {
        return "return " + expr.toString();
    }
}
