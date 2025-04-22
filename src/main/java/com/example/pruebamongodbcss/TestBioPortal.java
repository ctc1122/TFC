package com.example.pruebamongodbcss;

import com.example.pruebamongodbcss.service.BioPortalService;

public class TestBioPortal {
    public static void main(String[] args) {
        // Reemplaza "TU-API-KEY-AQUI" con tu API key real de BioPortal
        BioPortalService service = new BioPortalService("ba9de3d5-083d-45e0-88d9-4deb353a28d4");
        
        // Buscamos términos relacionados con equipos médicos
        String[] searchTerms = {
            "monitor de signos vitales",
            "electrocardiógrafo",
            "desfibrilador",
            "ventilador mecánico"
        };
        
        for (String term : searchTerms) {
            System.out.println("\nBuscando: " + term);
            System.out.println("----------------------------------------");
            
            try {
                var results = service.searchTerm(term);
                System.out.println("Resultados encontrados: " + results.size());
                for (String result : results) {
                    System.out.println("\n" + result);
                    System.out.println("----------------------------------------");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 