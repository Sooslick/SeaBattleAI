package ru.sooslick.seabattle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        String fname = System.getProperty("app.properties", "app.properties");
        try {
            // loading from file first
            loadProperties(new FileInputStream(fname));
        } catch (IOException | NumberFormatException e) {
            Log.warn("Cannot find or read app.properties, loading default settings");
            // then try load bundled resource
            try {
                loadProperties(SeaBattleProperties.class.getResourceAsStream("/app.properties"));
            } catch (IOException | NumberFormatException e1) {
                Log.warn("Cannot load default app.properties");
            }
        }
    }

    private static void loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        APP_SERVER_PORT = Integer.parseInt(properties.getProperty("app.server.port", "65535"));
        APP_SERVER_CONNECTIONS = Integer.parseInt(properties.getProperty("app.server.connections", "16"));
        SESSION_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("session.lifetime.total", "3000"));
        SESSION_LIFETIME_LOOKUP = Integer.parseInt(properties.getProperty("session.lifetime.lookup", "300"));
        SESSION_LIFETIME_PREPARE = Integer.parseInt(properties.getProperty("session.lifetime.prepare", "300"));
        SESSION_LIFETIME_PLAYER = Integer.parseInt(properties.getProperty("session.lifetime.player", "60"));
        TOKEN_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("token.lifetime.total", "600"));
        GAME_CORNER_COLLISION_ENABLE = Boolean.parseBoolean(properties.getProperty("game.corner.collision.enable", "false"));
        GAME_STRIKED_CHECK_ENABLE = Boolean.parseBoolean(properties.getProperty("game.striked.check.enable", "true"));
        // todo autostrike after kill setting
    }
}
