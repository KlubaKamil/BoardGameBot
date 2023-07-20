package com.czachodym.BoardgameBot.enumeration;

public enum Site {
    BOARDGAMECORE("http://play.boardgamecore.net/");

    private String url;

    Site(String url){
        this.url = url;
    }

    public String getValue(){
        return url;
    }
}
