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

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.recurly.api.RecurlyPaymentMethodPlugin;
import com.ning.billing.payment.plugin.recurly.model.Account;
import com.ning.billing.payment.plugin.recurly.model.BillingInfo;

public class RecurlyObjectFactory {
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

    public static String getExternalPaymentIdFromBillingInfo(final BillingInfo billingInfo) {
        // Recurly doesn't expose it, set a dummy one
        return String.format("%s::1", billingInfo.getAccount().getAccountCode());
    }

    public static BillingInfo createBillingInfoFromKillbill(final String accountKey, final PaymentMethodPlugin paymentMethodPlugin) {
        final BillingInfo billingInfo = new BillingInfo();

        final Account account = new Account();
        account.setAccountCode(accountKey);
        billingInfo.setAccount(account);

        billingInfo.setAddress1(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.ADDRESS_1));
        billingInfo.setAddress2(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.ADDRESS_2));
        billingInfo.setCardType(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.CARD_TYPE));
        billingInfo.setCity(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.CITY));
        billingInfo.setCompany(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.COMPANY));
        billingInfo.setCountry(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.COUNTRY));
        billingInfo.setFirstName(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.FIRST_NAME));
        billingInfo.setFirstSix(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.FIRST_SIX));
        billingInfo.setIpAddress(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.IP_ADDRESS));
        billingInfo.setIpAddressCountry(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.IP_ADDRESS_COUNTRY));
        billingInfo.setLastFour(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.LAST_FOUR));
        billingInfo.setLastName(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.LAST_NAME));
        billingInfo.setMonth(Integer.valueOf(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.MONTH)));
        billingInfo.setNumber(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.NUMBER));
        billingInfo.setPhone(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.PHONE));
        billingInfo.setState(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.STATE));
        billingInfo.setVatNumber(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.VAT_NUMBER));
        billingInfo.setVerificationValue(Integer.valueOf(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.VERIFICATION_VALUE)));
        billingInfo.setYear(Integer.valueOf(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.YEAR)));
        billingInfo.setZip(paymentMethodPlugin.getValueString(RecurlyPaymentMethodPlugin.ZIP));

        return billingInfo;
    }
}
