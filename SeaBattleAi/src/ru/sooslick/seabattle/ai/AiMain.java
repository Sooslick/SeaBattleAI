package ru.sooslick.seabattle.ai;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import ru.sooslick.seabattle.result.EventResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiMain {
    private static AsyncHttpClient client;

    public static void main(String[] args) {
        boolean create = true;
        int sessionId = -1;
        String sessionPw = null;
        String host = "localhost:8080";
        boolean useHeatMap = false;
        String heatDir = "out/workspace";
        boolean analyzePre = false;
        boolean analyzePost = true;

        // parse args
        Pattern pattern = Pattern.compile("[-]?([A-Za-z]*)(=(.*))?");
        Map<String, String> parsedArgs = new HashMap<>();
        for (String arg : args) {
            Matcher m = pattern.matcher(arg);
            if (m.matches())
                parsedArgs.put(m.group(1).toLowerCase(), m.group(3));
        }

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
            useHeatMap = true;

        //check custom data folder
        if (parsedArgs.containsKey("heatdir"))
            heatDir = parsedArgs.get("heatdir");

        //check analyze required
        if (parsedArgs.containsKey("analyze"))
            analyzePre = true;

        //check post-game analyze required
        if (parsedArgs.containsKey("ignorepost"))
            analyzePost = false;

        //check host
        if (parsedArgs.containsKey("host"))
            host = parsedArgs.get("host");

        AiHeatData.init(heatDir);
        if (analyzePre) {
            System.out.println("-analyze flag presents, update heat map");
            AiHeatData.analyze();
        }

        if (create) {
            System.out.println("sessionId not specified, ai will create new game");
        } else
            System.out.println("Selected session: " + sessionId);

        // init http client and request token
        client = Dsl.asyncHttpClient();
        BoundRequestBuilder request = client.prepareGet(host + "/api/getToken");
        ListenableFuture<EventResult> requestLf = request.execute(new AsyncGetEventResult());
        System.out.println("Request new player token");

        // get token
        EventResult lastResult = getResponse(requestLf);
        if (lastResult == null || !lastResult.getSuccess()) {
            aiShutdown(lastResult == null ? "/api/getToken not respond" : lastResult.getInfo());
            return;
        }
        String token = lastResult.getToken();
        System.out.println("Token is received // " + token);

        // request session
        if (create) {
            request = client.prepareGet(host + "/api/registerSession");
            System.out.println("Request new game session");
        } else {
            request = client.prepareGet(host + "/api/joinSession");
            request.addQueryParam("sessionId", Integer.toString(sessionId));
            System.out.println("Trying to join session " + sessionId + " with password \"" + sessionPw + "\"");
        }
        request.addQueryParam("token", token);
        if (sessionPw != null)
            request.addQueryParam("pw", sessionPw);
        requestLf = request.execute(new AsyncGetEventResult());

        // get session id if present
        lastResult = getResponse(requestLf);
        if (lastResult == null || !lastResult.getSuccess()) {
            aiShutdown(lastResult == null ? "Host does not respond" : lastResult.getInfo());
            return;
        }
        if (create) {
            if (lastResult.getSession() != null)
                sessionId = lastResult.getSession().stream().findFirst().orElse(-1);
            System.out.println("Created session // " + sessionId);
        }
        System.out.println("Successfully joined to session " + sessionId + ", waiting for start");

        // wait for start phase
        String phase = "";
        do {
            System.out.println("Sending longpoll status request...");
            request = client.prepareGet(host + "/api/longpoll/getSessionStatus");
            request.addQueryParam("token", token);
            request.addQueryParam("timeout", "55");
            requestLf = request.execute(new AsyncGetEventResult());
            lastResult = getResponse(requestLf);
            if (lastResult == null || !lastResult.getSuccess()) {
                aiShutdown(lastResult == null ? "/api/getSessionStatus not respond" : lastResult.getInfo());
                return;
            }
            if (lastResult.getGameResult() != null)
                phase = lastResult.getGameResult().getPhase();
        } while (!"PREPARE".equals(phase));
        System.out.println("Entered game phase. Generating ships");

        // place ships
        AiField myField = new AiField(useHeatMap, lastResult.getGameResult().getShips());
        AiField enemyField = new AiField(useHeatMap, lastResult.getGameResult().getShips());
        AiField.PlacePosition ppos;
        while ((ppos = myField.getPlacePosition()) != null) {
            request = client.prepareGet(host + "/api/placeShip");
            request.addQueryParam("token", token);
            request.addQueryParam("position", ppos.getPosition());
            request.addQueryParam("size", Integer.toString(ppos.getSize()));
            request.addQueryParam("vertical", Boolean.toString(ppos.isVert()));
            requestLf = request.execute(new AsyncGetEventResult());
            lastResult = getResponse(requestLf);
            if (lastResult == null) {
                aiShutdown("/api/placeShip not respond");
                return;
            }
            if (lastResult.getSuccess()) {
                myField.confirmPlace(ppos);
                System.out.println("Placed ship: " + ppos);
            } else {
                System.out.println("Try to place ship " + ppos);
                System.out.println(lastResult.getInfo());
                // todo handle severe errors (e.g. token / session expiration)
            }
        }
        System.out.println("Ships placed, check ready");

        // main loop
        do {
            System.out.println("Wait for turn...");
            do {
                request = client.prepareGet(host + "/api/longpoll/getSessionStatus");
                request.addQueryParam("token", token);
                request.addQueryParam("timeout", "55");
                requestLf = request.execute(new AsyncGetEventResult());
                lastResult = getResponse(requestLf);
                if (lastResult == null || !lastResult.getSuccess()) {
                    aiShutdown(lastResult == null ? "/api/getSessionStatus not respond" : lastResult.getInfo());
                    return;
                }
                if (lastResult.getGameResult() != null) {
                    phase = lastResult.getGameResult().getPhase();
                    if (Boolean.TRUE.equals(lastResult.getGameResult().isMyTurn()))
                        break;
                }
            } while (!"ENDGAME".equals(phase));
            if ("ENDGAME".equals(phase)) {
                System.out.println("Seems like defeat.");
                break;
            }

            // guess cell
            do {
                String shootPos = enemyField.getShootPosition();
                request = client.prepareGet(host + "/api/shoot");
                request.addQueryParam("token", token);
                request.addQueryParam("position", shootPos);
                requestLf = request.execute(new AsyncGetEventResult());
                lastResult = getResponse(requestLf);
                if (lastResult == null) {
                    aiShutdown("/api/shoot not respond");
                    return;
                }
                if (lastResult.getSuccess()) {
                    System.out.println("Guess cell " + shootPos + ", result is " + lastResult.getInfo());
                    enemyField.confirmShoot(shootPos, lastResult.getInfo());
                } else {
                    System.out.println("Try to guess cell " + shootPos);
                    System.out.println(lastResult.getInfo());
                    // todo check field if too many fails (already striked / etc)
                    // todo handle severe errors (e.g. token / session expiration)
                }
            } while ("hit".equals(lastResult.getInfo()) || "kill".equals(lastResult.getInfo()));
        } while (!"win".equals(lastResult.getInfo()));

        if (analyzePost) {
            System.out.println("Post-game action: analyze enemy field and update heat map");
            AiHeatData.analyze(lastResult.getGameResult().getEnemyField());
        }

        //exit
        aiShutdown("Done.");
    }

    private static <T> T getResponse(ListenableFuture<T> req) {
        try {
            return req.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Failed get response");
            e.printStackTrace();
            return null;
        }
    }

    private static void aiShutdown(String cause) {
        System.out.println(cause + "\nStopping...");
        try {
            client.close();
        } catch (IOException e) {
            System.out.println("HTTP client cannot stop normally");
        }
    }
}
