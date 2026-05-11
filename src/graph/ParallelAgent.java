package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Decorator that wraps an Agent and dispatches callbacks asynchronously
 * via a single dedicated background thread (Active Object pattern).
 */
public class ParallelAgent implements Agent {

    private final Agent agent;
    private final BlockingQueue<MessageBundle> queue;
    private final Thread worker;

    /** Pairs a topic name with its message so both can travel through the queue together. */
    private static class MessageBundle {
        final String topic;
        final Message message;

        MessageBundle(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);

        this.worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    MessageBundle bundle = queue.take(); // blocks when empty
                    agent.callback(bundle.topic, bundle.message);
                }
            } catch (InterruptedException e) {
                // clean exit when close() interrupts the thread
                Thread.currentThread().interrupt();
            }
        });
        this.worker.start();
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            queue.put(new MessageBundle(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        agent.close();
        worker.interrupt();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }
}
