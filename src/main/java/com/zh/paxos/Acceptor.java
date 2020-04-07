package com.zh.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author raozihao
 * @date 2020/4/5
 */
// P1. An acceptor must accept the first proposal that it receives.
// If a proposal with value v is chosen, then every higher-numbered proposal
// accepted by any acceptor has value v.
public class Acceptor {

    private static final Logger bizLogger = LoggerFactory.getLogger(Acceptor.class);


    int id;
    /**
     * the proposal accepted by the acceptor
     */
    Msg accept;
    /**
     * the biggest number promised by the acceptor
     */
    int promised;

//    PaxosNetwork paxosNetwork;

    int[] learners;

    public Acceptor(int id, int... learners) {
        this.id = id;
        int[] ls = new int[learners.length];
        int cnt = 0;
        for (int l : learners) {
            ls[cnt++] = l;
        }
        this.learners = ls;
    }


    public void run(PaxosNetwork pn) {
        while (true) {
            Msg recv = pn.recv(id);
            if (recv == null) continue;
            switch (recv.type) {
                //prepare
                case 0:
                    Msg promise = receivePrepare(recv);
                    if (promise != null)
                        pn.send(promise);
                    break;
                //propose
                case 2:
                    boolean accepted = receivePropose(recv);
                    //send msg to learners
                    if (accepted)
                        for (int learner : learners) {
                            Msg accept = this.accept;
                            accept.dest = learner;
                            accept.from = id;
                            pn.send(accept);
                        }
                    break;
            }
        }
    }


    // If an acceptor receives an accept request for a proposal numbered
    // n, it accepts the proposal unless it has already responded to a prepare
    // request having a number greater than n.
    public boolean receivePropose(Msg recv) {
        Msg msg;
        if (promised > recv.n) {
            bizLogger.info("acceptor: " + id + " [promised: " + accept + "] ignored proposal " + recv);
            return false;
        }
        if (promised < recv.n) {
            bizLogger.info("acceptor: " + id + " received unexpected proposal " + recv);
        }
        this.accept = recv;
        this.accept = new Msg(MessageType.ACCEPT.getType());
        return true;
    }

    // If an acceptor receives a prepare request with number n greater
    // than that of any prepare request to which it has already responded,
    // then it responds to the request with a promise not to accept any more
    // proposals numbered less than n and with the highest-numbered proposal
    // (if any) that it has accepted.
    public Msg receivePrepare(Msg recv) {
        Msg msg = null;
        if (promised >= recv.n) {
            bizLogger.info("acceptor: " + id + " abort a message!");
        } else {
            promised = recv.n;
            if (accept != null) {
                msg = new Msg(id, recv.from, recv.n, accept.n, MessageType.PROMISE.getType(), accept.content);
                bizLogger.info("acceptor: " + id + " [promised: " + accept + "] promised " + msg);
            } else {
                msg = new Msg(id, recv.from, recv.n, 0, MessageType.PROMISE.getType(), null);
                bizLogger.info("acceptor: " + id + " promised " + msg);
            }
        }
        return msg;
    }
}
