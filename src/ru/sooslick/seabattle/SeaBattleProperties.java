package ru.sooslick.seabattle;

import com.google.common.io.Files;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SeaBattleProperties {
    public final static String APP_VERSION;

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
    public static String AI_DATA_DIR = "aiData";

    static {
        // rd version
        String tempVer = "unknown";
        try {
            Properties properties = new Properties();
            properties.load(SeaBattleProperties.class.getResourceAsStream("/version.properties"));
            tempVer = properties.getProperty("app.version", tempVer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        APP_VERSION = tempVer;
        Log.info("Running engine version " + APP_VERSION);

        // check exist or copy props
        String fname = System.getProperty("app.properties", "app.properties");
        File propsFile = new File(fname);
        if (!propsFile.exists() && !Boolean.parseBoolean(System.getProperty("use.defaults", "false"))) {
            try (InputStream is = SeaBattleProperties.class.getResourceAsStream("/app.properties")) {
                Log.info("Copying default app.properties to " + fname);
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                Files.write(bytes, propsFile);
            } catch (NullPointerException | IOException e) {
                Log.warn("Can't copy app.properties: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // rd props
        try (InputStream is = new FileInputStream(propsFile)) {
            // loading from file first
            loadProperties(is);
        } catch (IOException e) {
            Log.warn("Cannot find or read app.properties, loading default settings");
            // then try load bundled resource
            try (InputStream is = SeaBattleProperties.class.getResourceAsStream("/app.properties")) {
                loadProperties(is);
            } catch (IOException e1) {
                Log.warn("Cannot load default app.properties");
            }
        }
    }

    public static String getRules(SeaBattlePlayer requester) {
        return "SeaBattle ver " + APP_VERSION +
                "\nToken expires in " + requester.getExpiration() +
                "\n\nGame rules:" +
                "\nField size: 10x10" +
                "\nAvailable ships: ↕4x1, ↕3x2, ↕2x3, ↕1x4" +
                "\nAllow ships corner collision: " + GAME_CORNER_COLLISION_ENABLE +
                "\nPrevent shooting marked cells: " + GAME_STRIKE_CHECK_ENABLE +
                "\nMark nearby cells after kill: " + GAME_STRIKE_AFTER_KILL;
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
        AI_DATA_DIR = properties.getProperty("ai.data.dir", "aiData");
    }
    
    private static int tryParse(Properties props, String property, int dflt) {
        try {
            return Integer.parseInt(props.getProperty(property, Integer.toString(dflt)));
        } catch (NumberFormatException e) {
            return dflt;
        }
    }
}
