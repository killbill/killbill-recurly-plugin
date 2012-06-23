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

import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RecurlyPaymentPluginApi implements PaymentPluginApi {
    @Override
    public String getName() {
        return null;
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
        return null;
    }

    @Override
    public List<PaymentMethodPlugin> getPaymentMethodDetails(final String s) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final String s, final String s1) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public String addPaymentMethod(final String s, final PaymentMethodPlugin paymentMethodPlugin, final boolean b) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public void updatePaymentMethod(final String s, final PaymentMethodPlugin paymentMethodPlugin) throws PaymentPluginApiException {
    }

    @Override
    public void deletePaymentMethod(final String s, final String s1) throws PaymentPluginApiException {
    }

    @Override
    public void setDefaultPaymentMethod(final String s, final String s1) throws PaymentPluginApiException {
    }
}
