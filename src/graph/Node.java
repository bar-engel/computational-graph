package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A node in the computational graph, holding directed edges to other nodes. */
public class Node {

    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Node> getEdges() { return edges; }
    public void setEdges(List<Node> edges) { this.edges = edges; }

    public Message getMsg() { return msg; }
    public void setMsg(Message msg) { this.msg = msg; }

    public void addEdge(Node node) {
        edges.add(node);
    }

    /** Returns true if this node is part of a directed cycle. */
    public boolean hasCycles() {
        return hasCycles(this, new HashSet<>());
    }

    /**
     * DFS cycle detection with backtracking.
     * visited holds nodes on the current path; if we reach one again, there's a cycle.
     */
    private boolean hasCycles(Node current, Set<Node> visited) {
        if (visited.contains(current)) return true;
        visited.add(current);
        for (Node neighbor : current.edges) {
            if (hasCycles(neighbor, visited)) return true;
        }
        visited.remove(current);
        return false;
    }
}
