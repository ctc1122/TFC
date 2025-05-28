package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Clase para exportar fichajes a diferentes formatos (Excel y PDF)
 */
public class ExportadorFichajes {
    
    /**
     * Exporta una lista de fichajes a un archivo Excel (.xls)
     */
    public static void exportarAExcel(List<ModeloFichaje> fichajes, String nombreEmpleado, 
                                     LocalDate fechaInicio, LocalDate fechaFin, Stage parentStage) {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel (*.xls)", "*.xls")
        );
        
        // Nombre sugerido para el archivo
        String nombreArchivo = "Fichajes_" + nombreEmpleado.replaceAll("\\s+", "_");
        if (fechaInicio != null && fechaFin != null) {
            nombreArchivo += "_" + fechaInicio.format(DateTimeFormatter.ofPattern("ddMMyyyy")) +
                           "_" + fechaFin.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        }
        nombreArchivo += ".xls";
        fileChooser.setInitialFileName(nombreArchivo);
        
        File archivo = fileChooser.showSaveDialog(parentStage);
        if (archivo == null) {
            return; // Usuario canceló
        }
        
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(archivo)) {
            
            HSSFSheet sheet = workbook.createSheet("Fichajes");
            
            // Crear estilos
            HSSFCellStyle headerStyle = crearEstiloEncabezado(workbook);
            HSSFCellStyle dataStyle = crearEstiloDatos(workbook);
            HSSFCellStyle titleStyle = crearEstiloTitulo(workbook);
            
            int rowNum = 0;
            
            // Título del reporte
            HSSFRow titleRow = sheet.createRow(rowNum++);
            HSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE FICHAJES - " + nombreEmpleado.toUpperCase());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            
            // Información del período
            if (fechaInicio != null && fechaFin != null) {
                rowNum++; // Fila vacía
                HSSFRow periodRow = sheet.createRow(rowNum++);
                HSSFCell periodCell = periodRow.createCell(0);
                periodCell.setCellValue("Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                      " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                periodCell.setCellStyle(dataStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7));
            }
            
            rowNum++; // Fila vacía
            
            // Encabezados
            HSSFRow headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Fecha", "Hora Entrada", "Hora Salida", "Tiempo Total", 
                              "Tipo Entrada", "Tipo Salida", "Estado", "Observaciones"};
            
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            for (ModeloFichaje fichaje : fichajes) {
                HSSFRow row = sheet.createRow(rowNum++);
                
                // Fecha
                HSSFCell cell0 = row.createCell(0);
                cell0.setCellValue(fichaje.getFechaFormateada());
                cell0.setCellStyle(dataStyle);
                
                // Hora Entrada
                HSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(fichaje.getHoraEntradaFormateada());
                cell1.setCellStyle(dataStyle);
                
                // Hora Salida
                HSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(fichaje.getHoraSalidaFormateada());
                cell2.setCellStyle(dataStyle);
                
                // Tiempo Total
                HSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(fichaje.getHorasTrabajadasFormateadas());
                cell3.setCellStyle(dataStyle);
                
                // Tipo Entrada
                HSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(fichaje.getTipoEntrada() != null ? 
                                 fichaje.getTipoEntrada().getDescripcion() : "N/A");
                cell4.setCellStyle(dataStyle);
                
                // Tipo Salida
                HSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(fichaje.getTipoSalida() != null ? 
                                 fichaje.getTipoSalida().getDescripcion() : "N/A");
                cell5.setCellStyle(dataStyle);
                
                // Estado
                HSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(fichaje.getEstado() != null ? 
                                 fichaje.getEstado().getDescripcion() : "N/A");
                cell6.setCellStyle(dataStyle);
                
                // Observaciones
                HSSFCell cell7 = row.createCell(7);
                String observaciones = "";
                if (fichaje.getMotivoIncidenciaEntrada() != null && !fichaje.getMotivoIncidenciaEntrada().isEmpty()) {
                    observaciones += "Entrada: " + fichaje.getMotivoIncidenciaEntrada();
                }
                if (fichaje.getMotivoIncidenciaSalida() != null && !fichaje.getMotivoIncidenciaSalida().isEmpty()) {
                    if (!observaciones.isEmpty()) observaciones += " | ";
                    observaciones += "Salida: " + fichaje.getMotivoIncidenciaSalida();
                }
                cell7.setCellValue(observaciones);
                cell7.setCellStyle(dataStyle);
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Establecer un ancho mínimo y máximo
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                } else if (currentWidth > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }
            
            workbook.write(fileOut);
            
            // Mostrar mensaje de éxito
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText("Archivo Excel generado");
            alert.setContentText("El archivo se ha guardado correctamente en:\n" + archivo.getAbsolutePath());
            alert.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al exportar a Excel", "No se pudo crear el archivo Excel: " + e.getMessage());
        }
    }
    
    /**
     * Crea el estilo para el título del reporte
     */
    private static HSSFCellStyle crearEstiloTitulo(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        
        return style;
    }
    
    /**
     * Crea el estilo para los encabezados de columna
     */
    private static HSSFCellStyle crearEstiloEncabezado(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        
        return style;
    }
    
    /**
     * Crea el estilo para las celdas de datos
     */
    private static HSSFCellStyle crearEstiloDatos(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        
        return style;
    }
    
    /**
     * Exporta una lista de fichajes a un archivo PDF
     */
    public static void exportarAPDF(List<ModeloFichaje> fichajes, String nombreEmpleado, 
                                   LocalDate fechaInicio, LocalDate fechaFin, Stage parentStage) {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf")
        );
        
        // Nombre sugerido para el archivo
        String nombreArchivo = "Fichajes_" + nombreEmpleado.replaceAll("\\s+", "_");
        if (fechaInicio != null && fechaFin != null) {
            nombreArchivo += "_" + fechaInicio.format(DateTimeFormatter.ofPattern("ddMMyyyy")) +
                           "_" + fechaFin.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        }
        nombreArchivo += ".pdf";
        fileChooser.setInitialFileName(nombreArchivo);
        
        File archivo = fileChooser.showSaveDialog(parentStage);
        if (archivo == null) {
            return; // Usuario canceló
        }
        
        try {
            Document document = new Document(PageSize.A4.rotate()); // Orientación horizontal
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();
            
            // Título del documento
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(FontFamily.HELVETICA, 18, 
                                                                         com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("REPORTE DE FICHAJES - " + nombreEmpleado.toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información del período
            if (fechaInicio != null && fechaFin != null) {
                com.itextpdf.text.Font periodFont = new com.itextpdf.text.Font(FontFamily.HELVETICA, 12, 
                                                                               com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
                Paragraph period = new Paragraph("Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                                " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), periodFont);
                period.setAlignment(Element.ALIGN_CENTER);
                period.setSpacingAfter(20);
                document.add(period);
            }
            
            // Crear tabla
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Establecer anchos de columnas
            float[] columnWidths = {10f, 12f, 12f, 12f, 15f, 15f, 12f, 20f};
            table.setWidths(columnWidths);
            
            // Encabezados
            String[] headers = {"Fecha", "Hora Entrada", "Hora Salida", "Tiempo Total", 
                              "Tipo Entrada", "Tipo Salida", "Estado", "Observaciones"};
            
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(FontFamily.HELVETICA, 10, 
                                                                           com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(70, 130, 180)); // SteelBlue
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(8);
                table.addCell(cell);
            }
            
            // Datos
            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(FontFamily.HELVETICA, 9, 
                                                                         com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            
            boolean alternate = false;
            for (ModeloFichaje fichaje : fichajes) {
                BaseColor bgColor = alternate ? new BaseColor(245, 245, 245) : BaseColor.WHITE;
                
                // Fecha
                PdfPCell cell1 = new PdfPCell(new Phrase(fichaje.getFechaFormateada(), dataFont));
                cell1.setBackgroundColor(bgColor);
                cell1.setPadding(5);
                table.addCell(cell1);
                
                // Hora Entrada
                PdfPCell cell2 = new PdfPCell(new Phrase(fichaje.getHoraEntradaFormateada(), dataFont));
                cell2.setBackgroundColor(bgColor);
                cell2.setPadding(5);
                table.addCell(cell2);
                
                // Hora Salida
                PdfPCell cell3 = new PdfPCell(new Phrase(fichaje.getHoraSalidaFormateada(), dataFont));
                cell3.setBackgroundColor(bgColor);
                cell3.setPadding(5);
                table.addCell(cell3);
                
                // Tiempo Total
                PdfPCell cell4 = new PdfPCell(new Phrase(fichaje.getHorasTrabajadasFormateadas(), dataFont));
                cell4.setBackgroundColor(bgColor);
                cell4.setPadding(5);
                table.addCell(cell4);
                
                // Tipo Entrada
                PdfPCell cell5 = new PdfPCell(new Phrase(fichaje.getTipoEntrada() != null ? 
                                                       fichaje.getTipoEntrada().getDescripcion() : "N/A", dataFont));
                cell5.setBackgroundColor(bgColor);
                cell5.setPadding(5);
                table.addCell(cell5);
                
                // Tipo Salida
                PdfPCell cell6 = new PdfPCell(new Phrase(fichaje.getTipoSalida() != null ? 
                                                       fichaje.getTipoSalida().getDescripcion() : "N/A", dataFont));
                cell6.setBackgroundColor(bgColor);
                cell6.setPadding(5);
                table.addCell(cell6);
                
                // Estado
                PdfPCell cell7 = new PdfPCell(new Phrase(fichaje.getEstado() != null ? 
                                                       fichaje.getEstado().getDescripcion() : "N/A", dataFont));
                cell7.setBackgroundColor(bgColor);
                cell7.setPadding(5);
                table.addCell(cell7);
                
                // Observaciones
                String observaciones = "";
                if (fichaje.getMotivoIncidenciaEntrada() != null && !fichaje.getMotivoIncidenciaEntrada().isEmpty()) {
                    observaciones += "Entrada: " + fichaje.getMotivoIncidenciaEntrada();
                }
                if (fichaje.getMotivoIncidenciaSalida() != null && !fichaje.getMotivoIncidenciaSalida().isEmpty()) {
                    if (!observaciones.isEmpty()) observaciones += " | ";
                    observaciones += "Salida: " + fichaje.getMotivoIncidenciaSalida();
                }
                PdfPCell cell8 = new PdfPCell(new Phrase(observaciones, dataFont));
                cell8.setBackgroundColor(bgColor);
                cell8.setPadding(5);
                table.addCell(cell8);
                
                alternate = !alternate;
            }
            
            document.add(table);
            
            // Pie de página con información de generación
            com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(FontFamily.HELVETICA, 8, 
                                                                           com.itextpdf.text.Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                            " - Total de registros: " + fichajes.size(), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);
            
            document.close();
            
            // Mostrar mensaje de éxito
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText("Archivo PDF generado");
            alert.setContentText("El archivo se ha guardado correctamente en:\n" + archivo.getAbsolutePath());
            alert.showAndWait();
            
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            mostrarError("Error al exportar a PDF", "No se pudo crear el archivo PDF: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private static void mostrarError(String titulo, String mensaje) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(titulo);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
} 
