package com.hive.bee.msg;

import java.io.Serializable;

public class Msg implements Serializable {

    private static final long serialVersionUID = -3440720619145833133L;

    private byte[] msg = {};

    public Msg(){}

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
