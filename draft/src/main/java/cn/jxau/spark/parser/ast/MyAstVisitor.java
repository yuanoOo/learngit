package cn.jxau.spark.parser.ast;


import org.apache.spark.sql.catalyst.parser.SqlBaseBaseVisitor;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;

public class MyAstVisitor extends SqlBaseBaseVisitor<Object> {

    @Override
    public Object visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        AstUtils.printRuleContextInTreeStyle(ctx.statement(), 1);

        return super.visitSingleStatement(ctx);
    }


}
