package lite;

/**
 * Trace lite interpreter
 *
 * @author duangsuse
 * @since 1.0
 */
public interface LiteTraceable {
    /**
     * Run trace
     *
     * @param context the engine
     * @return object you want to return(value of trace statement)
     */
    Object trace(Interpreter context);

    /**
     * Run a debug trace function
     *
     * @param context interpreter context
     * @param data    given data
     */
    void hook(Interpreter context, Object data);
}
