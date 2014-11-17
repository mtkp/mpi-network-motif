// Driver.java

import mpi.*;

public class Driver {
    private final static int aSize = 100; // the size of dArray
    private final static int master = 0;  // the master rank
    private final static int tag = 0;     // Send/Recv's tag is always 0.

    public static void main(String[] args) throws MPIException {
        // Start the MPI library.
        MPI.Init(args);

        // compute my own stripe
        int stripe = aSize / MPI.COMM_WORLD.Size(); // each portion of array
        double[] dArray = null;

        // scatter information
        if (MPI.COMM_WORLD.Rank() == master) {

            // initialize dArray.
            dArray = new double[aSize];
            for (int i = 0; i < aSize; i++) {
                dArray[i] = i;
            }

            // send a portion of dArray[100] to each slave
            for (int rank = 1; rank < MPI.COMM_WORLD.Size(); rank++) {
                MPI.COMM_WORLD.Send(dArray, rank * stripe, stripe,
                                    MPI.DOUBLE, rank, tag);
            }

        } else { // slaves: rank 1 to rank n - 1

            // allocates dArray portion for this slave.
            dArray = new double[stripe];

            // receive a portion of dArray[100] from the master
            MPI.COMM_WORLD.Recv(dArray, 0, stripe, MPI.DOUBLE, master, tag);
        }


        // compute the square root of each array element
        for (int i = 0; i < stripe; i++) {
            dArray[i] = Math.sqrt(dArray[i]);
        }

        // gather information
        if (MPI.COMM_WORLD.Rank() == master) { // master

            // receive answers from each slave
            for (int rank = 1; rank < MPI.COMM_WORLD.Size(); rank++) {
                MPI.COMM_WORLD.Recv(dArray, rank * stripe, stripe,
                                    MPI.DOUBLE, rank, tag);
            }

            // print out the results
            for (int i = 0; i < aSize; i++) {
                System.out.println("dArray[ " + i + " ] = " + dArray[i]);
            }

        } else { // slaves: rank 1 to rank n - 1

            // send the results back to the master
            MPI.COMM_WORLD.Send(dArray, 0, stripe, MPI.DOUBLE, master, tag);
        }

        // Terminate the MPI library.
        MPI.Finalize();
    }

}