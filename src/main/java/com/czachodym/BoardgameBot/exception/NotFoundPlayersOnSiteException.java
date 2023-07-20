package com.czachodym.BoardgameBot.exception;

import java.util.List;

public class NotFoundPlayersOnSiteException extends RuntimeException{
    public NotFoundPlayersOnSiteException(List<String> notFoundPlayers){
        super("Could not find players on site: " + notFoundPlayers);
    }
}
