package ru.sooslick.seabattle.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;
import ru.sooslick.seabattle.result.EventResult;

public class AsyncGetToken extends AsyncCompletionHandler<Void> {
    @Override
    public Void onCompleted(Response response) {
        if (response.getStatusCode() != 200) {
            AiMain.initAiFailure(response.getStatusCode());
            return null;
        }

        ObjectMapper om = new ObjectMapper();
        try {
            EventResult er = om.readValue(response.getResponseBody(), EventResult.class);
            if (er.getSuccess())
                AiMain.initSession(er.getToken());
            else
                AiMain.initAiFailure(-2);
        } catch (JsonProcessingException e) {
            AiMain.initAiFailure(-1);
        }
        return null;
    }
}
