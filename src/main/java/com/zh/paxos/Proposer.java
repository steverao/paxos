package com.zh.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public class Proposer {
    private static final Logger bizLogger = LoggerFactory.getLogger(PaxosNetwork.class);

    /**
     * proposal number
     */
    int lastSeq;

    int id;
    /**
     * The biggest proposal number received from acceptors
     */
    int prevN;
    /**
     * Proposal value to send.
     */
    String value;
    /**
     * Key represents the ID of acceptor.
     * Promise msg received from corresponding acceptor.
     */
    Map<Integer, Msg> acceptors;
    /**
     * The List of acceptor number in the system.
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
                continue;
            }
            switch (recv.type) {
                case PROMISE:
                    recvPromise(recv);
                    break;
                default:
                    bizLogger.info("received an unexpected Message!");
                    break;
            }
        }
        //stage 2: receive the promise and propose
        bizLogger.info("proposer: {} promise {} reached majority ", number(), getMajority());
        bizLogger.info("proposer: {} starts to propose [{}] : {}", id, number(), value);
        List<Msg> proposes = propose();
        for (Msg m : proposes) {
            pn.send(m);
        }
    }

    /**
     * A proposer chooses a new proposal number n and sends a request to
     * each member of some set of acceptors, asking it to respond with:
     * (a) A promise never again to accept a proposal numbered less than n, and
     * (b) The proposal with the highest number less than n that it has accepted, if any.
     *
     * @return
     */
    public List<Msg> prepare() {
        this.lastSeq++;
        // send promise to majority of acceptors
        int majority = getMajority();
        List<Msg> ms = new ArrayList<>(majority);
        Collections.shuffle(this.list);
        for (int i = 0; i < majority; i++) {
            Msg msg = new Msg(this.id, this.list.get(i), this.number(), 0, MessageType.PREPARE, null);
            // send msg to dest
            ms.add(msg);
        }
        return ms;
    }

    public void recvPromise(Msg m) {
        Msg msg = acceptors.get(m.from);
        if (msg.n < m.n) {
            bizLogger.info("proposer: {} receive a new promise {}", id, m);
            acceptors.put(m.from, m);
            if (m.prevN > prevN) {
                bizLogger.info("proposer updated the value [{}] to {}", value, m.content);
                prevN = m.prevN;
                value = m.content;
            }
        }
    }

    public List<Msg> propose() {
        List<Msg> msgs = new ArrayList<>();
        int cnt = 0;
        for (Msg m : acceptors.values()) {
            if (m.n == this.number()) {
                Msg mg = new Msg(id, m.from, this.number(), 0, MessageType.PROPOSE, value);
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
