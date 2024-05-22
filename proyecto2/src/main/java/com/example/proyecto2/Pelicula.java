package com.example.proyecto2;

import java.util.Map;


public class Pelicula {
    private String titulo;
    private String categoria;
    private String categoria2;
    private int critica;
    private String tipo;
    private double[] caracteristicas;

    // Constructor
    public Pelicula(String titulo, String categoria, String categoria2, int critica, String tipo, double[] caracteristicas) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.categoria2 = categoria2;
        this.critica = critica;
        this.tipo = tipo;
        this.caracteristicas = caracteristicas;
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getCategoria2() {
        return categoria2;
    }

    public int getCritica() {
        return critica;
    }

    public String getTipo() {
        return tipo;
    }

    public double[] getCaracteristicas() {
        return caracteristicas;
    }

    // Método para calcular la distancia euclidiana
    public double calcularDistanciaEuclidiana(Pelicula otraPelicula) {
        double[] otrasCaracteristicas = otraPelicula.getCaracteristicas();
        double suma = 0.0;
        for (int i = 0; i < this.caracteristicas.length; i++) {
            suma += Math.pow(this.caracteristicas[i] - otrasCaracteristicas[i], 2);
        }
        return Math.sqrt(suma);
    }

    // Método para crear un vector a partir de los mapas dados fields y weights
    public static double[] makeVector(Map<String, Object> fields, Map<String, Object> weights) {
        double[] vector = new double[weights.size()];
        int count = 0;
        for (String field : weights.keySet()) {
            if (fields.get(field) instanceof Number) {
                vector[count] = (double) fields.get(field) * (double) weights.get(field);
            } else if (fields.get(field).getClass() == String.class) {
                if (!((Map) weights.get(field)).containsKey(fields.get(field))) {
                    Double position = ((Map) weights.get(field)).size() + 1.0;
                    ((Map) weights.get(field)).put(fields.get(field), position);
                }
                vector[count] = (Double) ((Map) weights.get(field)).get(fields.get(field));
            }
            count++;
        }
        return vector;
    }
}
