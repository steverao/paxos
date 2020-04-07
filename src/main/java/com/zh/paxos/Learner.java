package com.zh.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public class Learner {

    private static final Logger bizLogger = LoggerFactory.getLogger(Learner.class);

    int id;

    Map<Integer, Msg> accepts;


    public Learner(int id,int... acceptor) {
        this.id=id;
        accepts = new HashMap<>();
        for (int a : acceptor) {
            accepts.put(a, new Msg(MessageType.ACCEPT.getType()));
        }
    }

    public Msg run(PaxosNetwork pn) {
        while (true) {
            Msg recv = pn.recv(id);
            if (recv == null) continue;

            receiveAccept(recv);
            Msg chosen = chosen();
            if (chosen == null) continue;
            bizLogger.info("learner: " + id + " learned the chosen propose " + chosen);
            return chosen;
        }
    }

    public void receiveAccept(Msg recv) {
        Msg msg = accepts.get(recv.from);
        if (msg.prevN < recv.n) {
            bizLogger.info("learner: " + id + " received a new accepted proposal " + recv);
            accepts.put(recv.from, recv);
        }
    }

    public int majority() {
        return accepts.size() / 2 + 1;
    }

    public Msg chosen() {
        Map<Integer, Integer> count = new HashMap<>();
        Map<Integer, Msg> accepts = new HashMap<>();
        Collection<Msg> values = accepts.values();
        for (Msg m : values) {
            if (count.containsKey(m.n)) {
                count.put(m.n, count.get(m.n) + 1);
                accepts.put(m.n, m);
            } else {
                count.put(m.n, 1);
            }
        }
        Set<Integer> keys = count.keySet();
        Iterator<Integer> it = keys.iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            if (count.get(key) >= majority()) {
                return accepts.get(key);
            }
        }
        return null;
    }


}
