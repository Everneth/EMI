package com.everneth.emi.api;

import lombok.Getter;

public class Path {
    public static class Web {
        @Getter public static final String ONE_STATS = "/stats/:uuid";
        @Getter public static final String ONE_ADV = "/advs/:uuid";
        @Getter public static final String ONE_DATA = "/data/:uuid";
    }
}
