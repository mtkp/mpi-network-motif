// AdjacencyList.java

import java.io.Serializable;

public class AdjacencyList implements Serializable {

    private CompactHashSet nodes;

    public AdjacencyList() {
        this.nodes = new CompactHashSet();
    }

    public void add(int node) {
        nodes.add(node);
    }

    public CompactHashSet.Iter iterator() {
        return nodes.iterator();
    }

    public boolean contains(int node) {
        return nodes.contains(node);
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public String toString() {
        return nodes.toString();
    }

    public boolean remove(int node) {
        return nodes.remove(node);
    }
}
