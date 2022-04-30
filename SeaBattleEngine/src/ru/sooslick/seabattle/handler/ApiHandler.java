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
import java.util.function.Function;

public class ApiHandler implements HttpHandler {
    private Function<Map<String, String>, EventResult> resultExtractor;

    public ApiHandler(Function<Map<String, String>, EventResult> function) {
        this.resultExtractor = function;
    }

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
        getParameters.put("path", httpExchange.getHttpContext().getPath());
        String query = httpExchange.getRequestURI().getQuery();
        try {
            if (query != null) {
                for (String entry : query.split("&")) {
                    String[] kv = entry.split("=");
                    getParameters.put(kv[0].toLowerCase(), kv[1]);
                }
            }
            EventResult extracted = resultExtractor.apply(getParameters);
            if (extracted != null)
                er = extracted;
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

    public static final Function<Map<String, String>, EventResult> LONG_POLL_STATUS = (getParameters) ->
            EventListener.getSessionStatusLongpoll(getParameters.get("token"), getParameters.get("sessionid"), getParameters.get("timeout"));

    public static final Function<Map<String, String>, EventResult> DEFAULT_API = (getParameters) -> {
        ApiMethod method = ApiMethod.find(getParameters.get("path"));
        if (method != null) {
            switch (method) {
                case GET_TOKEN:
                    return EventListener.getToken();
                case GET_RULES:
                    return EventListener.getRules(getParameters.get("token"));
                case REGISTER_SESSION:
                    return EventListener.registerSession(getParameters.get("token"), getParameters.get("pw"));
                case JOIN_SESSION:
                    return EventListener.joinSession(getParameters.get("token"), getParameters.get("sessionid"), getParameters.get("pw"));
                case GET_SESSIONS:
                    return EventListener.getSessions(getParameters.get("token"));
                case GET_SESSION_STATUS:
                    return EventListener.getSessionStatus(getParameters.get("token"), getParameters.get("sessionid"));
                case PLACE_SHIP:
                    return EventListener.placeShip(getParameters.get("token"), getParameters.get("position"), getParameters.get("size"), getParameters.get("vertical"));
                case SHOOT:
                    return EventListener.shoot(getParameters.get("token"), getParameters.get("position"));
            }
        }
        return null;
    };
}
