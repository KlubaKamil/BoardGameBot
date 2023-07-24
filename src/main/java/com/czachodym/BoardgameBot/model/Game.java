package com.czachodym.BoardgameBot.model;

import com.czachodym.BoardgameBot.enumeration.Site;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",nullable=false,unique=true)
    private int id;
    private long channelId;
    @Column()
    private Site site;
    private URL url;
    @Builder.Default
    private List<String> currentActivePlayers = new ArrayList<>();
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_game_mapping")
    @MapKeyColumn(name = "player_game_name")
    @Column(name = "player_game_role")
    private Map<String, String> playersGameMapping = new HashMap<>();
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_discord_mapping")
    @MapKeyColumn(name = "player_game_name")
    @Column(name = "player_discord_name")
    private Map<String, String> playersDiscordMapping = new HashMap<>();
}
