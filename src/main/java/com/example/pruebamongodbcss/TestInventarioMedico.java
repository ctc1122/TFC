package com.example.pruebamongodbcss;

import com.example.pruebamongodbcss.service.InventarioMedicoService;
import java.util.List;

public class TestInventarioMedico {
    public static void main(String[] args) {
        // Usar la misma API key que ya tenías configurada
        InventarioMedicoService service = new InventarioMedicoService("ba9de3d5-083d-45e0-88d9-4deb353a28d4");

        // Ejemplo 1: Validar nombres de equipos
        String[] equipos = {
            "monitor de signos vitales",
            "desfibrilador",
            "ventilador",
            "estetoscopio"
        };

        System.out.println("=== Validación de nombres de equipos ===");
        for (String equipo : equipos) {
            String nombreEstandarizado = service.validarNombreEquipo(equipo);
            System.out.println("Original: " + equipo);
            System.out.println("Estandarizado: " + nombreEstandarizado);
            System.out.println("----------------------------------------");
        }

        // Ejemplo 2: Obtener sugerencias
        System.out.println("\n=== Sugerencias para 'monitor' ===");
        List<String> sugerencias = service.obtenerSugerencias("monitor");
        for (String sugerencia : sugerencias) {
            System.out.println("- " + sugerencia);
        }

        // Ejemplo 3: Verificar términos válidos
        System.out.println("\n=== Verificación de términos ===");
        String[] terminos = {"monitor", "estetoscopio", "aparatoInventado"};
        for (String termino : terminos) {
            boolean esValido = service.esTerminoValido(termino);
            System.out.println(termino + ": " + (esValido ? "Válido" : "No válido"));
        }

        // Ejemplo 4: Obtener ontologías
        System.out.println("\n=== Ontologías relacionadas ===");
        for (String equipo : equipos) {
            service.obtenerOntologia(equipo).ifPresent(ontologia -> 
                System.out.println(equipo + " -> " + ontologia)
            );
        }
    }
} 