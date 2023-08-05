## SparkSessionExtensions

### UDF
在Spark中有两种自定义UDF的方法：
- 1、继承特定的类，实现Hive或者Spark的UDF
这种方法简单，但是可能性能低下，无法完全嵌入Spark-SQL的执行计划中，进行codegen

2、实现org.apache.spark.sql.SparkSessionExtensions.injectFunction方法
这种方法实现困难，需要对Spark-SQL的解析、优化、执行等阶段有深入的了解。
org.apache.spark.sql.catalyst.analysis.FunctionRegistry
```scala
  // ==============================================================================================
  //                          The guideline for adding SQL functions
  // ==============================================================================================
  // To add a SQL function, we usually need to create a new `Expression` for the function, and
  // implement the function logic in both the interpretation code path and codegen code path of the
  // `Expression`. We also need to define the type coercion behavior for the function inputs, by
  // extending `ImplicitCastInputTypes` or updating type coercion rules directly.
  //
  // It's much simpler if the SQL function can be implemented with existing expression(s). There are
  // a few cases:
  //   - The function is simply an alias of another function. We can just register the same
  //     expression with a different function name, e.g. `expression[Rand]("random", true)`.
  //   - The function is mostly the same with another function, but has a different parameter list.
  //     We can use `RuntimeReplaceable` to create a new expression, which can customize the
  //     parameter list and analysis behavior (type coercion). The `RuntimeReplaceable` expression
  //     will be replaced by the actual expression at the end of analysis. See `Left` as an example.
  //   - The function can be implemented by combining some existing expressions. We can use
  //     `RuntimeReplaceable` to define the combination. See `ParseToDate` as an example.
  //     To inherit the analysis behavior from the replacement expression
  //     mix-in `InheritAnalysisRules` with `RuntimeReplaceable`. See `TryAdd` as an example.
  //   - For `AggregateFunction`, `RuntimeReplaceableAggregate` should be mixed-in. See
  //     `CountIf` as an example.
  //
  // Sometimes, multiple functions share the same/similar expression replacement logic and it's
  // tedious to create many similar `RuntimeReplaceable` expressions. We can use `ExpressionBuilder`
  // to share the replacement logic. See `ParseToTimestampLTZExpressionBuilder` as an example.
  //
  // With these tools, we can even implement a new SQL function with a Java (static) method, and
  // then create a `RuntimeReplaceable` expression to call the Java method with `Invoke` or
  // `StaticInvoke` expression. By doing so we don't need to implement codegen for new functions
  // anymore. See `AesEncrypt`/`AesDecrypt` as an example.
```