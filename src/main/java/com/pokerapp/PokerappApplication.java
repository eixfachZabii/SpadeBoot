package com.pokerapp;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.PrintStream;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

    private static final Logger logger = Logger.getLogger(PokerappApplication.class.getName());
    private static boolean artDisplayed = false;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PokerappApplication.class);
        app.setBanner(new SpadeArtBanner());
        app.run(args);
    }

    static class SpadeArtBanner implements Banner {
        @Override
        public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
            if (!artDisplayed) {
                out.println("""
                      /$$$$$$  /$$$$$$$   /$$$$$$  /$$$$$$$  /$$$$$$$$
                     /$$__  $$| $$__  $$ /$$__  $$| $$__  $$| $$_____/
                    | $$  \\__/| $$  \\ $$| $$  \\ $$| $$  \\ $$| $$      
                    |  $$$$$$ | $$$$$$$/| $$$$$$$$| $$  | $$| $$$$$   
                     \\____  $$| $$____/ | $$__  $$| $$  | $$| $$__/   
                     /$$  \\ $$| $$      | $$  | $$| $$  | $$| $$      
                    |  $$$$$$/| $$      | $$  | $$| $$$$$$$/| $$$$$$$$
                     \\______/ |__/      |__/  |__/|_______/ |________/

        ---------- # ------------------------------------------------------------------
        --------- ##= ------------------------------ Exmatrikulation ------------------
        -------- ##=== ------------------ Luca, Markus, Sebi, Jonas, Paul, Matthi -----
        ------ ###==#=== --------------------------------------------------------------
        ---- ####===##==== ------------------------------------------------------------
        -- #####====###===== ------      "My name is Spade...                     -----
        - #####=====####===== -----       I am you AI Poker Dealer...             -----
        - #####=====####===== -----       Prepare to play!"                       -----
        --- ####=  #  #==== ------- - My little programmer (Markus Huber)         -----
        --------- ##= -----------------------------------------------------------------
        ------- ####=== ---------------------------------------------------------------                              
                """);
                artDisplayed = true;
            }
        }
    }
}