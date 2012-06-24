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
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.recurly.client.RecurlyObjectFactory;
import com.ning.billing.payment.plugin.recurly.model.BillingInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecurlyPaymentMethodPlugin implements PaymentMethodPlugin {
    public static final String ADDRESS_1 = "address1";
    public static final String ADDRESS_2 = "address2";
    public static final String CARD_TYPE = "cardType";
    public static final String CITY = "city";
    public static final String COMPANY = "company";
    public static final String COUNTRY = "country";
    public static final String FIRST_NAME = "firstName";
    public static final String FIRST_SIX = "firstSix";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_ADDRESS_COUNTRY = "ipAddressCountry";
    public static final String LAST_FOUR = "lastFour";
    public static final String LAST_NAME = "lastName";
    public static final String MONTH = "month";
    public static final String NUMBER = "number";
    public static final String PHONE = "phone";
    public static final String STATE = "state";
    public static final String VAT_NUMBER = "vatNumber";
    public static final String VERIFICATION_VALUE = "verificationValue";
    public static final String YEAR = "year";
    public static final String ZIP = "zip";

    // A single unique payment method is stored in Recurly
    private final boolean isDefaultPaymentMethod = true;
    private final Map<String, PaymentMethodKVInfo> properties = new HashMap<String, PaymentMethodKVInfo>();

    private final String externalPaymentMethodId;

    public RecurlyPaymentMethodPlugin(final BillingInfo recurlyBillingInfo) {
        this.externalPaymentMethodId = RecurlyObjectFactory.getExternalPaymentIdFromBillingInfo(recurlyBillingInfo);
        properties.put(ADDRESS_1, new PaymentMethodKVInfo(ADDRESS_1, recurlyBillingInfo.getAddress1(), true));
        properties.put(ADDRESS_2, new PaymentMethodKVInfo(ADDRESS_2, recurlyBillingInfo.getAddress2(), true));
        properties.put(CARD_TYPE, new PaymentMethodKVInfo(CARD_TYPE, recurlyBillingInfo.getCardType(), true));
        properties.put(CITY, new PaymentMethodKVInfo(CITY, recurlyBillingInfo.getCity(), true));
        properties.put(COMPANY, new PaymentMethodKVInfo(COMPANY, recurlyBillingInfo.getCompany(), true));
        properties.put(COUNTRY, new PaymentMethodKVInfo(COUNTRY, recurlyBillingInfo.getCountry(), true));
        properties.put(FIRST_NAME, new PaymentMethodKVInfo(FIRST_NAME, recurlyBillingInfo.getFirstName(), true));
        properties.put(FIRST_SIX, new PaymentMethodKVInfo(FIRST_SIX, recurlyBillingInfo.getFirstSix(), true));
        properties.put(IP_ADDRESS, new PaymentMethodKVInfo(IP_ADDRESS, recurlyBillingInfo.getIpAddress(), true));
        properties.put(IP_ADDRESS_COUNTRY, new PaymentMethodKVInfo(IP_ADDRESS_COUNTRY, recurlyBillingInfo.getIpAddressCountry(), true));
        properties.put(LAST_FOUR, new PaymentMethodKVInfo(LAST_FOUR, recurlyBillingInfo.getLastFour(), true));
        properties.put(LAST_NAME, new PaymentMethodKVInfo(LAST_NAME, recurlyBillingInfo.getLastName(), true));
        properties.put(MONTH, new PaymentMethodKVInfo(MONTH, recurlyBillingInfo.getMonth(), true));
        properties.put(NUMBER, new PaymentMethodKVInfo(NUMBER, recurlyBillingInfo.getNumber(), true));
        properties.put(PHONE, new PaymentMethodKVInfo(PHONE, recurlyBillingInfo.getPhone(), true));
        properties.put(STATE, new PaymentMethodKVInfo(STATE, recurlyBillingInfo.getState(), true));
        properties.put(VAT_NUMBER, new PaymentMethodKVInfo(VAT_NUMBER, recurlyBillingInfo.getVatNumber(), true));
        properties.put(VERIFICATION_VALUE, new PaymentMethodKVInfo(VERIFICATION_VALUE, recurlyBillingInfo.getVerificationValue(), true));
        properties.put(YEAR, new PaymentMethodKVInfo(YEAR, recurlyBillingInfo.getYear(), true));
        properties.put(ZIP, new PaymentMethodKVInfo(ZIP, recurlyBillingInfo.getZip(), true));
    }

    @Override
    public String getExternalPaymentMethodId() {
        return externalPaymentMethodId;
    }

    @Override
    public boolean isDefaultPaymentMethod() {
        return isDefaultPaymentMethod;
    }

    @Override
    public List<PaymentMethodKVInfo> getProperties() {
        return ImmutableList.<PaymentMethodKVInfo>copyOf(properties.values());
    }

    @Override
    public String getValueString(final String key) {
        final PaymentMethodKVInfo paymentMethodKVInfo = properties.get(key);
        if (paymentMethodKVInfo == null || paymentMethodKVInfo.getValue() == null) {
            return null;
        } else {
            return paymentMethodKVInfo.getValue().toString();
        }
    }
}
