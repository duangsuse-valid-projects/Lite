package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

/**
 * Lite while statement
 * <p>
 * while expr
 * block
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteWhile extends LiteNode {
    /**
     * while [expr]
     */
    public LiteNode situation;
    /**
     * [block]
     */
    public LiteBlock block;

    /**
     * Blank constructor
     */
    public LiteWhile() {
    }

    /**
     * Quick constructor
     *
     * @param sitExpr sit expression
     * @param blk     block to execute
     */
    public LiteWhile(LiteNode sitExpr, LiteBlock blk) {
        this.situation = sitExpr;
        this.block = blk;
    }

    /**
     * Run a while statement
     *
     * @param context lite interpreter context
     * @return always null
     */
    @Override
    public Object eval(Interpreter context) {
        context.enterScope();
        while (shouldContinue(context)) {
            block.eval(context);
            if (block.breaked)
                break;
        }
        context.leaveScope();
        return null;
    }

    /**
     * Should this loop continue?
     *
     * @param context lite interpreter
     * @return should?
     */
    public boolean shouldContinue(Interpreter context) {
        Object tmp = situation.eval(context);
        boolean sit = true;
        if (tmp == null || tmp == Boolean.FALSE)
            sit = false; // nil or false is false
        return sit;
    }

    /**
     * to String
     *
     * @return while statement code
     */
    @Override
    public String toString() {
        return String.format("while %1$s\n  %2$s", situation.toString(), block.toString());
    }
}
