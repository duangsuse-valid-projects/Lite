package lite.ast.data;

import lite.Interpreter;
import lite.LiteNode;

/**
 * A simple object value
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteValue extends LiteNode {
    /**
     * The value
     */
    public Object value;

    /**
     * Fast setup constructor
     *
     * @param val the value
     */
    public LiteValue(Object val) {
        value = val;
    }

    /**
     * Return bundled value
     *
     * @param context lite interpreter context
     * @return the value
     */
    @Override
    public Object eval(Interpreter context) {
        return value; // just return bundled value
    }

    /**
     * Value to string
     *
     * @return Value string
     */
    @Override
    public String toString() {
        if (value == null)
            return "nil";
        if (value instanceof String)
            return '"' + value.toString() + '"';
        return value.toString();
    }
}
