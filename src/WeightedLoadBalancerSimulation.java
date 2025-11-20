import java.util.*;
import java.io.*;

public class WeightedLoadBalancerSimulation {

    /**
     * Writes time-series server load snapshots to CSV.
     * First column is time; following columns are server IDs.
     */
    public static void writeLoadOverTimeCSV(List<LoadSnapshot> snapshots, String filename) {
        if (snapshots == null || snapshots.isEmpty()) {
            System.err.println("No snapshots to write.");
            return;
        }

        // Determine server ID order from first snapshot
        List<String> serverIds = new ArrayList<>(snapshots.get(0).getServerLoads().keySet());

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {

            // Header
            pw.print("time");
            for (String id : serverIds) pw.print("," + id);
            pw.println();

            // Rows
            for (LoadSnapshot snap : snapshots) {
                pw.print(String.format(Locale.ROOT, "%.3f", snap.getTime()));

                Map<String, Integer> loads = snap.getServerLoads();
                for (String id : serverIds) {
                    pw.print("," + loads.getOrDefault(id, 0));
                }

                pw.println();
            }

            pw.flush();
            System.out.println("Time-series data written to: " + filename);

        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }

    /**
     * Runs the Python plotting script automatically after simulation.
     */
    public static void runPythonPlot() {
        try {
            System.out.println("Running Python graph script...");

            ProcessBuilder pb = new ProcessBuilder(
                    "python", "plot_graph.py"   // Must match your file name
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Print output from Python script
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Graph generated successfully.");
            } else {
                System.err.println("Python exited with error code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("Error running Python script: " + e.getMessage());
        }
    }

    /**
     * Main simulation
     */
    public static void main(String[] args) {

        int numRequests = 1000;
        long seed = 42L;
        boolean modelServiceTime = true;
        double meanInterarrival = 1.0;
        double meanService = 3.0;
        double snapshotInterval = 1.0;   // seconds between snapshots

        Random rng = new Random(seed);

        // Server setup with weights
        List<Server> servers = Arrays.asList(
                new Server("S1", 5),
                new Server("S2", 3),
                new Server("S3", 2)
        );

        WeightedLoadBalancer balancer = new WeightedLoadBalancer(servers, rng);

        Simulator simulator = new Simulator(
                balancer,
                rng,
                meanInterarrival,
                meanService,
                snapshotInterval
        );

        System.out.println("Running Weighted Distribution Simulation");
        System.out.printf("Requests: %d, Seed: %d%n", numRequests, seed);

        double simTime = simulator.run(numRequests, modelServiceTime);

        // Write CSV of server loads over time
        writeLoadOverTimeCSV(
                simulator.getLoadOverTime(),
                "server_load_over_time.csv"
        );

        // Print final results
        System.out.println("\nFinal Load Distribution:");
        long total = servers.stream().mapToLong(s -> s.handledRequests).sum();

        for (Server s : servers) {
            double pct = 100.0 * s.handledRequests / Math.max(1, total);
            double util = s.totalServiceTime / simTime;

            System.out.printf("%s: handled=%d (%.1f%%), util=%.3f%n",
                    s, s.handledRequests, pct, util);
        }

        // Automatically generate graph
        System.out.println("\nGenerating graph...");
        runPythonPlot();

        System.out.println("\nSimulation complete.");
    }
}
