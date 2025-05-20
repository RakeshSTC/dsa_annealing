package com.dsa.compute;

import java.io.*;
import java.util.*;

public class MSTFromCSV {
	/*
	 * 
	 * Purpose:
	    - Stores a connection between two vertices (src and dest) and the weight (cost) of that connection.
		- Implements Comparable to allow sorting edges by weight for Kruskal's algorithm.
	 * 
	 * 
	 * class Edge:
	    string src       // one vertex of the edge
	    string dest      // other vertex of the edge
	    int weight       // weight or cost of the edge
	
	    function compareTo(Edge other):
        return this.weight - other.weight
        // allows sorting edges by increasing weight

	 */

	//Class: Represents a Graph Edge
    static class Edge implements Comparable<Edge> {
    	String src, dest;  // Endpoints of the edge
	    int weight;        // Weight (or cost) of the edge

	    // Constructor to initialize an edge
	    public Edge(String src, String dest, int weight) {
	        this.src = src;
	        this.dest = dest;
	        this.weight = weight;
	    }

	    // Used to sort edges by ascending weight (for Kruskal's)
	    public int compareTo(Edge other) {
	        return this.weight - other.weight;
	    }

	    // Nice string output
	    public String toString() {
	        return src + " - " + dest + " : " + weight;
	    }
    }

    /*
     * 
     * Purpose:
        - Efficiently tracks which vertices are connected.
		- Helps detect cycles in Kruskal's algorithm.
		- Uses path compression to optimize the find() operation.
     * 
     *
     * class UnionFind:
	    map parent
	
	    function makeSet(vertices):
	        for each vertex in vertices:
	            parent[vertex] = vertex
	        // Initially, each vertex is its own parent (disjoint)
	
	    function find(vertex):
	        if parent[vertex] != vertex:
	            parent[vertex] = find(parent[vertex])
	        return parent[vertex]
	        // Recursively finds the root parent, with path compression
	
	    function union(vertex1, vertex2):
	        root1 = find(vertex1)
	        root2 = find(vertex2)
	        if root1 != root2:
	            parent[root1] = root2
	        // Connects the two disjoint sets

     */
    static class UnionFind {
	    private Map<String, String> parent = new HashMap<>();

	    // Initializes the Union-Find structure: each vertex is its own parent
	    public void makeSet(Set<String> vertices) {
	        for (String vertex : vertices) {
	            parent.put(vertex, vertex);
	        }
	    }

	    // Finds the root of the set a vertex belongs to (with path compression)
	    public String find(String vertex) {
	        if (!vertex.equals(parent.get(vertex))) {
	            parent.put(vertex, find(parent.get(vertex)));  // Compress path
	        }
	        return parent.get(vertex);
	    }

	    // Unions two sets by attaching one root to the other
	    public void union(String v1, String v2) {
	        String root1 = find(v1);
	        String root2 = find(v2);
	        if (!root1.equals(root2)) {
	            parent.put(root1, root2);  // Merge trees
	        }
	    }
    }


    public static void main(String[] args) {
        String filePath = "edges.csv";
        List<Edge> edges = new ArrayList<>();
        Set<String> vertices = new HashSet<>();

        // Read edges from CSV
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String src = parts[0];
                String dest = parts[1];
                int weight = Integer.parseInt(parts[2]);
                edges.add(new Edge(src, dest, weight));
                vertices.add(src);
                vertices.add(dest);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        // Kruskal's Algorithm
        Collections.sort(edges);
        UnionFind uf = new UnionFind();
        uf.makeSet(vertices);

        List<Edge> mst = new ArrayList<>();
        int totalWeight = 0;

        for (Edge edge : edges) {
            if (!uf.find(edge.src).equals(uf.find(edge.dest))) {
                mst.add(edge);
                totalWeight += edge.weight;
                uf.union(edge.src, edge.dest);
            }
        }

        // Output MST
        System.out.println("Minimum Spanning Tree:");
        for (Edge e : mst) {
            System.out.println(e);
        }
        System.out.println("Total weight: " + totalWeight);
    }
}

