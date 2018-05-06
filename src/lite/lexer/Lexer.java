package lite.lexer;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * The Lite Lexer
 *
 * @author duangsuse
 * @see lite.Parser
 * @since 1.0
 */
public class Lexer {
    /**
     * Code string
     */
    public String code;

    /**
     * Parsing line
     */
    public int line;

    /**
     * Parsing at column
     */
    public int column;

    /**
     * Parsing char index
     */
    public int c;

    /**
     * Current character
     */
    public char curC;

    /**
     * Result tokens
     */
    public ArrayList<Token> tokens = new ArrayList<>();

    /**
     * Lexer state
     * 0 = null
     * 1 = ignoring comment, expecting newline/eof
     * 2 = building string
     * 3 = building single-quoted string
     * 4 = logging number
     * 5 = logging identifier
     * 66 = error when lexing
     */
    public byte lexerState;

    /**
     * Verbose lex
     */
    public boolean verbose = false;

    /**
     * Error string
     */
    public String error;

    /**
     * Where the string starts
     */
    public int stringStarting;

    /**
     * Temp String builder
     */
    public StringBuilder temp = new StringBuilder();

    /**
     * Split : in identifier (true)
     */
    public boolean splitComma = true;

    /**
     * Blank constructor
     */
    public Lexer() {
    }

    /**
     * Lite code lexer constructor
     *
     * @param lite lite code to lex
     */
    public Lexer(String lite) {
        code = lite;
        line = 0;
        column = 0;
        c = 0;
        lexerState = 0;
    }

    /**
     * Reads file from stdIn, output tokens to stdout
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        boolean verbose = false;
        boolean listOutput = true;
        boolean deflate = false;
        for (String s : args) {
            if (s.equals("-v")) {
                verbose = true;
            }
            if (s.equals("-p")) {
                listOutput = false;
            }
            if (s.equals("-d")) {
                deflate = true;
            }
        }
        Scanner scan = new Scanner(System.in);
        StringBuilder buf = new StringBuilder();
        while (scan.hasNextLine())
            buf.append(scan.nextLine()).append("\n");
        Lexer lex = new Lexer(buf.toString());
        lex.verbose = verbose;
        ArrayList<Token> result = lex.lex();
        if (deflate)
            result = Deflator.deflate(result);
        if (listOutput)
            System.out.println(result.toString());
        else
            for (Token t : result)
                System.out.print(t + " ");
        if (lex.error != null) {
            System.err.println(lex.error);
        }
    }

    // Real lexer logic

    /**
     * Main lexer logic
     * <p>
     * Supported identifier op: . :: -> ()
     * <p>
     * Supported number op: .
     *
     * @return ArrayList of token
     */
    public ArrayList<Token> lex() {
        // return blank list if blank string supplied
        if (code.length() == 0)
            return tokens;
        int strLen = code.length();
        // loop...
        while (c < strLen) {
            curC = code.charAt(c); // set current character
            // ignoring comment
            if (isIgnoringComment()) {
                if (isNewline()) {
                    push(TokenType.NEWLINE);
                    state(0);
                }
                nextC();
                continue;
            }
            // log string/identifier/number
            if (isBuildingString() || isBuildingIdentifier() || isBuildingNumber()) {
                if (isBuildingString()) {
                    if (isStringTerminator()) {
                        pushString();
                        state(0);
                        nextC();
                        continue;
                    }
                    log();
                    nextC();
                    continue;
                }
                if (isBuildingIdentifier()) {
                    if (isAlpha()) {
                        pushIdentifier();
                        state(0);
                        if (isNewline()) {
                            push(TokenType.NEWLINE);
                        }
                        if (lookAhead(1).equals(" ")) {
                            push(TokenType.IDENT);
                            skip(2);
                            continue;
                        }
                        nextC();
                        continue;
                    }
                    if (is('.') || is(':') || is('-') || is('(') || is('[') || is('+') || is('-')) {
                        // is . | :: | -> | ()
                        byte expecting = 0; // .
                        if (is(':'))
                            expecting = 1;
                        else if (is('-'))
                            expecting = 2;
                        else if (is('('))
                            expecting = 3;
                        else if (is('['))
                            expecting = 4;
                        else if (is('+'))
                            expecting = 5;
                        c++;
                        curC = code.charAt(c);
                        // is expecting char
                        if (expecting == 0 || is(':') && expecting == 1 || expecting == 1 && splitComma || expecting == 4 || is('>') && expecting == 2 ||
                                is(')') && expecting == 3 || is('+') && expecting == 5 || is('-') && expecting == 2) {
                            // call on identifier
                            pushIdentifier();
                            state(0);
                            c -= 2; // get back!
                            nextC();
                            continue;
                        }
                        c--;
                        curC = code.charAt(c);
                    }
                    // other terminators
                    if (is(',') || is(')') || is(']') || is('}') || is('(')) {
                        pushIdentifier(); // end of this identifier
                        state(0); // change state to null
                        c -= 1; // get back!
                        nextC(); // next char
                        continue;
                    }
                    log();
                    nextC();
                    continue;
                }
                if (isBuildingNumber()) {
                    if (isAlpha()) {
                        pushNumber();
                        state(0);
                        if (isNewline()) {
                            push(TokenType.NEWLINE);
                        }
                        if (lookAhead(1).equals(" ")) {
                            push(TokenType.IDENT);
                            skip(2);
                            continue;
                        }
                        nextC();
                        continue;
                    }
                    // 才不会告诉你们允许这种格式是为了偷懒 // 删除: 没有了
                    if (is('.')) {
                        c++;
                        curC = code.charAt(c);
                        if (!isNumeric()) {
                            // call on numeric
                            pushNumber();
                            state(0);
                            c -= 2; // get back!
                            nextC();
                            continue;
                        }
                        c--;
                        curC = code.charAt(c);
                    }
                    // other terminators
                    if (is(',') || is(')') || is(']') || is('}')) {
                        pushNumber(); // end of this number
                        state(0); // change state to null
                        c -= 1; // get back!
                        nextC(); // next char
                        continue;
                    }
                    log();
                    nextC();
                    continue;
                }
            }
            // is \n/2 space ident
            if (isAlpha()) {
                if (isNewline()) {
                    push(TokenType.NEWLINE);
                }
                if (lookAhead(1).equals(" ")) {
                    push(TokenType.IDENT);
                    skip(2);
                    continue;
                }
                nextC();
                continue;
            } // skip blanks
            // 不会告诉你们每个 case 都加 nextC() continue 是为了湊行数
            switch (curC) {
                case '"':
                    state(2);
                    stringStarting = c;
                    nextC();
                    continue;
                case '#':
                    state(1);
                    nextC();
                    continue;
                case '\'':
                    state(3);
                    stringStarting = c;
                    nextC();
                    continue;
                case '=':
                    if (lookAhead(2).equals("==")) {
                        push(TokenType.EQUAL_FULL);
                        skip(3);
                        continue;
                    } else if (lookAhead(1).equals("=")) {
                        push(TokenType.EQUAL);
                        skip(2);
                        continue;
                    } else {
                        push(TokenType.EQ);
                    }
                    nextC();
                    continue;
                case '>':
                    if (lookAhead(1).equals("=")) {
                        push(TokenType.GE);
                        skip(2);
                        continue;
                    } else
                        push(TokenType.GT);
                    nextC();
                    continue;
                case '<':
                    if (lookAhead(1).equals("=")) {
                        push(TokenType.LE);
                        skip(2);
                        continue;
                    } else
                        push(TokenType.LT);
                    nextC();
                    continue;
                case '!':
                    if (lookAhead(1).equals("=")) {
                        push(TokenType.NE);
                        skip(2);
                        continue;
                    } else
                        push(TokenType.NOT);
                    nextC();
                    continue;
                case '|':
                    push(TokenType.OR);
                    nextC();
                    continue;
                case '&':
                    push(TokenType.AND);
                    nextC();
                    continue;
                case '/':
                    push(TokenType.DIV);
                    nextC();
                    continue;
                case '*':
                    if (lookAhead(1).equals("*")) {
                        push(TokenType.PWR);
                        skip(2);
                        continue;
                    } else push(TokenType.MUL);
                    nextC();
                    continue;
                case '%':
                    push(TokenType.REM);
                    nextC();
                    continue;
                case '-':
                    String ahead = lookAhead(1);
                    switch (ahead) {
                        case "-":
                            push(TokenType.DEC);
                            skip(2);
                            continue;
                        case ">":
                            push(TokenType.STABBY_OP);
                            skip(2);
                            continue;
                        default:
                            push(TokenType.SUB);
                            nextC();
                            continue;
                    }
                case 'a':
                    // as operator
                    if (lookAhead(1).equals("s") && lookAheadIs("s ")) { // must be a blank there
                        push(TokenType.AS);
                        skip(2);
                        continue;
                    }
                    break;
                case 'f':
                    if (lookAhead(2).equals("or") && lookAheadIs("or ")) { // must be a blank there
                        push(TokenType.FOR);
                        skip(3);
                        continue;
                    } else if (lookAhead(4).equals("alse")) {
                        push(TokenType.FALSE);
                        skip(5);
                        continue;
                    }
                    break;
                case '@':
                    push(TokenType.AT);
                    nextC();
                    continue;
                case 'd':
                    if (lookAhead(2).equals("ef") && lookAheadIs("ef ")) {
                        push(TokenType.DEFINE);
                        skip(3);
                        continue;
                    } else if (lookAhead(1).equals("o") && lookAheadIs("o ")) {
                        push(TokenType.DO);
                        skip(2);
                        continue;
                    }
                    break;
                case 'i':
                    String ahead1 = lookAhead(1);
                    if (ahead1.equals("n") && lookAhead(2).equals("n ")) {
                        push(TokenType.IN);
                        skip(2);
                        continue;
                    } else if (ahead1.equals("f") && lookAhead(2).equals("f ")) {
                        push(TokenType.IF);
                        skip(2);
                        continue;
                    } else if (lookAheadIs("mport ")) {
                        push(TokenType.IMPORT);
                        skip(6);
                        continue;
                    }
                    break;
                case '+':
                    if (lookAhead(1).equals("+")) {
                        push(TokenType.INC);
                        skip(2);
                    } else
                        push(TokenType.ADD);
                    nextC();
                    continue;
                case '.':
                    push(TokenType.DOT);
                    nextC();
                    continue;
                case 'n':
                    if (lookAhead(2).equals("il")) {
                        push(TokenType.NIL);
                        skip(3);
                        continue;
                    } else if (lookAhead(3).equals("ext") && lookAhead(4).equals("ext\n")) {
                        push(TokenType.NEXT);
                        skip(4);
                        continue;
                    }
                    break;
                case '(':
                    if (lookAhead(1).equals(")")) {
                        push(TokenType.CALL);
                        skip(2);
                        continue;
                    }
                    push(TokenType.PAREN);
                    nextC();
                    continue;
                case ')':
                    push(TokenType.PAREN_END);
                    nextC();
                    continue;
                case '{':
                    push(TokenType.BRACE);
                    nextC();
                    continue;
                case '}':
                    push(TokenType.BRACE_END);
                    nextC();
                    continue;
                case '[':
                    push(TokenType.SQUARE);
                    nextC();
                    continue;
                case ']':
                    push(TokenType.SQUARE_END);
                    nextC();
                    continue;
                case 'e':
                    if (lookAheadIs("lif ")) {
                        push(TokenType.ELIF);
                        skip(4);
                        continue;
                    } else if (lookAheadIs("lse\n")) {
                        push(TokenType.ELSE);
                        skip(4);
                        continue;
                    } else if (lookAheadIs("nd\n")) {
                        push(TokenType.END);
                        skip(3);
                        continue;
                    }
                    break;
                case 't':
                    if (lookAhead(3).equals("rue")) {
                        push(TokenType.TRUE);
                        skip(4);
                        continue;
                    } else if (lookAheadIs("race ")) {
                        push(TokenType.TRACE);
                        skip(5);
                        continue;
                    }
                    break;
                case 'b':
                    if (lookAheadIs("reak\n")) {
                        push(TokenType.BREAK);
                        skip(5);
                        continue;
                    }
                    break;
                case ',':
                    push(TokenType.COMMA);
                    nextC();
                    continue;
                case ':':
                    if (lookAhead(1).equals(":")) {
                        push(TokenType.SQUARE_OP);
                        skip(2);
                        continue;
                    } else {
                        push(TokenType.QUOTE);
                    }
                    nextC();
                    continue;
                case 's':
                    if (lookAheadIs("cope\n")) {
                        push(TokenType.SCOPE);
                        skip(5);
                        continue;
                    }
                    break;
                case 'w':
                    if (lookAheadIs("hile ")) {
                        push(TokenType.WHILE);
                        skip(5);
                        continue;
                    }
                    break;
                case 'r':
                    if (lookAheadIs("equire ")) {
                        push(TokenType.REQUIRE);
                        skip(7);
                        continue;
                    } else if (lookAheadIs("eturn ")) {
                        push(TokenType.RETURN);
                        skip(6);
                        continue;
                    }
                    break;
                case '\n': // unused...
                    push(TokenType.NEWLINE);
                    nextC();
                    continue;
            }
            if (isNumeric())
                state(4);
            else
                state(5);
            log();
            nextC();
        }

        push(TokenType.EOF);

        if (isBuildingString())
            error = "Unterminated string from " + stringStarting;
        return tokens;
    }

    /**
     * put the lexer state
     *
     * @param state state to change
     */
    public void state(int state) {
        if (verbose)
            System.out.println("Changing state form " + lexerState + " to " + state);
        lexerState = (byte) state;
    }

    /**
     * State: is ignoring comment
     *
     * @return ignoring comment state
     */
    public boolean isIgnoringComment() {
        return lexerState == 1;
    }

    /**
     * State: is building string
     *
     * @return building string state
     */
    public boolean isBuildingString() {
        return lexerState == 2 || lexerState == 3;
    }

    /**
     * push this char to temp
     */
    public void log() {
        temp.append(curC);
    }

    /**
     * push a token to stack
     *
     * @param t the type of the token
     */
    public void push(TokenType t) {
        if (verbose)
            System.out.println("Pushing " + t.toString());
        tokens.add(new Token(line, t, ""));
    }

    /**
     * Push temp string to stack
     */
    public void pushString() {
        if (verbose)
            System.out.println("Pushing string " + temp.toString());
        tokens.add(new Token(line, lexerState == 3 ? TokenType.SINGLE_QUOTE_STRING : TokenType.STRING, temp.toString()));
        temp = new StringBuilder();
    }

    /**
     * Push an identifier to stack
     */
    public void pushIdentifier() {
        if (verbose)
            System.out.println("Pushing identifier " + temp.toString());
        tokens.add(new Token(line, TokenType.IDENTIFIER, temp.toString()));
        temp = new StringBuilder();
    }

    /**
     * is current character expected character
     *
     * @param c expected character
     * @return true if equals
     */
    public boolean is(char c) {
        return curC == c;
    }

    /**
     * Push number to stack
     */
    public void pushNumber() {
        if (verbose)
            System.out.println("Pushing number " + temp.toString());
        tokens.add(new Token(line, TokenType.NUMBER, temp.toString()));
        temp = new StringBuilder();
    }

    /**
     * is building number
     *
     * @return number building state
     */
    public boolean isBuildingNumber() {
        return lexerState == 4;
    }

    /**
     * is building identifier
     *
     * @return is Building identifier
     */
    public boolean isBuildingIdentifier() {
        return lexerState == 5;
    }

    /**
     * is error state
     *
     * @return error state
     */
    public boolean isError() {
        return lexerState == 66;
    }

    /**
     * next character
     */
    public void nextC() {
        c++;
        if (curC == '\n') {
            line++;
            column = 0;
            if (verbose)
                System.out.println("Newline " + line + " at char " + c);
        } else
            column++;
    }

    /**
     * skip n characters
     *
     * @param n number to skip
     */
    public void skip(int n) {
        if (verbose)
            System.out.println("Skipping " + n + " from " + c + " at line " + line + ":" + column);
        for (; n > 0; n--)
            nextC();
    }

    /**
     * get the [ahead] n chars
     *
     * @param n length of sub-sequence string
     * @return string of next n characters
     */
    public String lookAhead(int n) {
        if (c + n + 1 > code.length())
            return "";
        String result = code.substring(c + 1, c + n + 1);
        if (verbose)
            System.out.println("Looking Ahead: " + n + " from " + c + ":" + result);
        return result;
    }

    /**
     * lookahead is string
     *
     * @param expected expected string
     * @return true if equals
     */
    public boolean lookAheadIs(String expected) {
        String actual = lookAhead(expected.length());
        return actual.equals(expected);
    }

    /**
     * is blank character
     *
     * @return is blank
     */
    public boolean isAlpha() {
        return curC == ' ' || isNewline();
    }

    /**
     * is newline
     *
     * @return char is newline
     */
    public boolean isNewline() {
        return curC == '\n';
    }

    /**
     * is string terminator ' "
     *
     * @return is ' "
     */
    public boolean isStringTerminator() {
        char expected = lexerState == 3 ? '\'' : '"';
        return curC == expected;
    }

    /**
     * is start of comment
     *
     * @return is start of comment
     */
    public boolean isCommentStart() {
        return curC == '#';
    }

    /**
     * is numeric
     *
     * @return is numeric start
     */
    public boolean isNumeric() {
        switch (curC) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if (verbose)
                    System.out.println(curC + " is Number");
                return true;
        }
        if (verbose)
            System.out.println(curC + " not Number");
        return false;
    }

    /**
     * lookahead is ident '  '
     *
     * @return next 2 char is '  '
     */
    public boolean lookAheadIsIdent() {
        return lookAhead(2).equals("  ");
    }

    /**
     * lookahead is newline
     *
     * @return next character is newline
     */
    public boolean lookAheadIsNewline() {
        return lookAhead(1).equals("\n");
    }
}
