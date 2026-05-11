package graph;

import java.util.ArrayList;
import java.util.List;

/** A named channel that connects publishers to subscribers. */
public class Topic {

    public final String name;
    private List<Agent> subs = new ArrayList<>();
    private List<Agent> pubs = new ArrayList<>();

    /** Package-private: only TopicManager should create topics. */
    Topic(String name) {
        this.name = name;
    }

    public void subscribe(Agent a) {
        subs.add(a);
    }

    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    /** Delivers the message to every current subscriber. */
    public void publish(Message m) {
        for (Agent a : subs) {
            a.callback(name, m);
        }
    }

    public void addPublisher(Agent a) {
        pubs.add(a);
    }

    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    public List<Agent> getSubs() { return subs; }
    public List<Agent> getPubs() { return pubs; }
}
