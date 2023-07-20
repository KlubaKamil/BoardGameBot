package com.czachodym.BoardgameBot.exception;

public class GameAlreadyRegisteredException extends RuntimeException{
    public GameAlreadyRegisteredException(String url, long channelId){
        super("Game with given URL already registered on a given channel: " + url + " " + channelId);
    }
}
