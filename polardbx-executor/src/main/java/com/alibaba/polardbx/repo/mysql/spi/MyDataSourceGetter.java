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

package com.alibaba.polardbx.repo.mysql.spi;

import com.alibaba.polardbx.common.model.Group;
import com.alibaba.polardbx.executor.common.ExecutorContext;
import com.alibaba.polardbx.executor.common.TopologyHandler;
import com.alibaba.polardbx.executor.spi.IDataSourceGetter;
import com.alibaba.polardbx.executor.spi.IGroupExecutor;
import com.alibaba.polardbx.group.jdbc.TGroupDataSource;

import javax.sql.DataSource;

public class MyDataSourceGetter implements IDataSourceGetter {

    private final String schemaName;

    public MyDataSourceGetter(String schemaName) {
        this.schemaName = schemaName;
    }

    public static TGroupDataSource get(String schemaName, String groupName) {
        return new MyDataSourceGetter(schemaName).getDataSource(groupName);
    }

    @Override
    public TGroupDataSource getDataSource(String schemaName, String group) {
        ExecutorContext executorContext = ExecutorContext.getContext(schemaName);
        TopologyHandler topology = executorContext.getTopologyHandler();
        return getDatasourceByGroupNode(topology, group);
    }

    @Override
    public TGroupDataSource getDataSource(String group) {
        return getDataSource(schemaName, group);
    }

    private TGroupDataSource getDatasourceByGroupNode(TopologyHandler topology, String groupNode) {
        if ("undecided".equals(groupNode)) {
            return null;
        }
        IGroupExecutor matrixExecutor = topology.get(groupNode);

        if (matrixExecutor == null) {
            return null;
        }
        /*
         * ????????????hack?????? ??????????????????topologic??????????????????????????????mysql??????type??????
         * ????????????????????????Datasource?????????RemotingExecutor
         * com.taobao.ustore.jdbc.mysql.My_Reponsitory.buildRemoting(Group
         * group) ????????????type??????mysql.
         * ?????????????????????type?????????????????????DataSource???????????????oracle?????????mysql.
         * ?????????oracle??????type????????????oracle..
         */
        Group.GroupType type = matrixExecutor.getGroupInfo().getType();
        DataSource ds = matrixExecutor.getDataSource();

        if (isNotValidateNode(type)) {
            throw new IllegalArgumentException("target node is not a validated Jdbc node");
        }

        if (ds == null) {
            throw new IllegalArgumentException("can't find ds by group name ");
        }
        return (TGroupDataSource) ds;
    }

    protected boolean isNotValidateNode(Group.GroupType type) {
        return !Group.GroupType.MYSQL_JDBC.equals(type);
    }

}
