package com.zh.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public class Proposer {
    private static final Logger bizLogger = LoggerFactory.getLogger(Proposer.class);


    int lastSeq;

    int id;
    /**
     * the biggest proposal number received by acceptors
     */
    int key;
    /**
     * proposal value to proposal key
     */
    String value;

//    PaxosNetwork paxosNetwork;

    Map<Integer, Msg> acceptors;
    /**
     * acceptor number
     */
    List<Integer> list;


    public Proposer(int id, String value, int... acceptors) {
        this.id = id;
        this.value = value;
        this.acceptors = new HashMap<>();
        this.list = new ArrayList<>();
        for (int acceptorId : acceptors) {
            this.acceptors.put(acceptorId, new Msg());
            this.list.add(acceptorId);
        }
    }

    public void run(PaxosNetwork pn) {
        //reach the majority
        Msg recv = null;
        while (!this.majorityReached()) {
            //stage 1: create prepared msg and send to majority in acceptors
            if (recv == null) {
                List<Msg> prepare = prepare();
                for (Msg msg : prepare) {
                    pn.send(msg);
                }
            }
            recv = pn.recv(id);
            if (recv == null) {
                //未收到消息，开始新一轮prepare
                continue;
            }
            switch (recv.type) {
                case 1:
                    recvPromise(recv);
                    break;
                default:
                    bizLogger.info("received an unexpected Message!");
                    break;
            }
        }
        //stage 2: receive the promise and propose
        List<Msg> proposes = propose();
        for (Msg m : proposes) {
            pn.send(m);
        }
    }

    // A proposer chooses a new proposal number n and sends a request to
    // each member of some set of acceptors, asking it to respond with:
    // (a) A promise never again to accept a proposal numbered less than n, and
    // (b) The proposal with the highest number less than n that it has accepted, if any.
    public List<Msg> prepare() {
        this.lastSeq++;
        //1.send promise to majority of acceptors
        int majority = getMajority();
        List<Msg> ms = new ArrayList<>(majority);
        Collections.shuffle(this.list);
        for (int i = 0; i < majority; i++) {
            Msg msg = new Msg(this.id, this.list.get(i), this.number(), 0, MessageType.PREPARE.getType(), null);
            //send msg to dest
            ms.add(msg);
        }
        return ms;
    }

    public void recvPromise(Msg m) {
        Msg msg = acceptors.get(m.from);
        if (msg.n < m.n) {
            bizLogger.info("proposer: " + id + " receive a new promise " + m);
            acceptors.put(m.from, m);
            if (m.prevN > key) {
                key = m.prevN;
                value = m.content;
                bizLogger.info("proposer updated the value to " + value);
            }
        }
    }

    public List<Msg> propose() {
        List<Msg> msgs = new ArrayList<>();
        int cnt = 0;
        for (Msg m : acceptors.values()) {
            if (m.n == this.number()) {
                Msg mg = new Msg(id, m.dest, this.number(), 0, MessageType.PROPOSE.getType(), value);
                msgs.add(mg);
                cnt++;
            }
            if (cnt == getMajority()) break;
        }
        return msgs;
    }


    public int getMajority() {
        return acceptors.size() / 2 + 1;
    }

    public boolean majorityReached() {
        int cnt = 0;
        Collection<Msg> values = acceptors.values();
        for (Msg m : values) {
            if (m.dest == id) {
                cnt++;
            }
        }
        return cnt >= getMajority();
    }

    public int number() {
        return this.lastSeq << 16 | this.id;
    }


}
