package lite.ast.binop;

import lite.Interpreter;
import lite.LiteNode;

import java.util.List;
import java.util.Map;

/**
 * Lite in (as) expression
 * <p>
 * if "foo" in ![foo bar]
 * <p>  puts "foo in bar"
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIn extends LiteNode {
    /**
     * Left hand side object, may a string/character/object
     */
    public LiteNode lhs;

    /**
     * Right hand side object, may a string/list/map/class/array
     */
    public LiteNode rhs;

    /**
     * Run this in expression
     *
     * @param context lite interpreter context
     * @return true if lhs is in rhs (or lhs class is rhs), false otherwise
     */
    @Override
    public Object eval(Interpreter context) {
        Object lhsResult = lhs.eval(context);
        Object rhsResult = rhs.eval(context);
        // start
        if (rhsResult instanceof String) {
            if (lhsResult instanceof CharSequence) {
                return ((String) rhsResult).contains((CharSequence) rhsResult);
            }
        } else if (rhsResult instanceof List) {
            return ((List) rhsResult).contains(lhsResult);
        } else if (rhsResult instanceof Map) {
            return ((Map) rhsResult).containsKey(lhsResult);
        } else if (rhsResult instanceof Class) {
            return lhsResult.getClass().equals(rhsResult); // 1 in Integer
        } else if (rhsResult instanceof Object[]) {
            for (Object i : (Object[]) rhsResult)
                if (i.equals(lhsResult))
                    return true;
            return false;
        }
        context.error("Failed to in: " + toString() + " on " + rhsResult);
        return false;
    }

    /**
     * Get the code to the node
     *
     * @return always lhs in rhs
     */
    @Override
    public String toString() {
        return lhs + " in " + rhs;
    }
}
