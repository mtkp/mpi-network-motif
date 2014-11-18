import mpi.*;

public class Util {
    public static Object broadcastObject(Object obj, int rank) throws MPIException {
        Object[] packet = new Object[1];
        packet[0] = obj;
        MPI.COMM_WORLD.Bcast(packet, 0, 1, MPI.OBJECT, rank);
        return packet[0];
    }
}