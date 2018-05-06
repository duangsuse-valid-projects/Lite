package lite.ast.binop;

import lite.Interpreter;
import lite.LiteNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Square operator for Lite
 * <p>
 * {@code lhs 可以是 Java类, Map, Object, Array, List
 * 当 lhs 为 Java 类/对象 时:
 * 先尝试取出对应 field/subclass
 * 如果没有或 reflect 失败, 使用 getXxx
 * 如果没有, 错误
 * <p>
 * HashMap 时, 用 rhs index lhs
 * 如果失败错误, 如果无 index, 返回 null}
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteSquare extends LiteNode {
    /**
     * Left hand side node
     */
    public LiteNode lhs;

    /**
     * The string index
     */
    public String rhs;

    /**
     * Blank constructor
     */
    public LiteSquare() {
    }

    /**
     * The quick constructor
     *
     * @param nd    node
     * @param index index string
     */
    public LiteSquare(LiteNode nd, String index) {
        lhs = nd;
        rhs = index;
    }

    /**
     * Index a Java class/Object/Array/Map/List
     *
     * @param context lite interpreter context
     * @return indexed value
     */
    @Override
    public Object eval(Interpreter context) {
        Object result = lhs.eval(context);
        if (result instanceof Class) {
            // index a sub class, or a static, or a call to getXXX
            Class[] subClasses;
            subClasses = ((Class) result).getDeclaredClasses();
            for (Class c : subClasses) {
                if (c.getSimpleName().equals(rhs))
                    return c;
            }

            Field f = null;
            try {
                f = ((Class) result).getDeclaredField(rhs);
            } catch (NoSuchFieldException ignored) {
            }

            if (f != null) {
                f.setAccessible(true);
                try {
                    return f.get(result);
                } catch (IllegalAccessException ignored) {
                }
            }

            Method m = null;
            StringBuilder name = new StringBuilder(Character.toString(rhs.charAt(0)).toUpperCase());
            for (int i = 1; i < rhs.length(); i++)
                name.append(rhs.charAt(i));

            try {
                m = ((Class<?>) result).getDeclaredMethod("get" + name.toString());
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + name + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        } else if (result instanceof Object[]) {
            // get array index
            return ((Object[]) result)[Integer.valueOf(rhs)];
        } else if (result instanceof Map) {
            // get Map<String, ?>
            return ((Map) result).get(rhs);
        } else if (result instanceof List) {
            // get List index
            return ((List) result).get(Integer.valueOf(rhs));
        } else if (result != null) {
            // get Object field
            Field field = null;
            try {
                field = result.getClass().getField(rhs);
            } catch (NoSuchFieldException ignored) {
            }
            if (!(field == null || field.isAccessible()))
                field.setAccessible(true); // :frog:
            try {
                return field != null ? field.get(result) : null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            Method m = null;
            StringBuilder name = new StringBuilder(Character.toString(rhs.charAt(0)).toUpperCase());
            for (int i = 1; i < rhs.length(); i++)
                name.append(rhs.charAt(i));

            try {
                m = result.getClass().getMethod("get" + name.toString());
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + name + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        }
        context.error("Failed to square " + toString() + ")" + result);
        return null;
    }

    /**
     * @return lhs::rhs
     */
    @Override
    public String toString() {
        return lhs + "::" + rhs;
    }
}
