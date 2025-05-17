package com.example.pruebamongodbcss.Modulos.Farmacia;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio para interactuar con la base de datos CIMA.
 */
public class ServicioMedicamentoCIMA {
    private static final String CONNECTION_STRING = "mongodb://localhost:27020";
    private static final String DATABASE_NAME = "cima";
    private static final String COLLECTION_NAME = "medicamentos";
    
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    
    public ServicioMedicamentoCIMA() {
        this.mongoClient = MongoClients.create(CONNECTION_STRING);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
        
        // Crear índices si no existen
        collection.createIndex(Indexes.text("nombre"));
        collection.createIndex(Indexes.ascending("codigo"));
    }
    
    /**
     * Obtiene todos los medicamentos de la base de datos.
     * @return Lista observable de medicamentos
     */
    public ObservableList<ModeloMedicamentoCIMA> obtenerTodosMedicamentos() {
        ObservableList<ModeloMedicamentoCIMA> medicamentos = FXCollections.observableArrayList();
        
        collection.find().limit(1000).into(new ArrayList<>()).forEach(doc -> {
            medicamentos.add(documentoAMedicamento(doc));
        });
        
        return medicamentos;
    }
    
    /**
     * Busca medicamentos por nombre.
     * @param texto Texto a buscar
     * @return Lista observable de medicamentos que coinciden con la búsqueda
     */
    public ObservableList<ModeloMedicamentoCIMA> buscarMedicamentos(String texto) {
        if (texto == null || texto.isBlank()) {
            return FXCollections.observableArrayList();
        }
        
        ObservableList<ModeloMedicamentoCIMA> medicamentos = FXCollections.observableArrayList();
        
        Bson filter = Filters.text(texto);
        
        collection.find(filter).limit(50).into(new ArrayList<>()).forEach(doc -> {
            medicamentos.add(documentoAMedicamento(doc));
        });
        
        return medicamentos;
    }
    
    /**
     * Busca un medicamento por su código.
     * @param codigo Código del medicamento
     * @return ModeloMedicamentoCIMA encontrado o null si no existe
     */
    public ModeloMedicamentoCIMA buscarMedicamentoPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        
        Document doc = collection.find(Filters.eq("codigo", codigo)).first();
        return doc != null ? documentoAMedicamento(doc) : null;
    }
    
    /**
     * Convierte un documento de MongoDB a un objeto ModeloMedicamentoCIMA.
     * @param doc Documento de MongoDB
     * @return ModeloMedicamentoCIMA
     */
    private ModeloMedicamentoCIMA documentoAMedicamento(Document doc) {
        return new ModeloMedicamentoCIMA(
            doc.getString("codigo"),
            doc.getString("nombre")
        );
    }
    
    /**
     * Comprueba la conexión a la base de datos.
     * @return true si la conexión se estableció correctamente, false en caso contrario
     */
    public boolean comprobarConexion() {
        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("Error al comprobar conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cierra la conexión a la base de datos.
     */
    public void cerrarConexion() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
} 