package ru.sooslick.seabattle.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;
import ru.sooslick.seabattle.result.EventResult;

public class AsyncGetEventResult extends AsyncCompletionHandler<EventResult> {
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public EventResult onCompleted(Response response) {
        if (response.getStatusCode() != 200)
            return new EventResult(false).info("Wrong HTTP code " + response.getStatusCode());
        try {
            return om.readValue(response.getResponseBody(), EventResult.class);
        } catch (JsonProcessingException e) {
            return new EventResult(false).info("Response read error: " + e.getMessage());
        }
    }
}
