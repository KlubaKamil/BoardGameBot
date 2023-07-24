package com.czachodym.BoardgameBot.exception;

import java.net.URL;

public class NoActivePlayersException extends RuntimeException{
    public NoActivePlayersException(URL url){
        super("Could not find active players for game: " + url);
    }
}
