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

package org.killbill.billing.plugin.recurly.api;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.plugin.recurly.client.RecurlyObjectFactory;
import org.killbill.billing.util.entity.Pagination;

import com.ning.billing.recurly.RecurlyClient;
import com.ning.billing.recurly.model.Account;
import com.ning.billing.recurly.model.Accounts;
import com.ning.billing.recurly.model.BillingInfo;

public class RecurlyPagination implements Pagination<PaymentMethodPlugin> {

    private final String searchKey;
    private final Long offset;
    private final Long limit;

    private final RecurlyClient recurlyClient;

    public RecurlyPagination(final String searchKey, final Long offset, final Long limit, final RecurlyClient recurlyClient) {
        this.searchKey = searchKey;
        this.offset = offset;
        this.limit = limit;
        this.recurlyClient = recurlyClient;
    }

    @Override
    public Long getCurrentOffset() {
        return offset;
    }

    @Override
    public Long getNextOffset() {
        return null;
    }

    @Override
    public Long getMaxNbRecords() {
        return null;
    }

    @Override
    public Long getTotalNbRecords() {
        return null;
    }

    @Override
    public Iterator<PaymentMethodPlugin> iterator() {
        return new RecurlyPaymentMethodPluginIterator(searchKey, offset, limit, recurlyClient.getAccounts(), recurlyClient);
    }

    public static final class RecurlyPaymentMethodPluginIterator implements Iterator<PaymentMethodPlugin> {

        private final Deque<Account> stack = new ArrayDeque<Account>();

        private final RecurlyClient recurlyClient;
        private final String searchKey;
        private final Long offset;
        private final Long limit;

        private Accounts currentAccounts;
        private Long currentOffset = 0L;
        private Long foundAccounts = 0L;
        private PaymentMethodPlugin nextPaymentMethodPlugin;

        public RecurlyPaymentMethodPluginIterator(final String searchKey, final Long offset, final Long limit,
                                                  final Accounts accounts, final RecurlyClient recurlyClient) {
            this.searchKey = searchKey;
            this.offset = offset;
            this.limit = limit;
            this.recurlyClient = recurlyClient;

            this.currentAccounts = accounts;
            // Initialize the stack
            stack.addAll(accounts);
            findNext();
        }

        @Override
        public boolean hasNext() {
            return !(currentOffset < offset || foundAccounts >= limit || nextPaymentMethodPlugin == null);
        }

        @Override
        public PaymentMethodPlugin next() {
            final PaymentMethodPlugin toReturn = nextPaymentMethodPlugin;
            findNext();
            return toReturn;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void findNext() {
            while (!stack.isEmpty()) {
                final Account account = stack.pop();

                final PaymentMethodPlugin paymentMethodPlugin = getPaymentMethodPluginFromAccount(account);
                if (paymentMethodPlugin != null) {
                    currentOffset += 1;
                    if (currentOffset >= offset) {
                        foundAccounts += 1;
                        nextPaymentMethodPlugin = paymentMethodPlugin;
                        return;
                    }
                }

                if (stack.isEmpty()) {
                    populateStack();
                }
            }

            // No more
            nextPaymentMethodPlugin = null;
        }

        private PaymentMethodPlugin getPaymentMethodPluginFromAccount(final Account account) {
            final BillingInfo billingInfo = recurlyClient.getBillingInfo(account.getAccountCode());
            if ((billingInfo.getFirstName() != null && billingInfo.getFirstName().contains(searchKey)) ||
                (billingInfo.getLastName() != null && billingInfo.getLastName().contains(searchKey)) ||
                (billingInfo.getAddress1() != null && billingInfo.getAddress1().contains(searchKey)) ||
                (billingInfo.getAddress2() != null && billingInfo.getAddress2().contains(searchKey)) ||
                (billingInfo.getCardType() != null && billingInfo.getCardType().equals(searchKey)) ||
                (billingInfo.getCity() != null && billingInfo.getCity().equals(searchKey)) ||
                (billingInfo.getCountry() != null && billingInfo.getCountry().equals(searchKey)) ||
                (billingInfo.getLastFour() != null && billingInfo.getLastFour().equals(searchKey)) ||
                (billingInfo.getMonth() != null && billingInfo.getMonth().toString().equals(searchKey)) ||
                (billingInfo.getYear() != null && billingInfo.getYear().toString().equals(searchKey))) {
                return new RecurlyPaymentMethodPlugin(billingInfo, RecurlyObjectFactory.kbPaymentMethodIdFromBillingInfo(billingInfo));
            } else {
                return null;
            }
        }

        private void populateStack() {
            currentAccounts = currentAccounts.getNext();
            if (currentAccounts != null) {
                // More Recurly accounts?
                stack.addAll(currentAccounts);
            }
        }
    }
}
