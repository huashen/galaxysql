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

package com.alibaba.polardbx.executor.mpp.discover;

import com.alibaba.polardbx.common.exception.TddlNestableRuntimeException;
import com.alibaba.polardbx.common.utils.thread.NamedThreadFactory;
import com.alibaba.polardbx.config.ConfigDataMode;
import com.alibaba.polardbx.executor.utils.ExecUtils;
import com.alibaba.polardbx.gms.node.GmsNodeManager;
import com.alibaba.polardbx.gms.node.InternalNode;
import com.alibaba.polardbx.gms.node.InternalNodeManager;
import com.alibaba.polardbx.gms.node.Node;
import com.alibaba.polardbx.gms.node.NodeStatusManager;
import com.alibaba.polardbx.gms.topology.ServerInfoRecord;
import com.alibaba.polardbx.gms.topology.SystemDbHelper;
import com.alibaba.polardbx.gms.util.MetaDbUtil;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.alibaba.polardbx.gms.metadb.GmsSystemTables.NODE_INFO;

public class PolarDBXNodeStatusManager extends NodeStatusManager {

    protected ScheduledFuture checkDelayFuture;

    public PolarDBXNodeStatusManager(InternalNodeManager nodeManager, InternalNode localNode) {
        super(nodeManager, NODE_INFO, localNode);
        init();
    }

    @Override
    protected void doInit() {
        try (Connection conn = getConnection()) {

            doExecuteUpdate(deleteOldNodeSql, conn);

            doExecuteUpdate(insertOrUpdateTableMetaSql(localNode, 0), conn);

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("Mpp-Leader-Factory", true));
            injectFuture = scheduledExecutorService
                .scheduleWithFixedDelay(new Notify0dbTask(), 0L, KEEPALIVE_INTERVAR,
                    TimeUnit.SECONDS);

            checkFuture =
                scheduledExecutorService.scheduleWithFixedDelay(
                    new CheckAllNodeTask(), 0L, ACTIVE_LEASE, TimeUnit.SECONDS);
            logger.warn("injectNode " + localNode + " over");

            if (ConfigDataMode.isMasterMode()) {
                checkDelayFuture =
                    scheduledExecutorService.scheduleWithFixedDelay(
                        new CheckLearnerDNStatusTask(nodeManager), 0L, CheckLearnerDNStatusTask.frequency,
                        TimeUnit.SECONDS);
            }
        } catch (Throwable t) {
            logger.error("init PolarDBXNodeStatusManager error:", t);
            //???????????????????????????????????????????????????
            throw new TddlNestableRuntimeException(t);
        }

        ExecUtils.syncNodeStatus(SystemDbHelper.DEFAULT_DB_NAME);
    }

    @Override
    protected void updateActiveNodes(String instId, InternalNode node, Set<InternalNode> activeNodes,
                                     Set<InternalNode> otherActiveNodes, int role) {
        if (localNode.getInstId().equalsIgnoreCase(instId)) {
            //????????????????????????????????????????????????????????????????????????instId?????????
            activeNodes.add(node);
        } else if (ConfigDataMode.isMasterMode() && (role & ROLE_HTAP) == ROLE_HTAP) {
            //??????????????????????????????????????????????????????HTAP??????
            otherActiveNodes.add(node);
        }
    }

    @Override
    protected void doDestroy(boolean stop) {
        if (injectFuture != null) {
            injectFuture.cancel(true);
        }
        if (checkFuture != null) {
            checkFuture.cancel(true);
        }
        if (checkDelayFuture != null) {
            checkDelayFuture.cancel(true);
        }
        updateLocalNode(STATUS_SHUTDOWN);
        //??????SYNC???????????????????????????????????????
        ExecUtils.syncNodeStatus(SystemDbHelper.DEFAULT_DB_NAME);
        if (localNode.isLeader()) {
            localNode.setLeader(false);
        }
    }

    private class Notify0dbTask implements Runnable {
        @Override
        public void run() {
            GmsNodeManager.GmsNode gmsNode = GmsNodeManager.getInstance().getLocalNode();
            //FIXME ??????server_info?????????????????????????????????????????????
            if (gmsNode == null || gmsNode.status == ServerInfoRecord.SERVER_STATUS_READY) {
                updateLocalNode(STATUS_ACTIVE);
            }
        }
    }

    private class CheckAllNodeTask implements Runnable {
        @Override
        public void run() {
            refreshNode();
        }
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return MetaDbUtil.getConnection();
    }

    @Override
    protected String updateTableMetaSql(int status) {
        //STATUS_TEMP_INACTIVE??????????????????????????????
        if (status == STATUS_ACTIVE) {
            GmsNodeManager.GmsNode gmsNode = GmsNodeManager.getInstance().getLocalNode();
            //polarx????????????????????????????????????????????????
            if (gmsNode != null && !ConfigDataMode.isMasterMode()) {
                boolean changeHtapRole = false;
                if (gmsNode.instType == ServerInfoRecord.INST_TYPE_HTAP_SLAVE && !localNode.isHtap()) {
                    localNode.setHtap(true);
                    changeHtapRole = true;
                } else if (gmsNode.instType != ServerInfoRecord.INST_TYPE_HTAP_SLAVE && localNode.isHtap()) {
                    localNode.setHtap(false);
                    changeHtapRole = true;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        gmsNode.getServerKey() + ":gmsNode.instType=" + gmsNode.instType + ",localNode.isHtap()="
                            + localNode.isHtap() + ",changeHtapRole=" + changeHtapRole + ",getRole(localNode)="
                            + getRole(localNode));
                }

                if (changeHtapRole) {
                    return String.format(
                        "update %s set `IP`='%s',`STATUS`=%d,`GMT_MODIFIED`=CURRENT_TIMESTAMP,`ROLE`=%d "
                            + "where `CLUSTER`='%s' and `NODEID`='%s'",
                        tableName, localNode.getHost(), status, getRole(localNode), localNode.getCluster(),
                        localNode.getNodeIdentifier());
                }
            }

            return String.format(
                "update %s set `IP`='%s',`STATUS`=%d,`GMT_MODIFIED`=CURRENT_TIMESTAMP where `CLUSTER`='%s' and "
                    + "`NODEID`='%s' and (`STATUS`!=3 or TIMESTAMPDIFF(SECOND,`GMT_MODIFIED`,CURRENT_TIMESTAMP)>60)",
                tableName, localNode.getHost(), status, localNode.getCluster(), localNode.getNodeIdentifier());
        } else {
            return String.format(
                "update %s set `IP`='%s',`STATUS`=%d,`GMT_MODIFIED`=CURRENT_TIMESTAMP where `CLUSTER`='%s' and "
                    + "`NODEID`='%s'",
                tableName, localNode.getHost(), status, localNode.getCluster(), localNode.getNodeIdentifier());
        }
    }

    @Override
    protected void checkLeader(Connection conn, String leaderId) {
        //??????PolarDB-X??????????????????
        if (ConfigDataMode.isMasterMode()) {
            if (StringUtils.isEmpty(leaderId)) {
                //???????????????leader??????,????????????
                resetLeaderStatus();
                try {
                    if (tryGetLock(conn)) {
                        tryMarkLeader(conn);
                    }
                } catch (SQLException e) {
                    logger.warn("checkLeader error", e);
                } finally {
                    releaseLock(conn);
                }
            } else if (!localNode.getNodeIdentifier().equalsIgnoreCase(leaderId)) {
                //??????leader,??????leader???????????????????????????????????????????????????leader??????
                resetLeaderStatus();
            } else {
                //????????????metaDb???????????????leader??????????????????leader
                localNode.setLeader(true);
            }
        }
    }

    private void resetLeaderStatus() {
        localNode.setLeader(false);
        List<Node> coordinators = nodeManager.getAllCoordinators();
        if (coordinators != null && !coordinators.isEmpty()) {
            for (Node node : coordinators) {
                if (node.isLeader() && node.getNodeIdentifier().equalsIgnoreCase(localNode.getNodeIdentifier())) {
                    node.setLeader(false);
                }
            }
        }
    }

    private boolean tryGetLock(Connection conn) {
        try (Statement statement = conn.createStatement();
            ResultSet lockRs = statement.executeQuery("SELECT GET_LOCK('" + localNode.getCluster() + "', 0) ")) {
            return lockRs.next() && lockRs.getInt(1) == 1;
        } catch (Throwable e) {
            logger.warn("tryGetLock error", e);
            return false;
        }
    }

    private boolean releaseLock(Connection conn) {
        try (Statement statement = conn.createStatement();
            ResultSet lockRs = statement.executeQuery("SELECT RELEASE_LOCK('" + localNode.getCluster() + "') ")) {
            return lockRs.next() && lockRs.getInt(1) == 1;
        } catch (Exception e) {
            logger.warn("releaseLock error", e);
            return false;
        }
    }

    private void tryMarkLeader(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            ResultSet rs = statement.executeQuery(checkLeaderSql);
            boolean beLeader = false;
            if (!rs.next()) {
                logger.warn("setLeader:" + localNode);
                statement.executeUpdate(deleteLeaderSql);
                synchronized (this) {
                    statement.executeUpdate(insertOrUpdateTableMetaSql(localNode, ROLE_LEADER));
                    beLeader = true;
                }
            } else {
                String nodeId = rs.getString("NODEID");
                if (localNode.getNodeIdentifier().equalsIgnoreCase(nodeId)) {
                    beLeader = true;
                }
            }
            conn.commit();
            if (beLeader) {
                //????????????????????????
                ExecUtils.syncNodeStatus(SystemDbHelper.DEFAULT_DB_NAME);
                localNode.setLeader(true);
            }
        } catch (Throwable t) {
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
        }
    }

}