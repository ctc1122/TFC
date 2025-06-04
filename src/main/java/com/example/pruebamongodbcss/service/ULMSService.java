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
    private static final String URI_BASE = "https://uts-ws.nlm.nih.gov";
    private final HttpClient httpClient;

    // Clase interna para representar un término médico
    public static class MedicalTerm {
        private final String name;
        private final String code;
        private final String source;
        private final String uri;

        public MedicalTerm(String name, String code, String source, String uri) {
            this.name = name;
            this.code = code;
            this.source = source;
            this.uri = uri;
        }

        public String getName() { return name; }
        public String getCode() { return code; }
        public String getSource() { return source; }
        public String getUri() { return uri; }

        @Override
        public String toString() {
            return name + " [" + code + " - " + source + "]";
        }
    }

    public ULMSService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<MedicalTerm> searchTermSuggestions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }

        List<MedicalTerm> suggestions = new ArrayList<>();
        String version = "current";
        String contentEndpoint = "/rest/search/" + version;
        String fullUrl = URI_BASE + contentEndpoint;
        int page = 0;

        try {
            while (true) {
                page++;
                StringBuilder queryBuilder = new StringBuilder(fullUrl);
                queryBuilder.append("?string=").append(URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8));
                queryBuilder.append("&apiKey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
                queryBuilder.append("&pageNumber=").append(page);
                
                // Parámetros para mejorar la búsqueda
                queryBuilder.append("&language=SPA"); // Priorizar resultados en español
                queryBuilder.append("&searchType=words"); // Búsqueda por palabras
                queryBuilder.append("&inputType=atom"); // Buscar en todos los términos
                queryBuilder.append("&sabs=SCTSPA"); // SNOMED CT en español
                queryBuilder.append(",MSHSPA"); // MeSH en español
                queryBuilder.append(",MDRSPA"); // MedDRA en español
                queryBuilder.append(",SNOMEDCT_US"); // SNOMED CT US como respaldo
                queryBuilder.append(",MEDLINEPLUS"); // MedlinePlus
                queryBuilder.append("&returnIdType=concept"); // Devolver conceptos
                queryBuilder.append("&pageSize=25"); // 25 resultados por página
                queryBuilder.append("&partialSearch=true"); // Permitir búsquedas parciales
                
                String url = queryBuilder.toString();
                System.out.println("Buscando sugerencias... Página " + page);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("Error en la búsqueda. Código: " + response.statusCode());
                }

                JSONObject outputs = new JSONObject(response.body());
                JSONArray items = outputs.getJSONObject("result").getJSONArray("results");

                if (items.length() == 0) {
                    // Si no hay resultados en español, intentar sin filtro de idioma
                    if (page == 1 && suggestions.isEmpty()) {
                        queryBuilder = new StringBuilder(fullUrl);
                        queryBuilder.append("?string=").append(URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8));
                        queryBuilder.append("&apiKey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
                        queryBuilder.append("&pageNumber=1");
                        queryBuilder.append("&searchType=words");
                        queryBuilder.append("&sabs=SNOMEDCT_US,MEDLINEPLUS,MESH");
                        queryBuilder.append("&returnIdType=concept");
                        queryBuilder.append("&partialSearch=true"); // Permitir búsquedas parciales también en el respaldo
                        
                        request = HttpRequest.newBuilder()
                            .uri(URI.create(queryBuilder.toString()))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            outputs = new JSONObject(response.body());
                            items = outputs.getJSONObject("result").getJSONArray("results");
                        }
                    }
                    if (items.length() == 0) {
                        break;
                    }
                }

                for (int i = 0; i < items.length(); i++) {
                    JSONObject result = items.getJSONObject(i);
                    String name = result.getString("name");
                    String ui = result.getString("ui");
                    String rootSource = result.getString("rootSource");
                    String uri = result.getString("uri");

                    // Filtrar resultados no deseados
                    if (!name.contains(":") && !name.startsWith("HEDIS")) {
                        suggestions.add(new MedicalTerm(name, ui, rootSource, uri));
                    }
                }

                // Si tenemos suficientes resultados relevantes, paramos
                if (suggestions.size() >= 10 || items.length() < 25) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error en la búsqueda: " + e.getMessage());
            e.printStackTrace();
        }

        return suggestions;
    }

    // Mantener el método anterior para compatibilidad
    public List<String> searchTerm(String searchTerm, boolean preferSpanish) {
        List<MedicalTerm> suggestions = searchTermSuggestions(searchTerm);
        List<String> results = new ArrayList<>();
        
        if (suggestions.isEmpty()) {
            results.add("No se encontraron resultados para \"" + searchTerm + "\"");
        } else {
            for (MedicalTerm term : suggestions) {
                results.add(term.toString());
            }
        }
        
        return results;
    }
} 