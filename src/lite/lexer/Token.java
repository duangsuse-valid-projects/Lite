package lite.lexer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Lite Syntax Token
 *
 * @author duangsuse
 * @since 1.0
 */
public class Token {
    /**
     * Declared at line
     */
    public int line;

    /**
     * Type of the token
     */
    public TokenType type;

    /**
     * String token data
     */
    public String data;

    /**
     * Blank constructor
     */
    public Token() {
    }

    /**
     * Quick constructor
     */
    public Token(int l, TokenType t, String data) {
        line = l;
        type = t;
        this.data = data;
    }

    /**
     * Is this token typeof t
     *
     * @param t expected type
     * @return true if type is match
     */
    public boolean is(TokenType t) {
        return type == t;
    }

    /**
     * Is this token a keyword
     *
     * @return true if the token is a keyword
     */
    public boolean isKeyword() {
        switch (type) {
            case REQUIRE:
            case RETURN:
            case IMPORT:
            case DEFINE:
            case TRACE:
            case WHILE:
            case FOR:
            case IF:
            case ELIF:
            case ELSE:
            case SCOPE:
            case IN:
            case AS:
            case END:
            case BREAK:
            case NEXT:
            case DO:
                return true;
        }
        return false;
    }

    /**
     * Is this token a value token
     * <p>
     * Value token: string number true nil false
     *
     * @return true if this token is a value token
     */
    public boolean isValue() {
        switch (type) {
            case TRUE:
            case FALSE:
            case NIL:
            case SINGLE_QUOTE_STRING:
            case STRING:
            case NUMBER:
                return true;
        }
        return false;
    }

    /**
     * Gets the token value from raw string/type
     *
     * @return value of the token
     */
    public Object getValue() {
        if (!isValue())
            return null;
        switch (type) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NIL:
                return null;
            case SINGLE_QUOTE_STRING:
                return data;
            case STRING:
                return data.replace("\\n", "\n").replace("\\t", "\t");
            case NUMBER:
                return numberValue();
        }
        return null;
    }

    /**
     * Gets the value of a lite number string
     * 0212.33 33f 44d 788238237n
     *
     * @return the value number
     */
    public Object numberValue() {
        if (data.contains(".")) {
            return Double.valueOf(data);
        } else if (data.endsWith("l")) {
            return Long.valueOf(data.replace("l", ""));
        } else if (data.endsWith("b")) {
            return Byte.valueOf(data.replace("b", ""));
        } else if (data.endsWith("s")) {
            return Short.valueOf(data.replace("s", ""));
        } else if (data.endsWith("f")) {
            return Float.valueOf(data.replace("f", ""));
        } else if (data.endsWith("n")) {
            return new BigInteger(data.replace("n", ""));
        } else if (data.endsWith("d")) {
            return new BigDecimal(data.replace("d", ""));
        } else {
            return Integer.valueOf(data);
        }
    }

    /**
     * Is this token an identifier
     *
     * @return true if it is an identifier
     */
    public boolean isIdentifier() {
        return is(TokenType.IDENTIFIER);
    }

    /**
     * Is this token newline
     *
     * @return true if it is newline \n
     */
    public boolean isNewline() {
        return is(TokenType.NEWLINE);
    }

    /**
     * Is this token an ident
     *
     * @return true if it is '  '
     */
    public boolean isIdent() {
        return is(TokenType.IDENT);
    }

    /**
     * Is this token not eof
     *
     * @return false if it is end-of-file
     */
    public boolean isNotEof() {
        return !is(TokenType.EOF);
    }

    /**
     * Is this token a operator
     *
     * @return true if it is a binary operator + - * /
     */
    public boolean isBinaryOperator() {
        switch (type) {
            case EQ:
            case GE:
            case GT:
            case LE:
            case LT:
            case NE:
            case OR:
            case AND:
            case DIV:
            case MUL:
            case PWR:
            case REM:
            case SUB:
            case ADD:
            case DOT:
            case EQUAL:
            case SHIFT:
            case SQUARE_OP:
            case STABBY_OP:
            case EQUAL_FULL:
                return true;
        }
        return false;
    }

    /**
     * Is this token a operator
     *
     * @return true if it is a unary operator + - * /
     */
    public boolean isUnaryOperator() {
        switch (type) {
            case SUB:
            case DEC:
            case INC:
            case NOT:
                return true;
        }
        return false;
    }

    /**
     * Is this token a @
     *
     * @return true if it is a At
     */
    public boolean isAt() {
        return is(TokenType.AT);
    }

    /**
     * Is this token a comma
     *
     * @return true if it is ,
     */
    public boolean isComma() {
        return is(TokenType.COMMA);
    }

    /**
     * Is this token quote
     *
     * @return true if it is an :
     */
    public boolean isQuote() {
        return is(TokenType.QUOTE);
    }

    /**
     * Is this token an [
     *
     * @return true if it is an square
     */
    public boolean isListStart() {
        return is(TokenType.SQUARE);
    }

    /**
     * Is this token an brace
     *
     * @return true if it is an brace
     */
    public boolean isTableStart() {
        return is(TokenType.BRACE);
    }


    /**
     * Stringify this token
     *
     * @return token string
     */
    @Override
    public String toString() {
        switch (type) {
            case EQ:
                return "=";
            case GE:
                return ">=";
            case GT:
                return ">";
            case LE:
                return "<=";
            case LT:
                return "<";
            case NE:
                return "!=";
            case OR:
                return "|";
            case AND:
                return "&";
            case DIV:
                return "/";
            case MUL:
                return "*";
            case PWR:
                return "**";
            case REM:
                return "%";
            case SUB:
                return "-";
            case FALSE:
                return "false";
            case AT:
                return "@";
            case DO:
                return "do";
            case IF:
                return "if";
            case IN:
                return "in";
            case ADD:
                return "+";
            case DEC:
                return "--";
            case DOT:
                return ".";
            case EOF:
                return "<eof>";
            case FOR:
                return "for";
            case INC:
                return "++";
            case NIL:
                return "nil";
            case NOT:
                return "!";
            case CALL:
                return "()";
            case ELIF:
                return "elif";
            case ELSE:
                return "else";
            case NEXT:
                return "next";
            case TRUE:
                return "true";
            case BRACE:
                return "{";
            case BREAK:
                return "break";
            case COMMA:
                return ", ";
            case EQUAL:
                return "==";
            case IDENT:
                return "<ident>";
            case PAREN:
                return "(";
            case QUOTE:
                return ":";
            case SCOPE:
                return "scope";
            case SHIFT:
                return "<<";
            case TRACE:
                return "trace";
            case WHILE:
                return "while";
            case DEFINE:
                return "def";
            case IMPORT:
                return "import";
            case NUMBER:
                return data;
            case RETURN:
                return "return";
            case SQUARE:
                return "[";
            case STRING:
                return '"' + data + '"';
            case NEWLINE:
                return "<newline>\n";
            case REQUIRE:
                return "require";
            case BRACE_END:
                return "}";
            case PAREN_END:
                return ")";
            case SQUARE_OP:
                return "::";
            case STABBY_OP:
                return "->";
            case EQUAL_FULL:
                return "===";
            case IDENTIFIER:
                return data;
            case AS:
                return "as";
            case END:
                return "end";
            case SQUARE_END:
                return "]";
            case SINGLE_QUOTE_STRING:
                return "'" + data + "'";
            default:
                return "<unknown>";
        }
    }
}
