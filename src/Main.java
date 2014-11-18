// Main.java

import java.io.IOException;
import java.util.*;

import mpi.*;

public class Main {

    public  final static int master = 0;  // the master rank
    private final static int tag = 0;     // Send/Recv's tag is always 0.

    // app execution code goes here
    public void run() throws MPIException {
        Graph graph = null;

        long start = System.currentTimeMillis();
        long overallStart = start;

        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println("Parsing input data file '" + filename + "'...");
            start = System.currentTimeMillis();
            try {
                graph = new Graph(filename);
            } catch (IOException e) {
                System.out.println("Unable to parse data file");
                return;
            }
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "generate a network of size " + graph.size());
        }

        // broadcast the base graph to all nodes
        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println("Setting up MPI nodes...");
            start = System.currentTimeMillis();
        }

        graph = (Graph)Util.broadcastObject(graph, master);

        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "set up MPI nodes.");

            System.out.println("Executing ESU for motif size " + motifSize + "...");
            start = System.currentTimeMillis();
        }

        Map<String, Integer> subgraphs = new HashMap<String, Integer>();

        for (int i = MPI.COMM_WORLD.Rank(); i < graph.size(); i += MPI.COMM_WORLD.Size()) {
            graph.enumerate(
                i,
                new Subgraph(motifSize),
                new AdjacencyList(),
                subgraphs);
        }

        MPI.COMM_WORLD.Barrier();

        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "execute ESU.");

            System.out.println("Determining subgraph (label) frequency...");
            start = System.currentTimeMillis();
        }


        Labeler labeler = new Labeler();
        Map<String, Integer> labels = labeler.getCanonicalLabels(subgraphs);

        MPI.COMM_WORLD.Barrier();

        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "determine subgraph (label) frequency");

            System.out.println(
                (System.currentTimeMillis() - overallStart) + " milliseconds to " +
                "complete the program");
        }

        System.out.println(MPI.COMM_WORLD.Rank() + " subgraphs size: " + subgraphs.size() + ", labels size: " + labels.size());

        if (showResults) {
            System.out.println(MPI.COMM_WORLD.Rank() + " results: " + labels.toString());
        }

/*
        int networkSize = graph.size();

        // create network nodes
        System.out.println("Setting up MASS places and agents...");
        start = System.currentTimeMillis();
        Places placesGraph = new Places(
            1,
            "GraphNode",
            (Object)(new GraphNode.Constructor(networkSize)),
            networkSize);

        // initialize node edges
        Object[] params = new Object[networkSize];
        for (int node = 0; node < params.length; node++) {
            params[node] = (Object)(graph.getAdjacencyList(node));
        }
        placesGraph.callAll(GraphNode.initializeEdges_, params);

        // initialize agents, spawning 1 agent per node in the network
        // this approach assumes agents will be evenly spread across the nodes
        // so that each node will have one agent to start
        Agents crawlers = new Agents(
            2,
            "GraphCrawler",
            (Object)(new GraphCrawler.Constructor(motifSize)),
            placesGraph,
            networkSize);

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "set up MASS places and agents.");

        // run until agents terminate themselves
        System.out.println("Executing ESU for motif size " + motifSize + "...");
        start = System.currentTimeMillis();
        int remainingSubgraphs = crawlers.nAgents();
        while (remainingSubgraphs > 0) {
            System.out.println("enumerating ESU: " + remainingSubgraphs + " subgraphs in progress");
            crawlers.callAll(GraphCrawler.update_);
            crawlers.manageAll();
            remainingSubgraphs = crawlers.nAgents();
        }

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "execute ESU.");

        // collect the subgaph data left at the places
        // (MASS requires parameter array in order to get return values)
        start = System.currentTimeMillis();
        Object[] dummyParams = new Object[networkSize];
        for (int i = 0; i < dummyParams.length; i++) {
            dummyParams[i] = (Object)(0); // some serializable object
        }

        System.out.println("Collecting subgraph results...");
        Object[] results =(Object[])placesGraph.callAll(
            GraphNode.collectSubgraphs_,
            dummyParams);

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "collect subgraph results at master");

        // get the canonical label counts
        // this portion is executed sequentially on the master node.
        System.out.println("Determining subgraph (label) frequency...");
        start = System.currentTimeMillis();

        // collect the labels into one master subgraph collection
        Map<String, Integer> subgraphs = new HashMap<String,Integer>();
        for (int i = 0; i < results.length; i++) {
            // convert generic Object types
            @SuppressWarnings("unchecked")
            Map<String, Integer> result = (Map<String, Integer>)results[i];

            // merge the results into the master subgraph collection
            for (Map.Entry<String, Integer> entry:result.entrySet()) {
                int count = entry.getValue();
                if (subgraphs.containsKey(entry.getKey())) {
                    count += subgraphs.get(entry.getKey());
                }
                subgraphs.put(entry.getKey(), count);
            }
        }

        // get canonical label counts from subgraphs
        Labeler labeler = new Labeler();
        Map<String, Integer> labels = labeler.getCanonicalLabels(subgraphs);

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "determine subgraph (label) frequency");

        System.out.println(
            (System.currentTimeMillis() - overallStart) + " milliseconds to " +
            "complete the program");

        if (showResults) {
            System.out.println("Label\tFrequency");
            for (Map.Entry<String, Integer> entry:labels.entrySet()) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
*/
    }

    private int motifSize;
    private String filename;
    private boolean showResults;
    public Main(String filename, int motifSize, boolean showResults) {
        this.filename = filename;
        this.motifSize = motifSize;
        this.showResults = showResults;
    }
}
