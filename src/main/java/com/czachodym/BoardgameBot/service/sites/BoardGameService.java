package com.czachodym.BoardgameBot.service.sites;

import java.net.URL;
import java.util.List;
import java.util.Map;


public interface BoardGameService {
    public Map getAllPlayers(URL url);
    public List<String> getActivePlayers(URL url, Map<String, String> playerGameMapping);
    public URL adjustUrl(URL url);
    public URL getNotifyUrl(URL url, Map<String, String> playerGameMapping, Map<String, String> playerDiscordMapping,
                            List<String> playersToNotify);
}
