package cn.jxau.spark.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.spark.sql.catalyst.parser.SqlBaseLexer;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.parser.UpperCaseCharStream;

/**
 * org.apache.spark.sql.catalyst.parser.AbstractSqlParser#parse(java.lang.String, scala.Function1)
 * Spark sql调用Antlr API生成AST部分
 */
public class AstParserDriver {
    public static void main(String[] args) {
        String query = "select name from student where age > 18";
        SqlBaseLexer lexer = new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(query)));
        SqlBaseParser parser = new SqlBaseParser(new CommonTokenStream(lexer));

        MyAstVisitor astVisitor = new MyAstVisitor();
        astVisitor.visitSingleStatement(parser.singleStatement());
    }
}
