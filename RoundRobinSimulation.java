import java.io.*;
import java.util.*;

// Request class representing client requests
class Request {
    private int id;
    private int processingTime;
    private long arrivalTime;
    
    public Request(int id, int processingTime, long arrivalTime) {
        this.id = id;
        this.processingTime = processingTime;
        this.arrivalTime = arrivalTime;
    }
    
    public int getId() { return id; }
    public int getProcessingTime() { return processingTime; }
    public long getArrivalTime() { return arrivalTime; }
}

// Server class representing individual servers
class Server {
    private int id;
    private int currentLoad;
    private int totalRequestsProcessed;
    private List<Integer> loadHistory;
    
    public Server(int id) {
        this.id = id;
        this.currentLoad = 0;
        this.totalRequestsProcessed = 0;
        this.loadHistory = new ArrayList<>();
    }
    
    public void processRequest(Request request) {
        currentLoad += request.getProcessingTime();
        totalRequestsProcessed++;
        loadHistory.add(currentLoad);
    }
    
    public void decrementLoad(int time) {
        currentLoad = Math.max(0, currentLoad - time);
        loadHistory.add(currentLoad);
    }
    
    public int getId() { return id; }
    public int getCurrentLoad() { return currentLoad; }
    public int getTotalRequestsProcessed() { return totalRequestsProcessed; }
    public List<Integer> getLoadHistory() { return loadHistory; }
}

// Round Robin Load Balancer
class RoundRobinLoadBalancer {
    private List<Server> servers;
    private int currentServerIndex;
    private List<Map<String, Object>> metricsLog;
    
    public RoundRobinLoadBalancer(int numServers) {
        this.servers = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            servers.add(new Server(i));
        }
        this.currentServerIndex = 0;
        this.metricsLog = new ArrayList<>();
    }
    
    // Round Robin Algorithm: Distribute request to next server in sequence
    public void distributeRequest(Request request) {
        Server selectedServer = servers.get(currentServerIndex);
        selectedServer.processRequest(request);
        
        // Log metrics
        logMetrics(request, selectedServer);
        
        // Move to next server (circular)
        currentServerIndex = (currentServerIndex + 1) % servers.size();
    }
    
    private void logMetrics(Request request, Server server) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("request_id", request.getId());
        metrics.put("server_id", server.getId());
        metrics.put("server_load", server.getCurrentLoad());
        metrics.put("timestamp", System.currentTimeMillis());
        metricsLog.add(metrics);
    }
    
    public void simulateTimeStep(int timeUnits) {
        for (Server server : servers) {
            server.decrementLoad(timeUnits);
        }
    }
    
    public void printStatistics() {
        System.out.println("\n=== Load Balancing Statistics ===");
        for (Server server : servers) {
            System.out.println("Server " + server.getId() + ":");
            System.out.println("  Total Requests: " + server.getTotalRequestsProcessed());
            System.out.println("  Current Load: " + server.getCurrentLoad());
        }
        
        // Calculate load distribution variance
        double avgLoad = servers.stream()
            .mapToInt(Server::getTotalRequestsProcessed)
            .average()
            .orElse(0.0);
        
        double variance = servers.stream()
            .mapToDouble(s -> Math.pow(s.getTotalRequestsProcessed() - avgLoad, 2))
            .average()
            .orElse(0.0);
        
        System.out.println("\nAverage Requests per Server: " + avgLoad);
        System.out.println("Load Distribution Variance: " + variance);
    }
    
    // Export data for Python visualization
    public void exportDataForVisualization(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("request_id,server_id,server_load,timestamp");
            for (Map<String, Object> metrics : metricsLog) {
                writer.println(metrics.get("request_id") + "," +
                             metrics.get("server_id") + "," +
                             metrics.get("server_load") + "," +
                             metrics.get("timestamp"));
            }
            
            // Export load history
            writer.println("\n# Server Load History");
            writer.println("time_step,server_id,load");
            for (int i = 0; i < servers.size(); i++) {
                Server server = servers.get(i);
                List<Integer> history = server.getLoadHistory();
                for (int t = 0; t < history.size(); t++) {
                    writer.println(t + "," + server.getId() + "," + history.get(t));
                }
            }
            System.out.println("\nData exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    public List<Server> getServers() { return servers; }
}

// Simulation Driver
public class RoundRobinSimulation {
    public static void main(String[] args) {
        // Simulation parameters
        int numServers = 5;
        int numRequests = 100;
        int maxProcessingTime = 50;
        
        System.out.println("=== Round Robin Load Balancer Simulation ===");
        System.out.println("Number of Servers: " + numServers);
        System.out.println("Number of Requests: " + numRequests);
        
        // Initialize load balancer
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer(numServers);
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Generate and distribute requests
        for (int i = 0; i < numRequests; i++) {
            int processingTime = random.nextInt(maxProcessingTime) + 1;
            Request request = new Request(i, processingTime, System.currentTimeMillis());
            loadBalancer.distributeRequest(request);
            
            // Simulate time passing (server processing)
            if (i % 10 == 0) {
                loadBalancer.simulateTimeStep(5);
            }
        }
        
        // Print statistics
        loadBalancer.printStatistics();
        
        // Export data for visualization
        loadBalancer.exportDataForVisualization("load_balancer_data.csv");
        
        System.out.println("\nSimulation completed successfully!");
    }
}