package cn.zz.bi.parser

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

import scala.collection.convert.ImplicitConversions.`iterator asScala`
object AstUtils {
  /**
   * 树形打印抽象语法树
   */
  def printRuleContextInTreeStyle(ctx: ParserRuleContext, level: Int): Unit = {
    val prefix: String = "|"
    val curLevelStr: String = "-" * level
    val childLevelStr: String = "-" * (level + 1)
    println(s"${prefix}${curLevelStr} ${ctx.getClass.getCanonicalName}")
    val children: java.util.List[ParseTree] = ctx.children
    if (children == null || children.size() == 0) {
      return
    }
    children.iterator().foreach {
      case context: ParserRuleContext => printRuleContextInTreeStyle(context, level + 1)
      case _ => println(s"${prefix}${childLevelStr} ${ctx.getClass.getCanonicalName}")
    }
  }
}
