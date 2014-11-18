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

            System.out.println(
                "Executing ESU and determining subgraph (label) frequency " +
                "for motif size " + motifSize + "...");
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

        Labeler labeler = new Labeler();
        Map<String, Integer> labels = labeler.getCanonicalLabels(subgraphs);

        MPI.COMM_WORLD.Barrier();

        if (MPI.COMM_WORLD.Rank() == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "execute ESU and determine subgraph (label) frequency.");

            System.out.println("Collection label frequencies...");
            start = System.currentTimeMillis();
        }

        System.out.println(MPI.COMM_WORLD.Rank() + " subgraphs: " + subgraphs.size() + ", labels:" + labels.size());


        Object[] packet = new Object[1];
        packet[0] = (Object)labels;
        Object[] packets = new Object[MPI.COMM_WORLD.Size()];
        MPI.COMM_WORLD.Gather(
            packet,  0, 1, MPI.OBJECT,
            packets, 0, 1, MPI.OBJECT,
            master);

        // only need master node from this point forward.
        if (MPI.COMM_WORLD.Rank() != master) {
            return;
        }

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "collect label frequencies");

        // // collect the labels into one master label collection
        for (int i = 1; i < packets.length; i++) {
            // convert generic Object types
            @SuppressWarnings("unchecked")
            Map<String, Integer> result = (Map<String, Integer>)packets[i];

            // merge the received label counts with the master node's
            // label counts
            for (Map.Entry<String, Integer> entry:result.entrySet()) {
                int count = entry.getValue();
                if (labels.containsKey(entry.getKey())) {
                    count += labels.get(entry.getKey());
                }
                labels.put(entry.getKey(), count);
            }
        }

        System.out.println(
            (System.currentTimeMillis() - overallStart) + " milliseconds to " +
            "complete the program");

        if (showResults) {
            System.out.println("Label\tFrequency");
            for (Map.Entry<String, Integer> entry:labels.entrySet()) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
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
