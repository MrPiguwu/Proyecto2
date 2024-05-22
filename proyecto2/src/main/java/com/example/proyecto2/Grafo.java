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



import java.util.*;

public class Grafo<T> {
    private Map<T, List<T>> adjList;

    // Constructor
    public Grafo(List<T> vertices) {
        adjList = new HashMap<>();
        for (T vertex : vertices) {
            adjList.put(vertex, new ArrayList<>());
        }
    }

    // Añadir una arista
    public void addEdge(T src, T dest) {
        adjList.get(src).add(dest);
        adjList.get(dest).add(src); // Grafo no dirigido
    }

    // Obtener la lista de adyacencia
    public Map<T, List<T>> getAdjList() {
        return adjList;
    }

    // Imprimir el grafo
    public void printGraph() {
        for (T vertex : adjList.keySet()) {
            System.out.print(vertex.toString() + ": ");
            for (T neighbor : adjList.get(vertex)) {
                System.out.print(neighbor.toString() + " ");
            }
            System.out.println();
        }
    }
}
