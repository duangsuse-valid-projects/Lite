package lite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * The Lite interpreter context
 *
 * @author duangsuse
 * @since 1.0
 */
public class Interpreter {
    /**
     * The version of interpreter
     */
    public static final String version = "1.0";

    /**
     * Globals, be final to synchronized
     */
    public final Hashtable<String, Object> globals = new Hashtable<>();

    /**
     * Scope stack, be final to synchronized
     */
    public final Vector<Hashtable<String, Object>> stack = new Vector<>();

    /**
     * Imported packages
     */
    public ArrayList<String> importedPackages = new ArrayList<>();

    /**
     * Imported classes
     */
    public Hashtable<String, Class> importedClasses = new Hashtable<>();

    /**
     * When global variable missing...
     */
    public LiteTraceable globalMissing = null;

    /**
     * Error happened
     */
    public LiteTraceable onError = null;

    /**
     * On fatal (uncaught) error
     */
    public LiteTraceable onFatal = null;

    /**
     * Trace function
     */
    public LiteTraceable traceFunc = null;

    /**
     * Script path
     */
    public String scriptPath = "/";
    /**
     * Trigger when getting java class
     * <p> Got Java class saved to _JClass
     */
    public LiteTraceable onGetJava = null;

    // debug hooks
    /**
     * Entered a scope
     */
    public LiteTraceable onEnterScope = null;
    /**
     * Leaved a scope
     */
    public LiteTraceable onLeaveScope = null;
    /**
     * Get a local
     */
    public LiteTraceable onGetLocal = null;
    /**
     * Set a local
     */
    public LiteTraceable onSetLocal = null;
    /**
     * Get a global
     */
    public LiteTraceable onGetGlobal = null;
    /**
     * Set a global
     */
    public LiteTraceable onSetGlobal = null;

    /**
     * Blank constructor
     */
    public Interpreter() {
        globals.put("lite", this);
    }

    /**
     * Gets a java class
     *
     * @param className class name
     * @return the class
     */
    public Class getJava(String className) {
        // find if it's loaded class
        if (importedClasses.containsKey(className))
            return importedClasses.get(className);
        // load class
        Class clas = null;
        try {
            clas = Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            try {
                clas = Class.forName(className.replace("_", "."));
            } catch (ClassNotFoundException ignored1) {
            }
        }
        if (clas != null) {
            importedClasses.put(className.replace(".", "_"), clas);
            set("_JClass", clas);
            onGetJava.hook(this, clas);
            return clas;
        }
        // find in packages
        for (String s : importedPackages) {
            String name = s + '.' + className;
            try {
                clas = Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
            if (clas != null) {
                importedClasses.put(className, clas);
                set("_JClass", clas);
                onGetJava.hook(this, clas);
                return clas;
            }
        }
        return null;
    }

    /**
     * Pop top scope
     */
    public void leaveScope() {
        synchronized (stack) {
            if (stack.size() != 0) // must have a stack top
                stack.remove(0);
        }
        onLeaveScope.trace(this);
    }

    /**
     * Enter a scope
     */
    public void enterScope() {
        synchronized (stack) {
            stack.add(new Hashtable<>());
        }
        onEnterScope.trace(this);
    }

    /**
     * Put an object to scope
     *
     * @param identifier identifier
     * @param o          object to put
     */
    public void scopePut(String identifier, Object o) {
        synchronized (stack) {
            if (stack.size() == 0) {
                set(identifier, o);
                return;
            }
            stack.get(0).put(identifier, o);
        }
        onSetLocal.hook(this, identifier);
    }

    /**
     * Get an object from scope
     *
     * @param identifier identifier of this object
     * @return this object
     */
    public Object scopeGet(String identifier) {
        onGetLocal.hook(this, identifier);
        synchronized (stack) {
            if (stack.size() == 0)
                return get(identifier);
            return stack.get(0).get(identifier);
        }
    }

    public Object get(String varName) {
        onGetGlobal.hook(this, varName);
        synchronized (globals) {
            if (globals.containsKey(varName)) {
                return globals.get(varName);
            } else {
                if (globalMissing != null) {
                    return globalMissing.trace(this);
                }
                return null;
            }
        }
    }

    /**
     * Sets a global variable
     *
     * @param varName variable name
     * @param o       value
     */
    public void set(String varName, Object o) {
        synchronized (globals) {
            globals.put(varName, o);
        }
        onSetGlobal.hook(this, varName);
    }

    /**
     * Sets error
     *
     * @param msg error message
     */
    public void error(String msg) {
        set("error", msg);
        if (onError != null)
            onError.trace(this);
    }

    /**
     * Request a method, if not found, return null
     *
     * @param index the lite method name
     * @return null if not found, else the method
     */
    public LiteBlock method(String index) {
        Object indexed = get(index);
        if (indexed == null)
            return null;
        else if (indexed instanceof LiteBlock) {
            ((LiteBlock) indexed).defined = this;
            return (LiteBlock) indexed;
        } else {
            return null;
        }
    }

    /**
     * Version getter
     *
     * @return version
     * @see Interpreter#version
     */
    @SuppressWarnings("SameReturnValue")
    public String getVersion() {
        return Interpreter.version;
    }

    /**
     * Parse and evaluate a string
     *
     * @param s lite code
     * @return value of lite code
     */
    public Object eval(String s) throws Parser.MalformedCodeError {
        return eval("<unknown>", s);
    }

    /**
     * Eval a string with path
     *
     * @param path file path
     * @param code lite code
     * @return value of code
     * @throws Parser.MalformedCodeError if code is bad
     */
    public Object eval(String path, String code) throws Parser.MalformedCodeError {
        Parser p = new Parser(code);
        p.path = path;
        scriptPath = path;
        return eval(p.parse());
    }

    /**
     * Reads a input stream to string, send to eval(String)
     *
     * @param path  the script path
     * @param input string input stream
     * @return eval result
     */
    public Object eval(String path, InputStream input) throws Parser.MalformedCodeError {
        return eval(path, read(input));
    }

    /**
     * Reads a input stream, send to eval, without path
     *
     * @param input the input stream
     * @return script result
     * @throws Parser.MalformedCodeError if code cannot parse to AST
     */
    public Object eval(InputStream input) throws Parser.MalformedCodeError {
        return eval(read(input));
    }

    /**
     * Reads a input stream to String
     *
     * @param input string input stream
     * @return input stream string
     */
    public String read(InputStream input) {
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[input.available()];
        } catch (IOException e) {
            e.printStackTrace();
            error("IOException");
        }
        try {
            int ignored = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            error("IOException");
        }
        return new String(buffer);
    }

    /**
     * Proxy interfaces using blocks to handle call
     *
     * @param interfaces implements interfaces
     * @param tbl        implement table
     * @return new proxy object
     */
    public Object proxy(List<Class> interfaces, Hashtable<String, String> tbl) {
        Class[] implement = new Class[interfaces.size()];
        for (int i = 0; i < implement.length; i++)
            implement[i] = interfaces.get(i);

        LiteInvocationHandler handler = new LiteInvocationHandler(this);
        handler.invocationMap = tbl; // set table

        // new proxy object
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), implement, handler);
    }

    /**
     * Proxy an interface
     *
     * @param theInterface the interface
     * @param table        the call table
     * @return proxy object
     */
    public Object proxy(Class theInterface, Hashtable<String, String> table) {
        // setup interfaces
        Class[] interfaces = new Class[]{theInterface};

        // setup handler
        LiteInvocationHandler handler = new LiteInvocationHandler(this);
        handler.invocationMap = table;

        return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, handler);
    }

    /**
     * Evaluate a node
     *
     * @param node target node
     * @return value of target node
     */
    public Object eval(LiteNode node) {
        try {
            return node.eval(this);
        } catch (Exception e) {
            set("ERROR", e);
            // fatal error
            if (onFatal != null)
                return onFatal.trace(this);
            return null;
        }
    }
}
