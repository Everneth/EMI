package com.everneth.emi.api;

import lombok.Getter;
import lombok.Setter;

public class Path {
    public static class Web {
        @Getter public static final String ONE_STATS = "/stats/:uuid";
        @Getter public static final String ONE_ADV = "/advs/:uuid";
        @Getter public static final String ONE_DATA = "/pdata/:uuid";
        @Setter public static final String EXECUTE_COMMAND = "/cmd/:payload";
    }
}
