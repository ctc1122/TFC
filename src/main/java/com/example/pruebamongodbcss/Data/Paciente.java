package com.example.pruebamongodbcss.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Paciente {
    private String nombre;
    private String raza;
    private String fechaNacimiento;
    private String sexo;
    private String color;
    private String nombrePropietario;
    private String telefonoPropietario;
    private String tipoAnimal;

    /* Constructor */
    public Paciente(String nombre, String raza, String fechaNacimiento, String sexo, String color, String nombrePropietario, String telefonoPropietario) throws PatronExcepcion {
        this.setNombre(nombre);
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.color = color;
        this.nombrePropietario = nombrePropietario;
        this.telefonoPropietario = telefonoPropietario;
    }
    
    // Getters
    public String getNombre() {
        return nombre;
    }
    
    public String getRaza() {
        return this.raza;
    }
    
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public String getSexo() {
        return sexo;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getNombrePropietario() {
        return nombrePropietario;
    }
    
    public String getTelefonoPropietario() {
        return telefonoPropietario;
    }
    
    // Setters
    public void setNombre(String nombre) throws PatronExcepcion {
        Pattern patronNombre = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcher = patronNombre.matcher(nombre);
        if (!matcher.matches()) {
            throw new PatronExcepcion("Nombre no válido");
        }
        this.nombre = nombre;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }
    
    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }
    
    public void setTelefonoPropietario(String telefonoPropietario) {
        this.telefonoPropietario = telefonoPropietario;
    }
    
    /**
     * Establece el tipo de animal del paciente
     * @param tipoAnimal El tipo de animal (perro, gato, ave, etc.)
     */
    public void setTipoAnimal(String tipoAnimal) {
        this.tipoAnimal = tipoAnimal;
    }
    
    /**
     * Obtiene el tipo de animal del paciente
     * @return El tipo de animal
     */
    public String getTipoAnimal() {
        return this.tipoAnimal;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
    
    /**
     * Busca razas disponibles según el tipo de animal especificado
     * @param tipoAnimal El tipo de animal para el que se buscan razas
     * @return Array de razas disponibles para ese tipo de animal
     * @throws Exception Si hay un error en la conexión o en la respuesta
     */
    public static String[] buscarRazasPorTipoAnimal(String tipoAnimal) throws Exception {
        java.util.List<String> razasEncontradas = new java.util.ArrayList<>();
        
        // Normalizar el tipo de animal (minúsculas y sin acentos)
        String tipo = tipoAnimal.toLowerCase().trim();
        
        // Seleccionar la API adecuada según el tipo de animal
        switch (tipo) {
            case "perro":
                razasEncontradas = consultarAPIPerros();
                break;
            case "gato":
                razasEncontradas = consultarAPIGatos();
                break;
            case "ave":
                razasEncontradas = obtenerRazasAves();
                break;
            case "reptil":
                razasEncontradas = obtenerRazasReptiles();
                break;
            case "pez":
                razasEncontradas = obtenerRazesPeces();
                break;
            case "roedor":
                razasEncontradas = obtenerRazasRoedores();
                break;
            default:
                // Si no reconocemos el tipo, devolvemos algunas razas genéricas
                razasEncontradas.add("Raza común");
                razasEncontradas.add("Raza mixta");
                razasEncontradas.add("Desconocida");
        }
        
        // Ordenamos alfabéticamente
        java.util.Collections.sort(razasEncontradas);
        
        return razasEncontradas.toArray(new String[0]);
    }
    
    /**
     * Consulta la API de perros para obtener razas
     * @return Lista de razas de perros
     */
    private static java.util.List<String> consultarAPIPerros() throws Exception {
        java.util.List<String> razas = new java.util.ArrayList<>();
        
        try {
            java.net.URL apiUrl = new java.net.URL("https://dog.ceo/api/breeds/list/all");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Error al conectar con la API de perros. Código: " + responseCode);
            }
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            connection.disconnect();
            
            // Procesamiento básico de la respuesta JSON
            String jsonResponse = response.toString();
            
            // Extraer nombres de razas
            String[] partes = jsonResponse.split("\"");
            for (String parte : partes) {
                if (!parte.contains(":") && !parte.contains("{") && !parte.contains("}") 
                        && !parte.contains(",") && parte.length() > 1) {
                    razas.add(traducirRazaPerro(parte));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener razas de perros: " + e.getMessage());
            // Añadir algunas razas comunes en caso de error
            razas.add("Labrador");
            razas.add("Pastor Alemán");
            razas.add("Bulldog");
            razas.add("Caniche");
            razas.add("Beagle");
        }
        
        return razas;
    }
    
    /**
     * Consulta la API de gatos para obtener razas
     * @return Lista de razas de gatos
     */
    private static java.util.List<String> consultarAPIGatos() throws Exception {
        java.util.List<String> razas = new java.util.ArrayList<>();
        
        try {
            java.net.URL apiUrl = new java.net.URL("https://api.thecatapi.com/v1/breeds");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Error al conectar con la API de gatos. Código: " + responseCode);
            }
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            connection.disconnect();
            
            // Procesamiento básico de la respuesta JSON
            String jsonResponse = response.toString();
            
            // Extraer nombres de razas
            String[] partes = jsonResponse.split("\"name\":\"");
            for (int i = 1; i < partes.length; i++) {
                String nombre = partes[i].substring(0, partes[i].indexOf("\""));
                razas.add(traducirRazaGato(nombre));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener razas de gatos: " + e.getMessage());
            // Añadir algunas razas comunes en caso de error
            razas.add("Persa");
            razas.add("Siamés");
            razas.add("Maine Coon");
            razas.add("Bengalí");
            razas.add("Ragdoll");
        }
        
        return razas;
    }
    
    /**
     * Obtiene una lista de razas comunes de aves
     * @return Lista de razas de aves
     */
    private static java.util.List<String> obtenerRazasAves() {
        java.util.List<String> razas = new java.util.ArrayList<>();
        razas.add("Canario");
        razas.add("Periquito");
        razas.add("Agapornis");
        razas.add("Cacatúa");
        razas.add("Loro Gris Africano");
        razas.add("Guacamayo");
        razas.add("Ninfa");
        razas.add("Diamante Mandarín");
        razas.add("Jilguero");
        razas.add("Cotorra");
        return razas;
    }
    
    /**
     * Obtiene una lista de razas comunes de reptiles
     * @return Lista de razas de reptiles
     */
    private static java.util.List<String> obtenerRazasReptiles() {
        java.util.List<String> razas = new java.util.ArrayList<>();
        razas.add("Iguana Verde");
        razas.add("Gecko Leopardo");
        razas.add("Tortuga de Tierra");
        razas.add("Dragón Barbudo");
        razas.add("Camaleón Velado");
        razas.add("Pitón Real");
        razas.add("Tortuga de Orejas Rojas");
        razas.add("Boa Constrictor");
        razas.add("Lagarto Cornudo");
        razas.add("Serpiente del Maíz");
        return razas;
    }
    
    /**
     * Obtiene una lista de razas comunes de peces
     * @return Lista de razas de peces
     */
    private static java.util.List<String> obtenerRazesPeces() {
        java.util.List<String> razas = new java.util.ArrayList<>();
        razas.add("Guppy");
        razas.add("Betta");
        razas.add("Goldfish");
        razas.add("Pez Ángel");
        razas.add("Neón Tetra");
        razas.add("Molly");
        razas.add("Pez Disco");
        razas.add("Pez Payaso");
        razas.add("Carpa Koi");
        razas.add("Pez Cirujano");
        return razas;
    }
    
    /**
     * Obtiene una lista de razas comunes de roedores
     * @return Lista de razas de roedores
     */
    private static java.util.List<String> obtenerRazasRoedores() {
        java.util.List<String> razas = new java.util.ArrayList<>();
        razas.add("Hámster Sirio");
        razas.add("Hámster Ruso");
        razas.add("Cobaya Peruana");
        razas.add("Cobaya Abisinia");
        razas.add("Ratón Doméstico");
        razas.add("Jerbo");
        razas.add("Chinchilla");
        razas.add("Degú");
        razas.add("Ardilla de Richardson");
        razas.add("Rata Dumbo");
        return razas;
    }
    
    /**
     * Traduce razas de perros al castellano
     * @param razaOriginal Nombre original de la raza
     * @return Nombre traducido
     */
    private static String traducirRazaPerro(String razaOriginal) {
        java.util.Map<String, String> traducciones = new java.util.HashMap<>();
        traducciones.put("terrier", "Terrier");
        traducciones.put("bulldog", "Bulldog");
        traducciones.put("retriever", "Retriever");
        traducciones.put("shepherd", "Pastor");
        traducciones.put("poodle", "Caniche");
        traducciones.put("beagle", "Beagle");
        traducciones.put("boxer", "Boxer");
        traducciones.put("labrador", "Labrador");
        traducciones.put("husky", "Husky");
        traducciones.put("dalmatian", "Dálmata");
        
        return traducciones.getOrDefault(razaOriginal, 
                razaOriginal.substring(0, 1).toUpperCase() + razaOriginal.substring(1));
    }
    
    /**
     * Traduce razas de gatos al castellano
     * @param razaOriginal Nombre original de la raza
     * @return Nombre traducido
     */
    private static String traducirRazaGato(String razaOriginal) {
        java.util.Map<String, String> traducciones = new java.util.HashMap<>();
        traducciones.put("Persian", "Persa");
        traducciones.put("Siamese", "Siamés");
        traducciones.put("Maine Coon", "Maine Coon");
        traducciones.put("Ragdoll", "Ragdoll");
        traducciones.put("Bengal", "Bengalí");
        traducciones.put("Abyssinian", "Abisinio");
        traducciones.put("Birman", "Birmano");
        traducciones.put("Oriental", "Oriental");
        traducciones.put("Sphynx", "Esfinge");
        traducciones.put("British Shorthair", "Británico de Pelo Corto");
        
        return traducciones.getOrDefault(razaOriginal, razaOriginal);
    }
}
