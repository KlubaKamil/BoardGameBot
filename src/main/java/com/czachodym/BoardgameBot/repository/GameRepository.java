package com.czachodym.BoardgameBot.repository;

import com.czachodym.BoardgameBot.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findByUrlAndChannelId(URL url, long channelId);
}
