package com.zh.paxos;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author raozihao
 * @date 2020/4/5
 */
interface Network {
    void send(Msg msg);

    Msg recv();
}

class Pipe implements Network {
    private static final Logger bizLogger = LoggerFactory.getLogger(Pipe.class);
    PipedOutputStream write;
    PipedInputStream read;


    public Pipe() {
        try {
            write = new PipedOutputStream();
            read = new PipedInputStream(write, 1024);
        } catch (IOException e) {
            bizLogger.info("Pipe初始化失败");
        }
    }

    @Override
    public void send(Msg msg) {
        try {
            write.flush();
            ObjectOutputStream oos = new ObjectOutputStream(write);
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            bizLogger.info("send failed", e);
        }
    }

    @Override
    public Msg recv() {
        byte[] bytes = new byte[1024];
        Msg msg = null;
        try {
            int length = this.read.read(bytes);
            if (length != -1) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                msg = (Msg) objectInputStream.readObject();
                bizLogger.info("nt: recv " + msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            bizLogger.error("recv failed", e);
        }
        return msg;
    }
}

public class PaxosNetwork {

    private static final Logger bizLogger = LoggerFactory.getLogger(PaxosNetwork.class);


    Map<Integer, Pipe> recvQueue = new HashMap<>();

    public PaxosNetwork(int... acceptors) {
        for (int acceptor : acceptors) {
            recvQueue.put(acceptor, new Pipe());
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
