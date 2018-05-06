package lite.ast;

import lite.Interpreter;
import lite.LiteBlock;
import lite.LiteNode;

/**
 * for i in foo
 * puts i
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteFor extends LiteNode {
    /**
     * The used identifier for i
     */
    public LiteIdentifier ident;

    /**
     * The in expr
     */
    public LiteNode expr;

    /**
     * The block
     */
    public LiteBlock block;

    /**
     * Blank constructor
     */
    public LiteFor() {
    }

    /**
     * Quick constructor
     *
     * @param ident for variable
     * @param expr  iter expression
     * @param block do block
     */
    public LiteFor(LiteIdentifier ident, LiteNode expr, LiteBlock block) {
        this.ident = ident;
        this.expr = expr;
        this.block = block;
    }

    /**
     * Run for statement
     *
     * @param context lite interpreter context
     * @return always null
     */
    @Override
    public Object eval(Interpreter context) {
        Object iter = expr.eval(context);
        if (!(iter instanceof Iterable)) {
            context.error("Bad iterator");
            return null;
        }
        // enter for loop
        context.enterScope();
        for (Object o : (Iterable<?>) iter) {
            ident.set(context, o);
            block.eval(context);
            if (block.breaked)
                break;
        }
        // leave for loop
        context.leaveScope();
        return null;
    }

    /**
     * to a debug string expression
     *
     * @return for literal
     */
    @Override
    public String toString() {
        return "for " + ident + " in " + expr + "\n  " + block;
    }
}
