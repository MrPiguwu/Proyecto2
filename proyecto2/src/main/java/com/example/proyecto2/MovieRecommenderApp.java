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

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.*;

public class MovieRecommenderApp extends Application {

    static String csvFile = "C:\\Users\\Rober\\OneDrive\\Pig\\OneDrive\\Escritorio\\Ciclo3\\Estructura de datos\\proyecto2\\proyecto2\\src\\main\\java\\com\\example\\proyecto2\\datosdeprueba.csv";
    static double umbral = 2.0;
    static Map<String, Object> weights = new LinkedHashMap<>();
    private Grafo<Pelicula> baseDatosGrafo;
    private ComboBox<String> categoryComboBox;
    private ComboBox<String> category2ComboBox;
    private ComboBox<String> typeComboBox;
    private TextField ratingFromField;
    private TextField ratingToField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configura los pesos y tipos de datos
        weights.put("Titulo", 0.0);
        weights.put("Categoria", new HashMap<>());
        weights.put("Categoria2", new HashMap<>());
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

        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Seleccione una categoría");

        category2ComboBox = new ComboBox<>();
        category2ComboBox.setPromptText("Seleccione una segunda categoría");

        typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("Seleccione un tipo");

        ratingFromField = new TextField();
        ratingFromField.setPromptText("Crítica desde");

        ratingToField = new TextField();
        ratingToField.setPromptText("Crítica hasta");

        populateComboBoxes(baseDatosGrafo);

        recommendButton.setOnAction(event -> {
            String titulo = movieInput.getText();
            String categoria = categoryComboBox.getValue();
            String categoria2 = category2ComboBox.getValue();
            String tipo = typeComboBox.getValue();
            String criticaDesde = ratingFromField.getText();
            String criticaHasta = ratingToField.getText();

            recommendationsList.getItems().clear();
            List<Pelicula> recomendaciones = filterMovies(baseDatosGrafo, titulo, categoria, categoria2, tipo, criticaDesde, criticaHasta);
            for (Pelicula pp : recomendaciones) {
                String info = pp.getTitulo() + " - Categoría: " + pp.getCategoria() + ", " +
                        "Categoría 2: " + pp.getCategoria2() + ", " +
                        "Crítica: " + pp.getCritica() + ", " +
                        "Tipo: " + pp.getTipo();
                recommendationsList.getItems().add(info);
            }
        });

        root.getChildren().addAll(label, movieInput, categoryComboBox, category2ComboBox, typeComboBox, ratingFromField, ratingToField, recommendButton, recommendationsList);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Sistema de Recomendación de Películas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Grafo<Pelicula> loadMovies() {
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

    private List<Pelicula> filterMovies(Grafo<Pelicula> graph, String title, String category, String category2, String type, String ratingFrom, String ratingTo) {
        List<Pelicula> peliculas = new ArrayList<>(graph.getAdjList().keySet());
        List<Pelicula> filteredMovies = new ArrayList<>();

        // Ordenar películas por cantidad de criterios que cumplen
        peliculas.sort((p1, p2) -> {
            int p1Score = scoreMovie(p1, category, category2, type, ratingFrom, ratingTo);
            int p2Score = scoreMovie(p2, category, category2, type, ratingFrom, ratingTo);
            return Integer.compare(p2Score, p1Score); // Orden descendente
        });

        for (Pelicula pelicula : peliculas) {
            boolean matches = true;
            if (title != null && !title.isEmpty() && !pelicula.getTitulo().equalsIgnoreCase(title)) {
                continue;
            }
            filteredMovies.add(pelicula);
        }
        return filteredMovies;
    }

    private int scoreMovie(Pelicula pelicula, String category, String category2, String type, String ratingFrom, String ratingTo) {
        int score = 0;
        if (category != null && !category.isEmpty() && pelicula.getCategoria().equalsIgnoreCase(category)) {
            score += 4;
        }
        if (category2 != null && !category2.isEmpty() && pelicula.getCategoria2().equalsIgnoreCase(category2)) {
            score += 3;
        }
        if (type != null && !type.isEmpty() && pelicula.getTipo().equalsIgnoreCase(type)) {
            score += 2;
        }
        if (ratingFrom != null && !ratingFrom.isEmpty()) {
            int ratingFromInt = Integer.parseInt(ratingFrom);
            if (pelicula.getCritica() >= ratingFromInt) {
                score += 1;
            }
        }
        if (ratingTo != null && !ratingTo.isEmpty()) {
            int ratingToInt = Integer.parseInt(ratingTo);
            if (pelicula.getCritica() <= ratingToInt) {
                score += 1;
            }
        }
        return score;
    }

    private void populateComboBoxes(Grafo<Pelicula> graph) {
        Set<String> uniqueCategories = new HashSet<>();
        Set<String> uniqueCategories2 = new HashSet<>();
        Set<String> uniqueTypes = new HashSet<>();
        for (Pelicula pelicula : graph.getAdjList().keySet()) {
            uniqueCategories.add(pelicula.getCategoria());
            uniqueCategories2.add(pelicula.getCategoria2());
            uniqueTypes.add(pelicula.getTipo());
        }

        categoryComboBox.getItems().addAll(uniqueCategories);
        category2ComboBox.getItems().addAll(uniqueCategories2);
        typeComboBox.getItems().addAll(uniqueTypes);
    }
}
