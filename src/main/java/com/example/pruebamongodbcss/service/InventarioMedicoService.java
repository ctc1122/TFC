package com.example.pruebamongodbcss.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class InventarioMedicoService {
    private final ULMSService umlsService;
    private final BioPortalService bioPortalService;

    public InventarioMedicoService(String apiKey) {
        this.umlsService = new ULMSService(apiKey);
        this.bioPortalService = new BioPortalService(apiKey);
    }

    /**
     * Valida y estandariza el nombre de un equipo médico usando ambos servicios
     * @param nombreEquipo Nombre del equipo a validar
     * @return Nombre estandarizado si se encuentra, o el original si no hay coincidencias
     */
    public String validarNombreEquipo(String nombreEquipo) {
        // Primero intentamos con BioPortal
        List<String> resultadosBioPortal = bioPortalService.searchTerm(nombreEquipo, true);
        if (!resultadosBioPortal.isEmpty() && !resultadosBioPortal.get(0).startsWith("Error:") && !resultadosBioPortal.get(0).startsWith("No se encontraron")) {
            String primerResultado = resultadosBioPortal.get(0);
            return primerResultado.split(" \\[")[0];
        }

        // Si no hay resultados en BioPortal, intentamos con UMLS
        List<String> resultadosUMLS = umlsService.searchTerm(nombreEquipo, true);
        if (!resultadosUMLS.isEmpty() && !resultadosUMLS.get(0).startsWith("Error:") && !resultadosUMLS.get(0).startsWith("No se encontraron")) {
            String primerResultado = resultadosUMLS.get(0);
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
        List<String> sugerencias = new ArrayList<>();
        
        // Obtener sugerencias de BioPortal
        List<String> resultadosBioPortal = bioPortalService.searchTerm(nombreEquipo, true);
        for (String resultado : resultadosBioPortal) {
            if (!resultado.startsWith("Error:") && !resultado.startsWith("No se encontraron")) {
                String nombreSugerido = resultado.contains(" [") ? 
                    resultado.split(" \\[")[0] : resultado;
                if (!nombreSugerido.equals(nombreEquipo)) {
                    sugerencias.add(nombreSugerido);
                }
            }
        }

        // Obtener sugerencias de UMLS
        List<String> resultadosUMLS = umlsService.searchTerm(nombreEquipo, true);
        for (String resultado : resultadosUMLS) {
            if (!resultado.startsWith("Error:") && !resultado.startsWith("No se encontraron")) {
                String nombreSugerido = resultado.contains(" [") ? 
                    resultado.split(" \\[")[0] : resultado;
                if (!nombreSugerido.equals(nombreEquipo) && !sugerencias.contains(nombreSugerido)) {
                    sugerencias.add(nombreSugerido);
                }
            }
        }
        
        return sugerencias;
    }

    /**
     * Verifica si un término médico existe en alguno de los servicios
     * @param termino Término a verificar
     * @return true si el término existe en algún servicio
     */
    public boolean esTerminoValido(String termino) {
        // Verificar en BioPortal
        List<String> resultadosBioPortal = bioPortalService.searchTerm(termino, true);
        boolean validoEnBioPortal = resultadosBioPortal.stream()
                .anyMatch(r -> !r.startsWith("Error:") && !r.startsWith("No se encontraron"));

        if (validoEnBioPortal) return true;

        // Si no está en BioPortal, verificar en UMLS
        List<String> resultadosUMLS = umlsService.searchTerm(termino, true);
        return resultadosUMLS.stream()
                .anyMatch(r -> !r.startsWith("Error:") && !r.startsWith("No se encontraron"));
    }

    /**
     * Obtiene la ontología y definición relacionada con un equipo médico
     * @param nombreEquipo Nombre del equipo
     * @return Opcional con la fuente y definición si se encuentra
     */
    public Optional<String> obtenerOntologia(String nombreEquipo) {
        // Primero intentamos con BioPortal
        List<String> resultadosBioPortal = bioPortalService.searchTerm(nombreEquipo, true);
        for (String resultado : resultadosBioPortal) {
            if (!resultado.startsWith("Error:") && !resultado.startsWith("No se encontraron")) {
                String fuente = "";
                String definicion = "";
                
                if (resultado.contains(" [")) {
                    int startIndex = resultado.indexOf("[") + 1;
                    int endIndex = resultado.indexOf("]", startIndex);
                    if (endIndex > startIndex) {
                        fuente = resultado.substring(startIndex, endIndex);
                    }
                }
                
                if (resultado.contains(" - ")) {
                    definicion = resultado.substring(resultado.indexOf(" - ") + 3);
                }
                
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
        }

        // Si no encontramos en BioPortal, intentamos con UMLS
        List<String> resultadosUMLS = umlsService.searchTerm(nombreEquipo, true);
        for (String resultado : resultadosUMLS) {
            if (!resultado.startsWith("Error:") && !resultado.startsWith("No se encontraron")) {
                String fuente = "";
                String definicion = "";
                
                if (resultado.contains(" [")) {
                    int startIndex = resultado.indexOf("[") + 1;
                    int endIndex = resultado.indexOf("]", startIndex);
                    if (endIndex > startIndex) {
                        fuente = resultado.substring(startIndex, endIndex);
                    }
                }
                
                if (resultado.contains(" - ")) {
                    definicion = resultado.substring(resultado.indexOf(" - ") + 3);
                }
                
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
        }
        
        return Optional.empty();
    }
} 