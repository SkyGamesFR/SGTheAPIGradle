package fr.skygames.sgtheapi.api.data;

public class Discord {

    private String uuid;
    private String token;
    private String id;

    public Discord(String uuid, String token, String id) {
        this.uuid = uuid;
        this.token = token;
        this.id = id;
    }

    public Discord(String uuid, String token) {this(uuid, token, null);}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
