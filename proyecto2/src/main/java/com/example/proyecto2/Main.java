package com.example.proyecto2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Main.
 */
public class Main {

    static String csvFile = "C:\\Users\\Rober\\OneDrive\\Pig\\OneDrive\\Escritorio\\Ciclo3\\Estructura de datos\\proyecto2\\proyecto2\\src\\main\\java\\com\\example\\proyecto2\\datosdeprueba.csv";
    static double umbral = 2;
    static Map<String, Object> weights = new LinkedHashMap<>();

    public static void main(String[] args) {
        // Inicializa los pesos para las características de las películas
        weights.put("Titulo", 0.0);
        weights.put("Categoria", new HashMap<>());
        weights.put("Categoria2", new HashMap<>());
        weights.put("Critica", 1.0);
        weights.put("Tipo", new HashMap<>());

        Grafo<Pelicula> baseDatosGrafo = loadMovies();
        if (baseDatosGrafo == null) {
            System.err.println("Error: No se pudo cargar la base de datos de películas.");
            return;
        }
        conexion(baseDatosGrafo, umbral);
        baseDatosGrafo.printGraph();
        Scanner s = new Scanner(System.in);

        System.out.println("Bienvenido, Ingrese el título de una película");

        String titulo = s.nextLine();
        for (Pelicula p : baseDatosGrafo.getAdjList().keySet()) {
            if (p.getTitulo().equalsIgnoreCase(titulo)) {
                System.out.println("Películas similares:");
                for (Pelicula pp : baseDatosGrafo.getAdjList().get(p)) {
                    String info = pp.getTitulo() + " - Categoría: " + pp.getCategoria() + ", " +
                            "Categoría 2: " + pp.getCategoria2() + ", " +
                            "Crítica: " + pp.getCritica() + ", " +
                            "Tipo: " + pp.getTipo();
                    System.out.println(info);
                }
                return;
            }
        }
        System.out.println("Película no disponible en la base de datos");
    }

    private static void conexion(Grafo<Pelicula> grafo, double umbral) {
        List<Pelicula> peliculas = new ArrayList<>(grafo.getAdjList().keySet());

        double[][] similitud = new double[peliculas.size()][peliculas.size()];
        for (int i = 0; i < peliculas.size(); i++) {
            Pelicula pelicula1 = peliculas.get(i);
            for (int j = i; j < peliculas.size(); j++) {
                Pelicula pelicula2 = peliculas.get(j);
                similitud[i][j] = pelicula1.calcularDistanciaEuclidiana(pelicula2);
                similitud[j][i] = similitud[i][j]; // No dirigido
            }
        }

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
            boolean firstRow = true;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length < 5) {
                    throw new IllegalArgumentException("El archivo CSV no tiene el formato correcto");
                }
                Map<String, Object> fields = new HashMap<>();
                String titulo = values[0];
                String categoria = values[1];
                String categoria2 = values[2];
                int critica = Integer.parseInt(values[3]);
                String tipo = values[4];

                fields.put("Titulo", 0.0);
                fields.put("Categoria", categoria);
                fields.put("Categoria2", categoria2);
                fields.put("Critica", (double) critica);
                fields.put("Tipo", tipo);

                double[] caracteristicas = Pelicula.makeVector(fields, weights);
                Pelicula pelicula = new Pelicula(titulo, categoria, categoria2, critica, tipo, caracteristicas);
                peliculas.add(pelicula);
            }

            if (peliculas.isEmpty()) {
                throw new RuntimeException("No se encontraron películas en el archivo CSV.");
            }

            return new Grafo<>(peliculas);

        } catch (FileNotFoundException e) {
            System.err.println("* Error de lectura del archivo CSV: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("* Error procesando los datos del archivo CSV: " + e.getMessage());
            return null;
        }
    }
}
