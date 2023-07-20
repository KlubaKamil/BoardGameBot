package com.czachodym.BoardgameBot.service;

import com.czachodym.BoardgameBot.enumeration.Site;
import com.czachodym.BoardgameBot.exception.GameAlreadyRegisteredException;
import com.czachodym.BoardgameBot.exception.InvalidPlayerException;
import com.czachodym.BoardgameBot.exception.NoSiteException;
import com.czachodym.BoardgameBot.exception.NotFoundPlayersOnSiteException;
import com.czachodym.BoardgameBot.model.Game;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DiscordService extends ListenerAdapter {
    private final GameService gameService;
    private final JDA jda;

    public DiscordService(GameService gameService, JDA jda){
        this.gameService = gameService;

        this.jda = jda;
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

    private void registerGame(SlashCommandInteractionEvent event){
        log.info("Trying to register a game.");
        Site site = validateUrlOption(event);
        Map<String, String> playersMapping = validatePlayerOptions(event);
        validatePlayersOnSite(event, site, playersMapping);
        validateIfGameAlreadyExists(event);
        String url = event.getOption("url").getAsString();

        Game game = Game.builder()
                .channelId(event.getChannel().getIdLong())
                .site(site)
                .url(url)
                .playersMapping(playersMapping)
                .build();
        gameService.addGame(game);
        event.reply("Game has been registered!").queue();
        log.info("Game registered: {}", game);
    }

    private void deleteGame(SlashCommandInteractionEvent event){
        log.info("Trying to delete a game.");
        long channelId = event.getChannel().getIdLong();
        String url = event.getOption("url").getAsString();
        Optional<Game> game = gameService.deleteByUrl(url, channelId);
        if(game.isEmpty()){
            event.reply("No game for a given URL has been found.").queue();
            log.info("No game found");
        } else {
            event.reply("Game has been deleted. You will no longer be notified about players' turns.").queue();
            log.info("Game deleted: {}", game.get());
        }
    }

    private Site validateUrlOption(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        if(url.toLowerCase().contains(Site.BOARDGAMECORE.toString().toLowerCase())){
            return Site.BOARDGAMECORE;
        } else {
            event.reply("This site is not supported. Maybe one day...").queue();
            throw new NoSiteException(url);
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

    private void validatePlayersOnSite(SlashCommandInteractionEvent event, Site site,
                                               Map<String, String> playersMapping){
        String url = event.getOption("url").getAsString();
        List<String> notFoundPlayers = gameService.validatePlayers(url, site, playersMapping);
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

    private void validateIfGameAlreadyExists(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        long channelId = event.getChannel().getIdLong();
        Optional<Game> game = gameService.getByUrlAndChannelId(url, channelId);
        if(game.isPresent()){
            event.reply("Game with given URL is already registered on this channel.").queue();
            throw new GameAlreadyRegisteredException(url, channelId);
        }
    }
}
