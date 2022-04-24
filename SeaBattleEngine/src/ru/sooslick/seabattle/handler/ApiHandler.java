package ru.sooslick.seabattle.handler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.sooslick.seabattle.ApiMethod;
import ru.sooslick.seabattle.EventListener;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.result.EventResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Log.info("Api method request: " + httpExchange.getHttpContext().getPath());
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
        EventResult er = new EventResult(false).info("Not supported");
        Map<String, String> getParameters = new HashMap<>();
        String query = httpExchange.getRequestURI().getQuery();
        try {
            if (query != null)
                for (String entry : query.split("&")) {
                    String[] kv = entry.split("=");
                    getParameters.put(kv[0].toLowerCase(), kv[1]);
                }
            ApiMethod method = ApiMethod.find(httpExchange.getHttpContext().getPath());
            switch (method) {
                case GET_TOKEN:
                    er = EventListener.getToken();
                    break;
                case REGISTER_SESSION:
                    er = EventListener.registerSession(getParameters.get("token"), getParameters.get("pw"));
                    break;
                case JOIN_SESSION:
                    er = EventListener.joinSession(getParameters.get("token"), getParameters.get("sessionid"), getParameters.get("pw"));
                    break;
                case GET_SESSIONS:
                    er = EventListener.getSessions(getParameters.get("token"));
                    break;
                case GET_SESSION_STATUS:
                    er = EventListener.getSessionStatus(getParameters.get("token"), getParameters.get("sessionid"));
                    break;
                case PLACE_SHIP:
                    er = EventListener.placeShip(getParameters.get("token"), getParameters.get("position"), getParameters.get("size"), getParameters.get("vertical"));
                    break;
                case SHOOT:
                    er = EventListener.shoot(getParameters.get("token"), getParameters.get("position"));
            }
        } catch (Exception e) {
            er.info(e.getMessage());
            Log.warn(e.getMessage());
            e.printStackTrace();
        }
        String answer = ow.writeValueAsString(er);
        httpExchange.sendResponseHeaders(200, answer.length());
        httpExchange.getResponseBody().write(answer.getBytes());
        httpExchange.close();
    }
}
