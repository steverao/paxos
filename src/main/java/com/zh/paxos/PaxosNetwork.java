package com.zh.paxos;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author raozihao
 * @date 2020/4/5
 */

class Pipe {
    private static final Logger bizLogger = LoggerFactory.getLogger(PaxosNetwork.class);

    BlockingQueue<Msg> queue;

    public Pipe() {
        queue = new LinkedBlockingQueue<>(1);
    }


    public void send(Msg msg) {
        try {
            queue.put(msg);
        } catch (Exception e) {
            bizLogger.info("send failed", e);
        }
    }

    public Msg recv() {
        Msg msg = null;
        try {
            msg = queue.take();
        } catch (Exception e) {
            bizLogger.error("recv failed", e);
        }
        return msg;
    }
}

public class PaxosNetwork {

    private static final Logger bizLogger = LoggerFactory.getLogger(PaxosNetwork.class);

    Map<Integer, Pipe> recvQueue = new HashMap<>();

    public PaxosNetwork(int... roles) {
        for (int roleId : roles) {
            recvQueue.put(roleId, new Pipe());
        }
    }


    public void send(Msg msg) {
        Pipe pipe = recvQueue.get(msg.dest);
        bizLogger.info("nt: send " + msg);
        pipe.send(msg);
    }

    public Msg recv(int id) {
        Pipe pipe = recvQueue.get(id);
        Msg recv = pipe.recv();
        return recv;
    }
}
