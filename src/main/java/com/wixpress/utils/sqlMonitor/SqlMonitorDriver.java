package com.wixpress.utils.sqlMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC driver that wraps another jdbc driver and adds logging to all the sql operations
 * usage: prefix the jdbc URL with sql-monitor:
 *
 * e.g.
 * instead of
 * <code>jdbc:mysql://host:port/db</code>
 * use
 * <code>jdbc:sql-monitor:mysql://host:port/db</code>
 * User: yoava
 * Date: 08/02/2009
 * Time: 13:52:52
 * To change this template use File | Settings | File Templates.
 */
public class SqlMonitorDriver implements java.sql.Driver {

    protected static Logger log = LoggerFactory.getLogger(SqlMonitorDriver.class);
    private static Map<String, Driver> drivers = new ConcurrentHashMap<String, Driver>();
    private static ThreadLocal<Long> threadDBTime = new ThreadLocal<Long>();

    static 	{
        try {
            DriverManager.registerDriver(new SqlMonitorDriver());
        } catch (SQLException e) {
            log.error("failed to register the SqlMonitorDriver");
        }
    }

    public SqlMonitorDriver() throws SQLException {
    }

    public Connection connect(String url, Properties info) throws SQLException {
        String underlyingJdbcUrl = underlyingJdbcUrl(url);
        long start = System.nanoTime();
        try {
            Driver actualDriver = getActualDriver(underlyingJdbcUrl);
            final Connection actualConnection = actualDriver.connect(underlyingJdbcUrl, info);
            log.info("opened connection time=[{}] result=[{}]", System.nanoTime() - start, actualConnection);
            return (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class<?>[]{Connection.class},
                    new ConnectionProxyInvocationHandler(actualConnection));
        }
        catch (SQLException e) {
            log.error("failed opening connection time=[{}] result=[{}]", System.nanoTime() - start, e.getMessage());
            throw e;
        }
    }

    private Driver getActualDriver(String underlyingJdbcUrl) throws SQLException {
        return DriverManager.getDriver(underlyingJdbcUrl);
    }

    private String underlyingJdbcUrl(String url) {
        return url.replace("sql-monitor:", "");
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:sql-monitor");
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        String underlyingJdbcUrl = underlyingJdbcUrl(url);
        Driver actualDriver = getActualDriver(underlyingJdbcUrl);
        return actualDriver.getPropertyInfo(url, info);
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 1;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    public static void addDBTime(long timeDelta) {
        Long dbTime = threadDBTime.get();
        if (dbTime == null)
            threadDBTime.set(timeDelta);
        else
            threadDBTime.set(dbTime + timeDelta);
    }

    public static long getThreadDBTime() {
        return threadDBTime.get();
    }

    public static void resetThreadDBTime() {
        threadDBTime.set(0L);
    }
}
