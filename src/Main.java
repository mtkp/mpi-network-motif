// Main.java

import java.io.IOException;
import java.util.*;

import mpi.*;

public class Main {

    private static class ComputeThread extends Thread {

        private int start;
        private int interval;
        private int motifSize;
        private Graph graph;
        private Map<String, Integer> subgraphs;

        public ComputeThread(int start, int interval, int motifSize,
                             Graph graph, Map<String, Integer> subgraphs) {
            this.start = start;
            this.interval = interval;
            this.motifSize = motifSize;
            this.graph = graph;
            this.subgraphs = subgraphs;
        }

        public void run() {
            for (int i = start; i < graph.size(); i += interval) {
                Subgraph subgraph = new Subgraph(motifSize);
                AdjacencyList adjacencyList = new AdjacencyList();
                CompactHashSet.Iter iter = graph.getAdjacencyList(i).iterator();
                while (iter.hasNext()) {
                    int next = iter.next();
                    if (next > i) {
                        adjacencyList.add(next);
                    }
                }
                subgraph.add(i, graph.getAdjacencyList(i));
                graph.enumerate(subgraph, adjacencyList, subgraphs);
            }
        }
    }

    private final static int master = 0;  // the master rank
    private final static int tag = 0;     // Send/Recv's tag is always 0.

    // app execution code goes here
    public void run() throws MPIException {
        int commRank = MPI.COMM_WORLD.Rank();
        int commSize = MPI.COMM_WORLD.Size();
        long start = System.currentTimeMillis();

        Graph graph = null;

        if (commRank == master) {
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

        if (commRank == master) {
            System.out.println("Setting up MPI nodes...");
            start = System.currentTimeMillis();
        }

        // broadcast the base graph from the master to all nodes
        // (MPI requires object array to send and receive data)
        Object[] packet = mpiPacket(graph);
        MPI.COMM_WORLD.Bcast(packet, 0, 1, MPI.OBJECT, master);
        graph = (Graph)packet[0];

        if (commRank == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "set up MPI nodes.");

            System.out.println(
                "Executing ESU and determining subgraph (label) frequency " +
                "for motif size " + motifSize + "...");
            start = System.currentTimeMillis();
        }

        // execute ESU for the indexes this rank is responsible for, equal
        // to: indexes = (rank + (size * n))
        int interval = commSize * (threads.length + 1);
        Map<String, Integer> subgraphs = new HashMap<String, Integer>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ComputeThread(
                commRank + (commSize * (i + 1)),
                interval,
                motifSize,
                graph,
                subgraphs);
            threads[i].start();
        }
        for (int i = commRank; i < graph.size(); i += interval) {
            Subgraph subgraph = new Subgraph(motifSize);
            AdjacencyList adjacencyList = new AdjacencyList();
            CompactHashSet.Iter iter = graph.getAdjacencyList(i).iterator();
            while (iter.hasNext()) {
                int next = iter.next();
                if (next > i) {
                    adjacencyList.add(next);
                }
            }
            subgraph.add(i, graph.getAdjacencyList(i));
            graph.enumerate(subgraph, adjacencyList, subgraphs);
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception thrown...");
                e.printStackTrace();
            }
        }

        // run the labeler on each MPI node before gathering data,
        // to minimize size of data transfer
        Labeler labeler = new Labeler();
        Map<String, Integer> labels = labeler.getCanonicalLabels(subgraphs);

        MPI.COMM_WORLD.Barrier();

        if (commRank == master) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "execute ESU and determine subgraph (label) frequency.");

            System.out.println("Collection label frequencies...");
            start = System.currentTimeMillis();
        }

        // use gather to collect objects back at the master node
        // (MPI requires object array to send and receive data)
        Object[] packets = new Object[commSize];
        MPI.COMM_WORLD.Gather(
            mpiPacket(labels),  0, 1, MPI.OBJECT,
            packets           , 0, 1, MPI.OBJECT, master);

        // only need master node from this point forward.
        if (commRank != master) {
            return;
        }

        System.out.println(
            (System.currentTimeMillis() - start) + " milliseconds to " +
            "collect label frequencies");

        // collect the labels into one master label collection
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

        if (showResults) {
            System.out.println("Label\tFrequency");
            for (Map.Entry<String, Integer> entry:labels.entrySet()) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
    }

    private Thread[] threads;
    private int motifSize;
    private String filename;
    private boolean showResults;
    public Main(int nThreads, String filename, int motifSize, boolean showResults) {
        this.threads = new Thread[nThreads - 1];
        this.filename = filename;
        this.motifSize = motifSize;
        this.showResults = showResults;
    }

    // creates an object array container, or "packet", for a single object
    public static Object[] mpiPacket(Object obj) {
        Object[] packet = new Object[1];
        packet[0] = obj;
        return packet;
    }
}
