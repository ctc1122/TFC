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

public class ULMSService {
    private final String apiKey;
    private static final String SEARCH_URL = "https://uts-ws.nlm.nih.gov/rest/search/current";
    private static final String VALIDATOR_URL = "https://utslogin.nlm.nih.gov/validateUser";
    private final HttpClient httpClient;

    public ULMSService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    private boolean validateApiKey() {
        try {
            String url = VALIDATOR_URL + "?validatorApiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8) +
                        "&apiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error validando API key: " + e.getMessage());
            return false;
        }
    }

    public List<String> searchTerm(String searchTerm, boolean preferSpanish) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }

        // Validar API key primero
        if (!validateApiKey()) {
            List<String> error = new ArrayList<>();
            error.add("Error: API key no válida");
            return error;
        }

        List<String> results = new ArrayList<>();
        
        try {
            String encodedTerm = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);
            
            // Construir URL de búsqueda UMLS
            StringBuilder urlBuilder = new StringBuilder(SEARCH_URL);
            urlBuilder.append("?string=").append(encodedTerm);
            urlBuilder.append("&partialSearch=true"); // Permitir coincidencias parciales
            urlBuilder.append("&searchType=words"); // Buscar por palabras
            urlBuilder.append("&inputType=sourceUi"); // Tipo de entrada
            urlBuilder.append("&sabs=SNOMEDCT_US,MEDLINEPLUS,MESH"); // Vocabularios médicos
            if (preferSpanish) {
                urlBuilder.append("&language=SPA"); // Preferir resultados en español
            }
            
            System.out.println("Buscando término en UMLS: " + searchTerm);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new IOException("Error al buscar término. Código: " + response.statusCode());
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            if (!jsonResponse.has("result")) {
                throw new IOException("Respuesta inválida de UMLS");
            }

            JSONObject result = jsonResponse.getJSONObject("result");
            JSONArray results_array = result.getJSONArray("results");
            
            // Procesar resultados
            for (int i = 0; i < results_array.length(); i++) {
                JSONObject item = results_array.getJSONObject(i);
                String name = item.getString("name");
                String ui = item.getString("ui");
                String source = item.getString("rootSource");
                
                // Obtener definición si está disponible
                String definition = "";
                if (item.has("definitions") && !item.isNull("definitions")) {
                    JSONArray definitions = item.getJSONArray("definitions");
                    if (definitions.length() > 0) {
                        definition = definitions.getJSONObject(0).getString("value");
                    }
                }
                
                // Construir el resultado
                StringBuilder resultBuilder = new StringBuilder(name);
                resultBuilder.append(" [").append(source).append("]");
                if (!definition.isEmpty()) {
                    resultBuilder.append(" - ").append(definition);
                }
                
                results.add(resultBuilder.toString());
            }
            
            if (results.isEmpty()) {
                results.add("No se encontraron resultados para \"" + searchTerm + "\"");
            }
            
        } catch (Exception e) {
            System.err.println("Error al buscar término: " + e.getMessage());
            results.add("Error al buscar el término: " + e.getMessage());
        }
        
        return results;
    }
} 