```java
/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.datasource.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of the standard JDBC {@link javax.sql.DataSource} interface,
 * configuring the plain old JDBC {@link java.sql.DriverManager} via bean properties, and
 * returning a new {@link java.sql.Connection} from every {@code getConnection} call.
 *
 * <p><b>NOTE: This class is not an actual connection pool; it does not actually
 * pool Connections.</b> It just serves as simple replacement for a full-blown
 * connection pool, implementing the same standard interface, but creating new
 * Connections on every call.
 *
 * <p>Useful for test or standalone environments outside of a Java EE container, either
 * as a DataSource bean in a corresponding ApplicationContext or in conjunction with
 * a simple JNDI environment. Pool-assuming {@code Connection.close()} calls will
 * simply close the Connection, so any DataSource-aware persistence code should work.
 *
 * <p><b>NOTE: Within special class loading environments such as OSGi, this class
 * is effectively superseded by {@link SimpleDriverDataSource} due to general class
 * loading issues with the JDBC DriverManager that be resolved through direct Driver
 * usage (which is exactly what SimpleDriverDataSource does).</b>
 *
 * <p>In a Java EE container, it is recommended to use a JNDI DataSource provided by
 * the container. Such a DataSource can be exposed as a DataSource bean in a Spring
 * ApplicationContext via {@link org.springframework.jndi.JndiObjectFactoryBean},
 * for seamless switching to and from a local DataSource bean like this class.
 * For tests, you can then either set up a mock JNDI environment through Spring's
 * {@link org.springframework.mock.jndi.SimpleNamingContextBuilder}, or switch the
 * bean definition to a local DataSource (which is simpler and thus recommended).
 *
 * <p>This {@code DriverManagerDataSource} class was originally designed alongside
 * <a href="https://commons.apache.org/proper/commons-dbcp">Apache Commons DBCP</a>
 * and <a href="https://sourceforge.net/projects/c3p0">C3P0</a>, featuring bean-style
 * {@code BasicDataSource}/{@code ComboPooledDataSource} classes with configuration
 * properties for local resource setups. For a modern JDBC connection pool, consider
 * <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a> instead,
 * exposing a corresponding {@code HikariDataSource} instance to the application.
 *
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see SimpleDriverDataSource
 */
public class HiveDriverManagerDataSource extends AbstractDriverBasedDataSource {

	/**
	 * Constructor for bean-style configuration.
	 */
	public HiveDriverManagerDataSource() {
	}

	/**
	 * Create a new DriverManagerDataSource with the given JDBC URL,
	 * not specifying a username or password for JDBC access.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public HiveDriverManagerDataSource(String url) {
		setUrl(url);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard
	 * DriverManager parameters.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public HiveDriverManagerDataSource(String url, String username, String password) {
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create a new DriverManagerDataSource with the given JDBC URL,
	 * not specifying a username or password for JDBC access.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param conProps the JDBC connection properties
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public HiveDriverManagerDataSource(String url, Properties conProps) {
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * Set the JDBC driver class name. This driver will get initialized
	 * on startup, registering itself with the JDK's DriverManager.
	 * <p><b>NOTE: DriverManagerDataSource is primarily intended for accessing
	 * <i>pre-registered</i> JDBC drivers.</b> If you need to register a new driver,
	 * consider using {@link SimpleDriverDataSource} instead. Alternatively, consider
	 * initializing the JDBC driver yourself before instantiating this DataSource.
	 * The "driverClassName" property is mainly preserved for backwards compatibility,
	 * as well as for migrating between Commons DBCP and this DataSource.
	 * @see java.sql.DriverManager#registerDriver(java.sql.Driver)
	 * @see SimpleDriverDataSource
	 */
	public void setDriverClassName(String driverClassName) {
		Assert.hasText(driverClassName, "Property 'driverClassName' must not be empty");
		String driverClassNameToUse = driverClassName.trim();
		try {
			Class.forName(driverClassNameToUse, true, ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]", ex);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded JDBC driver: " + driverClassNameToUse);
		}
	}


	@Override
	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		String url = getUrl();
		Assert.state(url != null, "'url' not set");
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC DriverManager Connection to [" + url + "]");
		}
		return getConnectionFromDriverManager(url, props);
	}

	/**
	 * Getting a Connection using the nasty static from DriverManager is extracted
	 * into a protected method to allow for easy unit testing.
	 * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		DriverManager.setLoginTimeout(300);
		return DriverManager.getConnection(url, props);
	}

}

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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.hive.utils.CommonUtil;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import sun.security.krb5.Config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.*;

public class HiveDataSourceClient extends CommonDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceClient.class);

    private ScheduledExecutorService kerberosRenewalService;

    private Configuration hadoopConf;
    private UserGroupInformation ugi;
    private boolean retryGetConnection = true;

    private static final String HIKARI_CONN_TIMEOUT = "connectionTimeout";

    private static final String HIKARI_MAXIMUM_POOL_SIZE = "hiveOneSessionEnable";
    private HiveDriverManagerDataSource driverManagerDataSource;


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
        this.driverManagerDataSource =
                new HiveDriverManagerDataSource(DataSourceUtils.getJdbcUrl(DbType.HIVE, baseConnectionParam),
                        baseConnectionParam.getUser(), PasswordUtils.decodePassword(baseConnectionParam.getPassword()));
        driverManagerDataSource.setDriverClassName(baseConnectionParam.getDriverClassName());

        this.jdbcTemplate = new JdbcTemplate(driverManagerDataSource);
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
            return driverManagerDataSource.getConnection();
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
