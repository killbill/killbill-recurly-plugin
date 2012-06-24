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

package com.ning.billing.payment.plugin.recurly.client;

import com.ning.billing.payment.plugin.recurly.model.Account;
import com.ning.billing.payment.plugin.recurly.model.BillingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.ning.billing.payment.plugin.recurly.TestUtils.randomString;

public class TestRecurlyClient {
    private static final Logger log = LoggerFactory.getLogger(TestRecurlyClient.class);

    private RecurlyClient recurlyClient;

    @BeforeMethod(groups = "integration")
    public void setUp() throws Exception {
        final String apiKey = System.getProperty("killbill.payment.recurly.apiKey");
        if (apiKey == null) {
            Assert.fail("You need to set your Recurly api key to run integration tests: -Dkillbill.payment.recurly.apiKey=...");
        }

        recurlyClient = new RecurlyClient(apiKey);
        recurlyClient.open();
    }

    @AfterMethod(groups = "integration")
    public void tearDown() throws Exception {
        recurlyClient.close();
    }

    @Test(groups = "integration")
    public void testCreateAccount() throws Exception {
        final Account accountData = new Account();
        accountData.setAccountCode(randomString());
        accountData.setEmail(randomString() + "@laposte.net");
        accountData.setFirstName(randomString());
        accountData.setLastName(randomString());
        accountData.setUsername(randomString());
        accountData.setAcceptLanguage("en_US");
        accountData.setCompanyName(randomString());

        final Account account = recurlyClient.createAccount(accountData);
        Assert.assertNotNull(account);
        Assert.assertEquals(accountData.getAccountCode(), account.getAccountCode());
        Assert.assertEquals(accountData.getEmail(), account.getEmail());
        Assert.assertEquals(accountData.getFirstName(), account.getFirstName());
        Assert.assertEquals(accountData.getLastName(), account.getLastName());
        Assert.assertEquals(accountData.getUsername(), account.getUsername());
        Assert.assertEquals(accountData.getAcceptLanguage(), account.getAcceptLanguage());
        Assert.assertEquals(accountData.getCompanyName(), account.getCompanyName());
        log.info("Created account: {}", account.getAccountCode());

        final Account retrievedAccount = recurlyClient.getAccount(account.getAccountCode());
        Assert.assertEquals(retrievedAccount, account);

        final BillingInfo billingInfoData = new BillingInfo();
        billingInfoData.setFirstName(randomString());
        billingInfoData.setLastName(randomString());
        billingInfoData.setNumber("4111-1111-1111-1111");
        billingInfoData.setVerificationValue(123);
        billingInfoData.setMonth(11);
        billingInfoData.setYear(2015);
        billingInfoData.setAccount(account);

        final BillingInfo billingInfo = recurlyClient.createOrUpdateBillingInfo(billingInfoData);
        Assert.assertNotNull(billingInfo);
        Assert.assertEquals(billingInfoData.getFirstName(), billingInfo.getFirstName());
        Assert.assertEquals(billingInfoData.getLastName(), billingInfo.getLastName());
        Assert.assertEquals(billingInfoData.getMonth(), billingInfo.getMonth());
        Assert.assertEquals(billingInfoData.getYear(), billingInfo.getYear());
        Assert.assertEquals(billingInfo.getCardType(), "Visa");
        log.info("Added billing info: {}", billingInfo.getCardType());

        final BillingInfo retrievedBillingInfo = recurlyClient.getBillingInfo(account.getAccountCode());
        Assert.assertEquals(retrievedBillingInfo, billingInfo);
    }
}
