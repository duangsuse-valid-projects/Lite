package lite.lexer;

import java.util.ArrayList;

/**
 * Converts a lite ident-based syntax to (more...) context-free grammar for postprocessing
 *
 * @author duangsuse
 * @version 1.0
 * @since 1.0
 */
// NOTE: 只作测试用, CUP 会解决所有解析任务
public class Deflator {
    /**
     * Insert end in the end of token block
     *
     * @param tokens block-list tokens
     * @return block list with end token
     */
    public static ArrayList<Token> deflate(ArrayList<Token> tokens) {
        boolean loggingIdentLevel = false;
        int currentIdentLevel = 0;
        int lastIdentLevel = 0;
        ArrayList<Token> ret = new ArrayList<>();
        Token curT;
        for (int i = 0; i < tokens.size(); i++) {
            curT = tokens.get(i);
            if (loggingIdentLevel && !curT.isNewline()) {
                if (curT.isIdent())
                    currentIdentLevel++;
                else {
                    if (lastIdentLevel > currentIdentLevel) { // leaving scope
                        ret.add(new Token(curT.line - 3, TokenType.END, ""));
                        ret.add(new Token(curT.line - 2, TokenType.NEWLINE, ""));
                        for (int j = 0; j < currentIdentLevel; j++) {
                            ret.add(new Token(curT.line - 1 + i, TokenType.IDENT, ""));
                        }
                        currentIdentLevel++;
                    }
                    loggingIdentLevel = false;
                    lastIdentLevel = currentIdentLevel;
                    currentIdentLevel = 0;
                }
            }
            if (curT.isNewline()) { // now at end-of-line, next token should be ident
                // get the ident level
                loggingIdentLevel = true;
            }
            ret.add(curT);
        }
        return ret;
    }

    /*
     * 上面的 Deflator 其实有个 bug:
     * 对于这样的缩进会少加空格
     * def a_name arg1 arg2 arg3
     *   if a
     *     if b
     *       return 1
     * 只有 def 会被添加 end, 这是由于它只检查 leaveScope 的原因, 下面的这个新 deflator 可以解决这个问题
     * 这个基于标准 block 缩进的理念, 使用一个 Vector 来保证所有 entered scope 都得到 end // 还是有坑, 决定出绝招递归了
     */

    /**
     * Insert end in the end of token block, reborn
     *
     * @param tokens block-list tokens
     * @return block list with end token
     */
    // TODO ........... duangsuse 只好承认自己的失败, 我写了一个上午 尝试了无数次
    public static ArrayList<Token> deflateR(ArrayList<Token> tokens) {
        return deflate(tokens);
    }

    /*
     * Get a block tokens
     *
     * @param level    block level
     * @param starting block starting
     * @return block tokens
     *//*
    public static ArrayList<Token> deflateRecursive(ArrayList<Token> tokens, int level, int starting) {
        int i = starting;
        ArrayList<Token> ret = new ArrayList<>();
        while (i < tokens.size()) {
            Token t = tokens.get(i);
            if (isChunkFollowedBy(t.type))
                ret.add(deflateRecursive(tokens, level + 1, i));
            if (!(ident(tokens, starting) == level)) {
                i++;
                continue;
            }
            if (!t.isIdent())
                ret.add(t);
            i++;
        }
        return ret;
    }

    public static int ident(ArrayList<Token> tokens, int starting) {
        int level = 0;
        boolean beforeNewline = false;
        while (true)
            if (tokens.get(starting).isIdent() && beforeNewline) {
                level++;
                starting++;
            } else if (tokens.get(starting).isNewline()) {
                beforeNewline = true;
            } else {
                return level;
            }
    }*/

    /**
     * Is chunk followed by the token
     *
     * @param t token type
     * @return true if a block is in next line
     */
    public static boolean isChunkFollowedBy(TokenType t) {
        switch (t) {
            case WHILE: // while expr NEWLINE IDENT block
            case FOR: // for identifier in expr NEWLINE IDENT block
            case DEFINE: // def identifier args NEWLINE IDENT block
            case DO: // do OR args OR NEWLINE IDENT block
            case SCOPE: // scope NEWLINE IDENT block
            case IF: // if expr NEWLINE IDENT block { elif expr NEWLINE IDENT block } { else NEWLINE IDENT block }
            case ELIF:
            case ELSE:
                return true;
        }
        return false;
    }
}
