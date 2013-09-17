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

package me.schiz.jmeter.thallium.functions;

import com.google.common.base.Charsets;
import me.schiz.jmeter.thallium.config.RedisConfigElement;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import redis.Command;
import redis.client.RedisClient;
import redis.reply.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RedisFunction  extends AbstractFunction {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final List<String> desc = new LinkedList<String>();

    private static final String KEY = "__redis"; //$NON-NLS-1$

    static {
//        desc.add(JMeterUtils.getResString("redis_instance"));// $NON-NLS1$         \
        desc.add("instance name");
//        desc.add(JMeterUtils.getResString("redis_command"));// $NON-NLS1$
        desc.add("command");
//        desc.add(JMeterUtils.getResString("redis_command_arguments"));// $NON-NLS1$
        desc.add("arguments");
    }
    private Object[] values;

    public RedisFunction() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String result = null;
        String instance;
        String command;
        Object[] args = null;
        synchronized (values) {
            instance = ((CompoundVariable)values[0]).execute();
            command = ((CompoundVariable)values[1]).execute();

            if(values.length > 2) {
                args = new Object[values.length - 2];
                //fucking CompundVariable:(
//                System.arraycopy(values, 2, args, 0, values.length - 2);
                for(int i =0;i<values.length - 2; ++i) {
                    if(values[i+2] instanceof CompoundVariable) {
                        args[i] = ((CompoundVariable)values[i+2]).execute();
                    }
                }
            }
        }

        RedisClient client = RedisConfigElement.getClient(instance);
        if(client == null) {
            throw new InvalidVariableException("not found redis instance");
        }

        Reply reply = client.execute(command, new Command(command.getBytes(Charsets.US_ASCII), args));
        if(reply instanceof IntegerReply) result = String.valueOf(((IntegerReply)reply).data());
        if(reply instanceof ErrorReply) {
            result = ((ErrorReply)reply).data();
            log.error("error reply from redis: " + result);
        }
        if(reply instanceof BulkReply)  result = ((BulkReply)reply).asAsciiString();
        if(reply instanceof MultiBulkReply) {
            Reply[] replies = ((MultiBulkReply)reply).data();
            StringBuffer sb = new StringBuffer();
            for(int i=0;i>replies.length;++i) {
                sb.append(replies[i].data());
                sb.append(";");
            }
            result = sb.toString();
        }
        if(reply instanceof StatusReply)    result = ((StatusReply)reply).data();
        return result;

    }

    /*
     * Helper method for use by scripts
     *
     */
    public void log_info(String s) {
        log.info(s);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        checkParameterCount(parameters, 2, 130);

        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

}
