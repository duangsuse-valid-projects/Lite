package lite.ast.unop;

import lite.Interpreter;
import lite.LiteNode;

/**
 * Unary not
 * not true
 * !true
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteNot extends LiteNode {
    /**
     * The sub-expression
     */
    public LiteNode expr;

    /**
     * Quick construct
     *
     * @param expression expression to not op
     */
    public LiteNot(LiteNode expression) {
        this.expr = expression;
    }

    /**
     * Calculate this expression !true !false....
     *
     * @param context lite interpreter context
     * @return true/false or null if cannot convert
     */
    @Override
    public Object eval(Interpreter context) {
        Object value = expr.eval(context);
        if (!(value instanceof Boolean)) {
            context.error(expr + " Not a boolean, it's " + value.toString());
            return null;
        }
        return !(Boolean) value;
    }

    /**
     * Describe this expression
     *
     * @return always !expr
     */
    @Override
    public String toString() {
        return '!' + expr.toString();
    }
}
