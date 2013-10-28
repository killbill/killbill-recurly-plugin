/*
 * Copyright 2010-2013 Ning, Inc.
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.payment.plugin.api.PaymentPluginStatus;
import com.ning.billing.payment.plugin.api.RefundInfoPlugin;
import com.ning.billing.payment.plugin.api.RefundPluginStatus;
import com.ning.billing.recurly.RecurlyClient;
import com.ning.billing.recurly.TransactionErrorException;
import com.ning.billing.recurly.model.Account;
import com.ning.billing.recurly.model.BillingInfo;
import com.ning.billing.recurly.model.Errors;

public class TestIntegration {

    public static final String KILLBILL_PAYMENT_RECURLY_API_KEY = "killbill.payment.recurly.apiKey";
    public static final String KILLBILL_PAYMENT_RECURLY_DEFAULT_CURRENCY_KEY = "killbill.payment.recurly.currency";

    private static final Logger log = LoggerFactory.getLogger(TestIntegration.class);

    // Default to USD for all tests, which is expected to be supported by Recurly by default
    // Multi Currency Support is an enterprise add-on
    private static final String CURRENCY = System.getProperty(KILLBILL_PAYMENT_RECURLY_DEFAULT_CURRENCY_KEY, "USD");

    private final UUID kbAccountId = UUID.randomUUID();

    private RecurlyClient recurlyClient;
    private Account account;

    @BeforeMethod(groups = "integration")
    public void setUp() throws Exception {
        final String apiKey = System.getProperty(KILLBILL_PAYMENT_RECURLY_API_KEY);
        if (apiKey == null) {
            Assert.fail("You need to set your Recurly api key to run integration tests:" +
                        " -Dkillbill.payment.recurly.apiKey=...");
        }

        recurlyClient = new RecurlyClient(apiKey);
        recurlyClient.open();

        account = new Account();
        account.setAccountCode(kbAccountId.toString());
        final Account createdAccount = recurlyClient.createAccount(account);
        Assert.assertEquals(createdAccount.getAccountCode(), account.getAccountCode());
    }

    @AfterMethod(groups = "integration")
    public void tearDown() throws Exception {
        recurlyClient.closeAccount(account.getAccountCode());
        recurlyClient.close();
    }

    @Test(groups = "integration")
    public void testScenario() throws Exception {
        final RecurlyPaymentPluginApi pluginApi = new RecurlyPaymentPluginApi(recurlyClient);

        // Create a good payment method
        final UUID goodKbPaymentMethodId = UUID.randomUUID();
        final BillingInfo goodBillingInfo = new BillingInfo();
        goodBillingInfo.setNumber("4111-1111-1111-1111");
        goodBillingInfo.setMonth(12);
        goodBillingInfo.setYear(2020);
        goodBillingInfo.setFirstName("John");
        goodBillingInfo.setLastName("Doe");
        goodBillingInfo.setAddress1("2, Finite Loop");
        goodBillingInfo.setCity("Cupertino");
        goodBillingInfo.setState("CA");
        goodBillingInfo.setZip("95014");
        goodBillingInfo.setCountry("USA");
        final RecurlyPaymentMethodPlugin goodPaymentMethodProps = new RecurlyPaymentMethodPlugin(goodBillingInfo, goodKbPaymentMethodId);
        pluginApi.addPaymentMethod(kbAccountId, goodKbPaymentMethodId, goodPaymentMethodProps, true, null);

        final PaymentMethodPlugin goodPaymentMethodPlugin = pluginApi.getPaymentMethodDetail(kbAccountId, goodKbPaymentMethodId, null);
        Assert.assertEquals(goodPaymentMethodPlugin.getKbPaymentMethodId(), goodKbPaymentMethodId);
        Assert.assertTrue(goodPaymentMethodPlugin.isDefaultPaymentMethod());
        Assert.assertEquals(goodPaymentMethodPlugin.getCCName(), "John Doe");
        Assert.assertEquals(goodPaymentMethodPlugin.getCCExpirationMonth(), "12");
        Assert.assertEquals(goodPaymentMethodPlugin.getCCExpirationYear(), "2020");
        Assert.assertEquals(goodPaymentMethodPlugin.getCCLast4(), "1111");
        Assert.assertEquals(goodPaymentMethodPlugin.getAddress1(), "2, Finite Loop");
        Assert.assertEquals(goodPaymentMethodPlugin.getCity(), "Cupertino");
        Assert.assertEquals(goodPaymentMethodPlugin.getState(), "CA");
        Assert.assertEquals(goodPaymentMethodPlugin.getZip(), "95014");
        Assert.assertEquals(goodPaymentMethodPlugin.getCountry(), "USA");

        final List<PaymentMethodInfoPlugin> allPaymentMethodPlugins = pluginApi.getPaymentMethods(kbAccountId, true, null);
        Assert.assertEquals(allPaymentMethodPlugins.size(), 1);
        Assert.assertEquals(allPaymentMethodPlugins.get(0).getAccountId(), kbAccountId);
        Assert.assertEquals(allPaymentMethodPlugins.get(0).getPaymentMethodId(), goodKbPaymentMethodId);

        // Create a bad payment method
        final UUID badKbPaymentMethodId = UUID.randomUUID();
        final BillingInfo badBillingInfo = goodBillingInfo;
        // See http://docs.recurly.com/payment-gateways/test
        badBillingInfo.setNumber("4000-0000-0000-0093");
        final RecurlyPaymentMethodPlugin badPaymentMethodProps = new RecurlyPaymentMethodPlugin(badBillingInfo, badKbPaymentMethodId);
        try {
            pluginApi.addPaymentMethod(kbAccountId, badKbPaymentMethodId, badPaymentMethodProps, true, null);
            Assert.fail("Shouldn't have been able to add a fraudulent payment method");
        } catch (final PaymentPluginApiException e) {
            final Errors errors = ((TransactionErrorException) e.getCause()).getErrors();
            Assert.assertEquals(errors.getTransactionError().getErrorCode(), "fraud_ip_address");
            Assert.assertEquals(errors.getTransactionError().getErrorCategory(), "fraud");
            Assert.assertEquals(errors.getTransactionError().getMerchantMessage(), "The payment gateway declined the transaction because it originated from an IP address known for fraudulent transactions.");
            Assert.assertEquals(errors.getTransactionError().getCustomerMessage(), "The transaction was declined. Please contact support.");
            Assert.assertEquals(errors.getRecurlyErrors().size(), 1);
            Assert.assertEquals(errors.getRecurlyErrors().get(0).getField(), "billing_info.base");
            Assert.assertEquals(errors.getRecurlyErrors().get(0).getSymbol(), "fraud_ip_address");
            Assert.assertEquals(errors.getRecurlyErrors().get(0).getMessage(), "The transaction was declined. Please contact support.");
            Assert.assertEquals(errors.getTransaction().getStatus(), "declined");

        }

        // Attempt a payment
        final UUID kbPaymentId = UUID.randomUUID();
        final PaymentInfoPlugin paymentInfoPlugin = pluginApi.processPayment(kbAccountId, kbPaymentId, goodKbPaymentMethodId, BigDecimal.TEN, Currency.USD, null);
        Assert.assertEquals(paymentInfoPlugin.getStatus(), PaymentPluginStatus.PROCESSED);
        Assert.assertEquals(paymentInfoPlugin.getAmount().compareTo(BigDecimal.TEN), 0);

        // Retrieve the payment
        final PaymentInfoPlugin retrievedPaymentInfoPlugin = pluginApi.getPaymentInfo(kbAccountId, kbPaymentId, null);
        Assert.assertEquals(retrievedPaymentInfoPlugin, paymentInfoPlugin);

        // Refund the payment
        final RefundInfoPlugin refundInfoPlugin = pluginApi.processRefund(kbAccountId, kbPaymentId, BigDecimal.TEN, Currency.USD, null);
        Assert.assertEquals(refundInfoPlugin.getStatus(), RefundPluginStatus.PROCESSED);
        Assert.assertEquals(refundInfoPlugin.getAmount().compareTo(BigDecimal.TEN), 0);

        // Retrieve the refund
        final List<RefundInfoPlugin> retrievedRefundInfoPlugins = pluginApi.getRefundInfo(kbAccountId, kbPaymentId, null);
        Assert.assertEquals(retrievedRefundInfoPlugins.size(), 1);
        Assert.assertEquals(retrievedRefundInfoPlugins.get(0), refundInfoPlugin);

        // Delete the payment method
        pluginApi.deletePaymentMethod(kbAccountId, goodKbPaymentMethodId, null);
        Assert.assertNull(pluginApi.getPaymentMethodDetail(kbAccountId, goodKbPaymentMethodId, null));
    }
}
