package ru.sooslick.seabattle.ai;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiMain {
    private static boolean create = true;
    private static int sessionId = 0;
    private static String sessionPw = "";
    private static String host = "localhost:8080";
    private static boolean useHeatMap = false;
    private static AsyncHttpClient client;
    private static String token;

    public static void main(String[] args) {
        Map<String, String> parsedArgs = mapArgs(args);

        //check sessionId
        if (parsedArgs.containsKey("sessionid")) {
            sessionId = Integer.parseInt(parsedArgs.get("sessionid"));
            create = false;
        }

        //check sessionPw
        if (parsedArgs.containsKey("sessionpw"))
            sessionPw = parsedArgs.get("sessionpw");

        //check ai type
        if (parsedArgs.containsKey("useheatmap"))
            useHeatMap = Boolean.parseBoolean(parsedArgs.get("useheatmap"));

        //check host
        if (parsedArgs.containsKey("host"))
            host = parsedArgs.get("host");

        if (create) {
            System.out.println("sessionId not specified, ai will create new game");
        } else
            System.out.println("Selected session: " + sessionId);

        initAi();
    }

    private static Map<String, String> mapArgs(String[] args) {
        Pattern pattern = Pattern.compile("[-]?([A-Za-z]*)=(.*)");
        Map<String, String> returnMap = new HashMap<>();
        for (String arg : args) {
            Matcher m = pattern.matcher(arg);
            if (m.matches())
                returnMap.put(m.group(1).toLowerCase(), m.group(2));
        }
        return returnMap;
    }

    private static void initAi() {
        client = Dsl.asyncHttpClient();
        BoundRequestBuilder request = client.prepareGet(host + "/api/getToken");
        request.execute(new AsyncGetToken());
        System.out.println("Request new player token");
    }

    public static void initAiFailure(int code) {
        switch (code) {
            case -2: System.out.println("AI init failed: getToken response success is false"); break;
            case -1: System.out.println("AI init failed: can't parse response"); break;
            default: System.out.println("AI init failed: getToken response code is " + code);
        }
        stopClient();
    }

    public static void initSession(String token) {
        AiMain.token = token;
        System.out.println("Token is received // " + token);
        if (create) {
            BoundRequestBuilder request = client.prepareGet(host + "/api/registerSession");
            request.addFormParam("token", token);
            if (!sessionPw.isEmpty())
                request.addFormParam("pw", sessionPw);
            request.execute(new AsyncGetSession());
            System.out.println("Request new game session");
        } else {
            BoundRequestBuilder request = client.prepareGet(host + "/api/joinSession");
            request.addFormParam("token", token);
            request.addFormParam("sessionId", Integer.toString(sessionId));
            if (!sessionPw.isEmpty())
                request.addFormParam("pw", sessionPw);
            request.execute(new AsyncGetSession());
            System.out.println("Trying to join session " + sessionId + " with password \"" + sessionPw + "\"");
        }
    }

    public static void initSessionFailure(String info) {
        System.out.println(info);
        stopClient();
    }

    public static void startMainLoop(int sid) {
        if (create)
            System.out.println("Created session id " + sid);
        System.out.println("Init complete, entering main loop");
        System.out.println("not implemented...");
        //todo
    }

    private static void stopClient() {
        try {
            client.close();
        } catch (IOException e) {
            System.out.println("Failed stopping HTTP client...");
            System.out.println(e.getMessage());
        }
    }

    //todo queries retries
    // + replace async calls with sync?
}
