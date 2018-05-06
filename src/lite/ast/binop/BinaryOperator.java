package lite.ast.binop;

import lite.Interpreter;
import lite.LiteNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Simple binary operators
 * + - * / ** % & | << < > <= >= == === != and or
 * <p>
 * Math:
 * Add, Sub, Mul, Div, Pwr, Rem, Lt, Le, Gt, Ge
 * Logical:
 * And Or
 * Relational:
 * Equal FullEqual NotEqual
 * Operation:
 * Shl StrConcat TableMerge
 *
 * @author duangsuse
 * @since 1.0
 */
public class BinaryOperator extends LiteNode {
    /**
     * The binary operator
     *
     * @see BinaryOperator.Operators
     */
    public byte operator;

    /**
     * Left hand side of operator
     */
    public LiteNode lhsNode;

    /**
     * Right hand side of operator
     */
    public LiteNode rhsNode;

    /**
     * Blank constructor
     */
    public BinaryOperator() {
    }

    /**
     * Quick constructor
     */
    public BinaryOperator(LiteNode lhs, byte op, LiteNode rhs) {
        lhsNode = lhs;
        operator = op;
        rhsNode = rhs;
    }

    /**
     * Do a numeric operation
     *
     * @param lhs left hand side
     * @param op  operator
     * @param rhs right hand side
     * @return value
     */
    // 就没有做数值类型提升... 好麻烦 他们自己适配类型吧
    // 下面一大堆 boilerplate code
    public static Object numberOperation(Object lhs, char op, Object rhs) {
        if (lhs instanceof Number && rhs instanceof Number) {
            if (rhs instanceof Byte) {
                switch (op) {
                    case '+':
                        return (Byte) lhs + (Byte) rhs;
                    case '-':
                        return (Byte) lhs - (Byte) rhs;
                    case '*':
                        return (Byte) lhs * (Byte) rhs;
                    case '/':
                        return (Byte) lhs / (Byte) rhs;
                    case '^':
                        return (Byte) lhs ^ (Byte) rhs; // **
                    case '%':
                        return (Byte) lhs % (Byte) rhs;
                    case '<':
                        return (Byte) lhs < (Byte) rhs;
                    case '$':
                        return (Byte) lhs <= (Byte) rhs; // <=
                    case '>':
                        return (Byte) lhs > (Byte) rhs;
                    case '!':
                        return (Byte) lhs >= (Byte) rhs; // >=
                    case '&':
                        return (Byte) lhs & (Byte) rhs;
                    case '|':
                        return (Byte) lhs | (Byte) rhs;
                    case '(':
                        return (Byte) lhs << (Byte) rhs; // <<
                }
            }
            if (rhs instanceof Short) {
                switch (op) {
                    case '+':
                        return (Short) lhs + (Short) rhs;
                    case '-':
                        return (Short) lhs - (Short) rhs;
                    case '*':
                        return (Short) lhs * (Short) rhs;
                    case '/':
                        return (Short) lhs / (Short) rhs;
                    case '^':
                        return (Short) lhs ^ (Short) rhs; // **
                    case '%':
                        return (Short) lhs % (Short) rhs;
                    case '<':
                        return (Short) lhs < (Short) rhs;
                    case '$':
                        return (Short) lhs <= (Short) rhs; // <=
                    case '>':
                        return (Short) lhs > (Short) rhs;
                    case '!':
                        return (Short) lhs >= (Short) rhs; // >=
                    case '&':
                        return (Short) lhs & (Short) rhs;
                    case '|':
                        return (Short) lhs | (Short) rhs;
                    case '(':
                        return (Short) lhs << (Short) rhs; // <<
                }
            }
            if (rhs instanceof Integer) {
                switch (op) {
                    case '+':
                        return (Integer) lhs + (Integer) rhs;
                    case '-':
                        return (Integer) lhs - (Integer) rhs;
                    case '*':
                        return (Integer) lhs * (Integer) rhs;
                    case '/':
                        return (Integer) lhs / (Integer) rhs;
                    case '^':
                        return (Integer) lhs ^ (Integer) rhs; // **
                    case '%':
                        return (Integer) lhs % (Integer) rhs;
                    case '<':
                        return (Integer) lhs < (Integer) rhs;
                    case '$':
                        return (Integer) lhs <= (Integer) rhs; // <=
                    case '>':
                        return (Integer) lhs > (Integer) rhs;
                    case '!':
                        return (Integer) lhs >= (Integer) rhs; // >=
                    case '&':
                        return (Integer) lhs & (Integer) rhs;
                    case '|':
                        return (Integer) lhs | (Integer) rhs;
                    case '(':
                        return (Integer) lhs << (Integer) rhs; // <<
                }
            }
            if (rhs instanceof Long) {
                switch (op) {
                    case '+':
                        return (Long) lhs + (Long) rhs;
                    case '-':
                        return (Long) lhs - (Long) rhs;
                    case '*':
                        return (Long) lhs * (Long) rhs;
                    case '/':
                        return (Long) lhs / (Long) rhs;
                    case '^':
                        return (Long) lhs ^ (Long) rhs; // **
                    case '%':
                        return (Long) lhs % (Long) rhs;
                    case '<':
                        return (Long) lhs < (Long) rhs;
                    case '$':
                        return (Long) lhs <= (Long) rhs; // <=
                    case '>':
                        return (Long) lhs > (Long) rhs;
                    case '!':
                        return (Long) lhs >= (Long) rhs; // >=
                    case '&':
                        return (Long) lhs & (Long) rhs;
                    case '|':
                        return (Long) lhs | (Long) rhs;
                    case '(':
                        return (Long) lhs << (Long) rhs; // <<
                }
            }
            if (rhs instanceof Float) {
                switch (op) {
                    case '+':
                        return (Float) lhs + (Float) rhs;
                    case '-':
                        return (Float) lhs - (Float) rhs;
                    case '*':
                        return (Float) lhs * (Float) rhs;
                    case '/':
                        return (Float) lhs / (Float) rhs;
                    case '^':
                        return null; // **
                    case '%':
                        return (Float) lhs % (Float) rhs;
                    case '<':
                        return (Float) lhs < (Float) rhs;
                    case '$':
                        return (Float) lhs <= (Float) rhs; // <=
                    case '>':
                        return (Float) lhs > (Float) rhs;
                    case '!':
                        return (Float) lhs >= (Float) rhs; // >=
                    case '&':
                        return null;
                    case '|':
                        return null;
                    case '(':
                        return null; // <<
                }
            }
            if (rhs instanceof Double) {
                switch (op) {
                    case '+':
                        return (Double) lhs + (Double) rhs;
                    case '-':
                        return (Double) lhs - (Double) rhs;
                    case '*':
                        return (Double) lhs * (Double) rhs;
                    case '/':
                        return (Double) lhs / (Double) rhs;
                    case '^':
                        return null; // **
                    case '%':
                        return (Double) lhs % (Double) rhs;
                    case '<':
                        return (Double) lhs < (Double) rhs;
                    case '$':
                        return (Double) lhs <= (Double) rhs; // <=
                    case '>':
                        return (Double) lhs > (Double) rhs;
                    case '!':
                        return (Double) lhs >= (Double) rhs; // >=
                    case '&':
                        return null;
                    case '|':
                        return null;
                    case '(':
                        return null; // <<
                }
            }
        }
        return null;
    }

    /**
     * Implementation of & operator
     * bitwise and & logical and
     *
     * @param lhs lhs object
     * @param rhs rhs object
     * @return result
     */
    public static Object and(Object lhs, Object rhs) {
        if (lhs instanceof Boolean && rhs instanceof Boolean)
            return (Boolean) lhs && (Boolean) rhs;
        else
            return numberOperation(lhs, '&', rhs);
    }

    /**
     * Implementation of | operator
     * bitwise or and logical or
     *
     * @param lhs lhs object
     * @param rhs rhs object
     * @return result
     */
    public static Object or(Object lhs, Object rhs) {
        if (lhs instanceof Boolean && rhs instanceof Boolean)
            return (Boolean) lhs || (Boolean) rhs;
        else
            return numberOperation(lhs, '|', rhs);
    }

    /**
     * Bitwise shift or call .add(rhs)
     *
     * @param lhs number or object
     * @param rhs object
     * @return result or rhs(if lhs.add() called)
     */
    public static Object shift(Object lhs, Object rhs) {
        Method addMethod = null;
        try {
            addMethod = lhs.getClass().getMethod("add", rhs.getClass());
        } catch (NoSuchMethodException ignored) {
        }
        if (addMethod != null) {
            try {
                return addMethod.invoke(lhs, rhs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        return numberOperation(lhs, '(', rhs);
    }

    /**
     * Value of this binary expression
     *
     * @param context lite interpreter context
     * @return value of this expression
     */
    @Override
    public Object eval(Interpreter context) {
        Object lhs = lhsNode.eval(context);
        Object rhs = rhsNode.eval(context);
        switch (operator) {
            case 0: // +
                if (lhs instanceof String && rhs instanceof String)
                    return lhs + (String) rhs;
                if (lhs instanceof Map && rhs instanceof Map) {
                    ((Map) lhs).putAll((Map) rhs);
                    return lhs;
                }
                return numberOperation(lhs, '+', rhs);
            case 1: // -
                return numberOperation(lhs, '-', rhs);
            case 2: // *
                return numberOperation(lhs, '*', rhs);
            case 3: // /
                return numberOperation(lhs, '/', rhs);
            case 4: // **
                return numberOperation(lhs, '^', rhs);
            case 5: // %
                return numberOperation(lhs, '%', rhs);
            case 6: // <
                return numberOperation(lhs, '<', rhs);
            case 7: // <=
                return numberOperation(lhs, '$', rhs);
            case 8: // >
                return numberOperation(lhs, '>', rhs);
            case 9: // >=
                return numberOperation(lhs, '!', rhs);
            case 10: // &
                return and(lhs, rhs);
            case 11: // |
                return or(lhs, rhs);
            case 12: // ==
                return lhs.equals(rhs);
            case 13: // ===
                return lhs == rhs;
            case 14: // !=
                return lhs != rhs;
            case 15: // <<
                return shift(lhs, rhs);
            default:
                context.error("Failed to execute operator " + toString());
                return null;
        }
    }

    /**
     * Gets the symbol for operator
     *
     * @return symbol of operator
     */
    public String getOperatorSymbol() {
        switch (operator) {
            case 0:
                return "+";
            case 1:
                return "-";
            case 2:
                return "*";
            case 3:
                return "/";
            case 4:
                return "**";
            case 5:
                return "%";
            case 6:
                return "<";
            case 7:
                return "<=";
            case 8:
                return ">";
            case 9:
                return ">=";
            case 10:
                return "&";
            case 11:
                return "|";
            case 12:
                return "==";
            case 13:
                return "===";
            case 14:
                return "!=";
            case 15:
                return "<<";
            default:
                return "";
        }
    }

    /**
     * Get node code
     *
     * @return code for expression
     */
    @Override
    public String toString() {
        return lhsNode.toString() + ' ' + getOperatorSymbol() + ' ' + rhsNode.toString();
    }

    /**
     * The operator byte codes
     */
    public static class Operators {
        byte ADD_CONCAT_MERGE = 0;
        byte SUB = 1;
        byte MUL = 2;
        byte DIV = 3;
        byte PWR = 4;
        byte REM = 5;
        byte LT = 6;
        byte LE = 7;
        byte GT = 8;
        byte GE = 9;
        byte AND = 10;
        byte OR = 11;
        byte EQ = 12;
        byte EQQ = 13;
        byte NE = 14;
        byte SHL = 15;
    }
}
