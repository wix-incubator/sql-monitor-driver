package com.wixpress.utils.sqlMonitor;

import ch.qos.logback.classic.Level;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
* @author yoav
* @since 3/1/11
*/
class PreparedStatementProxyInvocationHandler extends StatementProxyInvocationHandler {
    public PreparedStatementProxyInvocationHandler(PreparedStatement preparedStatement) {
        super(preparedStatement);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object actualResult = super.invoke(proxy, method, args);
        return actualResult;
    }
}
