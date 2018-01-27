/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.collector.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import org.apache.skywalking.apm.collector.core.module.ModuleManager;
import org.apache.skywalking.apm.collector.core.util.Const;
import org.apache.skywalking.apm.collector.core.util.StringUtils;
import org.apache.skywalking.apm.collector.storage.StorageModule;
import org.apache.skywalking.apm.collector.storage.dao.IInstanceUIDAO;
import org.apache.skywalking.apm.collector.storage.ui.server.AppServerInfo;

/**
 * @author peng-yongsheng
 */
public class ServerService {

    private final Gson gson = new Gson();
    private final IInstanceUIDAO instanceDAO;

    public ServerService(ModuleManager moduleManager) {
        this.instanceDAO = moduleManager.find(StorageModule.NAME).getService(IInstanceUIDAO.class);
    }

    public List<AppServerInfo> searchServer(String keyword, long start, long end) {
        List<AppServerInfo> serverInfos = instanceDAO.searchServer(keyword, start, end);
        serverInfos.forEach(serverInfo -> {
            if (serverInfo.getId() == Const.NONE_INSTANCE_ID) {
                serverInfos.remove(serverInfo);
            }
        });
        
        buildAppServerInfo(serverInfos);
        return serverInfos;
    }

    public List<AppServerInfo> getAllServer(int applicationId, long start, long end) {
        List<AppServerInfo> serverInfos = instanceDAO.getAllServer(applicationId, start, end);
        buildAppServerInfo(serverInfos);
        return serverInfos;
    }

    private void buildAppServerInfo(List<AppServerInfo> serverInfos) {
        serverInfos.forEach(serverInfo -> {
            if (StringUtils.isNotEmpty(serverInfo.getOsInfo())) {
                JsonObject osInfoJson = gson.fromJson(serverInfo.getOsInfo(), JsonObject.class);
                if (osInfoJson.has("osName")) {
                    serverInfo.setName(osInfoJson.get("osName").getAsString());
                }
                if (osInfoJson.has("hostName")) {
                    serverInfo.setHost(osInfoJson.get("hostName").getAsString());
                }
                if (osInfoJson.has("processId")) {
                    serverInfo.setPid(osInfoJson.get("processId").getAsInt());
                }

                if (osInfoJson.has("ipv4s")) {
                    JsonArray ipv4Array = osInfoJson.get("ipv4s").getAsJsonArray();

                    List<String> ipv4s = new LinkedList<>();
                    ipv4Array.forEach(ipv4 -> {
                        ipv4s.add(ipv4.getAsString());
                    });
                    serverInfo.setIpv4(ipv4s);
                }
            }
        });
    }
}
