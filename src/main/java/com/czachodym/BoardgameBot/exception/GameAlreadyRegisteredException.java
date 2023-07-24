package com.czachodym.BoardgameBot.exception;

import java.net.URL;

public class GameAlreadyRegisteredException extends RuntimeException{
    public GameAlreadyRegisteredException(URL url, long channelId){
        super("Game with given URL already registered on a given channel: " + url + " " + channelId);
    }
}
