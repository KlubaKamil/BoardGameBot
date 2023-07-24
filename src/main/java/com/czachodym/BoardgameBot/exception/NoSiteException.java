package com.czachodym.BoardgameBot.exception;

import com.czachodym.BoardgameBot.enumeration.Site;

import java.net.URL;

public class NoSiteException extends RuntimeException{
    public NoSiteException(String url){
        super("No site could be found from a url: " + url);
    }
    public NoSiteException(URL url){
        super("No site could be found from a url: " + url);
    }

    public NoSiteException(Site site){
        super("No site could be found: " + site);
    }
}
