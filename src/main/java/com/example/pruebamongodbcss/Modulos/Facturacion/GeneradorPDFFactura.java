package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generador de PDF para facturas veterinarias
 * Cumple con los requisitos legales españoles
 */
public class GeneradorPDFFactura {
    
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    
    private DecimalFormat formatoMoneda = new DecimalFormat("#,##0.00 €");
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    
    public void generarPDF(ModeloFactura factura, String rutaArchivo) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
        
        document.open();
        
        // Encabezado de la empresa
        agregarEncabezadoEmpresa(document);
        
        // Información de la factura
        agregarInformacionFactura(document, factura);
        
        // Datos del cliente
        agregarDatosCliente(document, factura);
        
        // Tabla de servicios
        agregarTablaServicios(document, factura);
        
        // Tabla de medicamentos
        agregarTablaMedicamentos(document, factura);
        
        // Totales
        agregarTotales(document, factura);
        
        // Observaciones
        agregarObservaciones(document, factura);
        
        // Pie de página legal
        agregarPieLegal(document);
        
        document.close();
    }
    
    private void agregarEncabezadoEmpresa(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{70, 30});
        
        // Información de la empresa
        PdfPCell cellEmpresa = new PdfPCell();
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        
        Paragraph empresaNombre = new Paragraph("CLÍNICA VETERINARIA EJEMPLO", FONT_TITLE);
        empresaNombre.setAlignment(Element.ALIGN_LEFT);
        cellEmpresa.addElement(empresaNombre);
        
        Paragraph empresaDatos = new Paragraph();
        empresaDatos.add(new Chunk("Calle Ejemplo, 123\n", FONT_NORMAL));
        empresaDatos.add(new Chunk("28001 Madrid, España\n", FONT_NORMAL));
        empresaDatos.add(new Chunk("Tel: 91 123 45 67\n", FONT_NORMAL));
        empresaDatos.add(new Chunk("Email: info@veterinaria.com\n", FONT_NORMAL));
        empresaDatos.add(new Chunk("CIF: B12345678", FONT_NORMAL));
        cellEmpresa.addElement(empresaDatos);
        
        // Logo (placeholder)
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph logo = new Paragraph("LOGO", FONT_HEADER);
        logo.setAlignment(Element.ALIGN_RIGHT);
        cellLogo.addElement(logo);
        
        table.addCell(cellEmpresa);
        table.addCell(cellLogo);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarInformacionFactura(Document document, ModeloFactura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        
        // Título FACTURA
        PdfPCell cellTitulo = new PdfPCell();
        cellTitulo.setBorder(Rectangle.NO_BORDER);
        Paragraph titulo = new Paragraph("FACTURA", FONT_TITLE);
        titulo.setAlignment(Element.ALIGN_LEFT);
        cellTitulo.addElement(titulo);
        
        // Datos de la factura
        PdfPCell cellDatos = new PdfPCell();
        cellDatos.setBorder(Rectangle.NO_BORDER);
        cellDatos.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph datos = new Paragraph();
        datos.add(new Chunk("Número: " + (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR") + "\n", FONT_HEADER));
        datos.add(new Chunk("Fecha: " + (factura.getFechaEmision() != null ? formatoFecha.format(factura.getFechaEmision()) : formatoFecha.format(new Date())) + "\n", FONT_NORMAL));
        datos.add(new Chunk("Vencimiento: " + (factura.getFechaVencimiento() != null ? formatoFecha.format(factura.getFechaVencimiento()) : "30 días") + "\n", FONT_NORMAL));
        datos.add(new Chunk("Estado: " + (factura.getEstado() != null ? factura.getEstado().getDescripcion() : "Borrador"), FONT_NORMAL));
        datos.setAlignment(Element.ALIGN_RIGHT);
        cellDatos.addElement(datos);
        
        table.addCell(cellTitulo);
        table.addCell(cellDatos);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarDatosCliente(Document document, ModeloFactura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        
        // Datos del cliente
        PdfPCell cellCliente = new PdfPCell();
        cellCliente.setBorder(Rectangle.BOX);
        cellCliente.setPadding(10);
        
        Paragraph clienteTitulo = new Paragraph("DATOS DEL CLIENTE", FONT_HEADER);
        cellCliente.addElement(clienteTitulo);
        
        Paragraph clienteDatos = new Paragraph();
        clienteDatos.add(new Chunk("Nombre: " + (factura.getNombreCliente() != null ? factura.getNombreCliente() : "") + "\n", FONT_NORMAL));
        clienteDatos.add(new Chunk("DNI/CIF: " + (factura.getDniCliente() != null ? factura.getDniCliente() : "") + "\n", FONT_NORMAL));
        clienteDatos.add(new Chunk("Dirección: " + (factura.getDireccionCliente() != null ? factura.getDireccionCliente() : "") + "\n", FONT_NORMAL));
        clienteDatos.add(new Chunk("Teléfono: " + (factura.getTelefonoCliente() != null ? factura.getTelefonoCliente() : "") + "\n", FONT_NORMAL));
        clienteDatos.add(new Chunk("Email: " + (factura.getEmailCliente() != null ? factura.getEmailCliente() : ""), FONT_NORMAL));
        cellCliente.addElement(clienteDatos);
        
        // Datos del paciente
        PdfPCell cellPaciente = new PdfPCell();
        cellPaciente.setBorder(Rectangle.BOX);
        cellPaciente.setPadding(10);
        
        Paragraph pacienteTitulo = new Paragraph("DATOS DEL PACIENTE", FONT_HEADER);
        cellPaciente.addElement(pacienteTitulo);
        
        Paragraph pacienteDatos = new Paragraph();
        pacienteDatos.add(new Chunk("Nombre: " + (factura.getNombrePaciente() != null ? factura.getNombrePaciente() : "") + "\n", FONT_NORMAL));
        pacienteDatos.add(new Chunk("Especie: " + (factura.getEspeciePaciente() != null ? factura.getEspeciePaciente() : "") + "\n", FONT_NORMAL));
        pacienteDatos.add(new Chunk("Raza: " + (factura.getRazaPaciente() != null ? factura.getRazaPaciente() : "") + "\n", FONT_NORMAL));
        pacienteDatos.add(new Chunk("Veterinario: " + (factura.getVeterinarioNombre() != null ? factura.getVeterinarioNombre() : ""), FONT_NORMAL));
        cellPaciente.addElement(pacienteDatos);
        
        table.addCell(cellCliente);
        table.addCell(cellPaciente);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarTablaServicios(Document document, ModeloFactura factura) throws DocumentException {
        if (factura.getServicios() == null || factura.getServicios().isEmpty()) {
            return;
        }
        
        Paragraph titulo = new Paragraph("SERVICIOS VETERINARIOS", FONT_HEADER);
        document.add(titulo);
        document.add(new Paragraph(" "));
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 10, 15, 10, 10, 15});
        
        // Encabezados
        agregarCeldaEncabezado(table, "Descripción");
        agregarCeldaEncabezado(table, "Cant.");
        agregarCeldaEncabezado(table, "Precio Unit.");
        agregarCeldaEncabezado(table, "Desc. %");
        agregarCeldaEncabezado(table, "IVA %");
        agregarCeldaEncabezado(table, "Total");
        
        // Datos
        for (ModeloFactura.ConceptoFactura servicio : factura.getServicios()) {
            agregarCeldaDato(table, servicio.getDescripcion() != null ? servicio.getDescripcion() : "");
            agregarCeldaDato(table, String.valueOf(servicio.getCantidad()));
            agregarCeldaDato(table, formatoMoneda.format(servicio.getPrecioUnitario()));
            agregarCeldaDato(table, String.format("%.1f%%", servicio.getDescuento()));
            agregarCeldaDato(table, String.format("%.1f%%", servicio.getTipoIva()));
            agregarCeldaDato(table, formatoMoneda.format(servicio.getTotal()));
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarTablaMedicamentos(Document document, ModeloFactura factura) throws DocumentException {
        if (factura.getMedicamentos() == null || factura.getMedicamentos().isEmpty()) {
            return;
        }
        
        Paragraph titulo = new Paragraph("MEDICAMENTOS", FONT_HEADER);
        document.add(titulo);
        document.add(new Paragraph(" "));
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 10, 15, 10, 10, 15});
        
        // Encabezados
        agregarCeldaEncabezado(table, "Descripción");
        agregarCeldaEncabezado(table, "Cant.");
        agregarCeldaEncabezado(table, "Precio Unit.");
        agregarCeldaEncabezado(table, "Desc. %");
        agregarCeldaEncabezado(table, "IVA %");
        agregarCeldaEncabezado(table, "Total");
        
        // Datos
        for (ModeloFactura.ConceptoFactura medicamento : factura.getMedicamentos()) {
            agregarCeldaDato(table, medicamento.getDescripcion() != null ? medicamento.getDescripcion() : "");
            agregarCeldaDato(table, String.valueOf(medicamento.getCantidad()));
            agregarCeldaDato(table, formatoMoneda.format(medicamento.getPrecioUnitario()));
            agregarCeldaDato(table, String.format("%.1f%%", medicamento.getDescuento()));
            agregarCeldaDato(table, String.format("%.1f%%", medicamento.getTipoIva()));
            agregarCeldaDato(table, formatoMoneda.format(medicamento.getTotal()));
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarTotales(Document document, ModeloFactura factura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{70, 30});
        
        // Subtotal
        agregarFilaTotal(table, "Subtotal:", formatoMoneda.format(factura.getSubtotal()));
        
        // IVA General (21%)
        if (factura.getIvaGeneral() > 0) {
            agregarFilaTotal(table, "IVA General (21%):", formatoMoneda.format(factura.getIvaGeneral()));
        }
        
        // IVA Medicamentos (10%)
        if (factura.getIvaMedicamentos() > 0) {
            agregarFilaTotal(table, "IVA Medicamentos (10%):", formatoMoneda.format(factura.getIvaMedicamentos()));
        }
        
        // Total
        PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL:", FONT_HEADER));
        cellTotalLabel.setBorder(Rectangle.TOP);
        cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotalLabel.setPadding(5);
        
        PdfPCell cellTotalValue = new PdfPCell(new Phrase(formatoMoneda.format(factura.getTotal()), FONT_HEADER));
        cellTotalValue.setBorder(Rectangle.TOP);
        cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotalValue.setPadding(5);
        
        table.addCell(cellTotalLabel);
        table.addCell(cellTotalValue);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void agregarObservaciones(Document document, ModeloFactura factura) throws DocumentException {
        if (factura.getObservaciones() != null && !factura.getObservaciones().trim().isEmpty()) {
            Paragraph titulo = new Paragraph("OBSERVACIONES", FONT_HEADER);
            document.add(titulo);
            
            Paragraph observaciones = new Paragraph(factura.getObservaciones(), FONT_NORMAL);
            observaciones.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(observaciones);
            document.add(new Paragraph("\n"));
        }
        
        if (factura.getMetodoPago() != null && !factura.getMetodoPago().trim().isEmpty()) {
            Paragraph metodoPago = new Paragraph("Método de pago: " + factura.getMetodoPago(), FONT_NORMAL);
            document.add(metodoPago);
            document.add(new Paragraph("\n"));
        }
    }
    
    private void agregarPieLegal(Document document) throws DocumentException {
        Paragraph pieLegal = new Paragraph();
        pieLegal.add(new Chunk("Esta factura cumple con los requisitos legales establecidos por la normativa española vigente.\n", FONT_SMALL));
        pieLegal.add(new Chunk("En caso de impago, se aplicarán los intereses de demora legalmente establecidos.\n", FONT_SMALL));
        pieLegal.add(new Chunk("Factura generada electrónicamente el " + formatoFecha.format(new Date()), FONT_SMALL));
        pieLegal.setAlignment(Element.ALIGN_CENTER);
        
        document.add(new Paragraph("\n\n"));
        document.add(pieLegal);
    }
    
    private void agregarCeldaEncabezado(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_HEADER));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void agregarCeldaDato(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }
    
    private void agregarFilaTotal(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, FONT_NORMAL));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabel.setPadding(3);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(value, FONT_NORMAL));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValue.setPadding(3);
        
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
} 