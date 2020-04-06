/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.aquarius;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.FromJsonToModel;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery extends AbstractModel implements FromJsonToModel {

    @JsonIgnore
    private static final int DEFAULT_OFFSET = 100;

    @JsonIgnore
    private static final int DEFAULT_PAGE = 1;

    @JsonIgnore
    private static final int DEFAULT_SORT = 1;

    @JsonProperty
    public int offset;

    @JsonProperty
    public int page;

    @JsonProperty
    public Map<String, Object> query = new HashMap<>();

    @JsonProperty
    public Sort sort;

    public static class Sort {
        @JsonProperty
        public int value;

        public Sort() {
            value = 1;
        }

        public Sort(int sort) {
            this.value = sort;
        }
    }

    public SearchQuery() {
        this.offset = DEFAULT_OFFSET;
        this.page = DEFAULT_PAGE;
        this.sort = new Sort();
    }

    public SearchQuery(HashMap<String, Object> params) {
        this(params, DEFAULT_OFFSET, DEFAULT_PAGE, DEFAULT_SORT);
    }

    public SearchQuery(Map<String, Object> params, int offset, int page, int sort) {
        this.query = params;
        this.offset = offset;
        this.page = page;
        this.sort = new Sort(sort);

    }

    public void addQueryParam(String field, Object value) {
        this.query.put(field, value);
    }

}