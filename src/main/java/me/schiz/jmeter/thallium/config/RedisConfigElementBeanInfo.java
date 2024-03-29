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

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

public class RedisConfigElementBeanInfo
        extends BeanInfoSupport {
    protected RedisConfigElementBeanInfo() {
        super(RedisConfigElement.class);

        createPropertyGroup("options", new String[]{
                "instanceName",
                "host",
                "port",
        });
//                "database",
//                "password",});

        PropertyDescriptor p = property("instanceName");
        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
        p.setValue(DEFAULT, "default");

        p = property("host");
        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
        p.setValue(DEFAULT, RedisConfig.DEFAULT_HOST);

        p = property("port");
        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
        p.setValue(DEFAULT, RedisConfig.DEFAULT_PORT);

//        p = property("database");
//        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
//        p.setValue(DEFAULT, RedisConfig.DEFAULT_DB);

//        p = property("password");
//        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
//        p.setValue(DEFAULT, RedisConfig.DEFAULT_PASSWORD);
    }
}