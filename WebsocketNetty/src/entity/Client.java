package entity;

public class Client {
    private long id;
    private int roomId;

    public Client(){
        this.id = 0L;
        this.roomId = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
