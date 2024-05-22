package com.example.proyecto2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
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

/**
 * The type Main.
 */
public class Main {

    /**
     * The Csv file.
     */
    static String csvFile = "C:\\Users\\Rober\\OneDrive\\Pig\\OneDrive\\Escritorio\\Ciclo3\\Estructura de datos\\proyecto2\\proyecto2\\src\\main\\java\\com\\example\\proyecto2\\datosdeprueba.csv";
    /**
     * The Umbral.
     */
    static double umbral = 2;
    /**
     * The Weights.
     */
    static Map<String, Object> weights = new LinkedHashMap<>();

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(String args[]) {
        // Crea el grafo dado en la figura anterior
        weights.put("Titulo", 0.0);
        weights.put("Categoria", new HashMap<>());
        weights.put("Critica", 1.0);
        weights.put("Tipo", new HashMap<>());

        Grafo<Pelicula> baseDatosGrafo = loadMovies();
        conexion(baseDatosGrafo, umbral);
        baseDatosGrafo.printGraph();
        Scanner s = new Scanner(System.in);

        System.out.println("Bienvenido, Ingrese el título de una película");

        String Titulo = s.nextLine();
        for (Pelicula p : baseDatosGrafo.getAdjList().keySet()) {
            if (p.getTitulo().equals(Titulo)) {
                System.out.println("Películas similares:");
                for (Pelicula pp : baseDatosGrafo.getAdjList().get(p)) {
                    System.out.println(pp.getTitulo());
                }
                return;
            }
        }
        System.out.println("Película no disponible en la base de datos");
    }

    private static void conexion(Grafo<Pelicula> grafo, double umbral) {

        List<Pelicula> peliculas = new ArrayList<>();
        peliculas.addAll(grafo.getAdjList().keySet());

        // Calcula la similitud de las películas
        double[][] similitud = new double[peliculas.size()][peliculas.size()];
        for (int i = 0; i < peliculas.size(); i++) {
            Pelicula pelicula1 = peliculas.get(i);
            for (int j = i; j < peliculas.size(); j++) {
                Pelicula pelicula2 = peliculas.get(j);
                similitud[i][j] = pelicula1.calcularDistanciaEuclidiana(pelicula2);
                // Esto es para que no sea direccionado, y se puedan correlacionar
                similitud[j][i] = similitud[i][j];
            }
        }

        // Conecta las películas basándose en la similitud del umbral
        for (int i = 0; i < similitud.length; i++) {
            for (int j = i + 1; j < similitud[i].length; j++) {
                if (similitud[i][j] >= umbral) {
                    grafo.addEdge(peliculas.get(i), peliculas.get(j));
                }
            }
        }
    }

    private static Grafo<Pelicula> loadMovies() {
        List<Pelicula> peliculas = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(csvFile))) {
            boolean FirstRow = true;

            while (sc.hasNextLine()) {
                Scanner sl = new Scanner(sc.nextLine());
                sl.useDelimiter(",");
                if (FirstRow) {
                    FirstRow = false;
                    continue;
                }
                Map<String, Object> values = new HashMap<>();
                String titulo = "";
                for (String field : weights.keySet()) {
                    if (field.equals("Titulo")) {
                        values.put(field, 0.0);
                        titulo = sl.next();
                    } else {
                        if (weights.get(field) instanceof Number) {
                            values.put(field, Double.valueOf(sl.next()));
                        } else {
                            values.put(field, sl.next());
                        }
                    }
                }
                peliculas.add(new Pelicula(titulo, Pelicula.makeVector(values, weights)));
            }

            return new Grafo<>(peliculas);

        } catch (FileNotFoundException e) {
            System.err.println("* Error de lectura");
            return null;
        }
    }
}
