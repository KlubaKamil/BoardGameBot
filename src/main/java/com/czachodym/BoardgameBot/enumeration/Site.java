package com.czachodym.BoardgameBot.enumeration;

public enum Site {
    BOARDGAMECORE("play.boardgamecore.net"),
    RALLY_THE_TROOPS("rally-the-troops.com");

    private String url;

    Site(String url){
        this.url = url;
    }

    public String getValue(){
        return url;
    }
}
