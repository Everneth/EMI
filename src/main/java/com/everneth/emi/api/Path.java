package com.everneth.emi.api;

import lombok.Getter;

public class Path {
    public static class Web {
        @Getter public static final String ONE_STATS = "/stats/:uuid";
    }
}
