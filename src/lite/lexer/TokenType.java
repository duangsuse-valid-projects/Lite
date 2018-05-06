package lite.lexer;

/**
 * Lite token types
 *
 * @author duangsuse
 * @since 1.0
 */
public enum TokenType {
    /**
     * true
     */
    TRUE,

    /**
     * false
     */
    FALSE,

    /**
     * nil
     */
    NIL,

    /**
     * ""
     */
    STRING,

    /**
     * ''
     */
    SINGLE_QUOTE_STRING,

    /**
     * {
     */
    BRACE,

    /**
     * :
     */
    QUOTE,

    /**
     * ,
     */
    COMMA,

    /**
     * }
     */
    BRACE_END,

    /**
     * 0x1 2 2l 0b0000
     */
    NUMBER,

    /**
     * import
     */
    IMPORT,

    /**
     * require
     */
    REQUIRE,

    /**
     * @
     */
    AT,

    /**
     * =
     */
    EQ,

    /**
     * trace
     */
    TRACE,

    /**
     * \n
     */
    NEWLINE,

    /**
     * '  ' 2 space ident
     */
    IDENT,

    /**
     * End of file
     */
    EOF,

    /**
     * in
     */
    IN,

    /**
     * ::
     */
    SQUARE_OP,

    /**
     * ->
     */
    STABBY_OP,

    /**
     * .
     */
    DOT,

    /**
     * [
     */
    SQUARE,

    /**
     * ]
     */
    SQUARE_END,

    /**
     * (
     */
    PAREN,

    /**
     * )
     */
    PAREN_END,

    /**
     * def
     */
    DEFINE,

    /**
     * return
     */
    RETURN,

    /**
     * -
     */
    SUB,

    /**
     * +
     */
    ADD,

    /**
     * !
     */
    NOT,

    /**
     * >identifier
     */
    IDENTIFIER,

    /**
     * for
     */
    FOR,

    /**
     * while
     */
    WHILE,

    /**
     * break
     */
    BREAK,

    /**
     * next
     */
    NEXT,

    /**
     * if
     */
    IF,

    /**
     * elif
     */
    ELIF,

    /**
     * else
     */
    ELSE,

    /**
     * ++
     */
    INC,

    /**
     * --
     */
    DEC,

    /**
     * ()
     */
    CALL,

    /**
     * scope
     */
    SCOPE,

    /**
     * do
     */
    DO,

    /**
     * *
     */
    MUL,

    /**
     * /
     */
    DIV,

    /**
     * **
     */
    PWR,

    /**
     * %
     */
    REM,

    /**
     * <
     */
    LT,

    /**
     * <=
     */
    LE,

    /**
     * >
     */
    GT,

    /**
     * >=
     */
    GE,

    /**
     * &
     */
    AND,

    /**
     * |
     */
    OR,

    /**
     * ==
     */
    EQUAL,

    /**
     * ===
     */
    EQUAL_FULL,

    /**
     * !=
     */
    NE,

    /**
     * <<
     */
    SHIFT,

    /**
     * 新增加的 as 二元操作符
     */
    AS,

    /**
     * 特殊的, 对 lexer 无用的 token, 但 parser 会使用到
     */
    END
}
