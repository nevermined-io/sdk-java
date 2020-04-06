/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.aquarius;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.FromJsonToModel;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult extends AbstractModel implements FromJsonToModel {

    @JsonIgnore
    private static final int DEFAULT_PAGE = 0;

    @JsonIgnore
    private static final int DEFAULT_TOTAL_PAGES = 1;

    @JsonIgnore
    private static final int DEFAULT_TOTAL_RESULTS = 0;


    @JsonProperty
    public int page;

    @JsonProperty
    public int total_pages;

    @JsonProperty
    public int total_results;

    @JsonProperty
    public List<DDO> results = new ArrayList<>();


    public SearchResult() {
        this.page = DEFAULT_PAGE;
        this.total_pages = DEFAULT_TOTAL_PAGES;
        this.total_results = DEFAULT_TOTAL_RESULTS;
    }

    public SearchResult(List<DDO> results) {
        this(results, DEFAULT_PAGE, DEFAULT_TOTAL_PAGES, DEFAULT_TOTAL_RESULTS);
    }

    public SearchResult(List<DDO> results, int page, int total_pages, int total_results) {
        this.results = results;
        this.page = page;
        this.total_pages = total_pages;
        this.total_results = total_results;
    }

    public List<DDO> getResults() {
        return results;
    }
}