package ru.sooslick.seabattle.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;
import ru.sooslick.seabattle.result.EventResult;

public class AsyncGetSession extends AsyncCompletionHandler<Void> {
    @Override
    public Void onCompleted(Response response) {
        if (response.getStatusCode() != 200) {
            AiMain.initSessionFailure("Unsuccessful response code: " + response.getStatusCode());
            return null;
        }

        ObjectMapper om = new ObjectMapper();
        try {
            EventResult er = om.readValue(response.getResponseBody(), EventResult.class);
            int sid = er.getSession().stream().findFirst().orElse(-1);
            if (er.getSuccess())
                AiMain.startMainLoop(sid);
            else
                AiMain.initSessionFailure(er.getInfo());
        } catch (JsonProcessingException e) {
            AiMain.initSessionFailure(e.getMessage());
        }
        return null;
    }
}
