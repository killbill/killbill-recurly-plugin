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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestRecurlyClient {
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
        accountData.setAccountCode(UUID.randomUUID().toString().substring(0, 5));
        accountData.setEmail(UUID.randomUUID().toString().substring(0, 5) + "@laposte.net");
        accountData.setFirstName(UUID.randomUUID().toString().substring(0, 5));
        accountData.setLastName(UUID.randomUUID().toString().substring(0, 5));
        accountData.setUsername(UUID.randomUUID().toString().substring(0, 5));
        accountData.setAcceptLanguage("en_US");
        accountData.setCompanyName(UUID.randomUUID().toString().substring(0, 5));

        final Account account = recurlyClient.createAccount(accountData);
        Assert.assertNotNull(account);

        final Account retrievedAccount = recurlyClient.getAccount(account.getAccountCode());
        Assert.assertEquals(retrievedAccount, account);
    }
}
