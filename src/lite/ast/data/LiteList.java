package lite.ast.data;

import lite.Interpreter;
import lite.LiteNode;

import java.util.ArrayList;

/**
 * A list initializer
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteList extends LiteNode {
    /**
     * Initialize expr s
     */
    public ArrayList<LiteNode> elementInitializer = new ArrayList<>();

    /**
     * Blank constructor
     */
    public LiteList() {
    }

    /**
     * Quick constructor
     */
    public LiteList(ArrayList<LiteNode> nd) {
        elementInitializer = nd;
    }

    /**
     * Get this ary
     *
     * @param context lite interpreter context
     * @return initialized ary
     */
    @Override
    public Object eval(Interpreter context) {
        ArrayList<Object> initialized = new ArrayList<>();
        for (int i = 0; i < elementInitializer.size(); i++)
            initialized.set(i, elementInitializer.get(i).eval(context));
        return initialized;
    }

    /**
     * A list to string
     *
     * @return [ a b c ]
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ ");
        for (LiteNode n : elementInitializer)
            sb.append(n).append(' ');
        sb.append(']');
        return sb.toString();
    }
}
