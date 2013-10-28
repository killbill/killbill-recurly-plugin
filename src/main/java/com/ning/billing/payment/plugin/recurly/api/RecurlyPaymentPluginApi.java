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

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.payment.plugin.api.RefundInfoPlugin;
import com.ning.billing.payment.plugin.recurly.client.RecurlyObjectFactory;
import com.ning.billing.recurly.RecurlyClient;
import com.ning.billing.recurly.TransactionErrorException;
import com.ning.billing.recurly.model.Adjustment;
import com.ning.billing.recurly.model.BillingInfo;
import com.ning.billing.recurly.model.Invoice;
import com.ning.billing.recurly.model.Invoices;
import com.ning.billing.recurly.model.Transaction;
import com.ning.billing.recurly.model.Transactions;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.TenantContext;
import com.ning.billing.util.entity.Pagination;

import com.google.common.collect.ImmutableList;

public class RecurlyPaymentPluginApi implements PaymentPluginApi {

    private static final Logger log = LoggerFactory.getLogger(RecurlyPaymentPluginApi.class);

    private final RecurlyClient client;

    public RecurlyPaymentPluginApi(final RecurlyClient client) {
        this.client = client;
    }

    @Override
    public PaymentInfoPlugin processPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
        final Transaction transaction = new Transaction();
        transaction.setAmountInCents(100 * amount.intValue());
        transaction.setCurrency(currency.toString());

        // Magic description to retrieve the payment info
        transaction.setDescription(kbPaymentId);

        // Assume the account already exists
        final com.ning.billing.recurly.model.Account account = new com.ning.billing.recurly.model.Account();
        account.setAccountCode(RecurlyObjectFactory.createAccountCode(kbAccountId));
        transaction.setAccount(account);

        final Transaction createdTransaction = client.createTransaction(transaction);
        return new RecurlyPaymentInfoPlugin(createdTransaction);
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId, final TenantContext context) throws PaymentPluginApiException {
        final Transaction transactionForPayment = findTransactionForKbPaymentId(kbAccountId, kbPaymentId);
        if (transactionForPayment == null) {
            return null;
        } else {
            return new RecurlyPaymentInfoPlugin(transactionForPayment);
        }
    }

    @Override
    public RefundInfoPlugin processRefund(final UUID kbAccountId, final UUID kbPaymentId, final BigDecimal refundAmount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
        final Transaction transactionForPayment = findTransactionForKbPaymentId(kbAccountId, kbPaymentId);
        if (transactionForPayment == null) {
            return null;
        } else if (!transactionForPayment.getRefundable()) {
            throw new PaymentPluginApiException("REFUND", "Payment " + kbPaymentId + " is not refundable");
        } else {
            client.refundTransaction(transactionForPayment.getUuid(), refundAmount);
            return new RecurlyRefundInfoPlugin(client.getTransaction(transactionForPayment.getUuid()), refundAmount);
        }
    }

    @Override
    public List<RefundInfoPlugin> getRefundInfo(final UUID kbAccountId, final UUID kbPaymentId, final TenantContext context) throws PaymentPluginApiException {
        final Transaction transactionForPayment = findTransactionForKbPaymentId(kbAccountId, kbPaymentId);
        if (transactionForPayment == null) {
            return null;
        } else {
            return ImmutableList.<RefundInfoPlugin>of(new RecurlyRefundInfoPlugin(transactionForPayment));
        }
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final CallContext context) throws PaymentPluginApiException {
        try {
            client.createOrUpdateBillingInfo(RecurlyObjectFactory.createBillingInfoFromKillbill(kbAccountId, kbPaymentMethodId, paymentMethodProps));
        } catch (final TransactionErrorException e) {
            throw new PaymentPluginApiException("Unable to add a payment method for account id " + kbAccountId, e);
        }
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
        client.clearBillingInfo(RecurlyObjectFactory.createAccountCode(kbAccountId));
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId, final UUID kbPaymentMethodId, final TenantContext context) throws PaymentPluginApiException {
        final BillingInfo billingInfo = client.getBillingInfo(RecurlyObjectFactory.createAccountCode(kbAccountId));
        if (billingInfo == null) {
            return null;
        } else {
            return new RecurlyPaymentMethodPlugin(billingInfo, kbPaymentMethodId);
        }
    }

    @Override
    public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
        // No-op (one payment method only)
    }

    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final CallContext context) throws PaymentPluginApiException {
        final BillingInfo billingInfo = client.getBillingInfo(RecurlyObjectFactory.createAccountCode(kbAccountId));
        return ImmutableList.<PaymentMethodInfoPlugin>of(new RecurlyPaymentMethodInfoPlugin(billingInfo,
                                                                                            RecurlyObjectFactory.kbPaymentMethodIdFromBillingInfo(billingInfo)));
    }

    @Override
    public Pagination<PaymentMethodPlugin> searchPaymentMethods(final String searchKey, final Long offset, final Long limit, final TenantContext context) throws PaymentPluginApiException {
        // TODO Really slow... We might have to store a local copy just to support search
        return new RecurlyPagination(searchKey, offset, limit, client);
    }

    @Override
    public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods) throws PaymentPluginApiException {
        // No-op (one payment method only)
    }

    private Transaction findTransactionForKbPaymentId(final UUID kbAccountId, final UUID kbPaymentId) {
        // We need to find the invoice first, not the transaction, because the description field is added to the invoice
        Invoice invoiceForPayment = null;
        Invoices invoices = client.getAccountInvoices(RecurlyObjectFactory.createAccountCode(kbAccountId));
        while (invoiceForPayment == null && invoices != null) {
            for (final Invoice invoice : invoices) {
                for (final Adjustment charge : invoice.getLineItems()) {
                    if (kbPaymentId.toString().equals(charge.getDescription())) {
                        invoiceForPayment = invoice;
                        break;
                    }
                }
            }
            invoices = invoices.getNext();
        }
        if (invoiceForPayment == null) {
            return null;
        }

        // Find the associated transaction
        Transaction transactionForPayment = null;
        Transactions transactions = client.getAccountTransactions(RecurlyObjectFactory.createAccountCode(kbAccountId));
        while (transactionForPayment == null && transactions != null) {
            for (final Transaction transaction : transactions) {
                if (invoiceForPayment.getHref().equals(transaction.getInvoice().getHref())) {
                    transactionForPayment = transaction;
                    // Assume no partial payment
                    break;
                }
            }
            transactions = transactions.getNext();
        }

        return transactionForPayment;
    }
}
