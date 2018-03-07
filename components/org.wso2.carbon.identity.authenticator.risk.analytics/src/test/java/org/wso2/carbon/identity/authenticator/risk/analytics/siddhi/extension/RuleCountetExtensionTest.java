/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.authenticator.risk.analytics.siddhi.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;


/**
 * TODO: Class level comments
 */
public class RuleCountetExtensionTest {
    private static final Log log = LogFactory.getLog(RuleCounterExtension.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeClass
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void countRulesTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select symbol, isAnalytics:countRules(\"RiskScoreCalculator\") as count " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue(inEventCount > removeEventCount, "InEvents arrived before RemoveEvents");
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM,Damith,Mohan", 700f, 0});
        inputHandler.send(new Object[]{"WSO2,Test", 60.5f, 1});
        Thread.sleep(4000);
        Assert.assertEquals(5, inEventCount);
        Assert.assertEquals(0, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

//    @Test
//    public void eventDuplicatorSpecificDelimiterTest2() throws InterruptedException {
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        String cseEventStream = "" +
//                "define stream cseEventStream (symbol string, price float, volume int);";
//        String query = "" +
//                "@info(name = 'query1') " +
//                "from cseEventStream#isAnalytics:duplicator(symbol,\";\") " +
//                "select symbol,price,volume, role " +
//                "insert all events into outputStream ;";
//
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
//
//        executionPlanRuntime.addCallback("query1", new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                if (inEvents != null) {
//                    inEventCount = inEventCount + inEvents.length;
//                }
//                if (removeEvents != null) {
//                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
//                    removeEventCount = removeEventCount + removeEvents.length;
//                }
//                eventArrived = true;
//            }
//
//        });
//
//        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//        executionPlanRuntime.start();
//        inputHandler.send(new Object[]{"IBM;Damith;Mohan", 700f, 0});
//        inputHandler.send(new Object[]{"WSO2;Test", 60.5f, 1});
//        Thread.sleep(4000);
//        Assert.assertEquals(5, inEventCount);
//        Assert.assertEquals(0, removeEventCount);
//        Assert.assertTrue(eventArrived);
//        executionPlanRuntime.shutdown();
//
//    }
}

