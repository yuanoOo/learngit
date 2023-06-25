## Kylin4 SQL查询源码分析

既然Kylin提供了提交sql进行查询的rest接口，那我们就从controller层源码开始吧！

- ## Kylin SQL入口部分
org.apache.kylin.rest.controller.QueryController#query是rest接口的入口类，经过一系列
转换，校验后，在org.apache.kylin.rest.service.QueryService#queryWithSqlMassage方法中进行
实际的sql提交。
```java
    private SQLResponse queryWithSqlMassage(SQLRequest sqlRequest) throws Exception {
        Connection conn = null;
        boolean isPrepareRequest = isPrepareStatementWithParams(sqlRequest);
        boolean borrowPrepareContext = false;
        PreparedContextKey preparedContextKey = null;
        PreparedContext preparedContext = null;

        try {
            // 关键代码一
            conn = QueryConnection.getConnection(sqlRequest.getProject());
            
            if (!isPrepareRequest) {
                // 关键代码二
                return executeRequest(correctedSql, sqlRequest, conn);
            } 

        } finally {
            DBUtils.closeQuietly(conn);
            if (preparedContext != null) {
                if (borrowPrepareContext) {
                    // Set tag isBorrowedContext true, when return preparedContext back
                    for (OLAPContext olapContext : preparedContext.olapContexts) {
                        if (borrowPrepareContext) {
                            olapContext.isBorrowedContext = true;
                        }
                    }

                    preparedContextPool.returnObject(preparedContextKey, preparedContext);
                } else {
                    preparedContext.close();
                }
            }
        }
    }
```

别看这个方法很长，其实关键代码也就上面中文注释两行，下面我们分别来看两行关键代码：

- conn = QueryConnection.getConnection(sqlRequest.getProject())
> Apache Calcite 是一个动态数据管理框架，提供了如：SQL 解析、SQL 校验、SQL 查询优化、SQL 生成以及数据连接查询等典型数据库管理功能。目前，Apache Calcite 作为 SQL 解析与优化引擎，已经广泛使用在 Hive、Drill、Flink、Phoenix 和 Storm 等项目中。

Kylin是基于Calcite框架构建自己的sql系统，而这个方法就是Kylin Server与Calcite交互的入口类：
构建model.json字符串，进行Calcite的模式发现, 这就需要学习Calcite了。
```json
{
    "version": "1.0",
    "defaultSchema": "SALES",
    "schemas": [
        {
            "name": "SALES",
            "type": "custom",
            "factory": "org.apache.calcite.adapter.csv.CsvSchemaFactory",
            "operand": {
                "directory": "sales"
            }
        }
    ]
}
```
```java
    public static Connection getConnection(String project) throws SQLException {
        if (!isRegister) {
            try {
                Class<?> aClass = Thread.currentThread().getContextClassLoader()
                        .loadClass("org.apache.calcite.jdbc.Driver");
                Driver o = (Driver) aClass.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(o);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            isRegister = true;
        }
        // 关键代码
        File olapTmp = OLAPSchemaFactory.createTempOLAPJson(project, KylinConfig.getInstanceFromEnv());
        Properties info = new Properties();
        info.putAll(KylinConfig.getInstanceFromEnv().getCalciteExtrasProperties());
        // Import calcite props from jdbc client(override the kylin.properties)
        info.putAll(BackdoorToggles.getJdbcDriverClientCalciteProps());
        info.put("model", olapTmp.getAbsolutePath());
        info.put("typeSystem", "org.apache.kylin.query.calcite.KylinRelDataTypeSystem");
        return DriverManager.getConnection("jdbc:calcite:", info);
    }
```

## Kylin SQL的核心，Kylin-Calcite部分
代码位于Kylin query模块中。Kylin扩展Calcite实现自己的SQL查询系统的代码，集中在org/apache/kylin/query/schema包中，
改包下面有三个类：
```TEXT
// 
org.apache.kylin.query.schema.OLAPSchema 
org.apache.kylin.query.schema.OLAPSchemaFactory
org.apache.kylin.query.schema.OLAPTable
```

### OLAPSchema类似于Mysql中Database
实现了org.apache.calcite.schema.impl.AbstractSchema类，主要例举出Kylin Project下面有那些表。

### OLAPTable类似于Mysql中的Table
实现了org.apache.calcite.adapter.java.AbstractQueryableTable,
其中org.apache.kylin.query.schema.OLAPTable#toRel方法定义了如何查询SQL Kylin Table。
```java
    // Converts this table into a relational expression.
    @Override
    public RelNode toRel(ToRelContext context, RelOptTable relOptTable) {
        int fieldCount = relOptTable.getRowType().getFieldCount();
        int[] fields = identityList(fieldCount);
        return new OLAPTableScan(context.getCluster(), relOptTable, this, fields);
    }
```

https://aaaaaaron.github.io/2020/02/07/Kylin-query-process/
https://kyligence.io/wp-content/uploads/2019/07/Apache-Kylin-Query-Analysis.pdf

Apache Calcite系列（一）：整体流程解析
https://zhuanlan.zhihu.com/p/614668529

https://xie.infoq.cn/article/1df5a39bb071817e8b4cb4b29