package lite.ast;

import lite.Interpreter;
import lite.LiteNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Index= a map/list/array
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIndexLet extends LiteNode {
    /**
     * Left hand side
     */
    public LiteNode lhs;

    /**
     * Index
     */
    public LiteNode index;

    /**
     * Value to set
     */
    public LiteNode value;

    /**
     * Blank constructor
     */
    public LiteIndexLet() {
    }

    /**
     * Quick constructor
     *
     * @param lhs left hand side
     * @param rhs right hand side(index)
     * @param val value to set
     */
    public LiteIndexLet(LiteNode lhs, LiteNode rhs, LiteNode val) {
        this.lhs = lhs;
        this.index = rhs;
        this.value = val;
    }

    /**
     * Gets the value of stabby expression
     *
     * @param context lite interpreter context
     * @return value of the expression
     */
    @Override
    public Object eval(Interpreter context) {
        // let's get lhs value first
        Object result = lhs.eval(context);
        Object rhs = index.eval(context);

        if (result instanceof Class) {
            // index is a static, or a call to setXXX
            Field f = null;
            try {
                f = ((Class) result).getDeclaredField(rhs.toString());
            } catch (NoSuchFieldException ignored) {
            }

            if (f != null) {
                f.setAccessible(true);
                try {
                    Object val = value.eval(context);
                    f.set(result, val);
                    return val;
                } catch (IllegalAccessException ignored) {
                }
            }

            Method m = null;

            try {
                m = ((Class<?>) result).getDeclaredMethod("set");
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    Object val = value.eval(context);
                    return m.invoke(result, rhs, val);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + "setter " + rhs + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        } else if (result instanceof Object[]) {
            // set array index
            Object val = value.eval(context);
            ((Object[]) result)[Integer.valueOf(rhs.toString())] = val;
            return val;
        } else if (result instanceof Map) {
            // set Map<String, ?>
            return ((Map) result).put(rhs, value.eval(context));
        } else if (result instanceof List) {
            // set List index
            return ((List) result).set(Integer.valueOf(rhs.toString()), value.eval(context));
        } else if (result != null) {
            // set Object field
            Field field = null;
            try {
                field = result.getClass().getField(rhs.toString());
            } catch (NoSuchFieldException ignored) {
            }
            if (!(field == null || field.isAccessible()))
                field.setAccessible(true); // :frog:
            try {
                if (field != null) {
                    Object val = value.eval(context);
                    field.set(result, val);
                    return val;
                } else return null;
            } catch (IllegalAccessException ignored) {
            }

            Method m = null;

            try {
                m = result.getClass().getMethod("set");
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result, rhs, value.eval(context));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + "setter " + rhs + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        }
        context.error("Failed to indexLet " + toString() + "|" + result);
        return null;
    }

    /**
     * @return lhs[index]=value
     */
    @Override
    public String toString() {
        return lhs + "[" + index + "]" + "=" + value;
    }
}
