package com.czachodym.BoardgameBot.service.sites;

import com.czachodym.BoardgameBot.exception.NoActivePlayersException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BoardgamecoreService implements BoardGameService {
    private final WebClient webClient = new WebClient();

    @Override
    public Map<String, String> getAllPlayers(URL url) {
        try {
            HtmlPage page = webClient.getPage(url);
            DomElement playersDiv = page.getElementById("players");
            Iterable<DomNode> playersNodeIterable = playersDiv.getChildren();
            Map<String, String> players = new HashMap<>();
            for (DomNode playerNode : playersNodeIterable) {
                String nick = playerNode.getChildNodes().get(2).getTextContent();
                players.put(nick, nick);
            }
            return players;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NoActivePlayersException(url);
    }

    @Override
    public List<String> getActivePlayers(URL url, Map<String, String> playerGameMapping) {
        try {
            HtmlPage page = webClient.getPage(url);
            DomElement playersDiv = page.getElementById("players");
            Iterable<DomNode> playersNodeIterable = playersDiv.getChildren();
            List<String> players = new ArrayList<>();
            for (DomNode playerNode : playersNodeIterable) {
                if(playerNode.toString().contains("player active")) {
                    players.add(playerNode.getChildNodes().get(2).getTextContent());
                }
            }
            return players;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NoActivePlayersException(url);
    }

    @Override
    public URL adjustUrl(URL url) {
        return url;
    }

    @Override
    public URL getNotifyUrl(URL url, Map<String, String> playerGameMapping, Map<String, String> playerDiscordMapping,
                            List<String> playersToNotify) {
        return url;
    }


}
