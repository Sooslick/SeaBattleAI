package ru.sooslick.seabattle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SeaBattleProperties {
    public static int APP_SERVER_PORT = 65535;
    public static int APP_SERVER_CONNECTIONS = 16;
    public static int APP_CLEANUP_INTERVAL = 90;
    public static int SESSION_LIFETIME_TOTAL = 3000;
    public static int SESSION_LIFETIME_LOOKUP = 300;
    public static int SESSION_LIFETIME_PREPARE = 300;
    public static int SESSION_LIFETIME_PLAYER = 60;
    public static int TOKEN_LIFETIME_TOTAL = 600;
    public static boolean GAME_CORNER_COLLISION_ENABLE = false;
    public static boolean GAME_STRIKE_CHECK_ENABLE = true;
    public static boolean GAME_STRIKE_AFTER_KILL = true;

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
        APP_SERVER_PORT = tryParse(properties, "app.server.port", 65535);
        APP_SERVER_CONNECTIONS = tryParse(properties, "app.server.connections", 16);
        APP_CLEANUP_INTERVAL = tryParse(properties, "app.cleanup.interval", 90);
        SESSION_LIFETIME_TOTAL = tryParse(properties, "session.lifetime.total", 3000);
        SESSION_LIFETIME_LOOKUP = tryParse(properties, "session.lifetime.lookup", 300);
        SESSION_LIFETIME_PREPARE = tryParse(properties, "session.lifetime.prepare", 300);
        SESSION_LIFETIME_PLAYER = tryParse(properties, "session.lifetime.player", 60);
        TOKEN_LIFETIME_TOTAL = tryParse(properties, "token.lifetime.total", 600);
        GAME_CORNER_COLLISION_ENABLE = Boolean.parseBoolean(properties.getProperty("game.corner.collision.enable", "false"));
        GAME_STRIKE_CHECK_ENABLE = Boolean.parseBoolean(properties.getProperty("game.strike.check.enable", "true"));
        GAME_STRIKE_AFTER_KILL = Boolean.parseBoolean(properties.getProperty("game.strike.after.kill", "true"));
    }
    
    private static int tryParse(Properties props, String property, int dflt) {
        try {
            return Integer.parseInt(props.getProperty(property, Integer.toString(dflt)));
        } catch (NumberFormatException e) {
            return dflt;
        }
    }
}
