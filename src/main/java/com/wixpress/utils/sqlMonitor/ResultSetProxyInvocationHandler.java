package com.wixpress.utils.sqlMonitor;

import com.wixpress.framework.util.Pair;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
* @author yoav
* @since 3/1/11
*/
class ResultSetProxyInvocationHandler extends LoggingInvocationHandler {

    private String sql;
    private long executionTime;
    private long fetchTime = 0;
    private static final String CLOSE = "close";
    private static Set<String> methodsToTrack = new HashSet<String>();
    static {
        methodsToTrack.add("next");
        methodsToTrack.add("absolute");
        methodsToTrack.add("relative");
        methodsToTrack.add("previous");
    }

    public ResultSetProxyInvocationHandler(ResultSet resultSet, String sql, long executionTime) {
        super(resultSet);
        this.sql = sql;
        this.executionTime = executionTime;
    }

    public ResultSetProxyInvocationHandler(ResultSet resultSet) {
        super(resultSet);
        this.sql = "N/A";
        this.executionTime = 0;
    }

    @Override
    /**
     * Track the time of cursor changing methods
     */

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object actualResult;
        if (methodsToTrack.contains(method.getName())) {
            Pair<Object, Long> resultPair = invokeLogAndReturnTime(proxy, method, args);
            actualResult = resultPair.left;
            fetchTime += resultPair.right;
        }
        else if (CLOSE.equals(method.getName())) {
            Pair<Object, Long> resultPair = invokeLogAndReturnTime(proxy, method, args);
            actualResult = resultPair.left;
            fetchTime += resultPair.right;
            log.trace("resultSet statistics executionTime=[{}] fetchTime=[{}] sql=[{}]", new Object[] {executionTime, fetchTime, sql});
        }
        else {
            actualResult = super.invokeAndLog(proxy, method, args);
        }
        return actualResult;
    }
}
