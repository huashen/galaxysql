/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.common.mock;

import com.alibaba.polardbx.common.exception.NotSupportException;
import com.alibaba.polardbx.common.utils.logger.Logger;
import com.alibaba.polardbx.common.utils.logger.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MockStatement implements Statement {

    private static final Logger logger = LoggerFactory.getLogger(MockStatement.class);
    protected MockDataSource mds;
    protected String sql;
    protected List<String> sqls = new ArrayList<String>();
    protected int queryTimeout;
    protected int fetchSize;
    protected int maxRows;
    private boolean isClosed;
    private int closeInvokingTimes = 0;
    private boolean success = true;
    private int executeSqlInvokingTimes = 0;

    public MockStatement(String method, MockDataSource mockDataSource, String sql) {
        this.sql = sql;
        this.mds = mockDataSource;
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, method, null, null));
    }

    public MockStatement(String method, MockDataSource mockDataSource) {
        this.mds = mockDataSource;
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, method, null, null));
    }

    public void addBatch(String sql) throws SQLException {
        mds.checkState();
        this.sqls.add(sql);
    }

    public void cancel() throws SQLException {
        mds.checkState();
        throw new NotSupportException("");
    }

    public void clearBatch() throws SQLException {
        mds.checkState();
        this.sqls.clear();
    }

    public void clearWarnings() throws SQLException {
        throw new NotSupportException("");
    }

    public void close() throws SQLException {
        mds.checkState();
        closeInvokingTimes++;
    }

    protected void checkClosed() throws SQLException {
        if (isClosed) {
            throw new SQLException("closed");
        }
    }

    public boolean execute(String sql) throws SQLException {
        mds.checkState();
        this.sql = sql;
        executeSqlInvokingTimes++;
        logger.warn("[execute(String)]" + sql);
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, "execute", this.sql, null));
        return success;
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        mds.checkState();
        this.sql = sql;
        executeSqlInvokingTimes++;
        logger.warn("[execute(String,int)]" + sql);
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, "execute_sql_int", this.sql, null));
        return success;
    }

    protected ExecuteHandler executerHandler = new ExecuteHandler() {

        public ResultSet execute(String method, String tsql) {
            sql = tsql;
            executeSqlInvokingTimes++;
            logger.warn("[executerHandler]" + sql);
            MockDataSource.record(new MockDataSource.ExecuteInfo(MockStatement.this.mds,
                method,
                MockStatement.this.sql,
                null));
            return new MockResultSet(mds, MockDataSource.popPreData());
        }

        public boolean executeSql(String method, String tsql) {
            sql = tsql;
            executeSqlInvokingTimes++;
            logger.warn("[executerHandler]" + sql);
            MockDataSource.record(new MockDataSource.ExecuteInfo(MockStatement.this.mds,
                method,
                MockStatement.this.sql,
                null));
            return true;
        }

        ;

    };

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        mds.checkState();
        return executerHandler.executeSql("execute#sql_int[", sql);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        mds.checkState();
        return executerHandler.executeSql("execute#sql_string[", sql);
    }

    public int[] executeBatch() throws SQLException {
        mds.checkState();
        logger.warn("[executeBatch]" + sql);
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, "executeBatch", this.sql, null));
        return new int[] {-1, -1};
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        mds.checkState();
        return executerHandler.execute("executeQuery", sql);
    }

    protected int updateInternal(String method, String sql) {
        try {
            Thread.sleep(insertSleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.sql = sql;
        MockDataSource.record(new MockDataSource.ExecuteInfo(this.mds, method, this.sql, null));
        logger.warn("[UpdateHandler]" + sql);
        return MockDataSource.popPreAffectedRow();
    }

    public int executeUpdate(String sql) throws SQLException {
        mds.checkState();
        return updateInternal("executeUpdate", sql);
    }

    private long insertSleepTime = 0;

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        mds.checkState();
        return updateInternal("executeUpdate#sql_int", sql);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        mds.checkState();
        return updateInternal("executeUpdate#sql_int[", sql);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        mds.checkState();
        return updateInternal("executeUpdate#sql_string[", sql);
    }

    public Connection getConnection() throws SQLException {
        throw new NotSupportException("");
    }

    public int getFetchDirection() throws SQLException {
        throw new NotSupportException("");
    }

    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new NotSupportException("");
    }

    public int getMaxFieldSize() throws SQLException {
        throw new NotSupportException("");
    }

    public int getMaxRows() throws SQLException {
        return this.maxRows;
    }

    public boolean getMoreResults() throws SQLException {
        throw new NotSupportException("");
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new NotSupportException("");
    }

    public int getQueryTimeout() throws SQLException {
        return this.queryTimeout;
    }

    public ResultSet getResultSet() throws SQLException {
        mds.checkState();
        return new MockResultSet(mds, MockDataSource.popPreData());
    }

    public int getResultSetConcurrency() throws SQLException {
        throw new NotSupportException("");
    }

    public int getResultSetHoldability() throws SQLException {
        throw new NotSupportException("");
    }

    public int getResultSetType() throws SQLException {
        throw new NotSupportException("");
    }

    public int getUpdateCount() throws SQLException {
        throw new NotSupportException("");
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void setCursorName(String name) throws SQLException {
        throw new NotSupportException("");

    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new NotSupportException("");

    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new NotSupportException("");
    }

    public void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw new NotSupportException("");
    }

    public void setMaxRows(int max) throws SQLException {
        this.maxRows = max;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        this.queryTimeout = seconds;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getCloseInvokingTimes() {
        return closeInvokingTimes;
    }

    public void setCloseInvokingTimes(int closeInvokingTimes) {
        this.closeInvokingTimes = closeInvokingTimes;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getExecuteSqlInvokingTimes() {
        return executeSqlInvokingTimes;
    }

    public void setExecuteSqlInvokingTimes(int executeSqlInvokingTimes) {
        this.executeSqlInvokingTimes = executeSqlInvokingTimes;
    }

    public ExecuteHandler getExecuterHandler() {
        return executerHandler;
    }

    public void setExecuterHandler(ExecuteHandler executerHandler) {
        this.executerHandler = executerHandler;
    }

    public long getInsertSleepTime() {
        return insertSleepTime;
    }

    public void setInsertSleepTime(long insertSleepTime) {
        this.insertSleepTime = insertSleepTime;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
    }

    public boolean isPoolable() throws SQLException {
        return false;
    }

    public void closeOnCompletion() throws SQLException {
    }

    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

}
