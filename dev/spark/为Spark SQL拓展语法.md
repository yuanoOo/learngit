## 为Spark SQL拓展语法

#### 1、添加基于Antlr4的语法解析文件
```antlrv4
 grammar CallSparkBase;

 @lexer::members {
  /**
   * Verify whether current token is a valid decimal token (which contains dot).
   * Returns true if the character that follows the token is not a digit or letter or underscore.
   *
   * For example:
   * For char stream "2.3", "2." is not a valid decimal token, because it is followed by digit '3'.
   * For char stream "2.3_", "2.3" is not a valid decimal token, because it is followed by '_'.
   * For char stream "2.3W", "2.3" is not a valid decimal token, because it is followed by 'W'.
   * For char stream "12.0D 34.E2+0.12 "  12.0D is a valid decimal token because it is followed
   * by a space. 34.E2 is a valid decimal token because it is followed by symbol '+'
   * which is not a digit or letter or underscore.
   */
  public boolean isValidDecimal() {
    int nextChar = _input.LA(1);
    if (nextChar >= 'A' && nextChar <= 'Z' || nextChar >= '0' && nextChar <= '9' ||
      nextChar == '_') {
      return false;
    } else {
      return true;
    }
  }
}

 singleStatement
    : statement ';'* EOF
    ;

 statement
    : compactionStatement                                                       #compactionCommand
    | CALL multipartIdentifier '(' (callArgument (',' callArgument)*)? ')'      #call
    | .*?                                                                       #passThrough
    ;

 compactionStatement
    : operation = (RUN | SCHEDULE) COMPACTION  ON tableIdentifier (AT instantTimestamp = INTEGER_VALUE)?    #compactionOnTable
    | operation = (RUN | SCHEDULE) COMPACTION  ON path = STRING   (AT instantTimestamp = INTEGER_VALUE)?    #compactionOnPath
    | SHOW COMPACTION  ON tableIdentifier (LIMIT limit = INTEGER_VALUE)?                             #showCompactionOnTable
    | SHOW COMPACTION  ON path = STRING (LIMIT limit = INTEGER_VALUE)?                               #showCompactionOnPath
    ;

 tableIdentifier
    : (db=IDENTIFIER '.')? table=IDENTIFIER
    ;

 callArgument
    : expression                    #positionalArgument
    | identifier '=>' expression    #namedArgument
    ;

 expression
    : constant
    | stringMap
    ;

 constant
    : number                          #numericLiteral
    | booleanValue                    #booleanLiteral
    | STRING+                         #stringLiteral
    | identifier STRING               #typeConstructor
    ;

 stringMap
    : MAP '(' constant (',' constant)* ')'
    ;

 booleanValue
    : TRUE | FALSE
    ;

 number
    : MINUS? EXPONENT_VALUE           #exponentLiteral
    | MINUS? DECIMAL_VALUE            #decimalLiteral
    | MINUS? INTEGER_VALUE            #integerLiteral
    | MINUS? BIGINT_LITERAL           #bigIntLiteral
    | MINUS? SMALLINT_LITERAL         #smallIntLiteral
    | MINUS? TINYINT_LITERAL          #tinyIntLiteral
    | MINUS? DOUBLE_LITERAL           #doubleLiteral
    | MINUS? FLOAT_LITERAL            #floatLiteral
    | MINUS? BIGDECIMAL_LITERAL       #bigDecimalLiteral
    ;

 multipartIdentifier
    : parts+=identifier ('.' parts+=identifier)*
    ;

 identifier
    : IDENTIFIER              #unquotedIdentifier
    | quotedIdentifier        #quotedIdentifierAlternative
    | nonReserved             #unquotedIdentifier
    ;

 quotedIdentifier
    : BACKQUOTED_IDENTIFIER
    ;

 nonReserved
     : CALL
     | COMPACTION
     | CREATE
     | DROP
     | EXISTS
     | FROM
     | IN
     | INDEX
     | INDEXES
     | IF
     | LIMIT
     | NOT
     | ON
     | OPTIONS
     | REFRESH
     | RUN
     | SCHEDULE
     | SHOW
     | TABLE
     | USING
     ;

 LEFT_PAREN: '(';
 RIGHT_PAREN: ')';
 COMMA: ',';
 DOT: '.';

 ALL: 'ALL';
 AT: 'AT';
 CALL: 'CALL';
 COMPACTION: 'COMPACTION';
 RUN: 'RUN';
 SCHEDULE: 'SCHEDULE';
 ON: 'ON';
 SHOW: 'SHOW';
 LIMIT: 'LIMIT';
 MAP: 'MAP';
 NULL: 'NULL';
 TRUE: 'TRUE';
 FALSE: 'FALSE';
 INTERVAL: 'INTERVAL';
 TO: 'TO';
 CREATE: 'CREATE';
 INDEX: 'INDEX';
 INDEXES: 'INDEXES';
 IF: 'IF';
 NOT: 'NOT';
 EXISTS: 'EXISTS';
 TABLE: 'TABLE';
 USING: 'USING';
 OPTIONS: 'OPTIONS';
 DROP: 'DROP';
 FROM: 'FROM';
 IN: 'IN';
 REFRESH: 'REFRESH';

 EQ: '=' | '==';

 PLUS: '+';
 MINUS: '-';

 STRING
    : '\'' ( ~('\''|'\\') | ('\\' .) )* '\''
    | '"' ( ~('"'|'\\') | ('\\' .) )* '"'
    ;

 BIGINT_LITERAL
    : DIGIT+ 'L'
    ;

 SMALLINT_LITERAL
    : DIGIT+ 'S'
    ;

 TINYINT_LITERAL
    : DIGIT+ 'Y'
    ;

 INTEGER_VALUE
    : DIGIT+
    ;

 EXPONENT_VALUE
    : DIGIT+ EXPONENT
    | DECIMAL_DIGITS EXPONENT {isValidDecimal()}?
    ;

 DECIMAL_VALUE
    : DECIMAL_DIGITS {isValidDecimal()}?
    ;

 FLOAT_LITERAL
    : DIGIT+ EXPONENT? 'F'
    | DECIMAL_DIGITS EXPONENT? 'F' {isValidDecimal()}?
    ;

 DOUBLE_LITERAL
    : DIGIT+ EXPONENT? 'D'
    | DECIMAL_DIGITS EXPONENT? 'D' {isValidDecimal()}?
    ;

 BIGDECIMAL_LITERAL
    : DIGIT+ EXPONENT? 'BD'
    | DECIMAL_DIGITS EXPONENT? 'BD' {isValidDecimal()}?
    ;

 IDENTIFIER
    : (LETTER | DIGIT | '_')+
    ;

 BACKQUOTED_IDENTIFIER
    : '`' ( ~'`' | '``' )* '`'
    ;

 fragment DECIMAL_DIGITS
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    ;

 fragment EXPONENT
    : 'E' [+-]? DIGIT+
    ;

 fragment DIGIT
    : [0-9]
    ;

 fragment LETTER
    : [A-Z]
    ;

 SIMPLE_COMMENT
     : '--' ~[\r\n]* '\r'? '\n'? -> channel(HIDDEN)
     ;

 BRACKETED_COMMENT
     : '/*' .*? '*/' -> channel(HIDDEN)
     ;

 WS  : [ \r\n\t]+ -> channel(HIDDEN)
     ;

 // Catch-all for anything we can't recognize.
 // We use this to be able to ignore and recover all the text
 // when splitting statements with DelimiterLexer
 UNRECOGNIZED
     : .
     ;
```

#### 2、实现CallSparkBaseBaseVisitor
```scala
class CallSqlCommonAstBuilder extends CallSparkBaseBaseVisitor[AnyRef] with Logging{

  override def visitSingleStatement(ctx: CallSparkBaseParser.SingleStatementContext): LogicalPlan = {
    visit(ctx.statement()).asInstanceOf[LogicalPlan]
  }

  override def visitCall(ctx: CallSparkBaseParser.CallContext): LogicalPlan = {
    if (ctx.callArgument().isEmpty) {
      throw new ParseException(s"Procedures arguments is empty", ctx)
    }

    val name: Seq[String] = ctx.multipartIdentifier().parts.asScala.map(_.getText)
    val args = ctx.callArgument()
      .asScala
      .map(
        arg => {
          val namedArgumentContext = arg.asInstanceOf[NamedArgumentContext]
          (namedArgumentContext.identifier().getText, namedArgumentContext.expression().getText)
        })

    // CallCommand(name, args)
//    TestCommand1(name(0), args)
    CallKylinCommand(name(0), args)
  }

  private def typedVisit[T](ctx: ParseTree): T = {
    ctx.accept(this).asInstanceOf[T]
  }
}
```

#### 3、添加语法解析器Parser
```scala
class CallBaseSqlParser(session: SparkSession, delegate: ParserInterface)
  extends ParserInterface with Logging{

  private lazy val astBuilder = new CallSqlCommonAstBuilder

  override def parsePlan(sqlText: String): LogicalPlan = parse(sqlText) { parser =>
    astBuilder.visit(parser.singleStatement()) match {
      case plan: LogicalPlan => plan
      case _ => delegate.parsePlan(sqlText)
    }
  }

  def parse[T](command: String)(toResult: CallSparkBaseParser => T): T = {
    val lexer = new CallSparkBaseLexer(
      new UpperCaseCharStream(CharStreams.fromString(command)))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ParseErrorListener)

    val tokenStream = new CommonTokenStream(lexer)
    val parser = new CallSparkBaseParser(tokenStream)
    parser.addParseListener(PostProcessor)
    parser.removeErrorListeners()
    parser.addErrorListener(ParseErrorListener)

    try {
      try {
        // first, try parsing with potentially faster SLL mode
        parser.getInterpreter.setPredictionMode(PredictionMode.SLL)
        toResult(parser)
      } catch {
        case _: ParseCancellationException =>
          // if we fail, parse with LL mode
          tokenStream.seek(0) // rewind input stream
          parser.reset()

          // Try Again.
          parser.getInterpreter.setPredictionMode(PredictionMode.LL)
          toResult(parser)
      }
    } catch {
      case e: ParseException if e.command.isDefined =>
        logError("parse error " + e.message)
        throw e
      case e: ParseException =>
        throw e.withCommand(command)
      case e: AnalysisException =>
        val position = Origin(e.line, e.startPosition)
        throw new ParseException(Option(command), e.message, position, position)
    }
  }

  override def parseExpression(sqlText: String): Expression = delegate.parseExpression(sqlText)

  override def parseTableIdentifier(sqlText: String): TableIdentifier = delegate.parseTableIdentifier(sqlText)

  override def parseFunctionIdentifier(sqlText: String): FunctionIdentifier = delegate.parseFunctionIdentifier(sqlText)

  override def parseMultipartIdentifier(sqlText: String): Seq[String] = delegate.parseMultipartIdentifier(sqlText)

  override def parseTableSchema(sqlText: String): StructType = delegate.parseTableSchema(sqlText)

  override def parseDataType(sqlText: String): DataType = throw new UnsupportedOperationException(s"Unsupported parseRawDataType method")
}

/* Copied from Apache Spark's to avoid dependency on Spark Internals */
class UpperCaseCharStream(wrapped: CodePointCharStream) extends CharStream {
  override def consume(): Unit = wrapped.consume()
  override def getSourceName(): String = wrapped.getSourceName
  override def index(): Int = wrapped.index
  override def mark(): Int = wrapped.mark
  override def release(marker: Int): Unit = wrapped.release(marker)
  override def seek(where: Int): Unit = wrapped.seek(where)
  override def size(): Int = wrapped.size

  override def getText(interval: Interval): String = wrapped.getText(interval)

  // scalastyle:off
  override def LA(i: Int): Int = {
    val la = wrapped.LA(i)
    if (la == 0 || la == IntStream.EOF) la
    else Character.toUpperCase(la)
  }
  // scalastyle:on
}

```

#### 1、实现SparkSessionExtensions

```scala
class CallSparkSessionExtension extends (SparkSessionExtensions => Unit){
  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectParser {
      (session, parser) => new CallBaseSqlParser(session, parser)
    }

    extensions.injectPostHocResolutionRule(_ => TestRule())
  }
}
```

#### 2、注入逻辑计划规则Rule
```scala
case class TestRule() extends Rule[LogicalPlan]{
  override def apply(plan: LogicalPlan): LogicalPlan = {
    plan match {
      // Convert to HoodieCallProcedureCommand
      case c @CallKylinCommand(_, _) =>
        //TestCommand1(c.name, c.args)
        TestCommand1(c.name, c.args)

      case _ => plan
    }
  }
}
```

#### 3、实现LeafRunnableCommand
```scala
case class TestCommand1(name :String, args: Seq[(String, String)]) extends LeafRunnableCommand{

  val KYLIN_BUILD_COMMAND = "kylin"

  override def run(sparkSession: SparkSession): Seq[Row] = {
    name match {
      case KYLIN_BUILD_COMMAND => Seq(Row(CallKylinBuild.callKylinBuild(name, args)))
      case _ => throw new AnalysisException("UnSupport Call Command!", Array.empty)
    }
  }

  override val output: Seq[Attribute] = {
    Seq(AttributeReference("Result", StringType)())
  }
}
```

#### 4、真正的Command实现
```scala
object CallKylinBuild {
  def callKylinBuild(name :String, args: Seq[(String, String)]): String = {
    var cube_start_date = 0L
    var host = ""
    var project = ""
    var auth = ""
    args.foreach {
      case ("start_date", date) =>
        val str = date.drop(1).dropRight(1)
        val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
        format.setTimeZone(TimeZone.getTimeZone("UTC"))
        val time = format.parse(str).getTime
        cube_start_date = time

      case ("host", date) =>
        host = date.drop(1).dropRight(1)

      case ("project", date) =>
        project = date.drop(1).dropRight(1)

      case ("auth", date) =>
        auth = date.drop(1).dropRight(1)

      case _ =>
    }

    val json =
      s"""
        |
        |{"startTime":${cube_start_date}, "endTime":${cube_start_date + 86400000}, "buildType":"BUILD"}
        |""".stripMargin
    val response = HttpRequest.put(s"http://${host}/kylin/api/cubes/${project}/rebuild")
      //.header("Authorization", "Basic aDI0MTI1Mjk6YzQ2NzQxNTcyOEA=")
      .header("Authorization", s"Basic ${auth}")
      .header("Content-Type", "application/json")
      .body(json)
      .execute()

    response match {
      case rep if rep.isOk =>
        "Success Call Kylin Build!!!"
      case _ =>
        "Fail Kylin Build: " + response.body()
        throw new RuntimeException("Fail Kylin Build" + response.body())
    }
  }
}

```


#### 5、Spark Driver验证
```scala
object KylinDriver {
  val query = "call name (a => '3');"
  val query1 = "call show_commits_metadata(table => 'test_hudi_table', xx => 'tooo');"
//  val query2 = "call kylin(auth => 'aDI0MTI1Mjk6YzQ2NzQxNTcyOEA=', start_date => '2023-08-01', host => '10.222.196.21:7070', project => 'vanvve');"
  val query3 = "call kylin('2023-08-01', '2023-08-02');"

  val query2 =
    """
      |call kylin(
      |           start_date => '2023-08-01',
      |           host => '10.222.116.28:7070',
      |           project => 'cvvv',
      |           auth => 'aDI0MTI1Mjk6YzQ2NzQxNTcyOEA='
      |);
      |""".stripMargin
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[1]")
      .withExtensions(new CallSparkSessionExtension)
      .getOrCreate()
    // spark.sql("set spark.sql.planChangeLog.level=WARN;")
    val sql = spark.sql(query2)
    println(sql.explain(true))
    println(sql.show())
    spark.close()
  }
}
```
