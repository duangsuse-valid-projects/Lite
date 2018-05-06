import lite.Interpreter;
import org.junit.Test;
import org.junit.runners.model.TestClass;

/**
 * JUnit4 Test for Lite
 *
 * @author 由于懒惰没写测试的 duangsuse
 * @version 1.0
 * @since 1.0
 */
public class Tests extends TestClass {
    public Tests() {
        super(Test.class);
    }

    /**
     * This is a example test
     */
    @Test
    public void fooTest() {
        System.out.println("Hello");
    }

    // Begin interpreter tests
    @Test
    public void initializesRight() {
        Interpreter i = new Interpreter();
        assert i.get("lite") != null;
    }

    @Test
    public void scopeWorks() {
        Interpreter i = new Interpreter();
        i.set("a", 1);
        assert i.scopeGet("a").equals("a");
        i.enterScope();
        assert i.scopeGet("a") == null;
        i.scopePut("b", 1);
        assert (Integer) i.scopeGet("b") == 1;
        i.scopePut("a", "sm");
        assert i.scopeGet("a").equals("sm");
        i.leaveScope();
        assert i.scopeGet("a").equals("a");
        assert i.get("a").equals("a");
    }

    // Begin AST interpreter tests

    // Begin parser tests
}
