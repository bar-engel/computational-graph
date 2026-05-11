package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** A directed graph of Nodes built from the current topic/agent topology. */
public class Graph extends ArrayList<Node> {

    /** Returns true if any node in the graph is part of a directed cycle. */
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) return true;
        }
        return false;
    }

    /**
     * Rebuilds this graph from the current topics in the TopicManager.
     * Topic nodes are named "T<topicName>"; agent nodes are named "A<agentName>".
     * Edges: topic → subscriber agent, publisher agent → topic.
     */
    public void createFromTopics() {
        this.clear();
        Map<String, Node> nodeMap = new HashMap<>();

        for (Topic t : TopicManagerSingleton.get().getTopics()) {
            Node topicNode = nodeMap.computeIfAbsent("T" + t.name, Node::new);

            for (Agent a : t.getSubs()) {
                Node agentNode = nodeMap.computeIfAbsent("A" + a.getName(), Node::new);
                topicNode.addEdge(agentNode);
            }

            for (Agent a : t.getPubs()) {
                Node agentNode = nodeMap.computeIfAbsent("A" + a.getName(), Node::new);
                agentNode.addEdge(topicNode);
            }
        }

        this.addAll(nodeMap.values());
    }
}
