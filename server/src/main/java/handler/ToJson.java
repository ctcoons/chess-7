package handler;

import com.google.gson.Gson;

public class ToJson {

    private final String json;

    public ToJson(Object object) {
        Gson gson = new Gson();
        this.json = gson.toJson(object);
    }

    public String getJson() {
        return json;
    }
}
