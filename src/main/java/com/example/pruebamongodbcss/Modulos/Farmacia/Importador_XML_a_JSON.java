package com.example.pruebamongodbcss.Modulos.Farmacia;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bson.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Importador_XML_a_JSON {
    public static void main(String[] args) throws Exception {
        // --- Importar DICCIONARIO_DCPF.xml a nombre_unidades ---
        String xmlPathDCPF = "src/CIMA/DICCIONARIO_DCPF.xml";
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase("Inventario");
        MongoCollection<Document> coleccionUnidades = db.getCollection("nombre_unidades");
        coleccionUnidades.deleteMany(new Document());
        System.out.println("Colección 'nombre_unidades' vaciada antes de la importación.");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        org.w3c.dom.Document docDCPF = docBuilder.parse(new File(xmlPathDCPF));
        NodeList registrosDCPF = docDCPF.getElementsByTagName("dcpf");
        System.out.println("Registros encontrados en DICCIONARIO_DCPF.xml: " + registrosDCPF.getLength());
        int insertadosDCPF = 0;
        for (int i = 0; i < registrosDCPF.getLength(); i++) {
            Element reg = (Element) registrosDCPF.item(i);
            String codigodcpf = reg.getElementsByTagName("codigodcpf").item(0).getTextContent();
            String dimension = reg.getElementsByTagName("nombredcpf").item(0).getTextContent();
            String codigodcp = reg.getElementsByTagName("codigodcp").item(0).getTextContent();
            Document docMongo = new Document()
                .append("codigodcpf", codigodcpf)
                .append("dimension", dimension)
                .append("codigodcp", codigodcp);
            if (i < 3) {
                System.out.println("[DCPF] Ejemplo registro: " + docMongo.toJson());
            }
            coleccionUnidades.insertOne(docMongo);
            insertadosDCPF++;
        }
        System.out.println("Total documentos insertados en nombre_unidades: " + insertadosDCPF);

        // --- Importar DICCIONARIO_DCP.xml a nombre_via_administracion ---
        String xmlPathDCP = "src/CIMA/DICCIONARIO_DCP.xml";
        MongoCollection<Document> coleccionVia = db.getCollection("nombre_via_administracion");
        coleccionVia.deleteMany(new Document());
        System.out.println("Colección 'nombre_via_administracion' vaciada antes de la importación.");

        org.w3c.dom.Document docDCP = docBuilder.parse(new File(xmlPathDCP));
        NodeList registrosDCP = docDCP.getElementsByTagName("dcp");
        System.out.println("Registros encontrados en DICCIONARIO_DCP.xml: " + registrosDCP.getLength());
        int insertadosDCP = 0;
        for (int i = 0; i < registrosDCP.getLength(); i++) {
            Element reg = (Element) registrosDCP.item(i);
            String codigodcp = reg.getElementsByTagName("codigodcp").item(0).getTextContent();
            String viaAdmin = reg.getElementsByTagName("nombredcp").item(0).getTextContent();
            String codigodcsa = reg.getElementsByTagName("codigodcsa").item(0).getTextContent();
            Document docMongo = new Document()
                .append("codigodcp", codigodcp)
                .append("ViaAdmin", viaAdmin)
                .append("codigodcsa", codigodcsa);
            if (i < 3) {
                System.out.println("[DCP] Ejemplo registro: " + docMongo.toJson());
            }
            coleccionVia.insertOne(docMongo);
            insertadosDCP++;
        }
        System.out.println("Total documentos insertados en nombre_via_administracion: " + insertadosDCP);

        mongoClient.close();
        System.out.println("Importación completada de ambos XMLs.");
    }
} 