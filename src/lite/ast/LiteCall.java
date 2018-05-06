package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * A call to function(block)value/Java class constructor/Lite object(hash)
 * <p>
 * Hashtable()
 * String("hello")
 * def a
 * puts
 * a()
 * <p>
 * '@b = do |i| puts i
 * '@b(1)
 * <p>
 * identifier/class '(' params ')'
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteCall extends LiteNode {
    /**
     * The identifier to call
     */
    public LiteIdentifier identifier;

    /**
     * The call args
     */
    public ArrayList<LiteNode> args = new ArrayList<>();

    /**
     * Blank constructor
     */
    public LiteCall() {
    }

    /**
     * Quick constructor
     *
     * @param ident identifier for call
     * @param argv  arguments
     */
    public LiteCall(LiteIdentifier ident, ArrayList<LiteNode> argv) {
        this.identifier = ident;
        this.args = argv;
    }

    /**
     * Call the identifier, may be a Class/lite object/block
     *
     * @param context lite interpreter context
     * @return return result/new object
     */
    @Override
    public Object eval(Interpreter context) {
        // get called object
        Object idValue = identifier.eval(context);
        // get arguments
        Object[] arguments = new Object[args.size()];
        Object ret = null;
        for (int i = 0; i < args.size(); i++)
            arguments[i] = args.get(i).eval(context);
        // get argument classes
        Class[] argumentClasses = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++)
            argumentClasses[i] = arguments[i].getClass();
        // invoke
        if (idValue instanceof Class) {
            try {
                // try construct this class
                Constructor ctor = ((Class<?>) idValue).getDeclaredConstructor(argumentClasses);
                ret = ctor.newInstance(arguments);
            } catch (NoSuchMethodException e) {
                context.error("Failed to get constructor for class " + identifier);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                context.error("Failed to call constructor: " + e);
                context.set("err", e);
                e.printStackTrace();
            }
        } else if (idValue instanceof LiteBlock) {
            if (((LiteBlock) idValue).argNames.size() != arguments.length) {
                context.error("Argument size mismatch: expected " + arguments.length + " but actual " + ((LiteBlock) idValue).argNames.size());
                return null;
            }
            context.enterScope();
            for (int i = 0; i < ((LiteBlock) idValue).argNames.size(); i++) {
                context.scopePut(((LiteBlock) idValue).argNames.get(i), arguments[i]);
            }
            ret = ((LiteNode) idValue).eval(context);
            context.leaveScope();
        } else {
            context.error("Failed to call " + identifier + ": Not a JavaClass or LiteBlock");
        }
        return ret;
    }

    /**
     * call literal string
     *
     * @return identifier(args)
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(identifier.toString());
        ret.append("( ");
        for (LiteNode i : args)
            ret.append(i.toString()).append(' ');
        ret.append(')');
        return ret.toString();
    }
}
