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

import redis.client.RedisClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class RedisConfig {
    protected String host;
    protected int port;
    protected int database;
    protected String password;
    public ExecutorService executorService;

    public static String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 6379;
    public static int DEFAULT_DB = 0;
    public static String DEFAULT_PASSWORD = null;

    public RedisConfig(String host, int port, int database, String password, ExecutorService executorService) {
        this.host = (host == null ? DEFAULT_HOST : (host.isEmpty() ? DEFAULT_HOST : host));
        this.port = (port == 0 ? DEFAULT_PORT : port);
        this.database = database;
        this.password = password == null ? DEFAULT_PASSWORD : (password.isEmpty() ? DEFAULT_PASSWORD : password);
        this.executorService = executorService;
    }

    public RedisClient createClient() throws IOException {
        RedisClient client = new RedisClient(this.host,
            this.port,
            // Only in 0.8 version:)
            //this.database,
            //this.password,
            executorService);

        return client;
    }
}
