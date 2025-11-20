import java.util.*;
public class LoadSnapshot {
    private final double time;
    private final Map<String, Integer> serverLoads;

    public LoadSnapshot(double time) {
        this.time = time;
        this.serverLoads = new LinkedHashMap<>();
    }

    public void addServerLoad(String serverId, int load) {
        serverLoads.put(serverId, load);
    }

    public double getTime() {
        return time;
    }

    public Map<String, Integer> getServerLoads() {
        return serverLoads;
    }
}
