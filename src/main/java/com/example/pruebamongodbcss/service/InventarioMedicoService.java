package com.example.pruebamongodbcss.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class InventarioMedicoService {
    private final ULMSService umlsService;

    public InventarioMedicoService(String apiKey) {
        this.umlsService = new ULMSService(apiKey);
    }

    /**
     * Valida y estandariza el nombre de un equipo médico usando UMLS
     * @param nombreEquipo Nombre del equipo a validar
     * @return Nombre estandarizado si se encuentra, o el original si no hay coincidencias
     */
    public String validarNombreEquipo(String nombreEquipo) {
        List<String> resultados = umlsService.searchTerm(nombreEquipo, true);
        
        // Verificar si hay resultados válidos
        if (!resultados.isEmpty()) {
            String primerResultado = resultados.get(0);
            if (!primerResultado.startsWith("No se encontraron") && !primerResultado.startsWith("Error")) {
                // Extraemos solo el nombre sin la fuente ni la definición
                return primerResultado.split(" \\[")[0];
            }
        }
        
        return nombreEquipo; // Devolvemos el original si no hay coincidencias válidas
    }

    /**
     * Sugiere términos relacionados para un equipo médico
     * @param nombreEquipo Nombre del equipo
     * @return Lista de sugerencias
     */
    public List<String> obtenerSugerencias(String nombreEquipo) {
        List<String> resultados = umlsService.searchTerm(nombreEquipo, true);
        List<String> sugerencias = new ArrayList<>();
        
        for (String resultado : resultados) {
            // Si es un mensaje de error o "no se encontraron resultados", lo ignoramos
            if (resultado.startsWith("No se encontraron") || resultado.startsWith("Error")) {
                continue;
            }
            
            // Extraemos solo el nombre sin la fuente ni la definición
            String nombreSugerido = resultado.contains(" [") ? 
                resultado.split(" \\[")[0] : resultado;
            
            if (!nombreSugerido.equals(nombreEquipo) && !sugerencias.contains(nombreSugerido)) {
                sugerencias.add(nombreSugerido);
            }
        }
        
        return sugerencias;
    }

    /**
     * Verifica si un término médico existe en UMLS
     * @param termino Término a verificar
     * @return true si el término existe en UMLS
     */
    public boolean esTerminoValido(String termino) {
        List<String> resultados = umlsService.searchTerm(termino, true);
        return resultados.stream()
                .anyMatch(r -> !r.startsWith("No se encontraron") && !r.startsWith("Error"));
    }

    /**
     * Obtiene la ontología y definición relacionada con un equipo médico
     * @param nombreEquipo Nombre del equipo
     * @return Opcional con la fuente y definición si se encuentra
     */
    public Optional<String> obtenerOntologia(String nombreEquipo) {
        List<String> resultados = umlsService.searchTerm(nombreEquipo, true);
        
        for (String resultado : resultados) {
            // Si es un mensaje de error o "no se encontraron resultados", lo ignoramos
            if (resultado.startsWith("No se encontraron") || resultado.startsWith("Error")) {
                continue;
            }
            
            // Extraer la fuente y la definición
            String fuente = "";
            String definicion = "";
            
            // Extraer la fuente entre corchetes
            if (resultado.contains(" [")) {
                int startIndex = resultado.indexOf("[") + 1;
                int endIndex = resultado.indexOf("]", startIndex);
                if (endIndex > startIndex) {
                    fuente = resultado.substring(startIndex, endIndex);
                }
            }
            
            // Extraer la definición después del guión
            if (resultado.contains(" - ")) {
                definicion = resultado.substring(resultado.indexOf(" - ") + 3);
            }
            
            // Si tenemos fuente o definición, construimos la información
            if (!fuente.isEmpty() || !definicion.isEmpty()) {
                StringBuilder info = new StringBuilder();
                if (!fuente.isEmpty()) {
                    info.append("Fuente: ").append(fuente);
                }
                if (!definicion.isEmpty()) {
                    if (!fuente.isEmpty()) {
                        info.append(" | ");
                    }
                    info.append("Definición: ").append(definicion);
                }
                return Optional.of(info.toString());
            }
        }
        
        return Optional.empty();
    }
} 