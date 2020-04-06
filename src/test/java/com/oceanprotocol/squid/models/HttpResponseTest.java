/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import org.apache.commons.httpclient.Header;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpResponseTest {

    @Test
    public void testHttpResponse() {
        HttpResponse httpResponse= new HttpResponse(200, "{body}", "UTF-8", 6);

        Header[] headers= { new Header("key", "value")};
        httpResponse.setHeaders(headers);

        assertEquals(200, httpResponse.getStatusCode());
        assertEquals("{body}", httpResponse.getBody());
        assertEquals("UTF-8", httpResponse.getCharset());
        assertEquals(6, httpResponse.getContentLength());
        assertTrue(httpResponse.toString().contains("statusCode=200"));
        assertEquals(1, httpResponse.getHeaders().length);
        assertEquals("value", httpResponse.getHeader("key").getValue());
    }
}