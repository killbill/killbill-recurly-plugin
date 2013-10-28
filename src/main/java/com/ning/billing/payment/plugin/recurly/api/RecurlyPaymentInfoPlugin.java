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

import org.joda.time.DateTime;

import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginStatus;
import com.ning.billing.recurly.model.Transaction;

public class RecurlyPaymentInfoPlugin implements PaymentInfoPlugin {

    private final Transaction transaction;

    public RecurlyPaymentInfoPlugin(final Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public BigDecimal getAmount() {
        final Integer amount = transaction.getAmountInCents() / 100;
        return new BigDecimal(amount.toString());
    }

    @Override
    public DateTime getCreatedDate() {
        return transaction.getCreatedAt();
    }

    @Override
    public DateTime getEffectiveDate() {
        return getCreatedDate();
    }

    @Override
    public PaymentPluginStatus getStatus() {
        return "success".equals(transaction.getStatus()) ? PaymentPluginStatus.PROCESSED : PaymentPluginStatus.ERROR;
    }

    @Override
    public String getGatewayError() {
        return transaction.getStatus();
    }

    @Override
    public String getGatewayErrorCode() {
        return null;
    }

    @Override
    public String getFirstPaymentReferenceId() {
        return transaction.getUuid();
    }

    @Override
    public String getSecondPaymentReferenceId() {
        return transaction.getReference();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecurlyPaymentInfoPlugin{");
        sb.append("transaction=").append(transaction);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RecurlyPaymentInfoPlugin that = (RecurlyPaymentInfoPlugin) o;

        if (getAmount() != null ? getAmount().compareTo(that.getAmount()) != 0 : that.getAmount() != null) {
            return false;
        }
        if (getCreatedDate() != null ? getCreatedDate().compareTo(that.getCreatedDate()) != 0 : that.getCreatedDate() != null) {
            return false;
        }
        if (getEffectiveDate() != null ? getEffectiveDate().compareTo(that.getEffectiveDate()) != 0 : that.getEffectiveDate() != null) {
            return false;
        }
        if (getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null) {
            return false;
        }
        if (getGatewayError() != null ? !getGatewayError().equals(that.getGatewayError()) : that.getGatewayError() != null) {
            return false;
        }
        if (getGatewayErrorCode() != null ? !getGatewayErrorCode().equals(that.getGatewayErrorCode()) : that.getGatewayErrorCode() != null) {
            return false;
        }
        if (getFirstPaymentReferenceId() != null ? !getFirstPaymentReferenceId().equals(that.getFirstPaymentReferenceId()) : that.getFirstPaymentReferenceId() != null) {
            return false;
        }
        if (getSecondPaymentReferenceId() != null ? !getSecondPaymentReferenceId().equals(that.getSecondPaymentReferenceId()) : that.getSecondPaymentReferenceId() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getAmount() != null ? getAmount().hashCode() : 0);
        result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
        result = 31 * result + (getEffectiveDate() != null ? getEffectiveDate().hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        result = 31 * result + (getGatewayError() != null ? getGatewayError().hashCode() : 0);
        result = 31 * result + (getGatewayErrorCode() != null ? getGatewayErrorCode().hashCode() : 0);
        result = 31 * result + (getFirstPaymentReferenceId() != null ? getFirstPaymentReferenceId().hashCode() : 0);
        result = 31 * result + (getSecondPaymentReferenceId() != null ? getSecondPaymentReferenceId().hashCode() : 0);
        return result;
    }
}
