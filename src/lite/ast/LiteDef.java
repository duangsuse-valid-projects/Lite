package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

import java.util.Map;

/**
 * Define a method AST node
 * <p>
 * def a b
 * puts b
 * def $java.lang.String.fuck # static method fallback
 * def java.lang.Integer.get # method fallback
 * def java.io.File$ # instance method missing
 * def map.foo # define map block
 */
public class LiteDef extends LiteNode {
    /**
     * The assignment identifier
     */
    public LiteIdentifier ident;

    /**
     * The function body
     */
    public LiteBlock body;

    /**
     * Blank constructor
     */
    public LiteDef() {
    }

    /**
     * Quick constructor
     *
     * @param ident method identifier
     * @param body  method body
     */
    public LiteDef(LiteIdentifier ident, LiteBlock body) {
        this.ident = ident;
        this.body = body;
    }

    /**
     * Define this function
     *
     * @param context lite interpreter context
     * @return always null
     */
    @Override
    public Object eval(Interpreter context) {
        String id = ident.identifier;
        if (!ident.isLocalIdentifier && (id.contains(".") || id.contains("$"))) {
            String[] patches = id.replace(".", "`").split("`");
            String[] classNamePaths = new String[patches.length - 1];
            System.arraycopy(patches, 0, classNamePaths, 0, patches.length - 2 + 1);
            // the clasName
            String className = String.join("_", classNamePaths);

            if (id.contains("$")) {
                // is method_missing?
                if (id.endsWith("$")) {
                    // define method_missing
                    String missingName = "JavaMissing_" + id.replace(".", "_");
                    context.set(missingName, body);
                    return null;
                } else { // is a static fallback
                    context.set("Java_" + className.substring(1) + "!" + patches[patches.length - 1], body);
                    return null;
                }
            } else { // is a method fallback/map block define
                String methodName = patches[patches.length - 1];
                if (patches.length == 2) { // is a map define
                    Object obj = context.get(patches[0]);
                    if (obj instanceof Map) {
                        ((Map) obj).put(patches[1], body);
                        return null;
                    }
                } else { // is a method fallback
                    context.set("Java_" + className + "#" + patches[patches.length - 1], body);
                    return null;
                }
            }
        }
        ident.set(context, body);
        return null;
    }

    /**
     * String dump
     *
     * @return function literal
     */
    @Override
    public String toString() {
        return "def " + ident + "\n  " + body;
    }
}
