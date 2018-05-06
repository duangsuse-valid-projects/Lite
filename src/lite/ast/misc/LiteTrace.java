package lite.ast.misc;

import lite.Interpreter;
import lite.LiteNode;

/**
 * trace 'hello'
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteTrace extends LiteNode {
    /**
     * Message to send
     */
    public String content;

    /**
     * The quick constructor
     *
     * @param message the trace message
     */
    public LiteTrace(String message) {
        this.content = message;
    }

    /**
     * Put trace message to env, call trace func
     *
     * @param context lite interpreter context
     * @return trace function returns, if not set, null
     */
    @Override
    public Object eval(Interpreter context) {
        context.set("_trace", content);
        if (context.traceFunc != null) {
            Object tracey = context.traceFunc.trace(context);
            context.set("TraceResult", tracey);
            return tracey;
        } else
            return null;
    }

    /**
     * trace string
     *
     * @return trace '$string'
     */
    @Override
    public String toString() {
        return "trace " + "'" + content + "'";
    }
}
