package com.zh.paxos;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public enum MessageType {
    PREPARE(0, "Prepare"),
    PROMISE(1,"Promise"),
    PROPOSE(2, "Propose"),
    ACCEPT(3,"Accept");


    private int type;

    private String desc;

    MessageType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
