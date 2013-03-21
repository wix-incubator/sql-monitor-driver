package com.wixpress.utils.sqlMonitor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
* @author yoav
* @since 3/1/11
*/
class ConnectionProxyInvocationHandler extends LoggingInvocationHandler {

    public ConnectionProxyInvocationHandler(Connection actualConnection) {
        super(actualConnection);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object actualResult = super.invokeAndLog(proxy, method, args, Level.DEBUG);
        if (actualResult instanceof CallableStatement) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{CallableStatement.class},
                    new CallableStatementProxyInvocationHandler((CallableStatement) actualResult));
        }
        else if (actualResult instanceof PreparedStatement) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class},
                    new PreparedStatementProxyInvocationHandler((PreparedStatement)actualResult));
        }
        else if (actualResult instanceof Statement) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{Statement.class},
                    new StatementProxyInvocationHandler((Statement)actualResult));
        }
        else
            return actualResult;
    }

    /**
     * setAutoCommit
     *commit
     * rollback
     * close
     * setReadOnly
     * setTransactionIsolation
     *
      */

}
