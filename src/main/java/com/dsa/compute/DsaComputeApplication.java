package com.dsa.compute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@SpringBootApplication
@ComponentScan({"com.dsa.compute"})
public class DsaComputeApplication {
    static final double INITIAL_TEMP = 1000;
    static final double FINAL_TEMP = 1e-8;
    static final double ALPHA = 0.99;
    static final int MAX_ITER = 10000;

    static Random rand = new Random();

    // Objective function: f(x) = x^2
    static double f(double x) {
        return x * x;
    }

	public static void main(String[] args) {
		SpringApplication.run(DsaComputeApplication.class, args);
		//int iDecmial = Integer.parseInt("0x123");
		 // Input: Cyclic graph with 5 nodes and 5 edges (one extra)
        //int[][] edges = {{1, 2}, {2, 3}, {3, 4}, {4, 1}, {1, 5}};
        //System.out.println("Starting with input edges: " + Arrays.deepToString(edges));
        
        //int[] result = findRedundantDirectedConnection(edges);
        //System.out.println("Redundant edge to remove: " + Arrays.toString(result));
		
		
		 // Create the map of roads (graph)
        Map<String, List<int[]>> map = new HashMap<>();
        map.put("A", Arrays.asList(new int [] {"B".hashCode(),5}));
        map.put("B", Arrays.asList(new int [] {"A".hashCode(),5}));
        
        for(String house : map.keySet()) {
        	StringBuilder sb = new StringBuilder(house + "--");
        	for (int [] road : map.get(house)) {
                sb.append("[").append((char) road[0]).append(", km=").append(road[1]).append("] ");
            }
            System.out.println(sb);
        }
        
     // Run Dijkstra's
        findShortestPath("A", "B", map);
        
        simualtedAnnealing();
        
	}

	private static void simualtedAnnealing() {
		double current = rand.nextDouble() * 20 - 10; // Start from [-10, 10]
        double best = current;
        double temp = INITIAL_TEMP;

        System.out.printf("Initial x = %.4f, f(x) = %.4f\n", current, f(current));

        int totalCount = 0;
        
        for (int iter = 0; iter < MAX_ITER && temp > FINAL_TEMP; iter++) {
            // Generate neighbor by small random step
            double candidate = current + (rand.nextDouble() * 2 - 1); // Step in [-1, 1]

            // Calculate change in energy (cost)
            double delta = f(candidate) - f(current);

            // Accept move if it's better, or probabilistically if worse
            //P(accept)=e to the power−ΔE/T
            //i.e. ΔE: how much worse it is 𝑇 & T: temperature (exploration level)
            if (delta < 0 || Math.exp(-delta / temp) > rand.nextDouble()) {
                current = candidate;
            }

            // Update best solution
            if (f(current) < f(best)) {
                best = current;
            }

            // Cool down
            temp *= ALPHA;

            // Print progress every 1000 iterations
            if (iter % 1000 == 0) {
                System.out.printf("Iter %5d: x = %.4f, f(x) = %.4f, Temp = %.6f\n",
                        iter, current, f(current), temp);
            }
        }

        System.out.printf("\nBest solution: x = %.6f, f(x) = %.6f\n", best, f(best));
	}
	
	private static void findShortestPath(String start, String end, Map<String, List<int[]>> map) {
		
		Map<String, Integer> distances= new HashMap();
		Map<String, String> cameFrom = new HashMap();
		
		 // To-do list: [house, distance], sorted by smallest distance
        PriorityQueue<int[]> toDoList = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        
        Set<String> visited = new HashSet();
        
        //initialize the set of nodes
        distances.put("A", 0);
        distances.put("B", Integer.MAX_VALUE);
        distances.put("A", 0);
        distances.put("B", 0);
        
        toDoList.offer(new int[]{start.hashCode(), 0});
        
        while(!toDoList.isEmpty()) {
        	
        	int[] current = toDoList.poll();
        	String house = String.valueOf((char)current[0]);
        	int distance = current[1];
        	
        	if(visited.contains(house)) 
        		continue;
        	visited.add(house);
        	
        	//iterate all nodes
        	for(int [] nodes : map.getOrDefault(house, new ArrayList<int[]>())) {
        		
        		String nextHouse = String.valueOf((char) nodes[0]);
        		int nodeDistance = nodes[1];
        		
        		//can we reach next Node faster than current node
        		int newDistance = distance + nodeDistance;
        		if(newDistance <= distances.get(nextHouse)) {
        			distances.put(nextHouse, newDistance);//update shorter distance
        			cameFrom.put(nextHouse, house); //came from current house
        			toDoList.offer(new int [] {nextHouse.hashCode(), newDistance});
        			
        		}
        	}
        	
        }
        // Show delivery result
        System.out.println("\nShortest path from " + start + " to " + end + ":");
        System.out.println("Total distance: " + distances.get(end) + " km");
        // Build path by backtracking
        List<String> path = new ArrayList<>();
        for(String at = end; at!= null; at = cameFrom.get(at)) {
        	path.add(at);
        }
        
        Collections.reverse(path);
        System.out.println("Path: " + String.join(" → ", path));
        
		
	}
		
	
	 public static int[] findRedundantDirectedConnection(int[][] edges) {
	        int n = edges.length;
	        int[] inDegree = new int[n + 1]; // Track number of incoming edges (parents)
	        Map<Integer, List<Integer>> graph = new HashMap<>(); // Adjacency list
	        int[] parent1 = null, parent2 = null; // Edges causing two parents

	        // Step 1: Build graph and check for two parents, visualize as we go
	        System.out.println("\nBuilding graph step-by-step:");
	        for (int i = 0; i < edges.length; i++) {
	            int u = edges[i][0], v = edges[i][1];
	            graph.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
	            inDegree[v]++;
	            
	            // Visualize after adding each edge
	            System.out.println("Adding edge " + Arrays.toString(edges[i]));
	            printGraph(graph, n);
	            
	            // Check for two parents
	            if (inDegree[v] == 2) {
	                if (parent1 == null) parent1 = edges[i];
	                else parent2 = edges[i];
	            }
	        }

	        // Step 2: Detect cycle with DFS
	        System.out.println("\nDetecting cycle with DFS:");
	        boolean[] visited = new boolean[n + 1];
	        boolean[] inPath = new boolean[n + 1];
	        int[] cycleEdge = null;

	        for (int i = 1; i <= n && cycleEdge == null; i++) {
	            if (!visited[i]) {
	                cycleEdge = dfs(i, graph, visited, inPath, edges);
	            }
	        }

	        // Step 3: Decide which edge to remove
	        System.out.println("\nDecision phase:");
	        if (parent2 != null) {
	            System.out.println("Node with two parents detected.");
	            if (cycleEdge == null) {
	                System.out.println("No cycle, removing second parent edge: " + Arrays.toString(parent2));
	                return parent2;
	            } else {
	                System.out.println("Both cycle and two parents exist.");
	                for (int[] edge : edges) {
	                    if (edge[1] == parent2[1] && edge != parent2) {
	                        System.out.println("Removing first edge to two-parent node: " + Arrays.toString(edge));
	                        return edge;
	                    }
	                }
	            }
	        }
	        if (cycleEdge != null) {
	            System.out.println("Cycle detected, removing: " + Arrays.toString(cycleEdge));
	            return cycleEdge;
	        }
	        return new int[]{}; // Shouldn't reach here with valid input
	    }

	    private static int[] dfs(int node, Map<Integer, List<Integer>> graph, 
	                            boolean[] visited, boolean[] inPath, int[][] edges) {
	        visited[node] = true;
	        inPath[node] = true;
	        System.out.println("DFS visiting node " + node + ", path: " + Arrays.toString(inPath));

	        for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
	            if (!visited[neighbor]) {
	                int[] result = dfs(neighbor, graph, visited, inPath, edges);
	                if (result != null) return result;
	            } else if (inPath[neighbor]) {
	                System.out.println("Cycle detected at " + node + " -> " + neighbor);
	                for (int[] edge : edges) {
	                    if (edge[0] == node && edge[1] == neighbor) {
	                        return edge;
	                    }
	                }
	            }
	        }
	        inPath[node] = false; // Backtrack
	        return null;
	    }

	    // Helper to visualize the graph as a tree-like structure
	    private static void printGraph(Map<Integer, List<Integer>> graph, int n) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = 1; i <= n; i++) {
	            if (graph.containsKey(i)) {
	                sb.append(i).append(" → ");
	                for (int j = 0; j < graph.get(i).size(); j++) {
	                    sb.append(graph.get(i).get(j));
	                    if (j < graph.get(i).size() - 1) sb.append(", ");
	                }
	                sb.append("\n");
	            }
	        }
	        System.out.println(sb.toString());
	    }
	
	void dfs(int node, boolean[] visited, Map<Integer, List<Integer>> graph) {
	    visited[node] = true;
	    System.out.println(node); // Process node
	    for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
	        if (!visited[neighbor]) {
	            dfs(neighbor, visited, graph);
	        }
	    }
	}
}
