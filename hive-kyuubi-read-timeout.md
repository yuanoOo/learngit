## hive connect kyuubi timeout in dolphin
```java
org.apache.dolphinscheduler.plugin.datasource.hive.HiveDataSourceClient#initClient

@Override
protected void initClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
  logger.info("Create Configuration for hive configuration.");
  this.hadoopConf = createHadoopConf();
  logger.info("Create Configuration success.");

  logger.info("Create UserGroupInformation.");
  this.ugi = createUserGroupInformation(baseConnectionParam.getUser());
  logger.info("Create ugi success.");

  this.dataSource = JDBCDataSourceProvider.createOneSessionJdbcDataSource(baseConnectionParam, dbType);
  
  // 为连接池设置connectionTimeout参数
  String baseConnParamOther = baseConnectionParam.getOther();
  if (JSONUtils.checkJsonValid(baseConnParamOther)){
    Map<String, String> paramMap = JSONUtils.toMap(baseConnParamOther);
    if (paramMap.containsKey("connectionTimeout")){
      String connectionTimeout = paramMap.get("connectionTimeout");
      if (StringUtils.isNumeric(connectionTimeout)){
        this.dataSource.setConnectionTimeout(Long.parseLong(connectionTimeout));
      }
    }
  }else {
      this.dataSource.setConnectionTimeout(300000L);
  }

  this.jdbcTemplate = new JdbcTemplate(dataSource);
  logger.info("Init {} success.", getClass().getName());
}

```
- 其实在`org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider#createOneSessionJdbcDataSource`   
中进行连接池的设置是更好的选择。不过都是暂时方案，最好的方案是将配置文件中的连接池信息，传递到sqlTask

```text
[ERROR] 2023-06-08 17:39:06.166 +0800 - Task execute failed, due to meet an exception
org.apache.dolphinscheduler.plugin.task.api.TaskException: Execute sql task failed
	at org.apache.dolphinscheduler.plugin.task.sql.SqlTask.handle(SqlTask.java:168)
	at org.apache.dolphinscheduler.server.worker.runner.DefaultWorkerDelayTaskExecuteRunnable.executeTask(DefaultWorkerDelayTaskExecuteRunnable.java:49)
	at org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable.run(WorkerTaskExecuteRunnable.java:174)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleTask.runInterruptibly(TrustedListenableFutureTask.java:131)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:82)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.sql.SQLException: org.apache.thrift.transport.TTransportException: java.net.SocketTimeoutException: Read timed out
	at org.apache.hive.jdbc.HiveStatement.runAsyncOnServer(HiveStatement.java:323)
	at org.apache.hive.jdbc.HiveStatement.execute(HiveStatement.java:253)
	at org.apache.hive.jdbc.HiveStatement.executeUpdate(HiveStatement.java:490)
	at org.apache.hive.jdbc.HivePreparedStatement.executeUpdate(HivePreparedStatement.java:122)
	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)
	at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeUpdate(HikariProxyPreparedStatement.java)
	at org.apache.dolphinscheduler.plugin.task.sql.SqlTask.executeUpdate(SqlTask.java:312)
	at org.apache.dolphinscheduler.plugin.task.sql.SqlTask.executeFuncAndSql(SqlTask.java:210)
	at org.apache.dolphinscheduler.plugin.task.sql.SqlTask.handle(SqlTask.java:161)
	... 9 common frames omitted
Caused by: org.apache.thrift.transport.TTransportException: java.net.SocketTimeoutException: Read timed out
	at org.apache.thrift.transport.TIOStreamTransport.read(TIOStreamTransport.java:129)
	at org.apache.thrift.transport.TTransport.readAll(TTransport.java:86)
	at org.apache.thrift.transport.TSaslTransport.readLength(TSaslTransport.java:376)
	at org.apache.thrift.transport.TSaslTransport.readFrame(TSaslTransport.java:453)
	at org.apache.thrift.transport.TSaslTransport.read(TSaslTransport.java:435)
	at org.apache.thrift.transport.TSaslClientTransport.read(TSaslClientTransport.java:37)
	at org.apache.thrift.transport.TTransport.readAll(TTransport.java:86)
	at org.apache.thrift.protocol.TBinaryProtocol.readAll(TBinaryProtocol.java:429)
	at org.apache.thrift.protocol.TBinaryProtocol.readI32(TBinaryProtocol.java:318)
	at org.apache.thrift.protocol.TBinaryProtocol.readMessageBegin(TBinaryProtocol.java:219)
	at org.apache.thrift.TServiceClient.receiveBase(TServiceClient.java:77)
	at org.apache.hive.service.rpc.thrift.TCLIService$Client.recv_ExecuteStatement(TCLIService.java:237)
	at org.apache.hive.service.rpc.thrift.TCLIService$Client.ExecuteStatement(TCLIService.java:224)
	at sun.reflect.GeneratedMethodAccessor220.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.apache.hive.jdbc.HiveConnection$SynchronizedHandler.invoke(HiveConnection.java:1524)
	at com.sun.proxy.$Proxy183.ExecuteStatement(Unknown Source)
	at org.apache.hive.jdbc.HiveStatement.runAsyncOnServer(HiveStatement.java:312)
	... 17 common frames omitted
Caused by: java.net.SocketTimeoutException: Read timed out
	at java.net.SocketInputStream.socketRead0(Native Method)
	at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
	at java.net.SocketInputStream.read(SocketInputStream.java:171)
	at java.net.SocketInputStream.read(SocketInputStream.java:141)
	at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
	at java.io.BufferedInputStream.read1(BufferedInputStream.java:286)
	at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
	at org.apache.thrift.transport.TIOStreamTransport.read(TIOStreamTransport.java:127)
	... 35 common frames omitted
```
