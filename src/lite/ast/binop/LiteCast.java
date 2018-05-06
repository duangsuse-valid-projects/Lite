package lite.ast.binop;

import lite.Interpreter;
import lite.LiteNode;
import lite.ast.LiteIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Lite type cast expression
 * <p>
 * Byte(1) as Integer
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteCast extends LiteNode {
    /**
     * Left hand side expression (object)
     */
    public LiteNode expr;

    /**
     * Class to cast
     */
    public LiteIdentifier classToCast;

    /**
     * Cast the object to type
     *
     * @param context lite interpreter context
     * @return casted object or original object if failed
     */
    @Override
    public Object eval(Interpreter context) {
        Object rhsResult = classToCast.eval(context); // let's get the class to cast first
        Object lhsResult = expr.eval(context);
        if (!(rhsResult instanceof Class)) {
            context.error(classToCast + " not a Class: " + rhsResult);
            return lhsResult;
        } else { // rhs is class
            String className = ((Class) rhsResult).getSimpleName();
            Method toMethod = null;
            try {
                toMethod = lhsResult.getClass().getMethod("to" + className);
            } catch (NoSuchMethodException ignored) {
            }
            if (toMethod != null) {
                try {
                    return toMethod.invoke(lhsResult);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call caster to" + className + " on " + lhsResult);
                    return lhsResult;
                }
            }
            return ((Class) rhsResult).cast(rhsResult);
        }
    }

    /**
     * Get the code to the node
     *
     * @return always foo as Bar
     */
    @Override
    public String toString() {
        return expr + " as " + classToCast;
    }
}
