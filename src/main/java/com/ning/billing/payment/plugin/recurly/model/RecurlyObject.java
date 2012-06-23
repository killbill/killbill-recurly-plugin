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

package com.ning.billing.payment.plugin.recurly.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RecurlyObject {
    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class RecurlyDateTime {
        @XmlValue
        private DateTime dateTime;

        public DateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(final DateTime dateTime) {
            this.dateTime = dateTime;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("RecurlyDateTime");
            sb.append("{dateTime=").append(dateTime);
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

            final RecurlyDateTime that = (RecurlyDateTime) o;

            if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return dateTime != null ? dateTime.hashCode() : 0;
        }
    }
}
