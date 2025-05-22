package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.File;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TipoMensaje {
        MENSAJE_PRIVADO,
        ARCHIVO,
        LISTA_USUARIOS
    }

    private TipoMensaje tipo;
    private String remitente;
    private String destinatario;
    private String contenido;
    private byte[] archivoData;
    private String nombreArchivo;
    private long tamanoArchivo;

    // Constructor para mensajes de texto
    public ChatMessage(TipoMensaje tipo, String remitente, String destinatario, String contenido) {
        this.tipo = tipo;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.contenido = contenido;
    }

    // Constructor para archivos
    public ChatMessage(String remitente, String destinatario, File archivo) {
        this.tipo = TipoMensaje.ARCHIVO;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.nombreArchivo = archivo.getName();
        this.tamanoArchivo = archivo.length();
    }

    // Getters y setters
    public TipoMensaje getTipo() { return tipo; }
    public void setTipo(TipoMensaje tipo) { this.tipo = tipo; }

    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public byte[] getArchivoData() { return archivoData; }
    public void setArchivoData(byte[] archivoData) { this.archivoData = archivoData; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }
} 