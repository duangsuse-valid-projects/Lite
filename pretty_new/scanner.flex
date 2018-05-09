/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 1998-2015  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* Java 1.2 language lexer specification */

/* Modified by duangsuse to compat Lite lexical rules */

/* Use together with unicode.flex for Unicode preprocesssing */
/* and java12.cup for a Java 1.2 parser                      */

/* Note that this lexer specification is not tuned for speed.
   It is in fact quite slow on integer and floating point literals,
   because the input is read twice and the methods used to parse
   the numbers are not very fast.
   For a production quality application (e.g. a Java compiler)
   this could be optimized */


import java_cup.runtime.*;

%%

%public
%class Lexer
%implements sym

%unicode

%line
%column

%cup
%cupdebug

%{
  StringBuilder string = new StringBuilder();

  private Symbol symbol(int type) {
    return new LiteSymbol(type, yyline + 1, yycolumn + 1);
  }

  private Symbol symbol(int type, Object value) {
    return new LiteSymbol(type, yyline + 1, yycolumn + 1, value);
  }

  /**
   * assumes correct representation of a long value for
   * specified radix in scanner buffer from <code>start</code>
   * to <code>end</code>
   */
  private long parseLong(int start, int end, int radix) {
    long result = 0;
    long digit;

    for (int i = start; i < end; i++) {
      digit = Character.digit(yycharat(i),radix);
      result *= radix;
      result += digit;
    }

    return result;
  }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} |
          {DocumentationComment}

TraditionalComment = ">#" [^*] ~"#<" | ">#" "*"+ "#<"
EndOfLineComment = "#" {InputCharacter}* {LineTerminator}?
DocumentationComment = ">#" "*"+ [^>#] ~"#<"

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]

/* floating point literals */
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

%state STRING, STRING_SINGLE

%%

<YYINITIAL> {

  /* keywords */
  "def"                          { return symbol(DEFINE); }
  "do"                           { return symbol(DO); }
  "break"                        { return symbol(BREAK); }
  "next"                         { return symbol(NEXT); }
  "return"                       { return symbol(RETURN); }
  "scope"                        { return symbol(SCOPE); }
  "while"                        { return symbol(WHILE); }
  "for"                          { return symbol(FOR); }
  "in"                           { return symbol(IN); }
  "as"                           { return symbol(AS); }
  "to"                           { return symbol(TO); }
  "if"                           { return symbol(IF); }
  "elif"                         { return symbol(ELIF); }
  "else"                         { return symbol(ELSE); }
  "import"                       { return symbol(IMPORT); }
  "require"                      { return symbol(REQUIRE); }
  "end"                          { return symbol(END); }
  "and"                          { return symbol(ANDK); }
  "or"                           { return symbol(ORK); }

  /* boolean literals */
  "true"                         { return symbol(BOOLEAN_LITERAL, true); }
  "false"                        { return symbol(BOOLEAN_LITERAL, false); }

  /* null literal */
  "nil"                          { return symbol(NULL_LITERAL); }

  /* separators */
  "("                            { return symbol(LPAREN); }
  ")"                            { return symbol(RPAREN); }
  "{"                            { return symbol(LBRACE); }
  "}"                            { return symbol(RBRACE); }
  "["                            { return symbol(LBRACK); }
  "]"                            { return symbol(RBRACK); }
  ";"                            { return symbol(SEMICOLON); }
  ","                            { return symbol(COMMA); }
  "."                            { return symbol(DOT); }
  "@"                            { return symbol(AT); }

  /* operators */
  "="                            { return symbol(EQ); }
  ">"                            { return symbol(GT); }
  "<"                            { return symbol(LT); }
  "!"                            { return symbol(NOT); }
  ":"                            { return symbol(COLON); }
  "=="                           { return symbol(EQUAL); }
  ">="                           { return symbol(GE); }
  "<="                           { return symbol(LE); }
  "!="                           { return symbol(NOTEQ); }
  "==="                          { return symbol(FULLEQ); }
  "!=="                          { return symbol(NOTFULLEQ); }
  "++"                           { return symbol(INC); }
  "--"                           { return symbol(DEC); }
  "+"                            { return symbol(PLUS); }
  "-"                            { return symbol(SUB); }
  "*"                            { return symbol(MULT); }
  "/"                            { return symbol(DIV); }
  "&"                            { return symbol(AND); }
  "|"                            { return symbol(OR); }
  "^"                            { return symbol(XOR); }
  "%"                            { return symbol(MOD); }
  "**"                           { return symbol(PWR); }
  "<<"                           { return symbol(LSHIFT); }
  ">>"                           { return symbol(RSHIFT); }
  "+="                           { return symbol(PLUSEQ); }
  "-="                           { return symbol(SUBEQ); }
  "*="                           { return symbol(MULTEQ); }
  "::"                           { return symbol(SQUARE); }

  /* string literal */
  \"                             { yybegin(STRING); string.setLength(0); }
  \'                             { yybegin(STRING_SINGLE); string.setLength(0); }

  /* numeric literals */

  /* This is matched together with the minus, because the number is too big to
     be represented by a positive integer. */
  "-2147483648"                  { return symbol(INTEGER_LITERAL, new Integer(Integer.MIN_VALUE)); }

  {DecIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer(yytext())); }
  {DecLongLiteral}               { return symbol(INTEGER_LITERAL, new Long(yytext().substring(0, yylength() - 1))); }

  {HexIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer((int) parseLong(2, yylength(), 16))); }
  {HexLongLiteral}               { return symbol(INTEGER_LITERAL, new Long(parseLong(2, yylength() - 1, 16))); }

  {OctIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer((int) parseLong(0, yylength(), 8))); }
  {OctLongLiteral}               { return symbol(INTEGER_LITERAL, new Long(parseLong(0, yylength() - 1, 8))); }

  {FloatLiteral}                 { return symbol(FLOATING_POINT_LITERAL, new Float(yytext().substring(0, yylength() - 1))); }
  {DoubleLiteral}                { return symbol(FLOATING_POINT_LITERAL, new Double(yytext())); }
  {DoubleLiteral}[dD]            { return symbol(FLOATING_POINT_LITERAL, new Double(yytext().substring(0, yylength() - 1))); }

  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* newline */
  {LineTerminator}               { return symbol(NEWLINE); }

  /* identifiers */
  {Identifier}                   { return symbol(IDENTIFIER, yytext()); }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return symbol(STRING_LITERAL, string.toString()); }

  {StringCharacter}+             { string.append(yytext()); }

  /* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\f"                          { string.append('\f'); }
  "\\r"                          { string.append('\r'); }
  "\\\""                         { string.append('\"'); }
  "\\'"                          { string.append('\''); }
  "\\\\"                         { string.append('\\'); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1), 8);
                                   string.append(val); }

  /* error cases */
  \\.                            { throw new RuntimeException("Illegal escape sequence \"" + yytext() + "\""); }
  {LineTerminator}               { throw new RuntimeException("Unterminated string at end of line"); }
}

<STRING_SINGLE> {
  \'                             { yybegin(YYINITIAL); return symbol(STRING_LITERAL_SINGLE, string.toString()); }

  {SingleCharacter}+             { string.append(yytext()); }

  /* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\'"                          { string.append('\''); }

  /* error cases */
  \\.                            { throw new RuntimeException("Illegal escape sequence \'" + yytext() + "\'"); }
  {LineTerminator}               { throw new RuntimeException("Unterminated single-quoted string at end of line"); }
}

/* error fallback */
[^]                              { throw new RuntimeException("Illegal character \"" + yytext() +
                                                              "\" at line " + yyline + ", column " + yycolumn); }
<<EOF>>                          { return symbol(EOF); }
