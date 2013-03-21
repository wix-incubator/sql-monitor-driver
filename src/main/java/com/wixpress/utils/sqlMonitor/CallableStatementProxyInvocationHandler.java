package com.wixpress.utils.sqlMonitor;

import ch.qos.logback.classic.Level;

import java.lang.reflect.Method;
import java.sql.CallableStatement;

/**
* @author yoav
* @since 3/1/11
*/
class CallableStatementProxyInvocationHandler extends PreparedStatementProxyInvocationHandler {
    public CallableStatementProxyInvocationHandler(CallableStatement callableStatement) {
        super(callableStatement);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object actualResult = super.invoke(proxy, method, args);
        return actualResult;
    }

    /**
     * sql
     * parameters
     * execution time
     */
}
