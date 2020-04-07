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

    int type;

    int n;
    /**
     * the accepted proposal number by the acceptor that created the msg
     */
    int prevN;
    /**
     * the accepted proposal content
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

    public Msg() {
    }

    public Msg(int type) {
        this.type = type;
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
