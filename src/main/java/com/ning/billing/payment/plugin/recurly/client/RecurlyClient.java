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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.ning.billing.payment.plugin.recurly.model.Account;
import com.ning.billing.payment.plugin.recurly.model.RecurlyObject;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class RecurlyClient {
    private static final Logger log = LoggerFactory.getLogger(RecurlyClient.class);

    private final XmlMapper xmlMapper = new XmlMapper();

    private final String key;
    private final String baseUrl;
    private AsyncHttpClient client;

    public RecurlyClient(final String apiKey) {
        this(apiKey, "api.recurly.com", 443, "v2");
    }

    public RecurlyClient(final String apiKey, final String host, final int port, final String version) {
        this.key = DatatypeConverter.printBase64Binary(apiKey.getBytes());
        this.baseUrl = String.format("https://%s:%d/%s", host, port, version);

        final AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        final AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
        final AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);
        xmlMapper.setAnnotationIntrospector(pair);
        xmlMapper.registerModule(new JodaModule());
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Open the underlying http client
     */
    public synchronized void open() {
        client = createHttpClient();
    }

    /**
     * Close the underlying http client
     */
    public synchronized void close() {
        if (client != null) {
            client.close();
        }
    }

    public Account createAccount(final Account account) {
        return (Account) doPOST("/accounts", account, Account.class);
    }

    public Account getAccount(final String externalKey) {
        return (Account) doGET("/accounts/" + externalKey, Account.class);
    }

    private RecurlyObject doGET(final String resource, final Class<? extends RecurlyObject> clazz) {
        try {
            return callRecurly(client.prepareGet(baseUrl + resource), clazz);
        } catch (IOException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        } catch (InterruptedException e) {
            return null;
        }
    }

    private RecurlyObject doPOST(final String resource, final RecurlyObject payload, final Class<? extends RecurlyObject> clazz) {
        final String xmlPayload;
        try {
            xmlPayload = xmlMapper.writeValueAsString(payload);
        } catch (IOException e) {
            log.warn("Unable to serialize {} object as XML: {}", clazz.getName(), payload.toString());
            return null;
        }

        try {
            return callRecurly(client.preparePost(baseUrl + resource).setBody(xmlPayload), clazz);
        } catch (IOException e) {
            log.warn("Error while calling Recurly", e);
            return null;
        } catch (ExecutionException e) {
            log.error("Execution error", e);
            return null;
        } catch (InterruptedException e) {
            log.error("Interrupted while calling Recurly", e);
            return null;
        }
    }

    private RecurlyObject callRecurly(final AsyncHttpClient.BoundRequestBuilder builder, final Class<? extends RecurlyObject> clazz) throws IOException, ExecutionException, InterruptedException {
        return builder.addHeader("Authorization", "Basic " + key)
                .addHeader("Accept", "application/xml")
                .addHeader("Content-Type", "application/xml; charset=utf-8")
                .execute(new AsyncCompletionHandler<RecurlyObject>() {
                    @Override
                    public RecurlyObject onCompleted(final Response response) throws Exception {
                        if (response.getStatusCode() >= 300) {
                            log.warn("Recurly error: {}", response.getResponseBody());
                            return null;
                        }

                        final InputStream in = response.getResponseBodyAsStream();
                        try {
                            return xmlMapper.readValue(in, clazz);
                        } finally {
                            closeStream(in);
                        }
                    }
                }).get();
    }

    private void closeStream(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.warn("Failed to close http-client - provided InputStream: {}", e.getLocalizedMessage());
            }
        }
    }

    private AsyncHttpClient createHttpClient() {
        // Don't limit the number of connections per host
        // See https://github.com/ning/async-http-client/issues/issue/28
        final AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setMaximumConnectionsPerHost(-1);
        return new AsyncHttpClient(builder.build());
    }
}
