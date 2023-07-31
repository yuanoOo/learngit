package cn.zz.bi.spark.sql.ast;

import cn.zz.bi.parser.AstUtils;
import org.apache.spark.sql.catalyst.parser.SqlBaseBaseVisitor;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;

public class MyAstVisitor extends SqlBaseBaseVisitor {

    @Override
    public Object visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        AstUtils.printRuleContextInTreeStyle(ctx.statement(), 1);

        return super.visitSingleStatement(ctx);
    }


}
