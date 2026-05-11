package graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Outer class whose sole job is to expose the singleton accessor. */
public class TopicManagerSingleton {

    /** Singleton registry of all topics in the graph. */
    public static class TopicManager {

        private static final TopicManager instance = new TopicManager();

        private Map<String, Topic> topics = new HashMap<>();

        private TopicManager() {}

        /** Returns the existing topic with this name, or creates and registers a new one. */
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public void clear() {
            topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }
}
