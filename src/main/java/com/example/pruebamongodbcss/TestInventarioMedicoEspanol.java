package com.example.pruebamongodbcss;

import com.example.pruebamongodbcss.service.InventarioMedicoService;
import java.util.List;

public class TestInventarioMedicoEspanol {
    public static void main(String[] args) {
        // Usar la misma API key que ya tenías configurada
        InventarioMedicoService service = new InventarioMedicoService("ba9de3d5-083d-45e0-88d9-4deb353a28d4");

        // Ejemplo 1: Validar nombres de equipos en español
        String[] equipos = {
            "monitor de signos vitales",
            "desfibrilador",
            "ventilador mecánico",
            "estetoscopio",
            "electrocardiógrafo",
            "bomba de infusión",
            "respirador artificial",
            "oxímetro de pulso"
        };

        System.out.println("=== Validación de nombres de equipos en español ===");
        for (String equipo : equipos) {
            String nombreEstandarizado = service.validarNombreEquipo(equipo);
            System.out.println("Original: " + equipo);
            System.out.println("Estandarizado: " + nombreEstandarizado);
            System.out.println("----------------------------------------");
        }

        // Ejemplo 2: Obtener sugerencias en español
        System.out.println("\n=== Sugerencias para 'monitor' ===");
        List<String> sugerencias = service.obtenerSugerencias("monitor");
        for (String sugerencia : sugerencias) {
            System.out.println("- " + sugerencia);
        }

        // Ejemplo 3: Verificar términos válidos en español
        System.out.println("\n=== Verificación de términos en español ===");
        String[] terminos = {
            "monitor de signos vitales",
            "estetoscopio",
            "aparato inventado",
            "respirador",
            "oxímetro"
        };
        for (String termino : terminos) {
            boolean esValido = service.esTerminoValido(termino);
            System.out.println(termino + ": " + (esValido ? "Válido" : "No válido"));
        }

        // Ejemplo 4: Obtener ontologías para términos en español
        System.out.println("\n=== Ontologías relacionadas con términos en español ===");
        for (String equipo : equipos) {
            service.obtenerOntologia(equipo).ifPresent(ontologia -> 
                System.out.println(equipo + " -> " + ontologia)
            );
        }
    }
} 