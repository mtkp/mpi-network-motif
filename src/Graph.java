// Graph.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph implements java.io.Serializable {
    private List<AdjacencyList> adjacencyLists;
    private Map<String, Integer> nameToIndex;
    private Map<Integer, String> indexToName;

    public Graph(String filename) throws IOException {
        adjacencyLists = new ArrayList<AdjacencyList>();
        nameToIndex = new HashMap<String, Integer>();
        indexToName = new HashMap<Integer, String>();
        parse(filename);
    }

    // get the number of nodes in the graph
    public int size() {
        return adjacencyLists.size();
    }

    public AdjacencyList getAdjacencyList(Integer index) {
        return adjacencyLists.get(index);
    }

    public Integer nameToIndex(String name) {
        return nameToIndex.get(name);
    }

    public String indexToName(Integer index) {
        return indexToName.get(index);
    }

    public void enumerate(Subgraph subgraph, AdjacencyList extension,
                          Map<String, Integer> subgraphs) {
        if (subgraph.isComplete()) {
            String repr = subgraph.getByteString();
            int count = 1;
            synchronized(subgraphs) {
                if (subgraphs.containsKey(repr)) {
                    count += subgraphs.get(repr);
                }
                subgraphs.put(repr, count);
            }
        } else {
            int v = subgraph.root();
            CompactHashSet.Iter iter = extension.iterator();
            while (iter.hasNext()) {
                int w = iter.next();
                iter.remove();

                AdjacencyList nextExtension = extension.copy();
                AdjacencyList adjacencyList = adjacencyLists.get(w);

                CompactHashSet.Iter candidateIter =
                    adjacencyLists.get(w).iterator();
                while (candidateIter.hasNext()) {
                    int u = candidateIter.next();
                    if (u > v) {
                        if (isExclusive(u, subgraph)) {
                            nextExtension.add(u);
                        }
                    }
                }

                Subgraph subgraphUnion = subgraph.copy();
                subgraphUnion.add(w, adjacencyLists.get(w));
                enumerate(subgraphUnion, nextExtension, subgraphs);
            }
        }
    }

    private boolean isExclusive(int node, Subgraph subgraph) {
        for (int i = 0; i < subgraph.size(); i++) {
            int subgraphNode = subgraph.get(i);
            if (subgraphNode == node) {
                return false;
            }
        }
        for (int i = 0; i < subgraph.size(); i++) {
            int subgraphNode = subgraph.get(i);
            if (adjacencyLists.get(subgraphNode).contains(node)) {
                return false;
            }
        }
        return true;
    }

    // parses a data file into an adjacency list representing the graph
    private void parse(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<String>();
        String currentLine = reader.readLine();
        while (currentLine != null) {
            lines.add(currentLine);
            currentLine = reader.readLine();
        }
        reader.close();

        // avoid clustering (data collection bias) by randomly parsing the
        // input lines of data
        Collections.shuffle(lines);

        String delimiters = "\\s+"; // one or more whitespace characters
        for (String line:lines) {
            String[] edge = line.split(delimiters);
            int fromIndex = getOrCreateIndex(edge[0]);
            int toIndex = getOrCreateIndex(edge[1]);

            // don't add self edges
            if (fromIndex != toIndex) {
                adjacencyLists.get(fromIndex).add(toIndex);
                adjacencyLists.get(toIndex).add(fromIndex);
            }
        }
    }

    // get index of a node given the node's name
    // create an entry if it does not exist
    private Integer getOrCreateIndex(String nodeName) {
        if (!nameToIndex.containsKey(nodeName)) {
            nameToIndex.put(nodeName, adjacencyLists.size());
            indexToName.put(adjacencyLists.size(), nodeName);
            adjacencyLists.add(new AdjacencyList());
        }
        return nameToIndex.get(nodeName);
    }
}