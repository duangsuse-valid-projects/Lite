package lite.ast.binop;

import lite.Interpreter;
import lite.LiteNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Stabby operator for lite language
 * <p>
 * {@code lhs 可以是 Java类, HashMap, Object, Array
 * 当 lhs 为 Java 类/对象 时:
 * 先尝试设置对应 field
 * 如果没有或 reflect 失败, 使用 setXxx
 * 如果没有, 错误
 * <p>
 * HashMap 时, 用 rhs index-let lhs
 * 如果失败错误, 如果无 index, 返回 null}
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteStabby extends LiteNode {
    /**
     * Left hand side
     */
    public LiteNode lhs;

    /**
     * Right hand side
     */
    public String rhs;

    /**
     * Value to set
     */
    public LiteNode value;

    /**
     * Blank constructor
     */
    public LiteStabby() {
    }

    /**
     * Quick constructor
     *
     * @param lhs left hand side
     * @param rhs right hand side
     * @param val value to set
     */
    public LiteStabby(LiteNode lhs, String rhs, LiteNode val) {
        this.lhs = lhs;
        this.rhs = rhs;
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
        if (result instanceof Class) {
            // index is a static, or a call to setXXX
            Field f = null;
            try {
                f = ((Class) result).getDeclaredField(rhs);
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
            StringBuilder name = new StringBuilder(Character.toString(rhs.charAt(0)).toUpperCase());
            for (int i = 1; i < rhs.length(); i++)
                name.append(rhs.charAt(i));

            try {
                m = ((Class<?>) result).getDeclaredMethod("set" + name.toString());
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    Object val = value.eval(context);
                    return m.invoke(result, val);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + name + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        } else if (result instanceof Object[]) {
            // set array index
            Object val = value.eval(context);
            ((Object[]) result)[Integer.valueOf(rhs)] = val;
            return val;
        } else if (result instanceof Map) {
            // set Map<String, ?>
            return ((Map) result).put(rhs, value.eval(context));
        } else if (result instanceof List) {
            // set List index
            return ((List) result).set(Integer.valueOf(rhs), value.eval(context));
        } else if (result != null) {
            // set Object field
            Field field = null;
            try {
                field = result.getClass().getField(rhs);
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
            StringBuilder name = new StringBuilder(Character.toString(rhs.charAt(0)).toUpperCase());
            for (int i = 1; i < rhs.length(); i++)
                name.append(rhs.charAt(i));

            try {
                m = result.getClass().getMethod("set" + name.toString());
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result, value.eval(context));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + name + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        }
        context.error("Failed to stabby " + toString() + ">" + result);
        return null;
    }

    /**
     * @return lhs->rhs value
     */
    @Override
    public String toString() {
        return lhs + "->" + rhs + ' ' + value;
    }
}
