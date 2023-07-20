package com.czachodym.BoardgameBot.service;

import com.czachodym.BoardgameBot.enumeration.Site;
import com.czachodym.BoardgameBot.exception.NoSiteException;
import com.czachodym.BoardgameBot.model.Game;
import com.czachodym.BoardgameBot.repository.GameRepository;
import com.czachodym.BoardgameBot.service.sites.BoardgamecoreService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


@Service
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final JDA jda;
    private final BoardgamecoreService boardgamecoreService;


    public GameService(GameRepository gameRepository, JDA jda, BoardgamecoreService boardgamecoreService){
        this.gameRepository = gameRepository;
        this.jda = jda;
        this.boardgamecoreService = boardgamecoreService;
    }

    public Game addGame(Game game){
        return gameRepository.save(game);
    }

    public Optional<Game> getByUrlAndChannelId(String url, long channelId){
        return gameRepository.findByUrlAndChannelId(url, channelId);
    }

    public Optional<Game> deleteByUrl(String url, long channelId){
        Optional<Game> game = getByUrlAndChannelId(url, channelId);
        game.ifPresent(gameRepository::delete);
        return game;
    }

    public void checkGames(){
        for(Game game: gameRepository.findAll()){
            List<String> newActivePlayers = getActivePlayers(game.getSite(), game.getUrl());
            List<String> currentActivePlayers = game.getCurrentActivePlayers();
            List<String> playersToNotify = getPlayersToNotify(newActivePlayers, currentActivePlayers, game.getPlayersMapping());
            notifyPlayers(game.getChannelId(), game.getUrl(), playersToNotify);
            game.setCurrentActivePlayers(newActivePlayers);
            gameRepository.save(game);
        }
    }


    public List<String> validatePlayers(String url, Site site, Map<String, String> playerMapping){
        switch(site){
            case BOARDGAMECORE -> {
                List<String> allPlayers = boardgamecoreService.getAllPlayers(url);
                Set<String> registeredPlayers = playerMapping.keySet();
                List<String> notFoundPlayers = new ArrayList<>();
                for(String rp: registeredPlayers){
                    if(!allPlayers.contains(rp)){
                        notFoundPlayers.add(rp);
                    }
                }
                return notFoundPlayers;
            }
            default -> throw new NoSiteException(site);
        }
    }

    private List<String> getActivePlayers(Site site, String url){
        switch (site){
            case BOARDGAMECORE:
                return boardgamecoreService.getActivePlayers(url);
            default:
                throw new NoSiteException(site);
        }
    }

    private List<String> getPlayersToNotify(List<String> newActivePlayers, List<String> currentActivePlayers,
                                                             Map<String, String> mapping){
        List<String> players = new ArrayList<>();

        for(String np: newActivePlayers){
            boolean foundNew = true;
            for(String cp: currentActivePlayers){
                if(np.equals(cp)){
                    foundNew = false;
                    break;
                }
            }
            if(foundNew && mapping.containsKey(np)){
                players.add(mapping.get(np));
            }
        }
        return players;
    }

    private void notifyPlayers(long channelId, String url, List<String> players){
        StringBuilder message = new StringBuilder();
        boolean send = false;
        message.append("Game: ")
                .append(url)
                .append("\n");
        if(players != null && players.size() > 0) {
            send = true;
            message.append(String.join(", ", players))
                    .append(", Your turn!\n");
        }

        if(send){
            jda.getTextChannelById(channelId)
                    .sendMessage(message)
                    .queue();

        }
    }

}
