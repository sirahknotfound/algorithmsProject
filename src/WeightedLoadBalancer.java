import java.util.*;
public class WeightedLoadBalancer {
    private final List<Server> servers;
    private final int totalWeight;
    private final Random rng;

    public WeightedLoadBalancer(List<Server> servers, Random rng) {
        this.servers = new ArrayList<>(servers);
        int sum = 0;
        for (Server s : servers) sum += s.weight;
        this.totalWeight = Math.max(1, sum);
        this.rng = rng;
    }

    // Weighted random selection (probability proportional to weight)
    public Server selectServer() {
        int r = rng.nextInt(totalWeight); // 0 .. totalWeight-1
        int cumulative = 0;
        for (Server s : servers) {
            cumulative += s.weight;
            if (r < cumulative) return s;
        }
        return servers.get(servers.size() - 1); // defensive
    }

    public List<Server> getServers() {
        return Collections.unmodifiableList(servers);
    }
}
