/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.schiz.jmeter.thallium.config;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import redis.client.RedisClient;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisConfigElement extends ConfigTestElement
        implements TestStateListener, TestBean {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected static ConcurrentHashMap<String, RedisConfig> instances;
    protected static ConcurrentHashMap<String, ConcurrentHashMap<Long, RedisClient>> clients;

    protected String instanceName;
    protected String host;
    protected int port;
    protected int database;
    protected String password;
    protected ExecutorService executorService;

    public String getInstanceName() {
        return instanceName;
    }
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getDatabase() {
        return database;
    }
    public void setDatabase(int database) {
        this.database = database;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public RedisConfigElement() {
    }

    public static RedisClient getClient(String instanceName) {
        if(instances == null || clients == null) {
            log.error("Not found any redis instance");
            return null;
        }

        if(clients.get(instanceName) == null) {
            log.error("redis instance `" + instanceName + "` not found");
        }

        RedisClient client = null;
        long thread_id = Thread.currentThread().getId();
        if((client = clients.get(instanceName).get(thread_id)) == null) {
            //Create new client
            RedisConfig config = instances.get(instanceName);
            if(config == null) {
                log.error("not found instance `" + instanceName + "`");
                return null;
            }
            try {
                client = config.createClient();
                clients.get(instanceName).put(thread_id, client);
            } catch (IOException e) {
                log.error("Cannot create new redis client for thread_id #" + thread_id
                        + ", instance  `" + instanceName + "` ,", e);
            }
        }

        return client;
    }

    @Override
    public void testStarted() {
        if(instances == null || clients == null) {
            synchronized (RedisConfigElement.class) {
                if(instances == null) {
                    instances = new ConcurrentHashMap <String, RedisConfig >();
                    clients = new ConcurrentHashMap<String, ConcurrentHashMap<Long, RedisClient>>();
                }
            }
        }
        instances.put(getInstanceName(),
                new RedisConfig(getHost(),
                        getPort(),
                        getDatabase(),
                        getPassword(),
                        Executors.newSingleThreadExecutor()));
        clients.put(getInstanceName(), new ConcurrentHashMap<Long, RedisClient>());
        log.info("redis instance `" + getInstanceName() + "` started");

    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        ConcurrentHashMap<Long, RedisClient> instance_clients =
                clients.get(getInstanceName());

        for(Long thread_id : instance_clients.keySet()) {
            try {
                instance_clients.get(thread_id).close();
            } catch (IOException e) {
                log.warn("Cannot close redis connection for thread_id #" + thread_id + ", instance `" + getInstanceName() + "`", e);
            }
        }

        instances.get(getInstanceName()).executorService.shutdown();
        log.info("redis instance `" + getInstanceName() + "` stopped");
   }

    @Override
    public void testEnded(String s) {
        testEnded();
    }
}