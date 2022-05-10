package ru.sooslick.seabattle.ai;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.result.EventResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiMain {
    private AsyncHttpClient client;

    public static void main(String[] args) {
        new AiMain().run(args);
    }

    public void run(String @NotNull [] args) {
        boolean create = true;
        int sessionId = -1;
        String sessionPw = null;
        String host = "localhost:65535";
        boolean useHeatMap = false;
        String heatDir = "aiData";
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
        if (parsedArgs.containsKey("useheatmap")) {
            Log.info("Using heat map");
            useHeatMap = true;
        }

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
        if (parsedArgs.containsKey("host")) {
            host = parsedArgs.get("host");
            if (host.length() > 0 && host.charAt(host.length() - 1) == '/')
                host = host.substring(0, host.length() - 1);
        }

        AiHeatData.init(heatDir);
        if (analyzePre) {
            Log.info("-analyze flag presents, update heat map");
            AiHeatData.analyze();
        }

        if (create) {
            Log.info("sessionId not specified, ai will create new game");
        } else
            Log.info("Selected session: " + sessionId);

        // init http client and request token
        client = Dsl.asyncHttpClient();
        BoundRequestBuilder request = client.prepareGet(host + "/api/getToken");
        ListenableFuture<EventResult> requestLf = request.execute(new AsyncGetEventResult());
        Log.info("Request new player token");

        // get token
        EventResult lastResult = getResponse(requestLf);
        if (lastResult == null || !lastResult.getSuccess()) {
            aiShutdown(lastResult == null ? "/api/getToken not respond" : lastResult.getInfo());
            return;
        }
        String token = lastResult.getToken();
        Log.info("Token is received // " + token);

        // request session
        if (create) {
            request = client.prepareGet(host + "/api/registerSession");
            Log.info("Request new game session");
        } else {
            request = client.prepareGet(host + "/api/joinSession");
            request.addQueryParam("sessionId", Integer.toString(sessionId));
            Log.info("Trying to join session " + sessionId + " with password \"" + sessionPw + "\"");
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
            if (lastResult.getSessionInfos() != null)
                sessionId = lastResult.getSessionInfos().stream().map(EventResult.SessionInfo::getSessionId).findFirst().orElse(-1);
            Log.info("Created session // " + sessionId);
        }
        Log.info("Successfully joined to session " + sessionId + ", waiting for start");

        // wait for start phase
        String phase = "";
        do {
            Log.info("Sending longpoll status request...");
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
        Log.info("Entered game phase. Generating ships");

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
                Log.info("Placed ship: " + ppos);
            } else {
                Log.info("Tried place ship " + ppos);
                if ("Can't placeShip: unknown or expired token".equals(lastResult.getInfo())) {
                    aiShutdown(lastResult.getInfo());
                    return;
                }
                Log.info(lastResult.getInfo());
            }
        }
        if (myField.ships.size() > 0) {
            aiShutdown("Failed place ships");
            return;
        }
        Log.info("Ships placed, check ready");

        // main loop
        do {
            Log.info("Wait for turn...");
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
                Log.info("Seems like defeat.");
                break;
            }

            // guess cell
            int failures = 0;
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
                    Log.info("Guess cell " + shootPos + ", result is " + lastResult.getInfo());
                    enemyField.confirmShoot(shootPos, lastResult.getInfo());
                } else {
                    Log.info("Tried to guess cell " + shootPos);
                    if ("Can't shoot: unknown or expired token".equals(lastResult.getInfo())) {
                        aiShutdown(lastResult.getInfo());
                        return;
                    }
                    Log.info(lastResult.getInfo());
                    if (++failures > 10) {
                        aiShutdown("Too many failed shoot attempts");
                        return;
                    }
                }
            } while ("hit".equals(lastResult.getInfo()) || "kill".equals(lastResult.getInfo()));
        } while (!"win".equals(lastResult.getInfo()));

        if (analyzePost) {
            Log.info("Post-game action: analyze enemy field and update heat map");
            AiHeatData.analyze(lastResult.getGameResult().getEnemyField());
        }

        //exit
        aiShutdown("Done.");
    }

    private static <T> T getResponse(ListenableFuture<T> req) {
        try {
            return req.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.info("Failed get response");
            e.printStackTrace();
            return null;
        }
    }

    private void aiShutdown(String cause) {
        Log.info(cause + "\nAI thread finished");
        try {
            client.close();
        } catch (IOException e) {
            Log.info("HTTP client cannot stop normally");
        }
    }
}
