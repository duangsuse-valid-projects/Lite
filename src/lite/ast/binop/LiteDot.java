package lite.ast.binop;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * Dot operator for Lite language
 * {@code
 * 调用 Object/Class static 方法
 * 检查是否有 Java_$className!static Java_$className#method 补充存在
 * 如果没有对应方法, 尝试转而使用对象/类 field, static index, subclass index
 * 尝试转发给 Java_method_missing_$className
 * <p>
 * Lite 对象是有 liteClass key 的 hashtable
 * Lite 类是 liteClass 为 'class' 的 Lite 对象
 * Lite 对象的方法调用会转发给 Lite 类(如果找不到)
 * 找不到的方法调用会检查覆盖}
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteDot extends LiteNode {
    /**
     * Left hand side expr
     */
    public LiteNode lhs;

    /**
     * Right hand side expr
     */
    public String rhs;

    /**
     * Call arguments
     */
    public ArrayList<LiteNode> args = new ArrayList<>();

    /**
     * Is really a call?
     */
    // Lite 里没有参数的调用必须加上括号
    public boolean isCall = false;

    /**
     * Blank constructor
     */
    public LiteDot() {
    }

    /**
     * Quick constructor
     *
     * @param lhs  left hand side
     * @param rhs  right hand side
     * @param args arguments
     */
    public LiteDot(LiteNode lhs, String rhs, ArrayList<LiteNode> args) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.args = args;
    }

    /**
     * Make a call
     *
     * @param context lite interpreter context
     * @return call result
     */
    @Override
    public Object eval(Interpreter context) {
        Object result = lhs.eval(context);
        // Supported Class(static method/static/subclass/missing)/Object(method/field)/Map<String, Object>(object/liteBlock)

        if (args.size() == 0 && !isCall) { // no argument present
            if (result instanceof Class || result instanceof Map || result != null) {
                // to a liteSquare
                LiteSquare ls = new LiteSquare(lhs, rhs);
                return ls.eval(context);
            }
        } else
            // a class
            if (result instanceof Class) {
                // static method/missing
                Method target = null;
                Object[] argv = new Object[args.size()];
                for (int i = 0; i < argv.length; i++)
                    argv[i] = args.get(i).eval(context);
                Class[] argc = new Class[argv.length];
                for (int i = 0; i < argv.length; i++)
                    argc[i] = argv[i].getClass();

                try {
                    target = ((Class<?>) result).getDeclaredMethod(rhs, argc);
                } catch (NoSuchMethodException ignored) {
                }

                if (target != null) {
                    try {
                        return target.invoke(result, argv);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        context.error("Failed to call " + rhs + " on " + result);
                        context.set("err", e);
                        e.printStackTrace();
                    }
                } else { // missing!
                    Object mt = context.get("Java_" + ((Class<?>) result).getName().replace(".", "_") + "!" + rhs);
                    if (mt != null)
                        if (mt instanceof LiteBlock) {
                            if (argv.length != ((LiteBlock) mt).argNames.size())
                                return null;
                            context.enterScope();
                            for (int i = 0; i < ((LiteBlock) mt).argNames.size(); i++) {
                                context.scopePut(((LiteBlock) mt).argNames.get(i), argv[i]);
                            }
                            Object ret = ((LiteBlock) mt).eval(context);
                            context.leaveScope();
                            return ret;
                        }
                }
            } else if (result instanceof Map) {
                // lite block
                Object o = ((Map) result).get(rhs);
                if (o instanceof LiteBlock) {
                    Object[] argv = new Object[args.size()];

                    if (argv.length != ((LiteBlock) o).argNames.size()) {
                        context.error("Bad argument size calling " + toString());
                        return null;
                    }

                    for (int i = 0; i < argv.length; i++)
                        argv[i] = args.get(i).eval(context);

                    context.enterScope();
                    // put self
                    context.scopePut("self", result);
                    for (int i = 0; i < ((LiteBlock) o).argNames.size(); i++) {
                        context.scopePut(((LiteBlock) o).argNames.get(i), argv[i]);
                    }
                    Object ret = ((LiteBlock) o).eval(context);
                    context.leaveScope();
                    return ret;
                }
            } else if (result != null) { // an object
                // method/missing
                Method target = null;
                Object[] argv = new Object[args.size()];
                for (int i = 0; i < argv.length; i++)
                    argv[i] = args.get(i).eval(context);
                Class[] argc = new Class[argv.length];
                for (int i = 0; i < argv.length; i++)
                    argc[i] = argv[i].getClass();

                try {
                    target = result.getClass().getMethod(rhs, argc);
                } catch (NoSuchMethodException ignored) {
                }

                if (target != null) {
                    try {
                        return target.invoke(result, argv);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        context.error("Failed to call " + rhs + " on " + result);
                        context.set("err", e);
                        e.printStackTrace();
                    }
                } else { // missing!
                    Object mt = context.get("Java_" + result.getClass().getName().replace(".", "_") + "#" + rhs);
                    if (mt != null) {
                        if (mt instanceof LiteBlock) {
                            if (argv.length != ((LiteBlock) mt).argNames.size())
                                return null;
                            context.enterScope();
                            // put self
                            context.scopePut("self", result);
                            for (int i = 0; i < ((LiteBlock) mt).argNames.size(); i++) {
                                context.scopePut(((LiteBlock) mt).argNames.get(i), argv[i]);
                            }
                            Object ret = ((LiteBlock) mt).eval(context);
                            context.leaveScope();
                            return ret;
                        }
                    } else {
                        // call missing
                        mt = context.get("JavaMissing_" + result.getClass().getName().replace(".", "_") + "$");
                        if (mt != null)
                            // 复制粘贴... 0 代码重用
                            if (mt instanceof LiteBlock) {
                                if (argv.length != ((LiteBlock) mt).argNames.size())
                                    return null;
                                context.enterScope();
                                // put self
                                context.scopePut("self", result);
                                context.scopePut("name", rhs);
                                for (int i = 0; i < ((LiteBlock) mt).argNames.size(); i++) {
                                    context.scopePut(((LiteBlock) mt).argNames.get(i), argv[i]);
                                }
                                Object ret = ((LiteBlock) mt).eval(context);
                                context.leaveScope();
                                return ret;
                            }
                    }
                }
            }

        context.error("Failed to dot " + toString() + "}" + result);
        return null;
    }

    /**
     * @return lhs.rhs(arg1 arg2)
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(lhs.toString() + "." + rhs);
        ret.append("( ");
        for (LiteNode i : args)
            ret.append(i.toString()).append(' ');
        ret.append(')');
        return ret.toString();
    }
}
