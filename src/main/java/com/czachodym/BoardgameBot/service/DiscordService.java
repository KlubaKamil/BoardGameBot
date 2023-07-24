package com.czachodym.BoardgameBot.service;

import com.czachodym.BoardgameBot.enumeration.Site;
import com.czachodym.BoardgameBot.exception.GameAlreadyRegisteredException;
import com.czachodym.BoardgameBot.exception.InvalidPlayerException;
import com.czachodym.BoardgameBot.exception.NoSiteException;
import com.czachodym.BoardgameBot.exception.NotFoundPlayersOnSiteException;
import com.czachodym.BoardgameBot.model.Game;
import com.czachodym.BoardgameBot.service.sites.BoardGameService;
import com.czachodym.BoardgameBot.service.sites.BoardgamecoreService;
import com.czachodym.BoardgameBot.service.sites.RallythetroopsService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DiscordService extends ListenerAdapter {
    private final GameService gameService;
    private final JDA jda;
    private final BoardgamecoreService boardgamecoreService;
    private final RallythetroopsService rallythetroopsService;

    public DiscordService(GameService gameService, JDA jda, BoardgamecoreService boardgamecoreService, RallythetroopsService rallythetroopsService){
        this.gameService = gameService;
        this.jda = jda;
        this.boardgamecoreService = boardgamecoreService;
        this.rallythetroopsService = rallythetroopsService;

        jda.addEventListener(this);
        registerCommands(jda);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        switch (event.getName()) {
            case "register" -> registerGame(event);
            case "delete" -> deleteGame(event);
            case "sites" -> sites(event);
        }
    }

    private void registerCommands(JDA jda){
        CommandCreateAction registerCommand = jda.upsertCommand("register", "Use to register a game.")
                .addOption(OptionType.STRING, "url", "URL of a game", true);
        for(int i = 1; i <= 6; i++){
            registerCommand.addOption(OptionType.STRING, "player_" + i + "_discord", "Tag a player " + i + " on discord")
                            .addOption(OptionType.STRING, "player_" + i + "_in_game_name", "Provide the player " + i + " in game name");
        }
        registerCommand.queue();

        jda.upsertCommand("delete", "Use to delete previously registered game.")
                .addOption(OptionType.STRING, "url", "URL of a game.", true)
                .queue();

        jda.upsertCommand("sites", "Use to list all supported sites.")
                .queue();
    }

    @SneakyThrows
    private void registerGame(SlashCommandInteractionEvent event){
        log.info("Trying to register a game.");
        Site site = getSite(event);
        BoardGameService service = getService(site);
        URL url = validateUrlOption(service, event);
        Map<String, String> playersDiscordMapping = validatePlayerOptions(event);
        Map<String, String> playersGameMapping = getAllPlayers(service, url);
        validatePlayers(event, playersDiscordMapping, playersGameMapping);
        validateIfGameAlreadyExists(event);
        Game game = Game.builder()
                .channelId(event.getChannel().getIdLong())
                .site(site)
                .url(url)
                .playersGameMapping(playersGameMapping)
                .playersDiscordMapping(playersDiscordMapping)
                .build();
        log.info("{}", game);
        gameService.addGame(game);
        event.reply("Game has been registered!").queue();
        log.info("Game registered: {}", game);
    }

    @SneakyThrows
    private void deleteGame(SlashCommandInteractionEvent event){
        log.info("Trying to delete a game.");
        long channelId = event.getChannel().getIdLong();
        Site site = getSite(event);
        BoardGameService service = getService(site);
        URL url = validateUrlOption(service, event);
        Optional<Game> game = gameService.deleteByUrl(url, channelId);
        if(game.isEmpty()){
            event.reply("No game for a given URL has been found.").queue();
            log.info("No game found");
        } else {
            event.reply("Game has been deleted. You will no longer be notified about players' turns.").queue();
            log.info("Game deleted: {}", game.get());
        }
    }
    private Site getSite(SlashCommandInteractionEvent event){
        String urlString = event.getOption("url").getAsString();
        if(urlString.toLowerCase().contains(Site.BOARDGAMECORE.getValue().toLowerCase())) {
            return Site.BOARDGAMECORE;
        } else if(urlString.toLowerCase().contains(Site.RALLY_THE_TROOPS.getValue().toLowerCase())){
            return Site.RALLY_THE_TROOPS;
        } else {
            event.reply("This site is not supported. Maybe one day...").queue();
            throw new NoSiteException(urlString);
        }
    }

    private BoardGameService getService(Site site){
        return switch(site){
            case BOARDGAMECORE -> boardgamecoreService;
            case RALLY_THE_TROOPS -> rallythetroopsService;
        };
    }

    private URL validateUrlOption(BoardGameService service, SlashCommandInteractionEvent event){
        try {
            URL url = new URL(event.getOption("url").getAsString());
            return service.adjustUrl(url);
        } catch (MalformedURLException e) {
            event.reply("Something's wrong with your URL. Double check and try again.").queue();
            throw new RuntimeException(e);
        }
    }


    private Map<String, String> validatePlayerOptions(SlashCommandInteractionEvent event){
        StringBuilder reply = new StringBuilder();
        Map<String, String> playersMapping = new HashMap<>();
        for(int i = 1; i <= 6; i++){
            OptionMapping pDiscord = event.getOption("player_" + i + "_discord");
            OptionMapping pGame = event.getOption("player_" + i + "_in_game_name");
            if(pDiscord == null ^ pGame == null){
                reply.append("Player ").append(i).append(" has been configured incorrectly.\n");
            } else if(pDiscord != null){
                playersMapping.put(pGame.getAsString(), pDiscord.getAsString());
            }
        }
        if(!reply.isEmpty()) {
            event.reply(reply.toString()).queue();
            throw new InvalidPlayerException();
        }
        return playersMapping;
    }

    private Map<String, String> getAllPlayers(BoardGameService service, URL url){
        return service.getAllPlayers(url);
    }

    private void validatePlayers(SlashCommandInteractionEvent event, Map<String, String> playersDiscordMapping,
                                 Map<String, String> playersGameMapping){
        List<String> notFoundPlayers = new ArrayList<>();
        for(String nick: new ArrayList<>(playersDiscordMapping.keySet())){
            if(!playersGameMapping.values().contains(nick)){
                notFoundPlayers.add(nick);
            }
        }
        if(notFoundPlayers.size() > 0){
            event.reply(String.join(", ", notFoundPlayers) + " - these players could not be found. " +
                    "Probably you provided wrong names. Game has not been registered.").queue();
            throw new NotFoundPlayersOnSiteException(notFoundPlayers);
        }
    }

    private void sites(SlashCommandInteractionEvent event){
        String values = Arrays.stream(Site.values())
                        .map(Site::getValue)
                        .collect(Collectors.joining(", "));
        event.reply("Supported sites: " + values).queue();
    }

    @SneakyThrows
    private void validateIfGameAlreadyExists(SlashCommandInteractionEvent event){
        URL url = new URL(event.getOption("url").getAsString());
        long channelId = event.getChannel().getIdLong();
        Optional<Game> game = gameService.getByUrlAndChannelId(url, channelId);
        if(game.isPresent()){
            event.reply("Game with given URL is already registered on this channel.").queue();
            throw new GameAlreadyRegisteredException(url, channelId);
        }
    }
}
