package ru.sooslick.seabattle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SeaBattleProperties {
    public static int SESSION_LIFETIME_TOTAL = 3000;
    public static int SESSION_LIFETIME_LOOKUP = 300;
    public static int SESSION_LIFETIME_PREPARE = 300;
    public static int SESSION_LIFETIME_PLAYER = 60;
    public static int TOKEN_LIFETIME_TOTAL = 600;

    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
            SESSION_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("session.lifetime.total", "3000"));
            SESSION_LIFETIME_LOOKUP = Integer.parseInt(properties.getProperty("session.lifetime.lookup", "300"));
            SESSION_LIFETIME_PREPARE = Integer.parseInt(properties.getProperty("session.lifetime.prepare", "300"));
            SESSION_LIFETIME_PLAYER = Integer.parseInt(properties.getProperty("session.lifetime.player", "60"));
            TOKEN_LIFETIME_TOTAL = Integer.parseInt(properties.getProperty("token.lifetime.total", "600"));
        } catch (FileNotFoundException e) {
            Logger.info("Cannot find app.properties, loading default settings");
        } catch (IOException e) {
            Logger.info("Cannot read app.properties, loading deafult settings");
        }
    }
}
