package ru.sooslick.seabattle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SeaBattleProperties {
    public static int APP_SERVER_PORT = 65535;
    public static int APP_SERVER_CONNECTIONS = 16;
    public static int SESSION_LIFETIME_TOTAL = 3000;
    public static int SESSION_LIFETIME_LOOKUP = 300;
    public static int SESSION_LIFETIME_PREPARE = 300;
    public static int SESSION_LIFETIME_PLAYER = 60;
    public static int TOKEN_LIFETIME_TOTAL = 600;
    public static boolean GAME_CORNER_COLLISION_ENABLE = false;
    public static boolean GAME_STRIKED_CHECK_ENABLE = true;

    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
            APP_SERVER_PORT = Integer.parseInt(properties.getProperty("app.server.port", "65535"));
            APP_SERVER_CONNECTIONS = Integer.parseInt(properties.getProperty("app.server.connections", "16"));
            SESSION_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("session.lifetime.total", "3000"));
            SESSION_LIFETIME_LOOKUP = Integer.parseInt(properties.getProperty("session.lifetime.lookup", "300"));
            SESSION_LIFETIME_PREPARE = Integer.parseInt(properties.getProperty("session.lifetime.prepare", "300"));
            SESSION_LIFETIME_PLAYER = Integer.parseInt(properties.getProperty("session.lifetime.player", "60"));
            TOKEN_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("token.lifetime.total", "600"));
            GAME_CORNER_COLLISION_ENABLE = Boolean.parseBoolean(properties.getProperty("game.corner.collision.enable", "false"));
            GAME_STRIKED_CHECK_ENABLE = Boolean.parseBoolean(properties.getProperty("game.striked.check.enable", "true"));
        } catch (FileNotFoundException e) {
            Logger.info("Cannot find app.properties, loading default settings");
        } catch (IOException e) {
            Logger.info("Cannot read app.properties, loading deafult settings");
        }
    }
}
