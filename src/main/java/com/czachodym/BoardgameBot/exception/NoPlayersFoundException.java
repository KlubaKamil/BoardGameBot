package com.czachodym.BoardgameBot.exception;

public class NoPlayersFoundException extends RuntimeException{
    public NoPlayersFoundException(){
        super("An error occurred during getting players list");
    }
}
