import java.util.*;

public class Server {
    public final String id;
    public final int weight;
    public long handledRequests = 0;
    public double totalServiceTime = 0.0;
    public int activeConnections = 0;

    public Server(String id, int weight) {
        this.id = id;
        this.weight = weight;
    }

    public void assignRequest(double serviceTime) {
        handledRequests++;
        totalServiceTime += serviceTime;
        activeConnections++;
    }

    public void completeRequest() {
        activeConnections = Math.max(0, activeConnections - 1);
    }

    @Override
    public String toString() {
        return String.format("%s(w=%d)", id, weight);
    }
}
