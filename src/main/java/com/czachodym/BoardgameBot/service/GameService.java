package com.czachodym.BoardgameBot.service;

import com.czachodym.BoardgameBot.enumeration.Site;
import com.czachodym.BoardgameBot.model.Game;
import com.czachodym.BoardgameBot.repository.GameRepository;
import com.czachodym.BoardgameBot.service.sites.BoardGameService;
import com.czachodym.BoardgameBot.service.sites.BoardgamecoreService;
import com.czachodym.BoardgameBot.service.sites.RallythetroopsService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final JDA jda;
    private final BoardgamecoreService boardgamecoreService;
    private final RallythetroopsService rallythetroopsService;


    public GameService(GameRepository gameRepository, JDA jda, BoardgamecoreService boardgamecoreService, RallythetroopsService rallythetroopsService){
        this.gameRepository = gameRepository;
        this.jda = jda;
        this.boardgamecoreService = boardgamecoreService;
        this.rallythetroopsService = rallythetroopsService;
    }

    public Game addGame(Game game){
        return gameRepository.save(game);
    }

    public Optional<Game> getByUrlAndChannelId(URL url, long channelId){
        return gameRepository.findByUrlAndChannelId(url, channelId);
    }

    public Optional<Game> deleteByUrl(URL url, long channelId){
        Optional<Game> game = getByUrlAndChannelId(url, channelId);
        game.ifPresent(gameRepository::delete);
        return game;
    }

    public void checkGames(){
        for(Game game: gameRepository.findAll()){
            BoardGameService service = getService(game.getSite());
            List<String> newActivePlayers = service.getActivePlayers(game.getUrl(), game.getPlayersGameMapping());
            List<String> currentActivePlayers = game.getCurrentActivePlayers();
            List<String> playersToNotify = getPlayersToNotify(newActivePlayers, currentActivePlayers, game.getPlayersDiscordMapping());
            URL notifyUrl = service.getNotifyUrl(game.getUrl(), game.getPlayersGameMapping(), game.getPlayersDiscordMapping(),
                    playersToNotify);
            notifyPlayers(game.getChannelId(), notifyUrl, playersToNotify);
            game.setCurrentActivePlayers(newActivePlayers);
            gameRepository.save(game);
        }
    }

    private BoardGameService getService(Site site){
        return switch(site){
            case BOARDGAMECORE -> boardgamecoreService;
            case RALLY_THE_TROOPS -> rallythetroopsService;
        };
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

    private void notifyPlayers(long channelId, URL url, List<String> players){
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
