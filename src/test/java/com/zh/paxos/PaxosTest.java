package com.zh.paxos;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author raozihao
 * @date 2020/4/6
 */
public class PaxosTest {
    ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(20));

    @Test
    public void paxosWithSingleProposer() throws IOException {
        PaxosNetwork pn = new PaxosNetwork(1001, 1, 2, 3, 4, 5, 2001);
        for (int i = 1; i <= 5; i++) {
            Acceptor acceptor = new Acceptor(i, 2001);
            executorService.submit(() -> {
                acceptor.run(pn);
            });
        }
        Proposer proposer = new Proposer(1001, "hello world", 1, 2, 3, 4, 5);
        executorService.submit(() -> proposer.run(pn));

        Learner learner = new Learner(2001, 1, 2, 3, 4, 5);
        Msg msg = learner.run(pn);
        Assert.assertEquals(msg.content, "hello world");
        executorService.shutdown();
    }

    @Test
    public void paxosWithTwoProposer() throws InterruptedException {
        PaxosNetwork pn = new PaxosNetwork(1001, 1002, 1, 2, 3, 2001);
        for (int i = 1; i <= 3; i++) {
            Acceptor acceptor = new Acceptor(i, 2001);
            executorService.submit(() -> {
                acceptor.run(pn);
            });
        }
        Proposer proposer = new Proposer(1001, "hello world", 1, 2, 3);
        executorService.submit(() -> proposer.run(pn));
        Learner learner = new Learner(2001, 1, 2, 3);
        Msg msg = learner.run(pn);
        Thread.sleep(1000);
        Proposer proposer1 = new Proposer(1002, "bad day", 1, 2, 3);
        executorService.submit(() -> proposer1.run(pn));

        Thread.sleep(1000);
        Assert.assertEquals(msg.content, "hello world");
        executorService.shutdown();
    }

}
