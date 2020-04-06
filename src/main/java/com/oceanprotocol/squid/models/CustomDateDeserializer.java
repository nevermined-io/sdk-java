/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.oceanprotocol.squid.models.AbstractModel.DATE_PATTERN;

public class CustomDateDeserializer extends StdDeserializer<Date> {

    public static final String ALT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private SimpleDateFormat formatter = initializeFormatter(DATE_PATTERN);

    private SimpleDateFormat altFormatter = initializeFormatter(ALT_DATE_PATTERN);

    private SimpleDateFormat initializeFormatter(String pattern) {

        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formatter;

    }

    public CustomDateDeserializer() {
        this(null);
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String date = jsonParser.getText();
        try {
            return formatter.parse(date);
        } catch (ParseException e) {

            try {
                return altFormatter.parse(date);
            }
            catch (ParseException e2) {
                throw new IOException(e);
            }
        }
    }

    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

}
