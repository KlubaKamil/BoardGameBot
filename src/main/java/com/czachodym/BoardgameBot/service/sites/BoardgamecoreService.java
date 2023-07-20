package com.czachodym.BoardgameBot.service.sites;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BoardgamecoreService extends BoardGameWebsiteService{

    @Override
    protected List<String> findAllPlayers(String line) {
        Pattern pattern = Pattern.compile("\\$([^$@]*)@");
        Matcher matcher = pattern.matcher(line);
        return getMatches(matcher);
    }

    @Override
    protected List<String> findActivePlayers(String line) {
        Pattern pattern = Pattern.compile("global\\.currentPlayers = '(.+)'");
        Matcher matcher = pattern.matcher(line);
        List<String> matches = getMatches(matcher);
        if(matches.size() > 0) {
            return List.of(matches.get(0).split(","));
        } else {
            return matches;
        }
    }

    private List<String> getMatches(Matcher matcher){
        List<String> matches = new ArrayList<>();
        while(matcher.find()){
            String match = matcher.group(1);
            if(!matches.contains(match)) {
                matches.add(matcher.group(1));
            }
        }
        return matches;
    }
}
