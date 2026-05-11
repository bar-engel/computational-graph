package graph;

/** Publishes the sum of its two input topics to an output topic. */
public class PlusAgent implements Agent {

    private final String[] subs;
    private final String[] pubs;
    private double x = 0, y = 0;

    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).subscribe(this);
        tm.getTopic(subs[1]).subscribe(this);
        tm.getTopic(pubs[0]).addPublisher(this);
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(subs[0])) x = msg.asDouble;
        if (topic.equals(subs[1])) y = msg.asDouble;
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(x + y));
        }
    }

    @Override
    public void reset() { x = 0; y = 0; }

    @Override
    public String getName() { return "PlusAgent"; }

    @Override
    public void close() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(subs[0]).unsubscribe(this);
        tm.getTopic(subs[1]).unsubscribe(this);
    }
}
