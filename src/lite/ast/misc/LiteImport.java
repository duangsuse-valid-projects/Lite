package lite.ast.misc;

import lite.Interpreter;
import lite.LiteNode;

/**
 * Import a java class/package
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteImport extends LiteNode {
    /**
     * Package/Class to import
     * <p>
     * org.duangsuse.foo
     * org.duangsuse.foo.A
     * com.foo.pkg.Foo
     */
    public String toImport;

    /**
     * The quick constructor
     *
     * @param importing import string
     */
    public LiteImport(String importing) {
        this.toImport = importing;
    }

    /**
     * Add a class or a package to context
     *
     * @param context lite interpreter context
     * @return always null (is a statement)
     */
    @Override
    public Object eval(Interpreter context) {
        Class clazz = null;
        try {
            clazz = Class.forName(toImport);
        } catch (ClassNotFoundException ignored) {
        }
        if (clazz != null)
            context.importedClasses.put(clazz.getSimpleName(), clazz);
        else // is package name
            context.importedPackages.add(toImport);
        return null;
    }

    /**
     * Import statement code
     *
     * @return the code import $toImport
     */
    @Override
    public String toString() {
        return "import " + toImport;
    }
}
