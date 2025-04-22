package com.example.pruebamongodbcss.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class InventarioMedicoService {
    private final BioPortalService bioPortalService;

    public InventarioMedicoService(String apiKey) {
        this.bioPortalService = new BioPortalService(apiKey);
    }

    /**
     * Valida y estandariza el nombre de un equipo médico usando BioPortal
     * @param nombreEquipo Nombre del equipo a validar
     * @return Nombre estandarizado si se encuentra, o el original si no hay coincidencias
     */
    public String validarNombreEquipo(String nombreEquipo) {
        List<String> resultados = bioPortalService.searchTerm(nombreEquipo, true);
        if (!resultados.isEmpty()) {
            // Tomamos el primer resultado como el más relevante
            String primerResultado = resultados.get(0);
            // Extraemos solo el nombre sin la URL
            return primerResultado.split(" \\[")[0];
        }
        return nombreEquipo; // Devolvemos el original si no hay coincidencias
    }

    /**
     * Sugiere términos relacionados para un equipo médico
     * @param nombreEquipo Nombre del equipo
     * @return Lista de sugerencias
     */
    public List<String> obtenerSugerencias(String nombreEquipo) {
        List<String> resultados = bioPortalService.searchTerm(nombreEquipo, true);
        List<String> sugerencias = new ArrayList<>();
        
        for (String resultado : resultados) {
            // Si es un mensaje de error o "no se encontraron resultados", lo ignoramos
            if (resultado.startsWith("Error:") || resultado.startsWith("No se encontraron")) {
                continue;
            }
            
            // Extraemos solo el nombre sin la URL ni el tipo semántico
            String nombreSugerido = resultado;
            if (resultado.contains(" [")) {
                nombreSugerido = resultado.split(" \\[")[0];
            }
            
            // Eliminamos el tipo semántico si existe
            if (nombreSugerido.contains(" (Tipo:")) {
                nombreSugerido = nombreSugerido.split(" \\(Tipo:")[0];
            }
            
            if (!nombreSugerido.equals(nombreEquipo)) {
                sugerencias.add(nombreSugerido);
            }
        }
        
        return sugerencias;
    }

    /**
     * Verifica si un término médico existe en BioPortal
     * @param termino Término a verificar
     * @return true si el término existe en BioPortal
     */
    public boolean esTerminoValido(String termino) {
        List<String> resultados = bioPortalService.searchTerm(termino, true);
        // Verificamos que los resultados no sean mensajes de error
        return resultados.stream()
                .anyMatch(r -> !r.startsWith("Error:") && !r.startsWith("No se encontraron"));
    }

    /**
     * Obtiene la ontología relacionada con un equipo médico
     * @param nombreEquipo Nombre del equipo
     * @return Opcional con la URL de la ontología si se encuentra
     */
    public Optional<String> obtenerOntologia(String nombreEquipo) {
        List<String> resultados = bioPortalService.searchTerm(nombreEquipo, true);
        
        for (String resultado : resultados) {
            // Si es un mensaje de error o "no se encontraron resultados", lo ignoramos
            if (resultado.startsWith("Error:") || resultado.startsWith("No se encontraron")) {
                continue;
            }
            
            // Extraemos la URL de la ontología
            int startIndex = resultado.indexOf("[") + 1;
            int endIndex = resultado.indexOf("]");
            
            // Extraemos el tipo semántico si existe
            String tipoSemantico = "";
            if (resultado.contains("(Tipo:")) {
                int tipoStartIndex = resultado.indexOf("(Tipo:") + 6;
                int tipoEndIndex = resultado.indexOf(")", tipoStartIndex);
                if (tipoEndIndex > tipoStartIndex) {
                    tipoSemantico = " - " + resultado.substring(tipoStartIndex, tipoEndIndex).trim();
                }
            }
            
            if (startIndex > 0 && endIndex > startIndex) {
                String ontologia = resultado.substring(startIndex, endIndex);
                return Optional.of(ontologia + tipoSemantico);
            }
        }
        
        return Optional.empty();
    }
} 