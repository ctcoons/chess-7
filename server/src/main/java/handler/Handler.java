package handler;


@FunctionalInterface
interface Handler {
    String handle(String req, String res);
}
