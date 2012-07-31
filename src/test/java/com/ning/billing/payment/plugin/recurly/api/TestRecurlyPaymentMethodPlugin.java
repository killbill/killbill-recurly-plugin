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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.payment.plugin.recurly.client.RecurlyObjectFactory;
import com.ning.billing.recurly.model.Account;
import com.ning.billing.recurly.model.BillingInfo;

import static com.ning.billing.payment.plugin.recurly.TestUtils.randomString;

public class TestRecurlyPaymentMethodPlugin {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BillingInfo billingInfo = new BillingInfo();
        final Account account = new Account();
        account.setAccountCode(randomString());
        billingInfo.setAccount(account);
        billingInfo.setAddress1(randomString());
        billingInfo.setAddress2(randomString());
        billingInfo.setCardType(randomString());
        billingInfo.setCity(randomString());
        billingInfo.setCompany(randomString());
        billingInfo.setCountry(randomString());
        billingInfo.setFirstName(randomString());
        billingInfo.setFirstSix(randomString());
        billingInfo.setIpAddress(randomString());
        billingInfo.setIpAddressCountry(randomString());
        billingInfo.setLastFour(randomString());
        billingInfo.setLastName(randomString());
        billingInfo.setMonth(3);
        billingInfo.setNumber(randomString());
        billingInfo.setPhone(randomString());
        billingInfo.setState(randomString());
        billingInfo.setVatNumber(randomString());
        billingInfo.setVerificationValue(Integer.MAX_VALUE);
        billingInfo.setYear(Integer.MIN_VALUE);
        billingInfo.setZip(randomString());

        final RecurlyPaymentMethodPlugin paymentMethodPlugin = new RecurlyPaymentMethodPlugin(billingInfo);
        Assert.assertTrue(paymentMethodPlugin.isDefaultPaymentMethod());

        final BillingInfo billingInfoFromPaymentMethodPlugin = RecurlyObjectFactory.createBillingInfoFromKillbill(account.getAccountCode(), paymentMethodPlugin);
        Assert.assertEquals(billingInfoFromPaymentMethodPlugin, billingInfo);
    }
}
