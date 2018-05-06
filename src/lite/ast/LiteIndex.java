package lite.ast;

import lite.Interpreter;
import lite.LiteNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Index a map/list/array
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIndex extends LiteNode {
    /**
     * Left hand side node
     */
    public LiteNode lhs;

    /**
     * The index node
     */
    public LiteNode index;

    /**
     * Blank constructor
     */
    public LiteIndex() {
    }

    /**
     * The quick constructor
     *
     * @param nd    node
     * @param index index node
     */
    public LiteIndex(LiteNode nd, LiteNode index) {
        lhs = nd;
        this.index = index;
    }

    /**
     * Index a Java class/Object/Array/Map/List
     *
     * @param context lite interpreter context
     * @return indexed value
     */
    // duangsuse 说这是一种「类联」优化, Square Operator 可以不用调用自己就能工作...
    // ^ 上面的偷懒还嘴硬, 复制粘贴就复制粘贴
    @Override
    public Object eval(Interpreter context) {
        Object result = lhs.eval(context);
        Object idx = index.eval(context);

        if (result instanceof Class) {
            // index a sub class, or a static, or a call to getXXX
            Class[] subClasses;
            subClasses = ((Class) result).getDeclaredClasses();
            for (Class c : subClasses) {
                if (c.getSimpleName().equals(idx.toString()))
                    return c;
            }

            Field f = null;
            try {
                f = ((Class) result).getDeclaredField(idx.toString());
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

            try {
                m = ((Class<?>) result).getDeclaredMethod("get");
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result, idx);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + "getter " + idx + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        } else if (result instanceof Object[]) {
            // get array index
            return ((Object[]) result)[Integer.valueOf(idx.toString())];
        } else if (result instanceof Map) {
            // get Map<String, ?>
            return ((Map) result).get(idx);
        } else if (result instanceof List) {
            // get List index
            return ((List) result).get(Integer.valueOf(idx.toString()));
        } else if (result != null) {
            // get Object field
            Field field = null;
            try {
                field = result.getClass().getField(idx.toString());
            } catch (NoSuchFieldException ignored) {
            }
            if (!(field == null || field.isAccessible()))
                field.setAccessible(true); // :frog:
            try {
                return field != null ? field.get(result) : null;
            } catch (IllegalAccessException ignored) {
            }

            Method m = null;

            try {
                m = result.getClass().getMethod("get");
            } catch (NoSuchMethodException ignored) {
            }

            if (m != null) {
                try {
                    return m.invoke(result, idx);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    context.error("Failed to call " + "getter " + idx + " on " + result);
                    context.set("err", e);
                    e.printStackTrace();
                }
            }
        }
        context.error("Failed to index " + toString() + "]" + result);
        return null;
    }

    /**
     * @return lhs[index]
     */
    @Override
    public String toString() {
        return lhs + "[" + index + "]";
    }
}
