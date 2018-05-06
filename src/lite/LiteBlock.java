package lite;

import lite.ast.misc.LiteBreak;
import lite.ast.misc.LiteNext;
import lite.ast.misc.LiteReturn;

import java.util.ArrayList;

/**
 * A sequence of AST node to be executed
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteBlock extends LiteNode {
    /**
     * breaked by other node
     */
    public boolean breaked = false;

    /**
     * Do next?
     */
    public boolean next = false;

    /**
     * Arg names
     */
    public ArrayList<String> argNames = new ArrayList<>();

    /**
     * Node children
     */
    public ArrayList<LiteNode> childNodes = new ArrayList<>();

    /**
     * Defined context
     */
    public Interpreter defined;

    /**
     * Add a statement
     *
     * @param node node to add
     */
    public void addStatement(LiteNode node) {
        childNodes.add(node);
    }

    /**
     * Evaluate this block (all block statements)
     *
     * @return latest evaluated node return
     */
    @Override
    public Object eval(Interpreter context) {
        Object lastValue = null;
        for (LiteNode n : childNodes) {
            if (n instanceof LiteBreak)
                breaked = true;
            if (n instanceof LiteNext)
                next = true;
            if (n instanceof LiteReturn)
                return n.eval(context); // return value
            // eval this node
            lastValue = n.eval(context);
            if (breaked || next) { // for parent node
                next = false; // next call should not be breaked
                breaked = false;
                break;
            }
        }
        return lastValue;
    }

    /**
     * Invoke this block (self given)
     *
     * @param self    self (JavaObject/Map/...)
     * @param varargs arguments
     * @return block returns
     */
    public Object invoke(Object self, Object... varargs) {
        if (this.argNames.size() != varargs.length) {
            defined.error("Argument size mismatch: expected " + varargs.length + " but actual " + this.argNames.size());
            return null;
        }
        defined.enterScope();
        defined.scopePut("self", self);
        for (int i = 0; i < this.argNames.size(); i++) {
            defined.scopePut(this.argNames.get(i), varargs[i]);
        }
        Object ret = this.eval(defined);
        defined.leaveScope();
        return ret;
    }

    /**
     * Invoke this closure
     *
     * @param varargs arguments
     * @return block call result
     */
    public Object invoke(Object... varargs) {
        return invoke(null, varargs);
    }

    /**
     * Run this block
     *
     * @return block value
     */
    public Object run() {
        return eval(defined);
    }

    /**
     * Concat all block stmt to one String
     *
     * @return childNodes toString splited with newline
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (argNames.size() != 0)
            for (String s : argNames)
                sb.append("argument: ").append(s).append('\n');
        for (LiteNode s : childNodes)
            sb.append(s.toString()).append('\n');
        return sb.toString();
    }
}
