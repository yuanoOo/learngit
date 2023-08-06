package cn.jxau.spark.driver

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.logical.Project

object ParserDriver {

    val query = "select name from stu where age > 10"

    def main(args: Array[String]): Unit = {
        val session = SparkSession.builder().master("local[1]").getOrCreate()

        val logicalPlan = session.sessionState.sqlParser.parsePlan(query)
        logicalPlan.asInstanceOf[Project]
        println(logicalPlan.getClass.getName)
        println(logicalPlan.toString())

        session.sessionState

        val expression = session.sessionState.sqlParser.parseExpression(sqlText = query)
        println(expression)

    }
}
