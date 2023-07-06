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

```java
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.datasource.hive;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH;

import com.google.common.base.Strings;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.hive.utils.CommonUtil;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import sun.security.krb5.Config;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import sun.security.krb5.Config;

public class HiveDataSourceClient extends CommonDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceClient.class);

    private ScheduledExecutorService kerberosRenewalService;

    private Configuration hadoopConf;
    private UserGroupInformation ugi;
    private boolean retryGetConnection = true;

    private static final String HIKARI_CONN_TIMEOUT = "connectionTimeout";

    private static final String HIKARI_MAXIMUM_POOL_SIZE = "hiveOneSessionEnable";


    public HiveDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected void preInit() {
        logger.info("PreInit in {}", getClass().getName());
        this.kerberosRenewalService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("Hive-Kerberos-Renewal-Thread-").setDaemon(true).build());
    }

    @Override
    protected void initClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        logger.info("Create Configuration for hive configuration.");
        this.hadoopConf = createHadoopConf();
        logger.info("Create Configuration success.");

        logger.info("Create UserGroupInformation.");
        this.ugi = createUserGroupInformation(baseConnectionParam.getUser());
        logger.info("Create ugi success.");

        this.dataSource = JDBCDataSourceProvider.createOneSessionJdbcDataSource(baseConnectionParam, dbType);

        this.dataSource.setConnectionTimeout(300_000L);

        // no save idle connection for killing yarn-app quickly to clean usercache file size
        // as fast as possbible
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(0);

        String baseConnParamOther = baseConnectionParam.getOther();
        if (JSONUtils.checkJsonValid(baseConnParamOther)) {
            Map<String, String> paramMap = JSONUtils.toMap(baseConnParamOther);
            if (paramMap.containsKey(HIKARI_CONN_TIMEOUT)){
                String connectionTimeout = paramMap.get(HIKARI_CONN_TIMEOUT);
                if (StringUtils.isNumeric(connectionTimeout)){
                    this.dataSource.setConnectionTimeout(Long.parseLong(connectionTimeout));
                }
            }

            // now support config HIKARI_MAXIMUM_POOL_SIZE by ConnectionParam
            if (paramMap.containsKey(HIKARI_MAXIMUM_POOL_SIZE)){
                String maximumPoolSize = paramMap.get(HIKARI_MAXIMUM_POOL_SIZE);
                if (StringUtils.isNotBlank(maximumPoolSize)
                        && StringUtils.isNumeric(maximumPoolSize)){

                    int poolSize = Integer.parseInt(maximumPoolSize);
                    if (poolSize > 0) {
                        dataSource.setMaximumPoolSize(1);
                    }
                }
            }
        }

        // for quick release Kyuubi connection to reduce yarn resource usage
        // reset DriverManager#setLoginTimeout to
        dataSource.setIdleTimeout(60_000L);
        try {
            dataSource.setLoginTimeout(300);
        } catch (SQLException e) {
            logger.info("set LoginTimeout fail for hive datasource");
        }

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        logger.info("Init {} success.", getClass().getName());
    }

    @Override
    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        super.checkEnv(baseConnectionParam);
        checkKerberosEnv();
    }

    private void checkKerberosEnv() {
        String krb5File = PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (kerberosStartupState && StringUtils.isNotBlank(krb5File)) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5File);
            try {
                Config.refresh();
                Class<?> kerberosName = Class.forName("org.apache.hadoop.security.authentication.util.KerberosName");
                Field field = kerberosName.getDeclaredField("defaultRealm");
                field.setAccessible(true);
                field.set(null, Config.getInstance().getDefaultRealm());
            } catch (Exception e) {
                throw new RuntimeException("Update Kerberos environment failed.", e);
            }
        }
    }

    private UserGroupInformation createUserGroupInformation(String username) {
        String krb5File = PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH);
        String keytab = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH);
        String principal = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME);

        try {
            UserGroupInformation ugi = CommonUtil.createUGI(getHadoopConf(), principal, keytab, krb5File, username);
            try {
                Field isKeytabField = ugi.getClass().getDeclaredField("isKeytab");
                isKeytabField.setAccessible(true);
                isKeytabField.set(ugi, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.warn(e.getMessage());
            }

            kerberosRenewalService.scheduleWithFixedDelay(() -> {
                try {
                    ugi.checkTGTAndReloginFromKeytab();
                } catch (IOException e) {
                    logger.error("Check TGT and Renewal from Keytab error", e);
                }
            }, 5, 5, TimeUnit.MINUTES);
            return ugi;
        } catch (IOException e) {
            throw new RuntimeException("createUserGroupInformation fail. ", e);
        }
    }

    protected Configuration createHadoopConf() {
        Configuration hadoopConf = new Configuration();
        hadoopConf.setBoolean("ipc.client.fallback-to-simple-auth-allowed", true);
        return hadoopConf;
    }

    protected Configuration getHadoopConf() {
        return this.hadoopConf;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
            if (retryGetConnection && kerberosStartupState) {
                retryGetConnection = false;
                createUserGroupInformation(baseConnectionParam.getUser());
                Connection connection = getConnection();
                retryGetConnection = true;
                return connection;
            }
            logger.error("get oneSessionDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } finally {
            kerberosRenewalService.shutdown();
            this.ugi = null;
        }
        logger.info("Closed Hive datasource client.");

    }
}

```
