package lite;

import lite.ast.*;
import lite.ast.data.LiteList;
import lite.ast.data.LiteTable;
import lite.ast.misc.*;
import lite.lexer.Deflator;
import lite.lexer.Lexer;
import lite.lexer.Token;
import lite.lexer.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Lite parser
 *
 * @author 因为下不了 Cup IDEA 插件 而依然手写 LALR 解析器的 duangsuse
 * @author 因为手写即使是递归下降太麻烦而使用 Compiler Compiler 的 duangsuse
 * @author 非常生气的 duangsuse, 好气 白写了一晚上 parser, 倒车, 而且由于 end 不使用导致的 F**king problem 决定还是尝试重写 deflator -- 算了先做好要紧加个 end token
 * @implNote 手写 LALR(1) 简直丧病 LALR 那么机械的东西
 * @since 1.0
 * @deprecated duangsuse 很生气就没手写这么麻烦的 Parser, 比冰封那个 Lice 的 Parser 困难多了, S-expression 解析器多好写, 改天绝对重写个手写 Parser 和新解释器 换掉 Lite 1.0
 */
// TODO implement the f**king recursive top-down parser F**KING parser
@Deprecated
public class Parser {
    /**
     * The code path
     */
    public String path = "<unknown>";

    /**
     * The code string
     */
    public String code;

    /**
     * Be more verbose
     */
    public boolean verbose;

    /**
     * current token index
     */
    public int curT;

    /**
     * The tokens
     */
    public ArrayList<Token> tokens = new ArrayList<>();

    /**
     * Parse a lite code string
     *
     * @param s lite code
     */
    public Parser(String s) {
        code = s;
    }

    /**
     * The main method of this class
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        boolean verbose = false;
        for (String s : args) {
            if (s.equals("-v")) {
                verbose = true;
            }
        }
        Scanner scan = new Scanner(System.in);
        StringBuilder buf = new StringBuilder();
        while (scan.hasNextLine())
            buf.append(scan.nextLine()).append("\n");
        Parser recursiveParser = new Parser(buf.toString());
        recursiveParser.verbose = verbose;
        LiteBlock result = null;
        try {
            result = recursiveParser.parse();
        } catch (MalformedCodeError malformedCodeError) {
            malformedCodeError.printStackTrace();
        }
        System.out.println(result);
    }

    /**
     * Parse this code, return a block
     *
     * @return code block
     */
    public LiteBlock parse() throws MalformedCodeError {
        LiteBlock ret = new LiteBlock();
        if (code.length() == 0)
            return ret;
        // begin logic
        Lexer lexer = new Lexer(code); // lex the code!
        if (verbose)
            lexer.verbose = true;
        tokens = lexer.lex();
        if (lexer.error != null)
            error("Lexer failure: " + lexer.error);
        // now that's all right
        putsV("Lexer finish: " + tokens);
        // yyBlock 23333
        ret = yyBlock(0, 0);
        return ret;
    }

    /*
     * Complete Lite Syntax (DNF 范式, 是 duangsuse 设计的一种即使没有规则你们也能看懂的无上下文词条流模式文法描述)
     * (看起来很高大上的东西, 其实没啥用.....)
     * Lite 的一个比较特殊的地方在于使用缩进语义, 我也是为了好看... 不过如果使用递归下降法, 解析不是问题耶
     * 强制你使用 duangsuse 喜欢的 2 空格缩进代码风格, 语言本身类似 Ruby (Ruby 岛国语言好耶)
     * 有趣的语法: ![str1 str2 str3] . each { |e| puts e } if a == 1 & b === :c
     *
     * #### TABLED symbol ####
     * newline  : '\n'
     * ident    : '  '
     * l_square : '['
     * r_square : ']'
     * let      : '='
     * at       : '@'
     * call     : '()'
     * bang     : '!'
     * sub      : '-'
     * inc      : '++'
     * dec      : '--'
     * #### FINISH symbol ####
     *
     * math -> expr Maybe( '+' OR '-' OR '*' OR '/' OR '**' OR '%' OR '<' OR '<=' OR '>' OR '>=' OR '&' OR '|' OR '==' OR '===' OR '!=' OR '<<' ) expr
     * binary -> math | cast | dot | in | square | stabby
     * expression -> binary | list | table | value | incDec | not | negative | call | identifier | index | blockProcedure | doBlock
     * statement -> def | for | scope | while | if | excited_statement
     * excited_statement -> break | next | import | require | return | trace | assignment | indexLet | square | stabby | dot | incDec | call Maybe( IF expression )
     * block -> Ary( IDENT statement NEWLINE )
     * for -> FOR identifier IN expression NEWLINE block
     * while -> WHILE expression NEWLINE block
     * scope -> SCOPE NEWLINE block
     * indexLet -> expression L_SQUARE expression R_SQUARE LET expression
     * index -> expression L_SQUARE expression R_SQUARE
     * if -> IF expression NEWLINE block Maybe( Ary( IDENT ELIF expression NEWLINE block ) ) Maybe( IDENT ELSE NEWLINE block )
     * identifier -> Maybe( AT ) label
     * def -> DEF identifier Maybe( nameList ) NEWLINE block
     * call -> identifier Maybe( CALL OR exprList )
     * assignment -> identifier LET expression
     * not -> BANG expression
     * negative -> SUB expression
     * incDec -> identifier Maybe( INC OR DEC )
     * trace -> TRACE Maybe( Ary( Any() ) )
     * return -> RETURN expression
     * require -> REQUIRE Any()
     * next -> NEXT
     * break -> BREAK
     * import -> IMPORT Maybe( Ary( Any() ) )
     * value -> TRUE | FALSE | NIL | Number() | string
     * string -> '"' Maybe( Ary( Any() ) ) '"' | stringB | stringC
     * stringB -> "'" Maybe( Ary( Any() ) ) "'"
     * stringC -> ':' label
     * list -> Maybe( BANG ) L_SQUARE exprList R_SQUARE
     * table -> '{' kvList '}'
     * kvList -> Ary( label ':' expression Maybe( ',' OR NEWLINE ) )
     * stabby -> expression '->' label expression
     * square -> expression '::' label
     * in -> expression IN expression
     * dot -> expression '.' label Maybe( CALL OR exprList )
     * cast -> expression AS label
     * exprList -> Ary( expr Maybe( ' ' OR ',' ) )
     * nameList -> Maybe( '(' ) Ary( name Maybe( ',' OR ' ' ) ) Maybe( ')' )
     * nameListB -> '|' Ary( name Maybe( ',' OR ' ' ) ) '|'
     * blockProcedure -> '{' Maybe( nameListB ) Ary( excited_statement ':' ) '}'
     * doBlock -> DO Maybe ( nameListB ) block
     */

    /**
     * Parses a lite block
     *
     * @param level    block ident level
     * @param starting block starting at token index
     * @return the block
     */
    /*
    Lite 块是一些 Lite 表达式或语句的集合
    这个方法预期块的第一个 ident token 位置
    这个方法能判断添加 break/next/import/require/return/trace 语句
    同时可以判断使用 dot/stabby/incDec/assignment/call/def/for/if/indexLet/scope/while
     * statement -> def | for | scope | while | if | excited_statement
     * excited_statement -> break | next | import | require | return | trace | assignment | indexLet | square | stabby | dot | incDec | call Maybe( IF expression )
     * block -> Ary( IDENT statement NEWLINE )
     * TODO support statement IF expression
     */
    public LiteBlock yyBlock(int level, int starting) {
        LiteBlock blk = new LiteBlock(); // let's make a block first
        int savedCurt = curT;
        curT = starting;
        putsV(String.format("Parsing block level %1$s: %2$s at token %3$s", level, savedCurt, starting));
        while (ident() == level && lookAhead().isNotEof()) {
            putsV("Token: " + t());
            // types that have a chunk
            if (Deflator.isChunkFollowedBy(t().type)) {
                if (t().is(TokenType.DEFINE)) {
                    blk.addStatement(fill(yyDef(curT)));
                } else if (t().is(TokenType.FOR)) {
                    blk.addStatement(fill(yyFor(curT)));
                } else if (t().is(TokenType.SCOPE)) {
                    blk.addStatement(fill(yyScope(curT)));
                } else if (t().is(TokenType.WHILE)) {
                    blk.addStatement(fill(yyWhile(curT)));
                } else if (t().is(TokenType.IF)) {
                    blk.addStatement(fill(yyIf(curT)));
                }
                int thatIdent = identLevel(); // indent level of that block
                putsV("Skipping sub-block " + t().type + " level: " + thatIdent);
                skip(newLineDistance()); // go to new line
                while (ident() == thatIdent) {
                    nextT(); // skip that chunk
                }
                nextT();
                continue;
            }

            // parse a line
            skip(level);
            if (t().is(TokenType.BREAK)) {
                blk.addStatement(fill(new LiteBreak()));
                skip(2);
                continue;
            } else if (t().is(TokenType.NEXT)) {
                blk.addStatement(fill(new LiteNext()));
                skip(2);
                continue;
            } else if (t().is(TokenType.IMPORT)) {
                StringBuilder b = new StringBuilder();
                skip(1);
                for (Token i : tokensTillNl())
                    b.append(i.toString());
                blk.addStatement(fill(new LiteImport(b.toString())));
                skip(newLineDistance());
                continue;
            } else if (t().is(TokenType.REQUIRE)) {
                skip(1);
                StringBuilder b = new StringBuilder();
                for (Token i : tokensTillNl())
                    b.append(i.toString());
                blk.addStatement(fill(new LiteRequire(b.toString())));
                skip(newLineDistance());
                continue;
            } else if (t().is(TokenType.RETURN)) {
                nextT();
                blk.addStatement(fill(new LiteReturn(yyExpr(tokensTillNl()))));
                skip(newLineDistance());
                continue;
            } else if (t().is(TokenType.TRACE)) {
                skip(1);
                StringBuilder b = new StringBuilder();
                for (Token i : tokensTillNl())
                    b.append(i.toString());
                blk.addStatement(fill(new LiteTrace(b.toString())));
                skip(newLineDistance());
                continue;
            }
            // 接下来看看是不是 assignment/indexLet, 否则就是 exprStmt(square/stabby/dot/incDec/call)
            // assignment: '@'? label '=' expr
            // indexLet: expr '[' expr ']' '=' expr
            LinkedList<Token> tks = tokensTillNl();
            boolean hasMatch = false;
            boolean at;
            if (tks.size() == 0) {
                nextT();
                continue;
            }
            if (tks.size() >= 3)
                if ((at = tks.get(0).isAt()) || tks.get(0).isIdentifier())
                    if (tks.get(2).is(TokenType.EQ) && at || tks.get(1).is(TokenType.EQ) && !at) {
                        // push identifier
                        blk.addStatement(fill(new LiteIdentifier(at, at ? tks.get(1).data : tks.get(0).data)));
                        hasMatch = true;
                    }
            // let's see indexLet
            byte state = 0; // 0: null 1: got [ 2: got [] 3: got []=
            for (Token t : tks) {
                if (t.is(TokenType.SQUARE))
                    state = 1;
                if (t.is(TokenType.SQUARE_END) && state == 1)
                    state = 2;
                if (t.is(TokenType.EQ) && state == 2)
                    state = 3;
            }
            // if is indexLet
            if (state == 3) {
                LinkedList<Token> exprTokens = new LinkedList<>();
                LinkedList<Token> lhsTokens = new LinkedList<>();
                LinkedList<Token> rhsTokens = new LinkedList<>();
                LinkedList<Token> valueTokens = new LinkedList<>();
                for (Token t : tks) {
                    if (t.is(TokenType.SQUARE)) {
                        lhsTokens = exprTokens;
                        exprTokens.clear();
                    }
                    if (t.is(TokenType.SQUARE_END)) {
                        rhsTokens = exprTokens;
                        exprTokens.clear();
                    }
                    if (t.isNewline()) {
                        valueTokens = exprTokens;
                        valueTokens.add(new Token(233, TokenType.NEWLINE, ""));
                        exprTokens.clear();
                    }
                    exprTokens.add(t);
                }
                blk.addStatement(fill(new LiteIndexLet(yyExpr(lhsTokens), yyExpr(rhsTokens), yyExpr(valueTokens))));
            }
            // is expression-as-statement
            if (!hasMatch)
                blk.addStatement(fill(yyExpr(tokensTillNl())));
            nextT();
        }
        curT = savedCurt;
        return blk;
    }

    /**
     * Parses a lite expression
     *
     * @param tokens expression tokens
     * @return constructed lite expression node
     */
    /*
    Lite 有这些表达式：
    BinaryOperator, 包含了数学运算符 + - * /...
    LiteCast, Character() as String
    LiteDot, String.format :healthy
    LiteIn, "" in [ "" ]
    LiteSquare, Runtime::runtime
    LiteStabby, wrappedStr->wrappedValue "Be a gOO(OO(OO))OGC GAY"
    LiteList, ![I am a fooOOoOl].each { |e| puts e }
    LiteTable, @a = { a: b, b: @c, c: System.in }
    LiteValue, true false nil 233333333 "五系喳\n扎灰" '像孤单的类库家... 或许我不该' "'" '"'
    LiteIncDec, i++ @i++ @i-- 虽然我不想
    LiteNegative, -2333333333, -a
    LiteNot, !a !我是布帘儿（没错支持中文（没错Java支持（Java好耶）（没错允许这样的标识符）））
    Call, a() @a() AClass() AComplexClass 1 2 3 'foo'
    Identifier, abc a123 imported required（not_require）
    Index, a[] @a[] Hashtable()['2333333']

     * math -> expr Maybe( '+' OR '-' OR '*' OR '/' OR '**' OR '%' OR '<' OR '<=' OR '>' OR '>=' OR '&' OR '|' OR '==' OR '===' OR '!=' OR '<<' ) expr
     * binary -> math | cast | dot | in | square | stabby
     * expression -> binary | list | table | value | incDec | not | negative | call | identifier | index | blockProcedure | doBlock
     * index -> expression L_SQUARE expression R_SQUARE
     * identifier -> Maybe( AT ) label
     * call -> identifier Maybe( CALL OR exprList )
     * not -> BANG expression
     * negative -> SUB expression
     * incDec -> identifier Maybe( INC OR DEC )
     * value -> TRUE | FALSE | NIL | Number() | string
     * string -> '"' Maybe( Ary( Any() ) ) '"' | stringB | stringC
     * stringB -> "'" Maybe( Ary( Any() ) ) "'"
     * stringC -> ':' label
     * list -> Maybe( BANG ) L_SQUARE exprList R_SQUARE
     * table -> '{' kvList '}'
     * kvList -> Ary( label ':' expression Maybe( ',' OR NEWLINE ) )
     * stabby -> expression '->' label expression
     * square -> expression '::' label
     * in -> expression IN expression
     * dot -> expression '.' label Maybe( CALL OR exprList )
     * cast -> expression AS label
     */
    public LiteNode yyExpr(LinkedList<Token> tokens) {
        return null;
    }

    /**
     * Fill the node with correct debug data
     *
     * @param node node to fill sourceFile, line
     * @return filled node
     */
    public LiteNode fill(LiteNode node) {
        if (node == null)
            return null;
        putsV("Filling node " + node + ": " + path + ": " + t().line);
        node.sourceFile = this.path;
        node.sourceLine = this.t().line;
        return node;
    }

    /**
     * Parse a expression list
     *
     * @param starting expression list starting token
     * @return constructed expression list
     */
    public LinkedList<LiteNode> yyExprList(int starting) {
        return null;
    }

    /**
     * Parse a name list
     *
     * @param starting name list starting
     * @return name list
     */
    public ArrayList<String> yyNameList(int starting) {
        return null;
    }

    /**
     * yyParse a while statement
     *
     * @param starting while statement (keyword) starting
     * @return constructed while statement
     */
    public LiteWhile yyWhile(int starting) {
        return null;
    }

    /**
     * yyParse a scope statement
     *
     * @param starting scope starting
     * @return constructed scope node
     */
    public LiteScope yyScope(int starting) {
        return null;
    }

    /**
     * yyParse a for statement
     *
     * @param starting for starting
     * @return constructed for node
     */
    public LiteFor yyFor(int starting) {
        return null;
    }

    /**
     * yyParse a if statement
     *
     * @param starting if keyword starting
     * @return constructed if ast node
     */
    public LiteIf yyIf(int starting) {
        return null;
    }

    /**
     * yyParse a define statement
     *
     * @param starting def keyword starting
     * @return constructed def node
     */
    public LiteDef yyDef(int starting) {
        return null;
    }

    /**
     * yyParse a do statement
     *
     * @param starting scope starting
     * @return constructed scope node
     */
    public LiteBlock yyDo(int starting) {
        return null;
    }

    /**
     * yyParse a procedure expression
     *
     * @param starting procedure starting
     * @return procedure
     */
    public LiteBlock yyProcedure(int starting) {
        return null;
    }

    /**
     * yyParse an identifier
     *
     * @param starting identifier starting
     * @return constructed identifier
     */
    public LiteIdentifier yyIdentifier(int starting) {
        return null;
    }

    /**
     * yyParse a index expression
     *
     * @param starting expression starting
     * @return fool ast node
     */
    public LiteIndex yyIndex(int starting) {
        return null;
    }

    /**
     * yyParse a indexLet statement
     *
     * @param starting statement starting
     * @return constructed node
     */
    public LiteIndexLet yyIndexLet(int starting) {
        return null;
    }

    /**
     * yyParse a call code
     *
     * @param starting call starting
     * @return call ast node
     */
    public LiteCall yyCall(int starting) {
        return null;
    }

    /**
     * yyParse a assignment statement
     *
     * @param starting statement starting
     * @return constructed node
     */
    public LiteAssignment yyAssignment(int starting) {
        return null;
    }

    /**
     * yyParse a list expression
     *
     * @param starting expression starting
     * @return list initializer
     */
    public LiteList yyList(int starting) {
        return null;
    }

    /**
     * yyParse a table initializer
     *
     * @param starting table starting
     * @return constructed table initializer node
     */
    public LiteTable yyTable(int starting) {
        return null;
    }

    // 补充一下, 上面打错了, 手写当然是递归下降法

    /**
     * Put a verbose message
     *
     * @param msg The message
     */
    public void putsV(String msg) {
        if (verbose)
            System.out.println(msg);
    }

    /**
     * Get steps till new-line
     *
     * @return steps till new-line
     */
    public int newLineDistance() {
        int distance = 0;
        int saved_curT = curT;
        while (!t().isNewline()) {
            curT++;
            distance++;
        }
        curT = saved_curT;
        return distance;
    }

    /**
     * Get the current token
     *
     * @return current token
     */
    public Token t() {
        return tokens.get(curT);
    }

    /**
     * Increase token index
     */
    public void nextT() {
        curT++;
    }

    /**
     * Skip n tokens
     *
     * @param step step to skip
     */
    public void skip(int step) {
        curT += step;
    }

    /**
     * Tokens till new-line
     *
     * @return token till newline
     */
    public LinkedList<Token> tokensTillNl() {
        LinkedList<Token> ret = new LinkedList<>();
        int n = curT;
        while (n < tokens.size()) {
            Token tt = tokens.get(n);
            if (tt.isNewline())
                break;
            ret.add(tt);
            n++;
        }
        return ret;
    }

    /**
     * lookAhead one token
     *
     * @return next token
     */
    public Token lookAhead() {
        if (curT >= tokens.size() - 1)
            return new Token(0, TokenType.EOF, "");
        return tokens.get(curT + 1);
    }

    /**
     * Get the ident level from this line
     *
     * @return ident level of current line
     */
    public int ident() {
        int last_t = curT;
        int level = 0;
        while (lookAhead().isNotEof())
            if (t().isIdent()) {
                level++;
                nextT();
            } else {
                curT = last_t;
                return level;
            }
        curT = last_t;
        return level;
    }

    /**
     * Get next line ident level
     *
     * @return indent level of next line
     */
    public int identLevel() {
        int last_curT = curT;
        int level = 0;
        boolean logging = false;
        while (lookAhead().isNotEof()) {
            if (t().isNewline())
                logging = true;
            if (t().isIdent())
                if (logging)
                    break;
            if (logging)
                level++;
            nextT();
        }
        curT = last_curT;
        return level;
    }

    /**
     * Interrupt with a message
     *
     * @param message the error message
     * @throws MalformedCodeError always thrown
     */
    public void error(String message) throws MalformedCodeError {
        throw new MalformedCodeError(path, t().line, message);
    }

    /**
     * Bad code!
     */
    public class MalformedCodeError extends Exception {
        /**
         * Error at file
         */
        public String file = "<unknown>";

        /**
         * Error at line
         */
        public int line = 0;

        /**
         * Error message
         */
        public String message = "Unknown";

        /**
         * Blank constructor
         */
        public MalformedCodeError() {
        }

        /**
         * Default constructor
         *
         * @param line at line
         * @param msg  message
         */
        public MalformedCodeError(String file, int line, String msg) {
            this.file = file;
            this.line = line;
            message = msg;
        }

        /**
         * line: message
         *
         * @return "$line: $msg"
         */
        @Override
        public String toString() {
            return line + ": " + message;
        }
    }
}
