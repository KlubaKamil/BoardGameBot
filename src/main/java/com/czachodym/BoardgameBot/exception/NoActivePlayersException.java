package com.czachodym.BoardgameBot.exception;

public class NoActivePlayersException extends RuntimeException{
    public NoActivePlayersException(String url){
        super("Could not find active players for game: " + url);
    }
}
