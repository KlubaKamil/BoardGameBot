package com.czachodym.BoardgameBot.service.sites;

import com.czachodym.BoardgameBot.exception.NoPlayersFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;

@Service
@Slf4j
public class RallythetroopsService implements BoardGameService {
    @Override
    public Map<String, String> getAllPlayers(URL url) {
        try {
            Map<String, String> players = new HashMap<>();
            List<String> messages = getMessages(url);
            ObjectMapper mapper = new ObjectMapper();
            Object[] array = mapper.readValue(messages.get(0), Object[].class);
            List<Object> list = (ArrayList<Object>) array[1];
            list = List.of(list.get(1));
            List<LinkedHashMap<String, String>> map = (List<LinkedHashMap<String, String>>) list.get(0);

            for (LinkedHashMap<String, String> o : map) {
                players.put(o.get("role"), o.get("name"));
            }
            return players;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoPlayersFoundException();
    }

    @Override
    public List<String> getActivePlayers(URL url, Map<String, String> playerGameMapping) {
        try {
            List<String> players = new ArrayList<>();
            List<String> messages = getMessages(url);
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Object> array = mapper.readValue(messages.get(3), ArrayList.class);
            LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) array.get(1);
            players.add(playerGameMapping.get(map.get("active")));
            return players;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoPlayersFoundException();
    }

    @SneakyThrows
    @Override
    public URL adjustUrl(URL url) {
        String protocol = url.getProtocol();
        String host = url.getHost();
        String path = url.getPath();
        String query = url.getQuery();

        return new URL(protocol + "://" + host + path + "?" + query.split("&")[0]);
    }

    @SneakyThrows
    @Override
    public URL getNotifyUrl(URL url, Map<String, String> playerGameMapping, Map<String, String> playerDiscordMapping,
                            List<String> playersToNotify) {
        String role = null;
        for(Map.Entry<String, String> entry: playerDiscordMapping.entrySet()){
            if(playersToNotify.contains(entry.getValue())){
                role = entry.getKey();
                break;
            }
        }
        for(Map.Entry<String, String> entry: playerGameMapping.entrySet()){
            if(entry.getValue().equals(role)){
                role = entry.getKey();
                break;
            }
        }
        return new URL(url + "&role=" + role);
    }

    @SneakyThrows
    private List<String> getMessages(URL url){
        String gameId = url.getQuery().split("&")[0].split("=")[1];
        String gameTitle = url.getPath().split("/")[1];
        List<String> messages = new ArrayList<>();
        WebSocket websocket = new WebSocketFactory()
                .createSocket("wss://rally-the-troops.com/play-socket?title=" + gameTitle + "&game="
                        + gameId + "&role=Observer&seen=0")
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket ws, String message) {
                        messages.add(message);
                    }
                })
                .connect();
        Thread.sleep(100);
        websocket.disconnect();
        websocket.sendClose();
        return messages;
    }
}
