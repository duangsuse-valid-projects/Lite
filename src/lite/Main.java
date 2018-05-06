package lite;

import lite.lexer.Deflator;
import lite.lexer.Lexer;
import lite.lexer.Token;
import lite.lexer.TokenType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Main class (a executor) of lite
 *
 * @author duangsuse
 * @since 1.0
 */
public class Main {
    /**
     * Main method invoked by JVM
     * - to use System.in is supported
     *
     * @param args command line, a set of file to execute
     * @throws IOException                    if file path/perm bad
     * @throws lite.Parser.MalformedCodeError if code bad
     */
    public static void main(String[] args) throws IOException, Parser.MalformedCodeError {
        Interpreter interpreter = new Interpreter();
        File f = null;
        InputStream fin = null;
        Scanner scan = new Scanner(System.in);
        boolean lex = false;
        boolean lexDebug = false;
        boolean lexPp = false;
        boolean lexD = false;
        boolean deflateR = false;
        for (String path : args) {
            if (!path.equals("-")) {
                if (path.equals("-lex"))
                    lex = true;
                if (path.equals("-lexV")) {
                    lex = true;
                    lexDebug = true;
                }
                if (path.equals("-lexP")) {
                    lex = true;
                    lexPp = true;
                }
                if (path.equals("-lexD")) {
                    lex = true;
                    lexPp = true;
                    lexD = true;
                    lexDebug = true;
                }
                if (path.equals("-lexDR")) {
                    lex = true;
                    lexPp = true;
                    deflateR = true;
                }
                if (lex) {
                    // use lexer
                    String code;
                    StringBuilder str = new StringBuilder();
                    while (scan.hasNextLine())
                        str.append(scan.nextLine()).append("\n");
                    code = str.toString();
                    Lexer lexer = new Lexer(code);
                    if (lexDebug)
                        lexer.verbose = true;
                    ArrayList<Token> result = lexer.lex();
                    if (lexD)
                        result = Deflator.deflate(result);
                    if (deflateR)
                        result = Deflator.deflateR(result);
                    if (!lexPp)
                        System.out.println(result.toString());
                    else
                        for (int i = 0, resultSize = result.size(); i < resultSize; i++) {
                            Token t = result.get(i);
                            Token nextT = new Token();
                            if (i + 1 < resultSize)
                                nextT = result.get(i + 1);
                            String tokenString;
                            // identify...
                            tokenString = (t.isNewline() || nextT.is(TokenType.CALL) || t.isIdentifier() && (nextT.isComma() || nextT.is(TokenType.DOT) || nextT.is(TokenType.QUOTE) || nextT.is(TokenType.SQUARE_OP)) || nextT.is(TokenType.STABBY_OP)) || t.isAt() ||
                                    t.is(TokenType.SQUARE_OP) || (t.is(TokenType.NUMBER) || t.is(TokenType.TRUE) || t.is(TokenType.FALSE) || t.is(TokenType.NIL)) && nextT.isComma() || t.is(TokenType.DOT) || t.is(TokenType.STABBY_OP) ? t.toString() : t + " ";
                            if (t.isKeyword())
                                tokenString = escapeANSIColorTerm((byte) 51, tokenString);
                            else if (t.isValue() && !(t.is(TokenType.STRING) || t.is(TokenType.SINGLE_QUOTE_STRING)))
                                tokenString = escapeANSIColorTerm((byte) 52, tokenString);
                            else if (t.is(TokenType.STRING) || t.is(TokenType.SINGLE_QUOTE_STRING))
                                tokenString = escapeANSIColorTerm((byte) 54, tokenString);
                            else if (t.isBinaryOperator() || t.isAt())
                                tokenString = escapeANSIColorTerm((byte) 49, tokenString);
                            else if (t.isUnaryOperator())
                                tokenString = escapeANSIColorTerm((byte) 53, tokenString);
                            else if (t.isIdentifier())
                                tokenString = escapeANSIColorTerm((byte) 55, tokenString);
                            System.out.print(tokenString);
                        }
                }
                f = new File(path);
                if (!f.canRead())
                    System.exit(1);
                fin = new FileInputStream(f);
            }
            Date start = new Date();
            Object result;
            if (fin != null) {
                result = interpreter.eval(fin);
            } else {
                StringBuilder str = new StringBuilder();
                while (scan.hasNextLine())
                    str.append(scan.nextLine()).append("\n");
                result = interpreter.eval(str.toString());
            }
            Date end = new Date();
            if (result == null)
                result = "<null>";
            System.err.println(String.format("%1$s(%2$s): %3$s %4$s", f != null ? f.getCanonicalPath() : '-', end.compareTo(start), result.getClass().getCanonicalName(), result));
            if (fin != null) {
                fin.close();
            } // close file
            f = null; // clear file for next loop
            fin = null;
        }
    }

    /**
     * Escape a ANSI terminal colored text
     *
     * @param color the color to use, 51: yellow, 52: blue, 53: pink, 54: cyan, 49: red
     * @param str   the string to escape
     * @return escaped string
     */
    public static String escapeANSIColorTerm(byte color, String str) {
        byte[] lhs = new byte[]{27, 91, 57, color, 109};
        byte[] strBytes = str.getBytes();
        byte[] rhs = new byte[]{27, 91, 48, 109};
        ByteBuffer buffer = ByteBuffer.allocate(lhs.length + strBytes.length + rhs.length);
        buffer.put(lhs);
        buffer.put(strBytes);
        buffer.put(rhs);
        return new String(buffer.array());
    }
}
