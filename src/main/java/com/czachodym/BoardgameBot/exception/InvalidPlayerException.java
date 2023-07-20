package com.czachodym.BoardgameBot.exception;

public class InvalidPlayerException extends RuntimeException{
    public InvalidPlayerException(){
        super("Player has been configured incorrectly. Nothing happens.");
    }
}
