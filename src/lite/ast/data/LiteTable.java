package lite.ast.data;

import lite.Interpreter;
import lite.LiteNode;

import java.util.Hashtable;
import java.util.Map;

/**
 * A table initializer
 * <p>
 * { a:b, b:c }
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteTable extends LiteNode {
    /**
     * The hash initializer
     */
    public Hashtable<String, LiteNode> initializer;

    /**
     * Blank constructor
     */
    public LiteTable() {
    }

    /**
     * The quick constructor
     *
     * @param tblInit table initializer
     */
    public LiteTable(Hashtable<String, LiteNode> tblInit) {
        this.initializer = tblInit;
    }

    /**
     * Gets constructed table
     *
     * @param context lite interpreter context
     * @return the table
     */
    @Override
    public Object eval(Interpreter context) {
        Hashtable<String, Object> initialized = new Hashtable<>();
        for (String k : initializer.keySet()) {
            initialized.put(k, initializer.get(k).eval(context));
        }
        return initialized;
    }

    /**
     * return a pretty printed initializer
     *
     * @return "{ ... }"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        for (Map.Entry e : initializer.entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append(',');
        }
        sb.append("\n}");
        return sb.toString();
    }
}
