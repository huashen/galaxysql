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

package com.alibaba.polardbx.group.config;

import com.alibaba.polardbx.atom.TAtomDataSource;
import com.alibaba.polardbx.atom.TAtomDsStandard;
import com.alibaba.polardbx.atom.config.TAtomDsConfDO;
import com.alibaba.polardbx.atom.config.gms.TAtomDsGmsConfigHelper;
import com.alibaba.polardbx.common.exception.TddlException;
import com.alibaba.polardbx.common.exception.TddlNestableRuntimeException;
import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.exception.code.ErrorCode;
import com.alibaba.polardbx.common.jdbc.MasterSlave;
import com.alibaba.polardbx.common.logger.LoggerInit;
import com.alibaba.polardbx.common.model.lifecycle.AbstractLifecycle;
import com.alibaba.polardbx.common.model.lifecycle.Lifecycle;
import com.alibaba.polardbx.common.utils.GeneralUtil;
import com.alibaba.polardbx.common.utils.Pair;
import com.alibaba.polardbx.common.utils.logger.Logger;
import com.alibaba.polardbx.common.utils.logger.LoggerFactory;
import com.alibaba.polardbx.config.ConfigDataListener;
import com.alibaba.polardbx.config.ConfigDataMode;
import com.alibaba.polardbx.gms.config.impl.ConnPoolConfig;
import com.alibaba.polardbx.gms.ha.HaSwitchParams;
import com.alibaba.polardbx.gms.ha.HaSwitcher;
import com.alibaba.polardbx.gms.ha.impl.StorageHaManager;
import com.alibaba.polardbx.gms.ha.impl.StorageNodeHaInfo;
import com.alibaba.polardbx.gms.ha.impl.StorageRole;
import com.alibaba.polardbx.gms.listener.ConfigListener;
import com.alibaba.polardbx.gms.listener.impl.MetaDbConfigManager;
import com.alibaba.polardbx.gms.listener.impl.MetaDbDataIdBuilder;
import com.alibaba.polardbx.gms.topology.ServerInstIdManager;
import com.alibaba.polardbx.gms.util.GroupInfoUtil;
import com.alibaba.polardbx.gms.util.InstIdUtil;
import com.alibaba.polardbx.gms.util.MetaDbLogUtil;
import com.alibaba.polardbx.group.jdbc.DataSourceFetcher;
import com.alibaba.polardbx.group.jdbc.DataSourceLazyInitWrapper;
import com.alibaba.polardbx.group.jdbc.DataSourceWrapper;
import com.alibaba.polardbx.group.jdbc.TGroupDataSource;
import com.alibaba.polardbx.optimizer.biv.MockUtils;
import com.alibaba.polardbx.stats.MatrixStatistics;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.polardbx.gms.topology.StorageInfoRecord.INST_KIND_MASTER;

/**
 * @author ?????? 2017???11???21??? ??????10:48:24
 * @since 5.0.0
 */
public class OptimizedGroupConfigManager extends AbstractLifecycle implements Lifecycle {

    private static final Logger logger = LoggerFactory
        .getLogger(OptimizedGroupConfigManager.class);

    private final ConfigDataListener configReceiver;
    private final TGroupDataSource groupDataSource;
    private final HaSwitcher groupDsSwithcer;

    /**
     * ????????????GroupConfigManager???????????????????????????????????????atomDataSource
     */
    private boolean createTAtomDataSource = true;

    private volatile Map<String/* Atom dbIndex */, DataSourceWrapper/* Wrapper??????Atom DS */> dataSourceWrapperMap =
        new HashMap<>();

    private volatile GroupDataSourceHolder groupDataSourceHolder = null;
    private volatile String stroageInstId = "";

    public OptimizedGroupConfigManager(TGroupDataSource tGroupDataSource) {
        this.groupDataSource = tGroupDataSource;
        this.configReceiver = new ConfigReceiver();
        this.groupDsSwithcer = new GroupDataSourceSwitcher(groupDataSource);
        ((ConfigReceiver) this.configReceiver).setConfigManager(this);
    }

    /**
     * @param dsWeightCommaStr : ?????? db0:rwp1q1i0, db1:rwp0q0i1
     */
    public static List<DataSourceWrapper> buildDataSourceWrapper(String dsWeightCommaStr, DataSourceFetcher fetcher) {
        String[] dsWeightArray = dsWeightCommaStr.split(","); // ???????????????db0:rwp1q1i0,
        // db1:rwp0q0i1
        List<DataSourceWrapper> dss = new ArrayList<DataSourceWrapper>(dsWeightArray.length);
        for (int i = 0; i < dsWeightArray.length; i++) {
            String[] dsAndWeight = dsWeightArray[i].split(":"); // ???????????????db0:rwp1q1i0
            String dsKey = dsAndWeight[0].trim();
            String weightStr = dsAndWeight.length == 2 ? dsAndWeight[1] : null;

            try {
                DataSourceWrapper dsw = buildDataSourceWrapper(dsKey, weightStr, i, fetcher);
                dss.add(dsw);
            } catch (Throwable e) {

                LoggerInit.TDDL_DYNAMIC_CONFIG.error(String
                    .format("[buildDataSourceWrapper] failed, dsKey is [%s], weightStr is [%s] ", dsKey, weightStr), e);

                throw GeneralUtil.nestedException(String
                    .format("[buildDataSourceWrapper] failed, dsKey is [%s], weightStr is [%s] ", dsKey, weightStr), e);
            }

        }
        return dss;
    }

    protected static DataSourceWrapper buildDataSourceWrapper(String dsKey, String weightStr, int index,
                                                              DataSourceFetcher fetcher) {

        // ????????????group??????????????????dataSource??????????????????group??????
        // ??????dataSource????????? ??????????????????dataSource???????????????
        TAtomDataSource dataSource = fetcher.getDataSource(dsKey);
        DataSourceWrapper dsw = new DataSourceWrapper(dsKey, weightStr, dataSource, index);
        return dsw;
    }

    /**
     * ???Diamond?????????????????????????????????TAtomDataSource????????????????????????????????????DBSelector ---add by
     * mazhidan.pt
     */
    @Override
    public void doInit() {
        if (ConfigDataMode.isFastMock()) {
            // mock weight comma str
            parse(MockUtils.mockDsWeightCommaStr(groupDataSource.getDbGroupKey()));
            return;
        }

        // To be load by MetaDB
        initGroupDataSourceByMetaDb();
    }

    protected void initGroupDataSourceByMetaDb() {

        String instId = InstIdUtil.getInstId();
        if (groupDataSource.isEnforceMaster()) {
            /**
             * If curr group Ds is a force-master datasource, its storage inst must be rw-dn, should
             * its instId should use masterInstId
             */
            instId = ServerInstIdManager.getInstance().getMasterInstId();
        }
        String dbName = groupDataSource.getSchemaName();
        String groupName = groupDataSource.getDbGroupKey();

        Throwable ex = null;
        try {
            loadGroupDataSourceByMetaDb();
            registerHaSwitcher();
            MetaDbConfigManager.getInstance()
                .register(MetaDbDataIdBuilder.getGroupConfigDataId(instId, dbName, groupName), null);
            MetaDbConfigManager.getInstance()
                .bindListener(MetaDbDataIdBuilder.getGroupConfigDataId(instId, dbName, groupName),
                    new GroupDetailInfoListener(this));
        } catch (Throwable e) {
            ex = e;
            throw e;
        } finally {
            if (ex != null) {
                unregisterHaSwitcher();
            }
        }
    }

    /**
     * the Listener to handle all the change of system 'group_detail_info'
     */
    protected static class GroupDetailInfoListener implements ConfigListener {

        protected final OptimizedGroupConfigManager groupConfigManager;

        public GroupDetailInfoListener(OptimizedGroupConfigManager groupConfigManager) {
            this.groupConfigManager = groupConfigManager;
        }

        @Override
        public void onHandleConfig(String dataId, long newOpVersion) {
            this.groupConfigManager.loadGroupDataSourceByMetaDb();
        }
    }

    public void loadGroupDataSourceByMetaDb() {
        Pair<List<DataSourceWrapper>, HaSwitchParams> initResult = buildDataSourceWrapperByGms();
        List<DataSourceWrapper> dswList = initResult.getKey();
        HaSwitchParams haSwitchParams = initResult.getValue();
        resetByDataSourceWrapper(dswList);
        unregisterHaSwitcher();
        String oldStorageId = this.stroageInstId;
        this.stroageInstId = haSwitchParams.storageInstId;
        registerHaSwitcher();
        LoggerInit.TDDL_DYNAMIC_CONFIG.info(String
            .format(
                "[GroupStorageChangeSucceed] Group storageInstId change from [%s] to [%s], and HaSwitcher has been updated.",
                oldStorageId, this.stroageInstId));
    }

    protected void registerHaSwitcher() {
        String dbName = groupDataSource.getSchemaName();
        String groupName = groupDataSource.getDbGroupKey();
        StorageHaManager.getInstance().registerHaSwitcher(this.stroageInstId, dbName, groupName, groupDsSwithcer);
    }

    protected void unregisterHaSwitcher() {
        String groupName = groupDataSource.getDbGroupKey();
        StorageHaManager.getInstance().unregisterHaSwitcher(this.stroageInstId, groupName, groupDsSwithcer);
    }

    protected void unbindGroupConfigListener() {
        String instId = InstIdUtil.getInstId();
        String dbName = groupDataSource.getSchemaName();
        String groupName = groupDataSource.getDbGroupKey();
        MetaDbConfigManager.getInstance()
            .unbindListener(MetaDbDataIdBuilder.getGroupConfigDataId(instId, dbName, groupName));
    }

    protected List<DataSourceWrapper> switchGroupDs(HaSwitchParams haSwitchParams) {

        String userName = haSwitchParams.userName;
        String passwdEnc = haSwitchParams.passwdEnc;
        String phyDbName = haSwitchParams.phyDbName;
        ConnPoolConfig storageInstConfig = haSwitchParams.storageConnPoolConfig;
        String curAvailableAddr = haSwitchParams.curAvailableAddr;

        List<DataSourceWrapper> dswList = new ArrayList<>();
        try {
            String schemaName = groupDataSource.getSchemaName();
            String appName = groupDataSource.getAppName();
            String groupName = groupDataSource.getDbGroupKey();
            String availableNodeAddr = curAvailableAddr;
            boolean needDoSwitch = false;
            if (availableNodeAddr != null) {
                //??????leader?????????????????????????????????GroupDs
                Map<String, DataSourceWrapper> curDsWrappers = this.getDataSourceWrapperMap();
                if (ConfigDataMode.enableSlaveReadForPolarDbX()) {
                    //leader
                    String weightStr = GroupInfoUtil.buildWeightStr(10, 10);
                    String leaderDsKey = GroupInfoUtil
                        .buildAtomKey(groupName, haSwitchParams.storageInstId, availableNodeAddr,
                            haSwitchParams.phyDbName);
                    DataSourceWrapper newLeaderVal = curDsWrappers.get(leaderDsKey);
                    if (curDsWrappers.containsKey(leaderDsKey) && newLeaderVal != null &&
                        newLeaderVal.getWeightStr().equalsIgnoreCase(weightStr)) {
                        dswList.add(curDsWrappers.get(leaderDsKey));
                    } else {
                        TAtomDsConfDO atomDsConf = TAtomDsGmsConfigHelper
                            .buildAtomDsConfByGms(availableNodeAddr, haSwitchParams.xport, userName, passwdEnc,
                                phyDbName,
                                storageInstConfig, schemaName);
                        TAtomDataSource atomDs = new TAtomDataSource(true);
                        atomDs.init(appName, groupName, leaderDsKey, "", atomDsConf);
                        DataSourceWrapper dsw =
                            new DataSourceWrapper(leaderDsKey, GroupInfoUtil.buildWeightStr(10, 10), atomDs, 0);
                        dswList.add(dsw);
                        needDoSwitch = true;
                    }
                    //salves
                    if (haSwitchParams.storageHaInfoMap != null) {
                        String slaveWeightStr = GroupInfoUtil.buildWeightStr(10, 0);
                        int dataSourceIndex = 1;
                        for (StorageNodeHaInfo haInfo : haSwitchParams.storageHaInfoMap.values()) {
                            if (haInfo.getRole() == StorageRole.FOLLOWER) {
                                String slaveKey =
                                    GroupInfoUtil
                                        .buildAtomKey(groupName, haSwitchParams.storageInstId, haInfo.getAddr(),
                                            haSwitchParams.phyDbName);
                                DataSourceWrapper newSlaveVal = curDsWrappers.get(slaveKey);
                                if (curDsWrappers.containsKey(slaveKey) && newSlaveVal != null &&
                                    newSlaveVal.getWeightStr().equalsIgnoreCase(slaveWeightStr)) {
                                    dswList.add(curDsWrappers.get(slaveKey));
                                } else {
                                    //??????leader????????????xport???, slave???????????????
                                    int xport = -1;
                                    if (haSwitchParams.xport > 0) {
                                        xport = haInfo.getXPort();
                                    }
                                    TAtomDsConfDO slaveAtomDsConf = TAtomDsGmsConfigHelper
                                        .buildAtomDsConfByGms(haInfo.getAddr(), xport,
                                            haSwitchParams.userName,
                                            haSwitchParams.passwdEnc, haSwitchParams.phyDbName,
                                            haSwitchParams.storageConnPoolConfig, phyDbName);
                                    TAtomDataSource slaveAtomDs = new TAtomDataSource(true);
                                    slaveAtomDs.init(appName, groupName, slaveKey, "", slaveAtomDsConf);
                                    DataSourceWrapper slave =
                                        new DataSourceWrapper(slaveKey, slaveWeightStr, slaveAtomDs, dataSourceIndex++);
                                    dswList.add(slave);
                                    needDoSwitch = true;
                                }

                            }
                        }
                    }
                } else {
                    needDoSwitch = true;
                    String leaderDsKey = GroupInfoUtil
                        .buildAtomKey(groupName, haSwitchParams.storageInstId, availableNodeAddr,
                            haSwitchParams.phyDbName);
                    String weightStr = GroupInfoUtil.buildWeightStr(10, 10);
                    if (curDsWrappers.size() == 1 && curDsWrappers.containsKey(leaderDsKey)) {
                        DataSourceWrapper curDsw = curDsWrappers.get(leaderDsKey);
                        if (curDsw.getWeightStr().equalsIgnoreCase(weightStr)) {
                            needDoSwitch = false;
                        }
                    }
                    if (needDoSwitch) {
                        TAtomDsConfDO atomDsConf = TAtomDsGmsConfigHelper
                            .buildAtomDsConfByGms(availableNodeAddr, haSwitchParams.xport, userName, passwdEnc,
                                phyDbName,
                                storageInstConfig, schemaName);
                        TAtomDataSource atomDs = new TAtomDataSource(true);
                        atomDs.init(appName, groupName, leaderDsKey, "", atomDsConf);
                        DataSourceWrapper dsw = new DataSourceWrapper(leaderDsKey, weightStr, atomDs);
                        dswList.add(dsw);
                    }
                }
            }
            if (!dswList.isEmpty() && needDoSwitch) {
                resetByDataSourceWrapper(dswList);
            }
        } catch (Throwable ex) {
            throw GeneralUtil.nestedException(ex);
        }
        return dswList;
    }

    protected Pair<List<DataSourceWrapper>, HaSwitchParams> buildDataSourceWrapperByGms() {
        List<DataSourceWrapper> dswList = new ArrayList<>();
        HaSwitchParams haSwitchParams = null;
        try {
            String dbName = this.groupDataSource.getSchemaName();
            String appName = this.groupDataSource.getAppName();
            String groupName = this.groupDataSource.getDbGroupKey();

            String instId = InstIdUtil.getInstId();
            if (this.groupDataSource.isEnforceMaster() && !ConfigDataMode.isMasterMode()) {
                instId = ServerInstIdManager.getInstance().getMasterInstId();
            }

            String unitName = "";
            haSwitchParams =
                StorageHaManager.getInstance().getStorageHaSwitchParamsForInitGroupDs(instId, dbName, groupName);
            String availableAddr = haSwitchParams.curAvailableAddr;
            if (availableAddr == null) {
                throw new TddlRuntimeException(ErrorCode.ERR_GMS_GENERIC,
                    String.format("storageInst[%s] is NOT available", haSwitchParams.storageInstId));
            }
            String dsLeaderKey =
                GroupInfoUtil.buildAtomKey(groupName, haSwitchParams.storageInstId, availableAddr,
                    haSwitchParams.phyDbName);
            TAtomDsConfDO atomDsConf = TAtomDsGmsConfigHelper
                .buildAtomDsConfByGms(availableAddr, haSwitchParams.xport, haSwitchParams.userName,
                    haSwitchParams.passwdEnc, haSwitchParams.phyDbName, haSwitchParams.storageConnPoolConfig, dbName);
            String weightStr = GroupInfoUtil.buildWeightStr(10, 10);
            TAtomDataSource atomDs = new TAtomDataSource(true);
            atomDs.init(appName, groupName, dsLeaderKey, unitName, atomDsConf);
            DataSourceWrapper dsw = new DataSourceWrapper(dsLeaderKey, weightStr, atomDs, 0);
            dswList.add(dsw);

            if (ConfigDataMode.enableSlaveReadForPolarDbX() && haSwitchParams.storageKind == INST_KIND_MASTER) {
                //???PolarDb-X?????????curAvailableAddr ??????????????????????????????????????????
                if (haSwitchParams.storageHaInfoMap != null) {
                    for (StorageNodeHaInfo haInfo : haSwitchParams.storageHaInfoMap.values()) {
                        if (haInfo.getRole() == StorageRole.FOLLOWER) {
                            String slaveKey =
                                GroupInfoUtil.buildAtomKey(groupName, haSwitchParams.storageInstId, haInfo.getAddr(),
                                    haSwitchParams.phyDbName);
                            //??????leader????????????xport???, slave???????????????
                            int xport = -1;
                            if (haSwitchParams.xport > 0) {
                                xport = haInfo.getXPort();
                            }
                            TAtomDsConfDO slaveAtomDsConf = TAtomDsGmsConfigHelper
                                .buildAtomDsConfByGms(haInfo.getAddr(), xport, haSwitchParams.userName,
                                    haSwitchParams.passwdEnc, haSwitchParams.phyDbName,
                                    haSwitchParams.storageConnPoolConfig, dbName);
                            String slaveWeightStr = GroupInfoUtil.buildWeightStr(10, 0);
                            TAtomDataSource slaveAtomDs = new TAtomDataSource(true);
                            slaveAtomDs.init(appName, groupName, slaveKey, unitName, slaveAtomDsConf);
                            DataSourceWrapper slave = new DataSourceWrapper(slaveKey, slaveWeightStr, slaveAtomDs, 0);
                            dswList.add(slave);
                        }
                    }
                }
            }

        } catch (Throwable ex) {
            throw GeneralUtil.nestedException(ex);
        }
        Pair<List<DataSourceWrapper>, HaSwitchParams> result = new Pair<>(dswList, haSwitchParams);
        return result;
    }

    /**
     * ???????????????DataSource????????????DBSelector
     */
    public void init(List<DataSourceWrapper> dataSourceWrappers) {
        if ((dataSourceWrappers == null) || dataSourceWrappers.size() < 1) {
            throw new TddlRuntimeException(ErrorCode.ERR_CONFIG, "dataSourceWrappers is empty");
        }
        createTAtomDataSource = false;
        resetByDataSourceWrapper(dataSourceWrappers);
        isInited = true;
    }

    ;

    private TAtomDataSource initAtomDataSource(String appName, String groupKey, String dsKey, String unitName,
                                               Weight weight) {
        try {
            TAtomDataSource atomDataSource = new TAtomDataSource(weight.w > 0);
            atomDataSource.init(appName, groupKey, dsKey, unitName);
            atomDataSource.setLogWriter(groupDataSource.getLogWriter());
            atomDataSource.setLoginTimeout(groupDataSource.getLoginTimeout());
            return atomDataSource;
        } catch (TddlException e) {
            throw GeneralUtil.nestedException(e);
        } catch (SQLException e) {
            throw GeneralUtil.nestedException("TAtomDataSource init failed: dsKey=" + dsKey, e);
        }
    }

    ;

    public String getStroageInstId() {
        return stroageInstId;
    }

    // configInfo??????: db1:rw, db2:r, db3:r
    private synchronized void parse(String dsWeightCommaStr) {

        // ????????????????????????????????????: db1:rw, db2:r, db3:?????????????????????????????????
        List<DataSourceWrapper> dswList = parse2DataSourceWrapperList(dsWeightCommaStr);

        // ??????????????????????????????
        resetByDataSourceWrapper(dswList);
    }

    /**
     * ??????: ??????????????????????????????????????????????????????????????????????????????????????? ?????????????????? =
     * ???????????????+1??????0???1???2...???????????????"db1,,db3"???????????????3???????????????
     * ????????????????????????ThreadLocal?????????ThreadLocal??????????????????????????????
     */
    private List<DataSourceWrapper> parse2DataSourceWrapperList(String dsWeightCommaStr) {

        logger.info("[parse2DataSourceWrapperList]dsWeightCommaStr=" + dsWeightCommaStr);
        LoggerInit.TDDL_DYNAMIC_CONFIG.info("[parse2DataSourceWrapperList]dsWeightCommaStr=" + dsWeightCommaStr);
        this.groupDataSource.setDsKeyAndWeightCommaArray(dsWeightCommaStr);
        if ((dsWeightCommaStr == null) || (dsWeightCommaStr = dsWeightCommaStr.trim()).length() == 0) {
            throw new TddlRuntimeException(ErrorCode.ERR_MISS_GROUPKEY,
                groupDataSource.getDbGroupKey(),
                null,
                groupDataSource.getAppName(),
                groupDataSource.getUnitName());
        }
        return buildDataSourceWrapperSequential(dsWeightCommaStr);
    }

    /**
     * ???????????????AtomDataSource?????????????????????????????????????????????????????????????????????????????????DBSelector ---add by
     * mazhidan.pt
     */
    private synchronized void resetByDataSourceWrapper(List<DataSourceWrapper> dswList) {
        // ????????????????????????DataSourceWrapper
        Map<String, DataSourceWrapper> newDataSourceWrapperMap = new HashMap<String, DataSourceWrapper>(dswList.size());
        for (DataSourceWrapper dsw : dswList) {
            newDataSourceWrapperMap.put(dsw.getDataSourceKey(), dsw);
        }
        Map<String, DataSourceWrapper> old = this.dataSourceWrapperMap;
        this.dataSourceWrapperMap = newDataSourceWrapperMap;
        /**
         * ????????????atomDelayMap , ????????????????????????????????????????????????????????????????????????atom
         */
        if (dswList.size() == 1) {
            /**
             * ????????????????????????
             */
            this.groupDataSourceHolder = new MasterOnlyGroupDataSourceHolder(
                dswList.iterator().next().getWrappedDataSource());
        } else {

            TAtomDataSource masterDataSource = null;
            List<TAtomDataSource> slaveDataSources = new ArrayList<TAtomDataSource>();

            List<Pair<Object, Integer>> readWeightsWithMaster = new ArrayList<Pair<Object, Integer>>();
            List<Pair<Object, Integer>> readWeightsSlaveOnly = new ArrayList<Pair<Object, Integer>>();

            for (DataSourceWrapper dataSourceWrapper : dswList) {
                if (dataSourceWrapper.hasWriteWeight()) {
                    masterDataSource = dataSourceWrapper.getWrappedDataSource();
                    if (dataSourceWrapper.hasReadWeight()) {
                        readWeightsWithMaster
                            .add(Pair.of(dataSourceWrapper.getWrappedDataSource(), dataSourceWrapper.getWeight().r));
                    }
                } else {
                    slaveDataSources.add(dataSourceWrapper.getWrappedDataSource());
                    if (dataSourceWrapper.hasReadWeight()) {
                        readWeightsSlaveOnly
                            .add(Pair.of(dataSourceWrapper.getWrappedDataSource(), dataSourceWrapper.getWeight().r));
                        readWeightsWithMaster
                            .add(Pair.of(dataSourceWrapper.getWrappedDataSource(), dataSourceWrapper.getWeight().r));

                    }
                }

            }

            if (GeneralUtil.isEmpty(readWeightsSlaveOnly)) {
                /**
                 * ???????????????????????????
                 */
                this.groupDataSourceHolder = new MasterOnlyGroupDataSourceHolder(masterDataSource);
            } else {

                //FIXME PolarDb-X??????????????????????????????????????????????????????Hint?????????????????????????????????????????????CN????????????????????????DN
                this.groupDataSourceHolder = new MasterSlaveGroupDataSourceHolder(
                    masterDataSource,
                    slaveDataSources);
            }

        }

        // ????????????????????????DataSource??????
        for (String dbKey : old.keySet()) {
            if (!dataSourceWrapperMap.containsKey(dbKey)) {// ?????????????????????dbKey?????????????????????????????????????????????
                DataSourceWrapper dsw = old.get(dbKey);
                try {
                    DataSource ds = dsw.getWrappedDataSource();
                    if (ds instanceof TAtomDsStandard) {
                        TAtomDsStandard tads = (TAtomDsStandard) ds;
                        tads.destroyDataSource();
                        MatrixStatistics.removeAtom(groupDataSource.getAppName(),
                            groupDataSource.getDbGroupKey(),
                            dbKey);// ????????????GROUP????????????TAOM
                    } else {
                        logger.error("target datasource is not a TAtom Data Source");
                    }
                } catch (Throwable e) {
                    logger.error("we got exception when close datasource : " + dsw.getDataSourceKey(), e);
                }
            }
        }
        old.clear();
    }

    /**
     * ???????????????????????????????????????
     */
    protected DataSourceWrapper buildDataSourceWrapper(String dsKey, String weightStr, int index) {

        DataSourceWrapper dsw = null;

        Weight weight = new Weight(weightStr);

        // fetcher ????????????????????????????????????dsw????????????
        // ????????????dsw???dbType???????????????????????? by chengbi
        DataSourceFetcher fetcher = null;
        try {

            fetcher = new MyDataSourceFetcher(weight);

            // ????????????group??????????????????dataSource??????????????????group??????
            // ??????dataSource????????? ??????????????????dataSource???????????????
            TAtomDataSource dataSource = fetcher.getDataSource(dsKey);

            dsw = new DataSourceWrapper(dsKey, weightStr, dataSource, index);
            return dsw;

        } catch (Throwable e) {

            String msg = String.format(
                "[buildDataSourceWrapper] Failed to initialize atom datasource and changed to use lazyInit mode for atom datasource, dsKey is [%s], weightStr is [%s] ",
                dsKey,
                weightStr);

            Throwable ex = new TddlNestableRuntimeException(msg, e);
            logger.warn(ex);
            LoggerInit.TDDL_DYNAMIC_CONFIG.warn(ex);

            fetcher = new MyDataSourceLazyInitFetcher(weight);

            /**
             * ??????????????????????????????????????????????????????????????????????????????
             *
             * <pre>
             *  ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????, ????????????????????????????????????
             *  ?????????????????????????????????LazyInit(???????????????????????????????????????????????????)???????????????????????????
             *  ??????????????????????????????????????????????????????????????????????????????????????????
             * </pre>
             */

            // ????????????????????????????????????????????????
            // ??????LazyInit???dsw, ????????????????????????????????????????????????
            // ?????????????????????, ???????????? DBA ?????????????????????
            // ???????????????????????????????????????????????????????????????prefill=true??????????????????,
            // ???????????????LazyInitDataSourceWrapper??????
            dsw = new DataSourceLazyInitWrapper(dsKey, weightStr, fetcher, index);
        }

        return dsw;

    }

    public List<DataSourceWrapper> buildDataSourceWrapperSequential(String dsWeightCommaStr) {

        final String[] dsWeightArray = dsWeightCommaStr.split(","); // ???????????????db0:rwp1q1i0,

        // db1:rwp0q0i1
        List<DataSourceWrapper> dss = new ArrayList<DataSourceWrapper>(dsWeightArray.length);

        for (int i = 0; i < dsWeightArray.length; i++) {
            final int j = i;
            final String[] dsAndWeight = dsWeightArray[j].split(":"); // ???????????????db0:rwp1q1i0
            final String dsKey = dsAndWeight[0].trim();
            String weightStr = dsAndWeight.length == 2 ? dsAndWeight[1] : null;
            try {
                DataSourceWrapper newDsw = buildDataSourceWrapper(dsKey, weightStr, j);
                dss.add(newDsw);
            } catch (Throwable e) {
                throw GeneralUtil.nestedException(e);
            }

        }

        return dss;
    }

    // ???????????????
    public void receiveConfigInfo(String configInfo) {
        configReceiver.onDataReceived(null, configInfo);
    }

    // ???????????????
    public void resetDbGroup(String configInfo) {
        try {
            parse(configInfo);
        } catch (Throwable t) {
            logger.error("resetDbGroup failed:" + configInfo, t);
        }

    }

    @Override
    protected void doDestroy() {
        // ????????????DataSource
        if (dataSourceWrapperMap != null) {
            for (DataSourceWrapper dsw : dataSourceWrapperMap.values()) {
                try {
                    DataSource ds = dsw.getWrappedDataSource();
                    if (ds instanceof TAtomDsStandard) {
                        TAtomDsStandard tads = (TAtomDsStandard) ds;
                        tads.destroyDataSource();
                    } else {
                        logger.error("target datasource is not a TAtom Data Source");
                        LoggerInit.TDDL_DYNAMIC_CONFIG.error("target datasource is not a TAtom Data Source");

                    }
                } catch (Exception e) {
                    logger.error("we got exception when close datasource : " + dsw.getDataSourceKey(), e);
                    LoggerInit.TDDL_DYNAMIC_CONFIG
                        .error("we got exception when close datasource : " + dsw.getDataSourceKey(), e);

                }
            }
        }

        try {
            unregisterHaSwitcher();
            unbindGroupConfigListener();
        } catch (Exception e) {
            logger.error("we got exception when close datasource .", e);
        }
    }

    public void destroyDataSource() {
        destroy();
    }

    public Map<String/* Atom dbIndex */, DataSourceWrapper/* Wrapper??????Atom DS */> getDataSourceWrapperMap() {
        return this.dataSourceWrapperMap;
    }

    public TAtomDataSource getDataSource(MasterSlave masterSlave) {
        return this.groupDataSourceHolder.getDataSource(masterSlave);
    }

    public GroupDataSourceHolder getGroupDataSourceHolder() {
        return groupDataSourceHolder;
    }

    protected String getServerInstIdForGroupDataSource() {
        return null;
    }

    protected class GroupDataSourceSwitcher implements HaSwitcher {

        TGroupDataSource groupDs = null;

        public GroupDataSourceSwitcher(TGroupDataSource groupDs) {
            this.groupDs = groupDs;
        }

        @Override
        public void doHaSwitch(HaSwitchParams haSwitchParams) {

            String groupName = groupDs.getDbGroupKey();
            String dbName = groupDs.getSchemaName();
            try {
                switchGroupDs(haSwitchParams);
            } catch (Throwable ex) {
                MetaDbLogUtil.META_DB_DYNAMIC_CONFIG
                    .error(String.format("Failed to do switch ds for [%s/%s]", groupName, dbName),
                        ex);
                throw GeneralUtil.nestedException(ex);
            }
        }
    }

    protected class MyDataSourceFetcher implements DataSourceFetcher {

        private Weight weight;

        public MyDataSourceFetcher(Weight weight) {
            this.weight = weight;
        }

        @Override
        public TAtomDataSource getDataSource(String dsKey) {
            DataSourceWrapper dsw = dataSourceWrapperMap.get(dsKey);

            if (dsw != null) {
                // ???dsw????????????null??????????????????????????????
                return dsw.getWrappedDataSource();

            } else {

                // ???dsw?????????null?????????????????????????????????
                if (createTAtomDataSource) {
                    TAtomDataSource atomDs = initAtomDataSource(groupDataSource.getAppName(),
                        groupDataSource.getDbGroupKey(),
                        dsKey,
                        groupDataSource.getUnitName(),
                        weight);
                    return atomDs;
                } else {
                    throw new IllegalArgumentException(dsKey + " not exist!");
                }
            }
        }
    }

    /**
     * <pre>
     * ?????????atom?????????lazy init ???dataSoureFetcher
     *
     * ????????????????????????????????????????????????????????????????????????????????????????????????lazyInit???????????????
     * </pre>
     *
     * @author chenghui.lch 2017???1???21??? ??????5:12:55
     * @since 5.0.0
     */
    protected class MyDataSourceLazyInitFetcher implements DataSourceFetcher {

        private Weight weight;

        public MyDataSourceLazyInitFetcher(Weight weight) {
            this.weight = weight;
        }

        @Override
        public TAtomDataSource getDataSource(String dsKey) {

            TAtomDataSource atomDs = null;
            try {
                // ???dsw?????????null?????????????????????????????????
                atomDs = initAtomDataSource(groupDataSource.getAppName(),
                    groupDataSource.getDbGroupKey(),
                    dsKey,
                    groupDataSource.getUnitName(),
                    weight);

                return atomDs;
            } catch (Throwable e) {
                String msg = "Failed to initialize atom datasource in lazyInit mode ! dbKey is " + dsKey;
                throw GeneralUtil.nestedException(msg, e);
            }
        }

    }

    private class ConfigReceiver implements ConfigDataListener {

        private OptimizedGroupConfigManager configManager;

        public void setConfigManager(OptimizedGroupConfigManager configManager) {
            this.configManager = configManager;
        }

        @Override
        public void onDataReceived(String dataId, String data) {
            try {
                String oldData = this.configManager.groupDataSource.getDsKeyAndWeightCommaArray();
                LoggerInit.TDDL_DYNAMIC_CONFIG.info("[Data Recieved] [group datasource] dataId:" + dataId
                    + ", new data:" + data + ", old data:" + oldData);
                parse(data);
            } catch (Throwable t) {
                logger.error("error occurred during parsing group dynamic configs : " + data, t);
                LoggerInit.TDDL_DYNAMIC_CONFIG.error("error occurred during parsing group dynamic configs : " + data,
                    t);

            }
        }
    }
}
