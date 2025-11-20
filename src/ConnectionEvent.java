public class ConnectionEvent implements Comparable<ConnectionEvent> {
    public final double time;
    public final Server server;

    public ConnectionEvent(double time, Server server) {
        this.time = time;
        this.server = server;
    }

    @Override
    public int compareTo(ConnectionEvent o) {
        return Double.compare(this.time, o.time);
    }
}
