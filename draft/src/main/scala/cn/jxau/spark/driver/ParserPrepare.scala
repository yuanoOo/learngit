package cn.jxau.spark.driver

import org.apache.spark.sql.SparkSession

object ParserPrepare {

    def getSparkSession: SparkSession = {
        SparkSession.builder()
            .master("local[1]")
            .getOrCreate()
    }

    def prepareSql: SparkSession = {
        val session = getSparkSession
        session.sql("set spark.sql.legacy.createHiveTableByDefault=false;").show()
        session.sql(DDL).show()
        session
    }

    private val DDL =
        """
          |
          | CREATE TABLE fruit (
          |     id INT,
          |     name STRING,
          |     price FLOAT
          | );
          |
          |
          |""".stripMargin
}
