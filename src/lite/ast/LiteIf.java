package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

import java.util.Hashtable;
import java.util.Map;

/**
 * Like:
 * if expr
 * elif expr
 * puts
 * else
 * ...
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteIf extends LiteNode {
    /**
     * The expr
     */
    public LiteNode expr;

    /**
     * The if block
     */
    public LiteBlock branchIf;

    /**
     * elseif blocks
     */
    public Hashtable<LiteNode, LiteBlock> elif = new Hashtable<>();

    /**
     * else block
     */
    public LiteBlock not;

    /**
     * Blank constructor
     */
    public LiteIf() {
    }

    /**
     * Run the if statement
     *
     * @param context lite interpreter context
     * @return always null
     */
    public Object eval(Interpreter context) {
        boolean b = true;
        boolean elifMatch = true;
        Object o = expr.eval(context);
        if (o == null || o == Boolean.FALSE)
            b = false;
        if (b) {
            branchIf.eval(context);
        } else {
            for (Map.Entry e : elif.entrySet()) {
                elifMatch = true;
                Object obj = ((LiteNode) e.getKey()).eval(context);
                if (obj == null || obj == Boolean.FALSE)
                    elifMatch = false;
                if (elifMatch) {
                    ((LiteBlock) e.getValue()).eval(context);
                    break;
                }
            }

            if (!elifMatch && not != null)
                not.eval(context);
        }
        return null;
    }

    /**
     * to string if
     *
     * @return string if
     */
    public String toString() {
        StringBuilder ret = new StringBuilder("if " + expr + "\n");
        for (Map.Entry e : elif.entrySet()) {
            ret.append("  elif").append("\n  ").append(e.getKey());
            ret.append(e.getValue());
        }
        ret.append("  else").append("\n  ").append(not);
        return ret.toString();
    }
}
