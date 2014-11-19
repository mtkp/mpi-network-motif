// Driver.java

import java.util.*;

import mpi.*;

public class Driver {
    public static void main(String[] args) throws MPIException {
        // verify arguments
        if (args.length < 4) {
            System.out.println(
                "usage: Driver nodes threads_per_node " +
                "data_file motif_size [--show-results]");
            System.exit(-1);
        }

        // determine if results should be printed
        boolean showResults = false;
        if (args.length == 5 && args[4].equals("--show-results")) {
            showResults = true;
        }


        // Start the MPI library.
        MPI.Init(args);

        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.println(args[0] + " nodes, " + args[1] + " threads");
        }

        long start = System.currentTimeMillis();

        // run the program
        Main app = new Main(
            Integer.parseInt(args[1]),
            args[2],
            Integer.parseInt(args[3]),
            showResults);
        app.run();

        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.println(
                (System.currentTimeMillis() - start) + " milliseconds to " +
                "complete the program");
        }

        // Terminate the MPI library.
        MPI.Finalize();
    }
}
