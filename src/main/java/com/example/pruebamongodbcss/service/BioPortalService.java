package com.example.pruebamongodbcss.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class BioPortalService {
    private final String apiKey;
    private static final String BASE_URL = "https://data.bioontology.org";
    private final HttpClient httpClient;

    public BioPortalService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<String> searchTerm(String searchTerm) {
        return searchTerm(searchTerm, false);
    }

    public List<String> searchTerm(String searchTerm, boolean preferSpanish) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }

        List<String> results = new ArrayList<>();
        
        try {
            String encodedTerm = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);
            // Simplificamos la URL para evitar problemas con parámetros no compatibles
            String url = BASE_URL + "/search?q=" + encodedTerm;
            
            // Ampliamos a varias ontologías médicas relevantes
            url += "&ontologies=SNOMEDCT,MEDLINEPLUS,MESH,NCIT,RCD";
            url += "&pagesize=15"; // Aumentamos a 15 resultados para tener más opciones
            
            // Si queremos términos en español, intentamos priorizar fuentes con terminología en español
            if (preferSpanish) {
                url += "&include=prefLabel,synonym";
            }
            
            System.out.println("URL de búsqueda: " + url);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "apikey token=" + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.err.println("Error en BioPortal: " + response.statusCode() + " - " + response.body());
                throw new IOException("Failed to get data from BioPortal. Status code: " + response.statusCode());
            }

            String responseBody = response.body();
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            if (!jsonResponse.has("collection")) {
                throw new IOException("Invalid response from BioPortal: no collection found");
            }
            
            JSONArray collection = jsonResponse.getJSONArray("collection");
            
            for (int i = 0; i < collection.length() && i < 15; i++) {
                JSONObject item = collection.getJSONObject(i);
                String prefLabel = item.optString("prefLabel", "");
                
                // Extraer información de la ontología de manera segura
                String ontology = "";
                JSONObject links = item.optJSONObject("links");
                if (links != null) {
                    ontology = links.optString("ontology", "");
                }
                
                // Si preferimos español, intentamos obtener la etiqueta en español
                if (preferSpanish) {
                    JSONObject synonyms = item.optJSONObject("synonym");
                    if (synonyms != null) {
                        JSONArray spanishSynonyms = synonyms.optJSONArray("es");
                        if (spanishSynonyms != null && spanishSynonyms.length() > 0) {
                            prefLabel = spanishSynonyms.getString(0);
                        } else if (prefLabel.toLowerCase().contains(searchTerm.toLowerCase())) {
                            // Si no hay sinónimo español pero la etiqueta principal contiene el término de búsqueda
                            // la mantenemos como está
                        }
                    }
                }
                
                // Agregar etiqueta de tipo si está disponible (Instrumento, Procedimiento, etc.)
                String semanticType = "";
                try {
                    if (item.has("semanticType")) {
                        JSONArray semanticTypes = item.optJSONArray("semanticType");
                        if (semanticTypes != null && semanticTypes.length() > 0) {
                            semanticType = " (Tipo: " + semanticTypes.getString(0) + ")";
                        }
                    }
                } catch (Exception e) {
                    // Ignoramos errores en el procesamiento de tipos semánticos
                }
                
                if (!prefLabel.isEmpty()) {
                    results.add(prefLabel + (ontology.isEmpty() ? "" : " [" + ontology + "]") + semanticType);
                }
            }
            
            if (results.isEmpty()) {
                results.add("No se encontraron resultados para \"" + searchTerm + "\"");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            results.add("Error: " + e.getMessage());
        }
        
        return results;
    }
} 