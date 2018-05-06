package lite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Hashtable;

public class LiteInvocationHandler implements InvocationHandler {
    /**
     * The interpreter context
     */
    public Interpreter context;

    /**
     * Invocation map
     */
    public Hashtable<String, String> invocationMap = new Hashtable<>();

    /**
     * Blank constructor
     */
    public LiteInvocationHandler() {
    }

    /**
     * Fast constructor
     *
     * @param context lite context
     */
    public LiteInvocationHandler(Interpreter context) {
        this.context = context;
    }

    /**
     * Lite Function called when a proxy object function is invoked.
     */
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        String liteMethodName = invocationMap.get(methodName);
        if (liteMethodName == null)
            return null;
        Object mt = context.get(liteMethodName);
        if (!(mt instanceof LiteBlock)) {
            context.error(liteMethodName + " not a method calling " + methodName);
            return null;
        }

        if (args.length != ((LiteBlock) mt).argNames.size())
            return null;
        context.enterScope();
        // put self
        context.scopePut("self", proxy);
        for (int i = 0; i < ((LiteBlock) mt).argNames.size(); i++) {
            context.scopePut(((LiteBlock) mt).argNames.get(i), args[i]);
        }
        Object ret = ((LiteBlock) mt).eval(context);
        context.leaveScope();
        // call
        return ret;
    }
}
