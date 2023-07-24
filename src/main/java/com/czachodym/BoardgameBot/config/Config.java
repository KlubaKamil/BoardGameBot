package com.czachodym.BoardgameBot.config;

import com.czachodym.BoardgameBot.service.GameService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class Config {
    @Value("${bot.token}")
    private String token;

    @Autowired
    @Lazy
    private GameService gameService;

    private final ScheduledExecutorService checkGamesService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> checkGamesServiceResult;
    private Runnable runnable;

    @Bean
    public JDA jda() throws InterruptedException {
        return JDABuilder
                .createDefault(token)
                .build()
                .awaitReady();
    }

    @PostConstruct
    private void runCheckGamesService(){
        runnable = () -> {
            gameService.checkGames();
            checkGamesServiceResult = checkGamesService.schedule(runnable, 20, TimeUnit.SECONDS);
        };
        checkGamesServiceResult = checkGamesService.schedule(runnable, 20, TimeUnit.SECONDS);
        log.info("runCheckGamesService started.");
    }

    @PreDestroy
    private void killCheckGamesServiceOnAppExit(){
        checkGamesServiceResult.cancel(false);
        log.info("runCheckGamesService killed.");
    }
}
