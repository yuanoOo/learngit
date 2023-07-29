## 纯Spark SQL使用UDF
目前纯Spark SQL只支持hive udf和spark自己的UDAF。spark的UDF不支持下面的用法，会报`Error in query: No handler for UDF/UDAF/UDTF 'com.test.txt.udf.LenCountUDF'; line 3 pos 15`
错误。
因此spark官网对于Spark UDF也没有给出sql用法，容易令人迷惑：https://spark.apache.org/docs/3.2.4/sql-ref-functions-udf-scalar.html
```roomsql
add jar hdfs://namenode/tmp/data-quality-1.0-SNAPSHOT.jar;

CREATE temporary FUNCTION HiveUDFTest AS 'cn.jxau.spark.udf.HiveUDFTest' USING JAR '/tmp/data-quality-1.0-SNAPSHOT.jar';

SHOW USER FUNCTIONS;

drop function default.UDFTest;

drop temporary function hiveudftest;
```


## Spark UDF1/UDF2...
```roomsql
class UDFTest extends UDF1[String, String]{
  override def call(t1: String): String = {
    return t1 + "-UDF-TEST"
  }
}
```
```text
(1)自定义UDF类，实现UDF1/2/3....22中的接口之一，其中UDF后跟的数字，
 比如UDF1、UDF2；表示输入参数的个数，1表示有一个入参，2表示有两个入参，
  最多可传入22个输入参数 
  实现 call()方法
  两种方式 ： 过匿名函数 和  通过实名函数
(2)注册UDF函数：  SparkSQL UDF 两种方式：udf() 和 register()   	    
  01.
	Spark1.x:  sqlContext.udf.register
	Spark2.x:       spark.udf.register
	   org.apache.spark.sql  SparkSession
	     def udf: UDFRegistration = sessionState.udfRegistration
  02. DataFrame的udf方法在 org.apache.spark.sql.functions 里
  org.apache.spark.sql
        functions.{col, lit, udf}
     spark.sql.function.udf() 方法 此时注册的方法，对外部可见
	   def udf(f: AnyRef, dataType: DataType): UserDefinedFunction = { UserDefinedFunction(f, dataType, None)}
 (3) 目前使用UDAF可以在SQL中，而Spark UDF使用，在Spark 任务中可以执行，在SparkSQL任务中执行报错
```

```text
(3) SQL 中使用UDF 
打包：
  ADD  jar sparkudf.jar;
 CREATE  TEMPORARY FUNCTION  trans_len AS  'com.test.txt.udf.StringLenUDF';
 select t1.data,trans_len(t1.data) as uid_bitmap_byte
 from (
 select 100 as user_id,  '2020' as data
 union all
 select 200  as user_id, '2019' as data)  t1
注释：
 情景： Spark SQL 在SQL中使用 Spark的UDF报错
 报错： Error in query: No handler for UDF/UDAF/UDTF 'com.test.txt.udf.LenCountUDF'; line 3 pos 15
 原因：
      SessionCatalog calls registerFunction to add a function to function registry. 
	  However, makeFunctionExpression supports only UserDefinedAggregateFunction.
 参考：
    Default SessionCatalog should support UDFs https://issues.apache.org/jira/browse/SPARK-25334
   Hive中Binary类型于0.8版本以上开始支持。
   https://stackoverflow.com/questions/52164488/spark-hive-udf-no-handler-for-udaf-analysis-exception
```
