package com.example.proyecto2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Grafo<T> {
    private int numVertices;
    private Map<T, LinkedList<T>> adjList;

    public Map<T, LinkedList<T>> getAdjList() {
        return adjList;
    }

    // Constructor
    Grafo(List<T> vertices) {
        this.numVertices = vertices.size();
        adjList = new HashMap<>();

        // Crea una nueva lista para cada vértice de tal manera que se puedan almacenar nodos adyacentes
        for (T vertice : vertices) {
            adjList.put(vertice, new LinkedList<>());
        }
    }

    // Agrega una arista a un grafo no dirigido
    void addEdge(T src, T dest) {
        if (!adjList.containsKey(src)) {
            adjList.put(src, new LinkedList<>());
        }
        if (!adjList.containsKey(dest)) {
            adjList.put(dest, new LinkedList<>());
        }

        // Agrega una arista de src a dest.
        adjList.get(src).add(dest);

        // Dado que el grafo es no dirigido, agrega una arista de dest a src también
        adjList.get(dest).add(src);
    }

    // Una función de utilidad para imprimir la representación de la lista de adyacencia del grafo
    void printGraph() {
        for (T v : adjList.keySet()) {
            System.out.println("Lista de adyacencia del vértice " + v);
            System.out.print("head");
            for (T pCrawl : adjList.get(v)) {
                System.out.print(" -> " + pCrawl);
            }
            System.out.println("\n");
        }
    }
}