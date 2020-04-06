/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.oceanprotocol.common.helpers.CryptoHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class AbstractModel {

    private static ObjectMapper objectMapper = null;

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    public static ObjectMapper getMapperInstance() {
        if (objectMapper == null) {

            objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setDateFormat(DATE_FORMAT);
        }

        return objectMapper;
    }


    private static <T> ObjectReader getReaderInstance(Class<T> clazz) {
        return getMapperInstance().readerFor(clazz);
    }

    public static <T> Object convertToModel(Class<T> clazz, String json) throws IOException {
        return getReaderInstance(clazz).readValue(json);
    }

    public static <T> T fromJSON(final TypeReference<T> type, final String json) throws IOException {
        return getMapperInstance().readValue(json, type);
    }

    public String checksum() throws JsonProcessingException {
        return CryptoHelper.sha3_256(toJson());
    }

    public String toJson() throws JsonProcessingException {
        return getMapperInstance().writeValueAsString(this);
    }

    public String toJson(Object object) throws JsonProcessingException {
        return getMapperInstance().writeValueAsString(object);
    }

    private static String getNowFormatted() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    public static Date getDateNowFormatted() {
        try {
            return DATE_FORMAT.parse(getNowFormatted());
        } catch (ParseException ex) {
            return new Date();
        }
    }

}