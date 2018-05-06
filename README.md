# Lite

😃 一个写了几天浪费青春浪费时光的垃圾 JVM AST 解释器脚本语言

## 什么情况？

某天突发奇想认为可以设计一门非常简单的 JVM 程序设计语言, 直接无需专门的分词解析过程及可以运行

从 `2018-5-2` 到 `2018-5-6` 在疯狂写这门语言....

而且还打算做一个 JavaScript/那门语言（那时叫 H2）/另外一门更复杂的语言（那时叫 Hydrogen）（后来又改名 H2） 一起作为很小的以「快速开发小程序」 为特性的语言

^ (我的意思是一个类似 Int 的解释器工具, 有扩展功能：快速输入 Keyword|Operator/AST 查看/Lex 结果查看/高亮/获取设置变量/脚本分享/Reflect 可视化查看(类/对象/方法/直接创建原生类型/调用方法和类型构造器)/Intent 接口 等的 Android 应用)

当时的（H2）设计为使用虚拟机执行，虚拟机后来被命名为 AqoursVM，可是由于太麻烦的原因也没有开发，而是转而做了个 AST 解释器... (此时还有 GeekApk 等项目待完成)

最后的结果是苦战四天写了大概 6k 行代码（大多是复制粘贴，我说的是 Copy-Your-Self）后 终于还有一个解析器.... 解析器... 没写

接下来这门语言将会非常简单的使用 PEG.js 使用某些序列化操作与 Java 交互作为解析器发布，甚至不需要带当初计划的重构删除无用代码/重复代码，写测试查 coverage, 用 CI, 写详细文档和注释等

## 以后？

这以后肯定要重写的，钦定新版本完全独立并且 lexer（Look-Ahead stateful） 和 parser（递归下降法） 全手写, 依然将 lex 和 parse 作为两个独立进程, 并且添加 case 表达式和基于缩进的语意（现在的版本由于某些比较 ~~fucking~~ 的原因即使设计了依然依赖手写 end ）, 为能实现 Java 接口的实现 Java 接口（如 Serializable） 名字就叫 YaLite（Yet Another Lite Interperter）（虽然不是同一门语言，或许，至少语法版本不同）

以后还有升级为 H2 的机会（就是换执行模型）...

以后 Lime（基于 Sexp 的 S 表达式解释语言） 依然会写的，放心不会很久，十年吧（皮）

## 关于这门语言（你也可以看代码，有一些里面的文档）

目前包括 Lexer 和写了 6% 的 Parser 在内的 AST 类 和帮助类一共 130k, 需要注意的是压缩后有 70k, 里面有不少无用的代码可以 proguard

这包括整个语言文本处理类（Lexer 和 Parser），因为是 PEG.js 解析的

需要注意的是，LuaJ 都没这么小，LuaJ 如果包含（JavaCC）解析器的话，它的解析器都比 Lite 整个要大（170k），即使 Lua 的语法并不是很复杂

BeanShell 怎么精简，它的 JavaCC 解析器类至少要占 60k.... 而 Lite 所谓 Lite 可能整个解释器都没这么大（没错，Lite 也专为体积设计）

目前计划使用 PEG.js 自动生成解释器， WIP，即使已经有一个手写 Lexer 了

### 语法

```ruby
 true false nil
 '' "" 字符串
 1 1l 1n 1f //0x1 0b1000 number
 ^ 不打算支持进制自动换算了
 ![] [] 列表
 { a.exit } do || Proc
 { a: 1, b: 2 } Hash

import java.lang.String
import java.lang

require lib/a
require a

1 + 1 - 1 % 0x2
System.out.println 1
System::out.print @a
lite['a'] = 1
lite->a "Hello"
![a b c]->1 'a'
lite[1]
lite[@a]
{ a: 1, b: 2, c:3 }
{ a: @a, b: @b }
nil 1 23 true

trace hello world

for i in @iter
  break
  next # comment

for @i in System.collection
  puts @i

@i++ @i--

if "bad linux" in [ "foo", System.out.toString, Runtime.runtime as String ]
  puts "2333 333333 333333"

def a
  return 1

def java.lang.System.b
  return "Goodbye, cruel world"

def $java.lang.Integer.fmt a b c
  puts 'oh no'
  return String.format a b c

def neg i
  return -i

def not_ i
  return !i

@a = System.in
b = '2'

import java.lang.String
str = String("Be a good guy") # Construct a java object
str = java_lang_String("yes") # alternative java Class identifier
str = java.lang.Integer(1) # Bad but acceptable
str = neg(233f)
@notnot = not true # must not be true....
Hashtable()[Integer(1)] = "kotlin f**king comming"
Hashtable()::1
def tbl
  {a:"Hello"}

puts tbl()::a
scope
  @b = tbl()
  @b->a "Goodbye"
  @b->c { puts @self::a }
  @b.c # LiteBlock
  @b.c()

java_lang_System # identifier

while true
  break

while System.out == "a"
  if System.in == "b"
    elif a === null
      next
    else
      break
```

> 以上直接使用了 Ruby 高亮，实际上 Lite 的 Lexer 自带高亮, 使用 `-lexP` 就可以在 `ANSI escaped terminal` 上看到高亮字体

![shot](https://github.com/duangsuse/Lite/raw/master/2018-05-06%2015-26-53%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE.png)

duangsuse 设计了一种类似 BNF 的语言来描述 Lite 的语法

```plain
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
```

## 赛艇链接

+ GitLab 上有一个叫 h2 的公开项目, 我这里不提供链接（网速...）垃圾 GFW 是不是 QOS 我
+ BitBucket 上也有个，要不然就叫 Hydrogen 的 Mercurial Repo
+ Coding 上或许也有，不过大概是空的

