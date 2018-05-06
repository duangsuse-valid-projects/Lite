package lite;

/**
 * Simple AST Node for Lite
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteNode {
    /**
     * File source
     */
    public String sourceFile = "<unknown>";

    /**
     * Source line
     */
    public int sourceLine = 0;

    /**
     * Blank constructor
     */
    public LiteNode() {
    }

    /**
     * Run this node
     *
     * @param context lite interpreter context
     * @return eval result (value of this node), default null
     */
    public Object eval(Interpreter context) {
        return null;
    }

    /**
     * toString, .... node code
     */
    @SuppressWarnings("EmptyMethod")
    @Override
    public String toString() {
        return super.toString();
    }
}
