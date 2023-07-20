package com.czachodym.BoardgameBot.service.sites;

import com.czachodym.BoardgameBot.exception.NoActivePlayersException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static net.dv8tion.jda.api.requests.RestConfig.USER_AGENT;


@Slf4j
public abstract class BoardGameWebsiteService{
    public List<String> getActivePlayers(String url){
        try {
            URL myURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = reader.readLine()) != null){
                List<String> players = findActivePlayers(line);
                if(players.size() > 0){
                    return players;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NoActivePlayersException(url);
    }

    public List<String> getAllPlayers(String url){
        try {
            URL myURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = reader.readLine()) != null){
                List<String> players = findAllPlayers(line);
                if(players.size() > 0){
                    return players;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NoActivePlayersException(url);
    }

    protected abstract List<String> findAllPlayers(String line);
    protected abstract List<String> findActivePlayers(String line);
}
