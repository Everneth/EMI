package com.everneth.emi.models.enums;

import com.everneth.emi.EMI;

public enum ServerApiUrl {
    TEST_SERVER("test-server-api-url"),
    BUILD_SERVER("build-server-api-url"),
    GAMES_SERVER("games-server-api-url"),
    MAPDEV_SERVER("mapdev-server-api-url");

    private final String Url;
    private ServerApiUrl(String url) {this.Url = url;}

    public String get()
    {
        return EMI.getConfigString(this.Url);
    }
}
