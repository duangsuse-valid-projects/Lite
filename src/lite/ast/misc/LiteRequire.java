package lite.ast.misc;

import lite.Interpreter;
import lite.LiteNode;
import lite.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Load a lite file
 *
 * @author duangsuse
 * @since 1.0
 */
public class LiteRequire extends LiteNode {
    /**
     * Required file
     * require lib/a
     */
    public String required;

    /**
     * The quick constructor
     *
     * @param required required path
     */
    public LiteRequire(String required) {
        this.required = required;
    }

    /**
     * Loads that script
     *
     * @param context lite interpreter context
     * @return script returns
     */
    @Override
    public Object eval(Interpreter context) {
        // file path
        String path = String.format("%1$s/%2$s.lite", context.scriptPath, required);
        FileInputStream fin;
        try {
            File f = new File(path);
            fin = new FileInputStream(f);
        } catch (FileNotFoundException ignored) {
            context.error("Error loading file " + path + ": File not found");
            return null;
        }
        try {
            return context.eval(path, context.read(fin));
        } catch (Parser.MalformedCodeError e) {
            e.printStackTrace();
            context.error("Error loading file " + path + ": Malformed code");
        }
        return null;
    }

    /**
     * require xx
     *
     * @return "require $required"
     */
    @Override
    public String toString() {
        return "require " + required;
    }
}
