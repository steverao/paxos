package com.zh.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author raozihao
 * @date 2020/4/5
 */
public class Learner {

    private static final Logger bizLogger = LoggerFactory.getLogger(PaxosNetwork.class);

    int id;
    /**
     * Key represents the ID of acceptor.
     * Value represents the biggest number accepted proposal that received from the acceptor.
     */
    Map<Integer, Msg> accepts;

    public Learner(int id, int... acceptor) {
        this.id = id;
        accepts = new HashMap<>();
        for (int a : acceptor) {
            accepts.put(a, new Msg());
        }
    }

    public Msg run(PaxosNetwork pn) {
        while (true) {
            Msg recv = pn.recv(id);
            if (recv == null) continue;
            receiveAccept(recv);
            Msg chosen = chosen();
            if (chosen == null) continue;
            bizLogger.info("learner: {} learned the chosen propose ", chosen);
            return chosen;
        }
    }

    public void receiveAccept(Msg recv) {
        Msg msg = accepts.get(recv.from);
        if (msg.n < recv.n) {
            bizLogger.info("learner: {} received a new accepted proposal {}", id, recv);
            accepts.put(recv.from, recv);
        }
    }

    public int majority() {
        return accepts.size() / 2 + 1;
    }

    public Msg chosen() {
        Map<Integer, Integer> count = new HashMap<>();
        Map<Integer, Msg> acceptValue = new HashMap<>();
        Collection<Msg> values = accepts.values();
        for (Msg m : values) {
            if (m.type != MessageType.ACCEPT.getType()) continue;
            if (count.containsKey(m.n)) {
                count.put(m.n, count.get(m.n) + 1);
            } else {
                count.put(m.n, 1);
                acceptValue.put(m.n, m);
            }
        }
        Iterator<Integer> it = count.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            if (count.get(key) >= majority()) {
                return acceptValue.get(key);
            }
        }
        return null;
    }


}
