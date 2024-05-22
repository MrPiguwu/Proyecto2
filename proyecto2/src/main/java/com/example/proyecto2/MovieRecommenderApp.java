package com.example.proyecto2;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class MovieRecommenderApp extends Application {

    static String csvFile = "C:\\Users\\Rober\\OneDrive\\Pig\\OneDrive\\Escritorio\\Ciclo3\\Estructura de datos\\proyecto2\\proyecto2\\src\\main\\java\\com\\example\\proyecto2\\datosdeprueba.csv";
    static double umbral = 2.0;
    static Map<String, Object> weights = new LinkedHashMap<>();
    private Grafo<Pelicula> baseDatosGrafo;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configura los pesos y tipos de datos
        weights.put("Titulo", 0.0);
        weights.put("Categoria", new HashMap<>());
        weights.put("Critica", 1.0);
        weights.put("Tipo", new HashMap<>());

        try {
            baseDatosGrafo = loadMovies();
            if (baseDatosGrafo == null) {
                throw new RuntimeException("Error cargando los datos de las películas.");
            }
            conexion(baseDatosGrafo, umbral);
        } catch (Exception e) {
            System.err.println("* Error inicializando la base de datos: " + e.getMessage());
            return;
        }

        VBox root = new VBox();
        Label label = new Label("Ingrese el título de una película:");
        TextField movieInput = new TextField();
        Button recommendButton = new Button("Recomendar");
        ListView<String> recommendationsList = new ListView<>();

        recommendButton.setOnAction(event -> {
            String titulo = movieInput.getText();
            recommendationsList.getItems().clear();
            for (Pelicula p : baseDatosGrafo.getAdjList().keySet()) {
                if (p.getTitulo().equalsIgnoreCase(titulo)) {
                    for (Pelicula pp : baseDatosGrafo.getAdjList().get(p)) {
                        recommendationsList.getItems().add(pp.getTitulo());
                    }
                    return;
                }
            }
            recommendationsList.getItems().add("Película no disponible en la base de datos");
        });

        root.getChildren().addAll(label, movieInput, recommendButton, recommendationsList);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Sistema de Recomendación de Películas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Grafo<Pelicula> loadMovies() {
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

    private void conexion(Grafo<Pelicula> grafo, double umbral) {
        List<Pelicula> peliculas = new ArrayList<>(grafo.getAdjList().keySet());

        double[][] similitud = new double[peliculas.size()][peliculas.size()];
        for (int i = 0; i < peliculas.size(); i++) {
            Pelicula pelicula1 = peliculas.get(i);
            for (int j = i; j < peliculas.size(); j++) {
                Pelicula pelicula2 = peliculas.get(j);
                similitud[i][j] = pelicula1.calcularDistanciaEuclidiana(pelicula2);
                similitud[j][i] = similitud[i][j];
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
}