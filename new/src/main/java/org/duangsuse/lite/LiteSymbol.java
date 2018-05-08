package org.duangsuse.lite;

/**
 * The symbol class used by lite lexer
 *
 * @author duangsuse
 * @version 1
 * @see java_cup.runtime.Symbol
 * @since 1.1
 */
public class LiteSymbol extends java_cup.runtime.Symbol {
    /**
     * The symbol is seen at line
     */
    private int line;

    /**
     * The symbol is seen at column
     */
    private int column;

    /**
     * Construct with no value but type and debug info
     *
     * @param type   symbol type
     * @param line   seen at line
     * @param column seen at column
     */
    public LiteSymbol(int type, int line, int column) {
        this(type, line, column, -1, -1, null);
    }

    /**
     * Construct with value and type, debug info
     *
     * @param type   type of this token
     * @param line   seen at line
     * @param column seen at column
     * @param value  token value
     */
    public LiteSymbol(int type, int line, int column, Object value) {
        this(type, line, column, -1, -1, value);
    }

    /**
     * Construct with value, type, debug info and l/r
     *
     * @param type   type of the token
     * @param line   line seen
     * @param column column seen
     * @param left   left
     * @param right  right
     * @param value  symbol value
     * @see java_cup.runtime.Symbol#left
     * @see java_cup.runtime.Symbol#right
     */
    public LiteSymbol(int type, int line, int column, int left, int right, Object value) {
        super(type, left, right, value);
        this.line = line;
        this.column = column;
    }

    /**
     * Get the symbol line
     *
     * @return symbol seen at line
     */
    public int getLine() {
        return line;
    }

    /**
     * Get the symbol column
     *
     * @return symbol seen at column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the symbol string
     *
     * @return "line $line, column $column, sym: $sym(, value: value)"
     */
    public String toString() {
        return "line " + line + ", column " + column + ", sym: " + sym + (value == null ? "" : (", value: '" + value + "'"));
    }
}
