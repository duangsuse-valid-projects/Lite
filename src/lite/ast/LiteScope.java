package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

/**
 * A Lite scope
 *
 * @author duangsuse
 * @see lite.Interpreter#stack
 * @since 1.0
 */
public class LiteScope extends LiteNode {
    /**
     * Block in this scope
     */
    public LiteBlock block;

    /**
     * Quick constructor
     *
     * @param block block to do in scope
     */
    public LiteScope(LiteBlock block) {
        this.block = block;
    }

    /**
     * Execute a block in a scope
     *
     * @param context lite interpreter context
     * @return block returns
     */
    @Override
    public Object eval(Interpreter context) {
        context.enterScope();
        Object ret = block.eval(context);
        context.leaveScope();
        return ret;
    }

    /**
     * Scope code literal
     *
     * @return scope\n  $code
     */
    @Override
    public String toString() {
        return String.format("scope\n  %1$s", block);
    }
}
