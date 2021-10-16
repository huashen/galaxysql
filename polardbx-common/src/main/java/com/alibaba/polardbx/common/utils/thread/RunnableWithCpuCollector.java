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

package com.alibaba.polardbx.common.utils.thread;

public class RunnableWithCpuCollector implements Runnable {

    protected Runnable task;
    protected CpuCollector cpuCollector;

    public RunnableWithCpuCollector(Runnable task, CpuCollector cpuCollector) {

        this.cpuCollector = cpuCollector;
        if (cpuCollector != null) {
            RunnableWithStatHandler taskWithStat = new RunnableWithStatHandler(task, new CpuStatHandler() {

                @Override
                public long getStartTimeNano() {
                    return ThreadCpuStatUtil.getThreadCpuTimeNano();
                }

                @Override
                public void handleCpuStat(long startTimeNano) {
                    cpuCollector.collectThreadCpu(ThreadCpuStatUtil.getThreadCpuTimeNano() - startTimeNano);
                }
            });
            this.task = taskWithStat;
        } else {
            this.task = task;
        }

    }

    @Override
    public void run() {
        this.task.run();
    }
}
