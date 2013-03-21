package com.wixpress.utils.sqlMonitor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yoav
 * @since 3/1/11
 */
class StatementProxyInvocationHandler extends LoggingInvocationHandler {

    private List<String> batchSqls = new ArrayList<String>();

    private static List<MethodToTrack> methodsToTrackAsInfo = new ArrayList<MethodToTrack>();
    static {
        methodsToTrackAsInfo.add(new MethodToTrack("executeUpdate", int.class, String.class));
        methodsToTrackAsInfo.add(new MethodToTrack("execute", boolean.class, String.class));
        methodsToTrackAsInfo.add(new MethodToTrack("executeUpdate", int.class, String.class, int.class));
        methodsToTrackAsInfo.add(new MethodToTrack("executeUpdate", int.class, String.class, int[].class));
        methodsToTrackAsInfo.add(new MethodToTrack("executeUpdate", int.class, String.class, String[].class));

        methodsToTrackAsInfo.add(new MethodToTrack("execute", boolean.class, String.class, int.class));
        methodsToTrackAsInfo.add(new MethodToTrack("execute", boolean.class, String.class, int[].class));
        methodsToTrackAsInfo.add(new MethodToTrack("execute", boolean.class, String.class, String[].class));
    }


    public StatementProxyInvocationHandler(Statement statement) {
        super(statement);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object actualResult;
        if (isThisMethod(method, "executeQuery", ResultSet.class, String.class)) {
            Pair<Object, Long> resultPair = invokeLogAndReturnTime(proxy, method, args);
            actualResult = resultPair.left;
            long executeTime = resultPair.right;
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ResultSet.class},
                    new ResultSetProxyInvocationHandler((ResultSet) actualResult, (String)args[0], executeTime));
        }
        else if (isThisMethod(method, "addBatch", Void.class, String.class)) {
            actualResult = super.invokeAndLog(proxy, method, args);
            batchSqls.add((String)args[0]);
        }
        else if (isThisMethod(method, "clearBatch", Void.class)) {
            actualResult = super.invokeAndLog(proxy, method, args);
            batchSqls.clear();
        }
        else if (isThisMethod(method, "executeBatch", Void.class, int[].class)) {
            Pair<Object, Long> resultPair = super.invokeLogAndReturnTime(proxy, method, args, Level.OFF);
            actualResult = resultPair.left;
            log.info("method=[Statement.executeBatch] executionTime=[{}] sqls=[{}]", resultPair.right, batchSqls);
            batchSqls.clear();
        }
        else if (isTrackMethodAsInfo(method)) {
            actualResult = super.invokeAndLog(proxy, method, args, Level.INFO);
        }
        else {
            actualResult = super.invokeAndLog(proxy, method, args);
        }

        if (actualResult instanceof ResultSet) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ResultSet.class},
                    new ResultSetProxyInvocationHandler((ResultSet) actualResult));
        }
        else
            return actualResult;
    }

    private boolean isTrackMethodAsInfo(Method method) {
        for (MethodToTrack methodToTrack: methodsToTrackAsInfo) {
            if (isThisMethod(method,  methodToTrack.name, methodToTrack.returnType, methodToTrack.params))
                return true;
        }
        return false;
    }

    private static class MethodToTrack {

        String name;
        Class<?> returnType;
        Class<?>[] params;

        public MethodToTrack(String name, Class<?> returnType, Class<?> ... params) {
            this.name = name;
            this.returnType = returnType;
            this.params = params;
        }
    }

}
        /**
         * execute
         * executeBatch
         * addBatch
         */
