/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014 The Billing Project, LLC
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.recurly;

import java.util.Dictionary;
import java.util.Hashtable;

import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.plugin.recurly.api.RecurlyPaymentPluginApi;
import org.killbill.killbill.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.impl.StaticLoggerBinder;

import com.ning.billing.recurly.RecurlyClient;

public class RecurlyActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "recurly";

    private RecurlyClient recurlyClient;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        // Configure slf4j for libraries
        StaticLoggerBinder.getSingleton().setLogService(logService);

        recurlyClient = new RecurlyClient(System.getProperty("killbill.payment.recurly.apiKey"));
        final RecurlyPaymentPluginApi recurlyPaymentPluginApi = new RecurlyPaymentPluginApi(recurlyClient);

        registerPaymentPluginApi(context, recurlyPaymentPluginApi);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (recurlyClient != null) {
            recurlyClient.close();
        }
        super.stop(context);
    }

    @Override
    public OSGIKillbillEventHandler getOSGIKillbillEventHandler() {
        return null;
    }

    private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
        final Dictionary props = new Hashtable();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, PaymentPluginApi.class, api, props);
    }
}
