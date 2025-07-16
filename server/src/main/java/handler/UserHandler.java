package handler;

import model.RegisterRequest;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {


    public UserHandler() {

    }


    public Object register(Request request, Response response) {
        String body = request.body();
        return "Hello";
    }
}
