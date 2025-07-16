package server;

import com.google.gson.Gson;

public class FromJson {

    private final String json;

    public FromJson(String json) {
        this.json = json;
    }


    public <T> Object toObject(Class<T> myClass){
        Gson gson = new Gson();
        return gson.fromJson(json, myClass);
    }

}
