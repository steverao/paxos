package com.zh.paxos;

import java.io.Serializable;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public class Msg implements Serializable {

    private static final long serialVersionUID = 1L;

    int from;

    int dest;
    /**
     * Message type.
     */
    int type;
    /**
     * Message number.
     */
    int n;
    /**
     * The accepted proposal number by the acceptor that created the msg
     */
    int prevN;
    /**
     * Message content.
     */
    String content;

    public Msg(int from, int dest, int n, int prevN, int type, String content) {
        this.from = from;
        this.dest = dest;
        this.n = n;
        this.prevN = prevN;
        this.type = type;
        this.content = content;
    }

    public Msg(Msg msg) {
        this.from = msg.from;
        this.dest = msg.dest;
        this.n = msg.n;
        this.prevN = msg.prevN;
        this.type = msg.type;
        this.content = msg.content;
    }


    public Msg() {
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getPrevN() {
        return prevN;
    }

    public void setPrevN(int prevN) {
        this.prevN = prevN;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "from=" + from +
                ", dest=" + dest +
                ", type=" + type +
                ", n=" + n +
                ", prevN=" + prevN +
                ", content='" + content + '\'' +
                '}';
    }
}
