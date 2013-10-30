package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.model.Event;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <pp>Events</pp> API methods
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class EventsApi {

    private final OdklApi api;

    public EventsApi(OdklApi api) {
        this.api = api;
    }

    /**
     * Returns events for current user: activities, marks, guests, messages, notifications, discussions,
     * application events.
     * @return list of {@link Event} for current user
     * @throws com.github.mastersobg.odkl.exception.OdklApiException if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/events.get">http://apiok.ru/wiki/display/api/events.get</a>
     */
    public List<Event> get() {
        OdklRequest request = api.createApiRequest("events", "get");

        JSONArray json = JsonUtil.parseArray(api.sendRequest(request));
        List<Event> result = new ArrayList<Event>(json.size());
        for (Object o : json) {
            result.add(new Event((JSONObject) o));
        }

        return result;
    }


}
