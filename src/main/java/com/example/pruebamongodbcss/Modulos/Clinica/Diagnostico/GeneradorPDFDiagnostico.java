package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Clase utilitaria para generar PDFs de diagnósticos veterinarios con diseño moderno y compacto.
 */
public class GeneradorPDFDiagnostico {
    
    // Fuentes modernas y compactas
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(33, 37, 41));
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(52, 58, 64));
    private static final Font FONT_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(73, 80, 87));
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(33, 37, 41));
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7, new BaseColor(108, 117, 125));
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(73, 80, 87));
    
    // Colores modernos (inspirados en Material Design)
    private static final BaseColor COLOR_PRIMARY = new BaseColor(25, 118, 210);      // Azul moderno
    private static final BaseColor COLOR_SECONDARY = new BaseColor(245, 245, 245);   // Gris muy claro
    private static final BaseColor COLOR_ACCENT = new BaseColor(13, 110, 253);       // Azul accent
    private static final BaseColor COLOR_TEXT_LIGHT = new BaseColor(108, 117, 125);  // Texto secundario
    
    /**
     * Genera un PDF compacto y moderno del diagnóstico veterinario.
     */
    public static void generarPDFDiagnostico(
            File archivo,
            ModeloPaciente paciente,
            ModeloPropietario propietario,
            ModeloCita cita,
            ModeloDiagnostico diagnostico,
            java.util.List<ModeloDiagnosticoUMLS> diagnosticosSeleccionados,
            String anamnesis,
            String examenFisico,
            String tratamiento,
            String observaciones,
            Date proximaVisita) throws DocumentException, IOException {
        
        Document document = new Document(PageSize.A4, 30, 30, 30, 30); // Márgenes reducidos
        PdfWriter.getInstance(document, new FileOutputStream(archivo));
        
        document.open();
        
        try {
            // Encabezado compacto
            agregarEncabezadoModerno(document);
            
            // Información del paciente y propietario en una sola tabla compacta
            agregarInformacionCompacta(document, paciente, propietario, cita);
            
            // Información del diagnóstico existente (si existe)
            if (diagnostico != null) {
                agregarInfoDiagnostico(document, diagnostico);
            }
            
            // Secciones clínicas en formato compacto
            if (anamnesis != null && !anamnesis.trim().isEmpty()) {
                agregarSeccionCompacta(document, "ANAMNESIS", anamnesis);
            }
            
            if (examenFisico != null && !examenFisico.trim().isEmpty()) {
                agregarSeccionCompacta(document, "EXPLORACIÓN FÍSICA", examenFisico);
            }
            
            // Diagnósticos en tabla compacta
            if (diagnosticosSeleccionados != null && !diagnosticosSeleccionados.isEmpty()) {
                agregarDiagnosticosCompactos(document, diagnosticosSeleccionados);
            }
            
            if (tratamiento != null && !tratamiento.trim().isEmpty()) {
                agregarSeccionCompacta(document, "PLAN TERAPÉUTICO", tratamiento);
            }
            
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                agregarSeccionCompacta(document, "OBSERVACIONES", observaciones);
            }
            
            if (proximaVisita != null) {
                agregarProximaVisitaCompacta(document, proximaVisita);
            }
            
            // Pie de página moderno
            agregarPieModerno(document);
            
        } finally {
            document.close();
        }
    }
    
    /**
     * Encabezado moderno y compacto.
     */
    private static void agregarEncabezadoModerno(Document document) throws DocumentException {
        // Tabla para el encabezado
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{70, 30});
        headerTable.setSpacingAfter(15);
        
        // Título principal
        PdfPCell titleCell = new PdfPCell(new Phrase("INFORME CLÍNICO VETERINARIO", FONT_TITULO));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingBottom(5);
        headerTable.addCell(titleCell);
        
        // Fecha y número de informe
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaHora = sdf.format(new Date());
        String numeroInforme = "Nº " + System.currentTimeMillis() % 100000;
        
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.addElement(new Paragraph("Fecha: " + fechaHora, FONT_SMALL));
        infoCell.addElement(new Paragraph("Informe: " + numeroInforme, FONT_SMALL));
        headerTable.addCell(infoCell);
        
        document.add(headerTable);
        
        // Línea separadora sutil
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        lineTable.setSpacingAfter(10);
        PdfPCell lineCell = new PdfPCell(new Phrase(" ", FONT_SMALL));
        lineCell.setBackgroundColor(COLOR_PRIMARY);
        lineCell.setFixedHeight(2);
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineTable.addCell(lineCell);
        document.add(lineTable);
    }
    
    /**
     * Información del paciente, propietario y cita en formato compacto.
     */
    private static void agregarInformacionCompacta(Document document, ModeloPaciente paciente, 
            ModeloPropietario propietario, ModeloCita cita) throws DocumentException {
        
        // Tabla principal con 4 columnas para máxima compacidad
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{25, 25, 25, 25});
        tabla.setSpacingAfter(12);
        
        // Encabezado de paciente
        PdfPCell headerPaciente = new PdfPCell(new Phrase("DATOS DEL PACIENTE", FONT_SUBTITULO));
        headerPaciente.setColspan(2);
        headerPaciente.setBackgroundColor(COLOR_PRIMARY);
        headerPaciente.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerPaciente.setPadding(6);
        tabla.addCell(headerPaciente);
        
        // Encabezado de propietario
        PdfPCell headerPropietario = new PdfPCell(new Phrase("DATOS DEL PROPIETARIO", FONT_SUBTITULO));
        headerPropietario.setColspan(2);
        headerPropietario.setBackgroundColor(COLOR_PRIMARY);
        headerPropietario.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerPropietario.setPadding(6);
        tabla.addCell(headerPropietario);
        
        // Datos del paciente
        agregarCeldaCompacta(tabla, "Nombre:", paciente.getNombre());
        agregarCeldaCompacta(tabla, "Especie:", paciente.getEspecie());
        
        // Datos del propietario (si existe)
        if (propietario != null) {
            agregarCeldaCompacta(tabla, "Propietario:", propietario.getNombreCompleto());
            agregarCeldaCompacta(tabla, "DNI:", propietario.getDni());
            
            agregarCeldaCompacta(tabla, "Raza:", paciente.getRaza());
            agregarCeldaCompacta(tabla, "Sexo:", paciente.getSexo());
            
            String telefono = (propietario.getTelefono() != null && !propietario.getTelefono().isEmpty()) 
                    ? propietario.getTelefono() : "No especificado";
            agregarCeldaCompacta(tabla, "Teléfono:", telefono);
            
            String email = (propietario.getEmail() != null && !propietario.getEmail().isEmpty()) 
                    ? propietario.getEmail() : "No especificado";
            agregarCeldaCompacta(tabla, "Email:", email);
        } else {
            agregarCeldaCompacta(tabla, "Propietario:", "No especificado");
            agregarCeldaCompacta(tabla, "DNI:", "No especificado");
            agregarCeldaCompacta(tabla, "Raza:", paciente.getRaza());
            agregarCeldaCompacta(tabla, "Sexo:", paciente.getSexo());
            agregarCeldaCompacta(tabla, "Teléfono:", "No especificado");
            agregarCeldaCompacta(tabla, "Email:", "No especificado");
        }
        
        // Peso y edad en una fila
        agregarCeldaCompacta(tabla, "Peso:", paciente.getPeso() + " kg");
        
        String edad = "No especificada";
        if (paciente.getFechaNacimiento() != null) {
            long edadMilis = new Date().getTime() - paciente.getFechaNacimiento().getTime();
            long edadAnios = edadMilis / (1000L * 60 * 60 * 24 * 365);
            edad = edadAnios + " años";
        }
        agregarCeldaCompacta(tabla, "Edad:", edad);
        
        // Información de la cita (si existe)
        if (cita != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String fechaCita = cita.getFechaHora() != null ? 
                    sdf.format(java.sql.Timestamp.valueOf(cita.getFechaHora())) : "No especificada";
            agregarCeldaCompacta(tabla, "Fecha cita:", fechaCita);
            
            String motivo = (cita.getMotivo() != null && !cita.getMotivo().isEmpty()) 
                    ? cita.getMotivo() : "Consulta general";
            agregarCeldaCompacta(tabla, "Motivo:", motivo);
        } else {
            agregarCeldaCompacta(tabla, "Fecha cita:", "No especificada");
            agregarCeldaCompacta(tabla, "Motivo:", "No especificado");
        }
        
        document.add(tabla);
    }
    
    /**
     * Información del diagnóstico existente de forma compacta.
     */
    private static void agregarInfoDiagnostico(Document document, ModeloDiagnostico diagnostico) 
            throws DocumentException {
        
        if (diagnostico.getFecha() != null || 
            (diagnostico.getVeterinario() != null && !diagnostico.getVeterinario().isEmpty())) {
            
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setSpacingAfter(8);
            tabla.setWidths(new float[]{25, 25, 25, 25});
            
            if (diagnostico.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                agregarCeldaCompacta(tabla, "Fecha diagnóstico:", sdf.format(diagnostico.getFecha()));
            } else {
                agregarCeldaCompacta(tabla, "Fecha diagnóstico:", "No especificada");
            }
            
            String veterinario = (diagnostico.getVeterinario() != null && !diagnostico.getVeterinario().isEmpty()) 
                    ? diagnostico.getVeterinario() : "No especificado";
            agregarCeldaCompacta(tabla, "Veterinario:", veterinario);
            
            // Celdas vacías para completar la fila
            agregarCeldaCompacta(tabla, "", "");
            agregarCeldaCompacta(tabla, "", "");
            
            document.add(tabla);
        }
    }
    
    /**
     * Sección de texto en formato compacto.
     */
    private static void agregarSeccionCompacta(Document document, String titulo, String contenido) 
            throws DocumentException {
        
        // Título de sección con fondo
        PdfPTable tituloTable = new PdfPTable(1);
        tituloTable.setWidthPercentage(100);
        tituloTable.setSpacingBefore(8);
        tituloTable.setSpacingAfter(3);
        
        PdfPCell tituloCell = new PdfPCell(new Phrase(titulo, FONT_SECCION));
        tituloCell.setBackgroundColor(COLOR_SECONDARY);
        tituloCell.setPadding(4);
        tituloCell.setBorder(Rectangle.NO_BORDER);
        tituloTable.addCell(tituloCell);
        document.add(tituloTable);
        
        // Contenido
        Paragraph contenidoParagraph = new Paragraph(contenido, FONT_NORMAL);
        contenidoParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        contenidoParagraph.setSpacingAfter(5);
        contenidoParagraph.setIndentationLeft(10);
        document.add(contenidoParagraph);
    }
    
    /**
     * Tabla de diagnósticos en formato compacto.
     */
    private static void agregarDiagnosticosCompactos(Document document, 
            java.util.List<ModeloDiagnosticoUMLS> diagnosticos) throws DocumentException {
        
        // Título
        PdfPTable tituloTable = new PdfPTable(1);
        tituloTable.setWidthPercentage(100);
        tituloTable.setSpacingBefore(8);
        tituloTable.setSpacingAfter(3);
        
        PdfPCell tituloCell = new PdfPCell(new Phrase("DIAGNÓSTICOS", FONT_SECCION));
        tituloCell.setBackgroundColor(COLOR_SECONDARY);
        tituloCell.setPadding(4);
        tituloCell.setBorder(Rectangle.NO_BORDER);
        tituloTable.addCell(tituloCell);
        document.add(tituloTable);
        
        // Tabla de diagnósticos compacta
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{65, 20, 15});
        tabla.setSpacingAfter(8);
        
        // Encabezados
        agregarCeldaHeaderCompacta(tabla, "Descripción");
        agregarCeldaHeaderCompacta(tabla, "Código CUI");
        agregarCeldaHeaderCompacta(tabla, "Fuente");
        
        // Datos
        for (ModeloDiagnosticoUMLS diag : diagnosticos) {
            agregarCeldaDatosCompacta(tabla, diag.getStr());
            agregarCeldaDatosCompacta(tabla, diag.getCui());
            agregarCeldaDatosCompacta(tabla, diag.getSab());
        }
        
        document.add(tabla);
    }
    
    /**
     * Próxima visita en formato compacto.
     */
    private static void agregarProximaVisitaCompacta(Document document, Date proximaVisita) 
            throws DocumentException {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setSpacingAfter(8);
        tabla.setWidths(new float[]{30, 70});
        
        agregarCeldaCompacta(tabla, "Próxima revisión:", sdf.format(proximaVisita));
        
        document.add(tabla);
    }
    
    /**
     * Pie de página moderno y compacto.
     */
    private static void agregarPieModerno(Document document) throws DocumentException {
        // Espacio mínimo
        document.add(new Paragraph(" ", FONT_SMALL));
        
        // Tabla para firma
        PdfPTable firmaTable = new PdfPTable(2);
        firmaTable.setWidthPercentage(100);
        firmaTable.setSpacingBefore(15);
        firmaTable.setWidths(new float[]{50, 50});
        
        PdfPCell firmaCell = new PdfPCell();
        firmaCell.setBorder(Rectangle.NO_BORDER);
        firmaCell.addElement(new Paragraph("Firma y sello del veterinario:", FONT_SMALL));
        firmaCell.addElement(new Paragraph(" ", FONT_NORMAL));
        firmaCell.addElement(new Paragraph("_".repeat(30), FONT_SMALL));
        firmaTable.addCell(firmaCell);
        
        PdfPCell fechaCell = new PdfPCell();
        fechaCell.setBorder(Rectangle.NO_BORDER);
        fechaCell.addElement(new Paragraph("Fecha:", FONT_SMALL));
        fechaCell.addElement(new Paragraph(" ", FONT_NORMAL));
        fechaCell.addElement(new Paragraph("_".repeat(20), FONT_SMALL));
        firmaTable.addCell(fechaCell);
        
        document.add(firmaTable);
        
        // Información del sistema
        Paragraph info = new Paragraph("Documento generado automáticamente por el Sistema de Gestión Veterinaria", FONT_SMALL);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingBefore(10);
        document.add(info);
    }
    
    /**
     * Método auxiliar para agregar celdas compactas.
     */
    private static void agregarCeldaCompacta(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FONT_LABEL));
        celdaEtiqueta.setBackgroundColor(COLOR_SECONDARY);
        celdaEtiqueta.setPadding(3);
        celdaEtiqueta.setBorderWidth(0.5f);
        celdaEtiqueta.setBorderColor(BaseColor.LIGHT_GRAY);
        tabla.addCell(celdaEtiqueta);
        
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor != null ? valor : "No especificado", FONT_NORMAL));
        celdaValor.setPadding(3);
        celdaValor.setBorderWidth(0.5f);
        celdaValor.setBorderColor(BaseColor.LIGHT_GRAY);
        tabla.addCell(celdaValor);
    }
    
    /**
     * Método auxiliar para encabezados compactos.
     */
    private static void agregarCeldaHeaderCompacta(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FONT_SECCION));
        celda.setBackgroundColor(COLOR_ACCENT);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(4);
        celda.setBorderWidth(0.5f);
        celda.setBorderColor(BaseColor.WHITE);
        tabla.addCell(celda);
    }
    
    /**
     * Método auxiliar para datos compactos.
     */
    private static void agregarCeldaDatosCompacta(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", FONT_NORMAL));
        celda.setPadding(3);
        celda.setBorderWidth(0.5f);
        celda.setBorderColor(BaseColor.LIGHT_GRAY);
        tabla.addCell(celda);
    }
} 