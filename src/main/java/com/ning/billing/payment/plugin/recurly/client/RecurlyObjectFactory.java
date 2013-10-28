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

package com.ning.billing.payment.plugin.recurly.client;

import java.nio.charset.Charset;
import java.util.UUID;

import com.ning.billing.payment.api.PaymentMethodKVInfo;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.recurly.api.RecurlyPaymentMethodPlugin;
import com.ning.billing.recurly.model.Account;
import com.ning.billing.recurly.model.BillingInfo;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RecurlyObjectFactory {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static Account createAccountFromKillbill(final com.ning.billing.account.api.Account killbillAccount) {
        final Account account = new Account();

        account.setAcceptLanguage(killbillAccount.getLocale());
        account.setCompanyName(killbillAccount.getCompanyName());
        // Not used within Recurly
        account.setUsername(killbillAccount.getId().toString());
        // Use Killbill external key for the unique account code
        account.setAccountCode(killbillAccount.getExternalKey());
        account.setEmail(killbillAccount.getEmail());
        account.setFirstName(killbillAccount.getName());
        account.setState(killbillAccount.getStateOrProvince());

        return account;
    }

    public static String createAccountCode(final UUID kbAccountId) {
        return kbAccountId.toString();
    }

    public static UUID kbPaymentMethodIdFromBillingInfo(final BillingInfo billingInfo) {
        // We use company because it is big enough (phone can hold up to 30 characters and VAT is limited to 20)
        if (billingInfo.getCompany() == null) {
            return null;
        } else {
            return UUID.fromString(billingInfo.getCompany());
        }
    }

    public static BillingInfo createBillingInfoFromKillbill(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodPlugin) {
        final BillingInfo billingInfo = new BillingInfo();

        final Account account = new Account();
        account.setAccountCode(kbAccountId.toString());
        billingInfo.setAccount(account);

        billingInfo.setAddress1(paymentMethodPlugin.getAddress1());
        billingInfo.setAddress2(paymentMethodPlugin.getAddress2());
        billingInfo.setCardType(paymentMethodPlugin.getCCType());
        billingInfo.setCity(paymentMethodPlugin.getCity());
        billingInfo.setCountry(paymentMethodPlugin.getCountry());
        billingInfo.setLastFour(paymentMethodPlugin.getCCLast4());
        billingInfo.setMonth(paymentMethodPlugin.getCCExpirationMonth());
        billingInfo.setState(paymentMethodPlugin.getState());
        billingInfo.setYear(paymentMethodPlugin.getCCExpirationYear());
        billingInfo.setZip(paymentMethodPlugin.getZip());

        // See below
        //billingInfo.setCompany(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.COMPANY));

        billingInfo.setFirstName(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.FIRST_NAME));
        billingInfo.setFirstSix(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.FIRST_SIX));
        billingInfo.setIpAddress(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.IP_ADDRESS));
        billingInfo.setIpAddressCountry(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.IP_ADDRESS_COUNTRY));
        billingInfo.setLastName(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.LAST_NAME));
        billingInfo.setNumber(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.NUMBER));
        billingInfo.setPhone(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.PHONE));
        billingInfo.setVatNumber(getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.VAT_NUMBER));
        final Object verificationValue = getPaymentMethodKVValue(paymentMethodPlugin, RecurlyPaymentMethodPlugin.VERIFICATION_VALUE);
        if (verificationValue != null) {
            billingInfo.setVerificationValue(Integer.valueOf(verificationValue.toString()));
        }

        // Magic! Use the company field to store the kb payment method id
        billingInfo.setCompany(kbPaymentMethodId.toString());

        return billingInfo;
    }

    private static Object getPaymentMethodKVValue(final PaymentMethodPlugin paymentMethodPlugin, final String key) {
        final PaymentMethodKVInfo paymentMethodKVInfo = Iterables.<PaymentMethodKVInfo>find(paymentMethodPlugin.getProperties(),
                                                                                            new Predicate<PaymentMethodKVInfo>() {
                                                                                                @Override
                                                                                                public boolean apply(final PaymentMethodKVInfo input) {
                                                                                                    return key.equals(input.getKey());
                                                                                                }
                                                                                            },
                                                                                            null);
        return paymentMethodKVInfo == null ? null : paymentMethodKVInfo.getValue();
    }
}
