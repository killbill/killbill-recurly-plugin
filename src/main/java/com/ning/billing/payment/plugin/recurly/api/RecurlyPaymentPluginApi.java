/*
 * Copyright 2010-2012 Ning, Inc.
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

package com.ning.billing.payment.plugin.recurly.api;

import com.google.common.collect.ImmutableList;
import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.payment.plugin.recurly.client.RecurlyClient;
import com.ning.billing.payment.plugin.recurly.client.RecurlyObjectFactory;
import com.ning.billing.payment.plugin.recurly.model.BillingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RecurlyPaymentPluginApi implements PaymentPluginApi {
    private static final Logger log = LoggerFactory.getLogger(RecurlyPaymentPluginApi.class);

    private final String instanceName;
    private final RecurlyClient client;

    public RecurlyPaymentPluginApi(final String instanceName, final RecurlyClient client) {
        this.instanceName = instanceName;
        this.client = client;
    }

    @Override
    public String getName() {
        return instanceName;
    }

    @Override
    public PaymentInfoPlugin processPayment(final String s, final UUID uuid, final BigDecimal bigDecimal) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID uuid) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public List<PaymentInfoPlugin> processRefund(final Account account) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public String createPaymentProviderAccount(final Account account) throws PaymentPluginApiException {
        final com.ning.billing.payment.plugin.recurly.model.Account recurlyAccount = client.createAccount(RecurlyObjectFactory.createAccountFromKillbill(account));
        if (recurlyAccount != null) {
            return recurlyAccount.getAccountCode();
        } else {
            log.warn("Unable to create Recurly account for account key {}", account.getExternalKey());
            return null;
        }
    }

    @Override
    public List<PaymentMethodPlugin> getPaymentMethodDetails(final String accountKey) throws PaymentPluginApiException {
        final BillingInfo recurlyBillingInfo = client.getBillingInfo(accountKey);
        return ImmutableList.<PaymentMethodPlugin>of(new RecurlyPaymentMethodPlugin(recurlyBillingInfo));
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final String accountKey, final String externalPaymentId) throws PaymentPluginApiException {
        return getPaymentMethodDetails(accountKey).get(0);
    }

    @Override
    public String addPaymentMethod(final String accountKey, final PaymentMethodPlugin paymentMethodPlugin, final boolean isDefault) throws PaymentPluginApiException {
        final BillingInfo recurlyBillingInfo = client.createOrUpdateBillingInfo(RecurlyObjectFactory.createBillingInfoFromKillbill(accountKey, paymentMethodPlugin));
        if (recurlyBillingInfo != null) {
            return RecurlyObjectFactory.getExternalPaymentIdFromBillingInfo(recurlyBillingInfo);
        } else {
            log.warn("Unable to add a Recurly payment method for account key {}", accountKey);
            return null;
        }
    }

    @Override
    public void updatePaymentMethod(final String accountKey, final PaymentMethodPlugin paymentMethodPlugin) throws PaymentPluginApiException {
        addPaymentMethod(accountKey, paymentMethodPlugin, true);
    }

    @Override
    public void deletePaymentMethod(final String accountKey, final String externalPaymentId) throws PaymentPluginApiException {
        client.clearBillingInfo(accountKey);
    }

    @Override
    public void setDefaultPaymentMethod(final String accountKey, final String externalPaymentId) throws PaymentPluginApiException {
        // No-op
    }
}
