import java.util.*;

public class Simulator {
    private final WeightedLoadBalancer balancer;
    private final Random rng;
    private final double meanInterarrival;
    private final double meanService;
    private final List<LoadSnapshot> loadOverTime = new ArrayList<>();
    private final double snapshotInterval; // set interval for snapshots

    public Simulator(WeightedLoadBalancer balancer, Random rng,
                     double meanInterarrival, double meanService, double snapshotInterval) {
        this.balancer = balancer;
        this.rng = rng;
        this.meanInterarrival = meanInterarrival;
        this.meanService = meanService;
        this.snapshotInterval = snapshotInterval;
    }

    // Sample from exponential distribution
    private double expSample(double mean) {
        double u = rng.nextDouble();
        if (u < 1e-12) u = 1e-12; // avoid log(0)
        return -mean * Math.log(u);
    }

    // Record a snapshot of server loads
    private void recordSnapshot(double currentTime) {
        LoadSnapshot snap = new LoadSnapshot(currentTime);
        for (Server s : balancer.getServers()) {
            snap.addServerLoad(s.id, s.activeConnections);
        }
        loadOverTime.add(snap);
    }

    // Run simulation
    public double run(long numRequests, boolean modelServiceTime) {
        double currentTime = 0.0;
        double nextSnapshotTime = snapshotInterval; // first snapshot at t = 10
        PriorityQueue<ConnectionEvent> completions = new PriorityQueue<>(Comparator.comparingDouble(e -> e.time));

        // initial snapshot at t=0
        recordSnapshot(0.0);

        for (long i = 0; i < numRequests; i++) {
            // advance time
            double interarrival = expSample(meanInterarrival);
            currentTime += interarrival;

            // handle completed requests
            while (!completions.isEmpty() && completions.peek().time <= currentTime) {
                ConnectionEvent ev = completions.poll();
                ev.server.completeRequest();
            }

            // take snapshots at intervals
            while (currentTime >= nextSnapshotTime) {
                recordSnapshot(nextSnapshotTime);
                nextSnapshotTime += snapshotInterval;
            }

            // assign new request
            Server chosen = balancer.selectServer();
            double serviceTime = modelServiceTime ? expSample(meanService) : 0.0;
            chosen.assignRequest(serviceTime);

            // schedule completion
            if (modelServiceTime) {
                double completionTime = currentTime + serviceTime;
                completions.add(new ConnectionEvent(completionTime, chosen));
            }
        }

        // process remaining completions
        while (!completions.isEmpty()) {
            ConnectionEvent ev = completions.poll();
            currentTime = Math.max(currentTime, ev.time);
            ev.server.completeRequest();

            // take remaining snapshots if needed
            while (currentTime >= nextSnapshotTime) {
                recordSnapshot(nextSnapshotTime);
                nextSnapshotTime += snapshotInterval;
            }
        }

        return currentTime;
    }

    public List<LoadSnapshot> getLoadOverTime() {
        return Collections.unmodifiableList(loadOverTime);
    }
}
