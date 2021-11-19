package service;

import entity.Client;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestService {

    public static Client registerClient(String request) throws ParseException {
        String res = new String(Base64.decodeBase64(request));
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(res);

        Client client = new Client();

        if(json.get("rid") == null){
            return client;
        }

        client.setRoomId((Integer) json.get("rid"));

        if(json.get("id") == null || json.get("token") == null){
            return client;
        }

        Long id = (Long) json.get("id");
        String token = (String) json.get("token");

        if(!checkToken(id, token)){
            return client;
        }

        client.setId(id);

        return client;
    }

    private static boolean checkToken(Long id, String token) {
        return true;
    }
}
