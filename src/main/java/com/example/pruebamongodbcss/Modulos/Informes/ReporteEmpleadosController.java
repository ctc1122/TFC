package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ReporteEmpleadosController implements Initializable {

    @FXML
    private Label lblTotalEmpleados;
    
    @FXML
    private Label lblEmpleadosActivos;
    
    @FXML
    private Label lblPromedioHoras;
    
    @FXML
    private VBox chartContainer;
    
    @FXML
    private Button btnGenerar;
    
    @FXML
    private Button btnExportar;
    
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    private DecimalFormat formatoHoras = new DecimalFormat("#0.0");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gestorSocket = GestorSocket.getInstance();
        
        // Configurar eventos
        btnGenerar.setOnAction(e -> generarReporte());
        btnExportar.setOnAction(e -> exportarReporte());
        
        // IMPORTANTE: Aplicar tema correcto desde el inicio
        aplicarTemaCorreecto();
        
        // Generar reporte inicial
        generarReporte();
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private void generarReporte() {
        try {
            // Obtener estadísticas de empleados del servidor
            EstadisticasEmpleadosData stats = obtenerEstadisticasEmpleados();
            
            if (stats != null) {
            // Actualizar métricas
            lblTotalEmpleados.setText(String.valueOf(stats.getTotalEmpleados()));
            lblEmpleadosActivos.setText(String.valueOf(stats.getEmpleadosActivos()));
            lblPromedioHoras.setText(formatoHoras.format(stats.getPromedioHorasMes()) + " h");
            
            // Actualizar gráfico de empleados por rol
            actualizarGraficoRoles(stats.getEmpleadosPorRol());
            }
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte de empleados: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al generar reporte", 
                        "Ha ocurrido un error al generar el reporte de empleados: " + e.getMessage());
        }
    }
    
    private void actualizarGraficoRoles(List<DatoGraficoData> datos) {
        chartContainer.getChildren().clear();
        
        PieChart chart = new PieChart();
        chart.setTitle("Distribución de Empleados por Rol");
        chart.setLegendVisible(true);
        
        // Configurar el gráfico para que se adapte al contenedor
        chart.prefWidthProperty().bind(chartContainer.widthProperty());
        chart.prefHeightProperty().bind(chartContainer.heightProperty());
        chart.setMinHeight(300); // Altura mínima para evitar que se vea muy pequeño
        
        // Configurar las propiedades de crecimiento
        VBox.setVgrow(chart, javafx.scene.layout.Priority.ALWAYS);
        
        for (DatoGraficoData dato : datos) {
            PieChart.Data slice = new PieChart.Data(dato.getEtiqueta(), dato.getValor());
            chart.getData().add(slice);
        }
        
        // Configurar tooltips y efectos visuales
        Platform.runLater(() -> {
            for (PieChart.Data data : chart.getData()) {
                if (data.getNode() != null) {
                    // Crear tooltip personalizado
                    Tooltip tooltip = new Tooltip();
                    tooltip.setText(data.getName() + ": " + (int)data.getPieValue() + " empleados");
                    tooltip.setStyle("-fx-background-color: rgba(0,0,0,0.8); " +
                                   "-fx-text-fill: white; " +
                                   "-fx-background-radius: 6px; " +
                                   "-fx-padding: 8px; " +
                                   "-fx-font-size: 12px;");
                    
                    // Instalar tooltip
                    Tooltip.install(data.getNode(), tooltip);
                    
                    // Efectos visuales al pasar el mouse
                    data.getNode().setOnMouseEntered(e -> {
                        data.getNode().setScaleX(1.1);
                        data.getNode().setScaleY(1.1);
                    });
                    
                    data.getNode().setOnMouseExited(e -> {
                        data.getNode().setScaleX(1.0);
                        data.getNode().setScaleY(1.0);
                    });
                }
            }
        });
        
        chartContainer.getChildren().add(chart);
    }
    
    private void exportarReporte() {
        try {
            // Selector de archivo para guardar el PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Empleados");
            fileChooser.setInitialFileName("Reporte_Empleados_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );
            
            java.io.File archivo = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());
            if (archivo == null) {
                return; // Usuario canceló
            }
            
            // Crear el documento PDF
            Document documento = new Document(PageSize.A4);
            PdfWriter.getInstance(documento, new FileOutputStream(archivo));
            documento.open();
            
            // Definir fuentes
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            
            // Título principal
            Paragraph titulo = new Paragraph("REPORTE DE EMPLEADOS", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(15);
            documento.add(titulo);
            
            // Fecha y hora del reporte
            Paragraph fecha = new Paragraph("Generado el: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " a las " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                smallFont);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            fecha.setSpacingAfter(20);
            documento.add(fecha);
            
            // Obtener datos actuales
            EstadisticasEmpleadosData stats = obtenerEstadisticasEmpleados();
            
            if (stats != null) {
                // Sección: Métricas principales
                Paragraph seccionMetricas = new Paragraph("ESTADÍSTICAS PRINCIPALES", subtituloFont);
                seccionMetricas.setSpacingBefore(15);
                seccionMetricas.setSpacingAfter(10);
                documento.add(seccionMetricas);
                
                // Tabla de métricas en dos columnas
                PdfPTable tablaMetricas = new PdfPTable(2);
                tablaMetricas.setWidthPercentage(100);
                tablaMetricas.setSpacingAfter(18);
                
                // Configurar celdas de métricas
                addMetricaCell(tablaMetricas, "Total de Empleados", String.valueOf(stats.getTotalEmpleados()), normalFont);
                addMetricaCell(tablaMetricas, "Empleados Activos", String.valueOf(stats.getEmpleadosActivos()), normalFont);
                addMetricaCell(tablaMetricas, "Promedio Horas/Mes", formatoHoras.format(stats.getPromedioHorasMes()) + " h", normalFont);
                addMetricaCell(tablaMetricas, "Período de Análisis", LocalDate.now().getYear() + "", normalFont);
                
                documento.add(tablaMetricas);
                
                // Sección: Distribución por roles
                Paragraph seccionRoles = new Paragraph("DISTRIBUCIÓN POR ROLES", subtituloFont);
                seccionRoles.setSpacingBefore(15);
                seccionRoles.setSpacingAfter(10);
                documento.add(seccionRoles);
                
                // Tabla de distribución por roles
                PdfPTable tablaRoles = new PdfPTable(2);
                tablaRoles.setWidthPercentage(100);
                tablaRoles.setWidths(new float[]{3, 1});
                tablaRoles.setSpacingAfter(18);
                
                // Headers de la tabla de roles
                addHeaderCell(tablaRoles, "Rol", subtituloFont);
                addHeaderCell(tablaRoles, "Cantidad", subtituloFont);
                
                // Datos de roles
                for (DatoGraficoData dato : stats.getEmpleadosPorRol()) {
                    addDataCell(tablaRoles, dato.getEtiqueta(), normalFont);
                    addDataCell(tablaRoles, String.valueOf((int)dato.getValor()), normalFont);
                }
                
                documento.add(tablaRoles);
            }
            
            // Pie de página
            Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Gestión Veterinaria", smallFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setSpacingBefore(20);
            documento.add(pie);
            
            // Cerrar documento
            documento.close();
            
            // Mostrar mensaje de éxito
            mostrarAlert("Éxito", "Reporte exportado", 
                        "El reporte se ha exportado correctamente a:\n" + archivo.getAbsolutePath());
                        
        } catch (DocumentException | IOException e) {
            System.err.println("Error al exportar reporte: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al exportar", 
                        "Ha ocurrido un error al exportar el reporte:\n" + e.getMessage());
        }
    }
    
    @FXML
    private void volverAlDashboard() {
        try {
            // Buscar la ventana principal y obtener la escena
            Window ventanaPrincipal = btnGenerar.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar la vista completa de informes (con filtros)
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/informes-view.fxml"));
                        Parent informesView = com.example.pruebamongodbcss.theme.ThemeUtil.loadWithTheme(loader);
                        
                        // Establecer el usuario en el controlador
                        InformesController controller = loader.getController();
                        if (controller != null && usuarioActual != null) {
                            controller.setUsuarioActual(usuarioActual);
                        }
                        
                        // Buscar el BorderPane central (donde se cargan los módulos)
                        if (mainRoot.getCenter() instanceof BorderPane) {
                            BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                            // IMPORTANTE: Restaurar la vista completa del dashboard
                            centerPane.setTop(null);     // Limpiar primero
                            centerPane.setBottom(null);  
                            centerPane.setLeft(null);    
                            centerPane.setRight(null);   
                            centerPane.setCenter(informesView); // Restaurar vista completa con filtros
                        } else {
                            // Fallback: reemplazar directamente en el centro
                            mainRoot.setCenter(informesView);
                        }
                        
                        // Aplicar temas para asegurar consistencia
                        javafx.application.Platform.runLater(() -> {
                            com.example.pruebamongodbcss.theme.ThemeUtil.applyThemeToAllOpenWindows();
                        });
                        
                    } else {
                        System.err.println("Root no es BorderPane: " + root.getClass().getSimpleName());
                        mostrarAlert("Error", "Error de navegación", 
                                    "No se pudo identificar la estructura de navegación.");
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de informes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al cargar dashboard", 
                        "Ha ocurrido un error al volver al dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error inesperado", 
                        "Ha ocurrido un error inesperado: " + e.getMessage());
        }
    }
    
    // Métodos auxiliares para crear celdas de PDF
    private void addMetricaCell(PdfPTable tabla, String etiqueta, String valor, Font font) {
        PdfPCell cellEtiqueta = new PdfPCell(new Phrase(etiqueta + ":", font));
        cellEtiqueta.setBorder(PdfPCell.NO_BORDER);
        cellEtiqueta.setPadding(6);
        tabla.addCell(cellEtiqueta);
        
        PdfPCell cellValor = new PdfPCell(new Phrase(valor, new Font(font.getFamily(), font.getSize(), Font.BOLD)));
        cellValor.setBorder(PdfPCell.NO_BORDER);
        cellValor.setPadding(6);
        cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(cellValor);
    }
    
    private void addHeaderCell(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
        tabla.addCell(cell);
    }
    
    private void addDataCell(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        tabla.addCell(cell);
    }
    
    private void mostrarAlert(String titulo, String header, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Aplica el tema correcto al reporte basado en el estado actual del ThemeManager
     */
    private void aplicarTemaCorreecto() {
        Platform.runLater(() -> {
            try {
                // Obtener la escena actual
                if (btnGenerar != null && btnGenerar.getScene() != null) {
                    javafx.scene.Scene scene = btnGenerar.getScene();
                    javafx.scene.Parent root = scene.getRoot();
                    
                    // Verificar si el tema oscuro está activo
                    boolean temaOscuroActivo = com.example.pruebamongodbcss.theme.ThemeManager.getInstance().isDarkTheme();
                    
                    // Aplicar o quitar la clase dark-theme del root
                    if (temaOscuroActivo) {
                        if (!root.getStyleClass().contains("dark-theme")) {
                            root.getStyleClass().add("dark-theme");
                        }
                    } else {
                        root.getStyleClass().remove("dark-theme");
                    }
                    
                    // Asegurar que los temas estén aplicados correctamente
                    com.example.pruebamongodbcss.theme.ThemeUtil.applyThemeToAllOpenWindows();
                }
            } catch (Exception e) {
                System.err.println("Error al aplicar tema al reporte de empleados: " + e.getMessage());
            }
        });
    }
    
    // Métodos para peticiones al servidor
    
    private EstadisticasEmpleadosData obtenerEstadisticasEmpleados() {
        try {
            String peticion = Protocolo.OBTENER_ESTADISTICAS_EMPLEADOS + Protocolo.SEPARADOR_CODIGO;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_ESTADISTICAS_EMPLEADOS_RESPONSE) {
                return (EstadisticasEmpleadosData) entrada.readObject();
            } else {
                System.err.println("Error al obtener estadísticas de empleados");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerEstadisticasEmpleados: " + e.getMessage());
            return null;
        }
    }
    
    // Clases de datos para la comunicación cliente-servidor
    
    public static class EstadisticasEmpleadosData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int totalEmpleados;
        private int empleadosActivos;
        private List<DatoGraficoData> empleadosPorRol;
        private double promedioHorasMes;
        
        public int getTotalEmpleados() { return totalEmpleados; }
        public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }
        
        public int getEmpleadosActivos() { return empleadosActivos; }
        public void setEmpleadosActivos(int empleadosActivos) { this.empleadosActivos = empleadosActivos; }
        
        public List<DatoGraficoData> getEmpleadosPorRol() { return empleadosPorRol; }
        public void setEmpleadosPorRol(List<DatoGraficoData> empleadosPorRol) { this.empleadosPorRol = empleadosPorRol; }
        
        public double getPromedioHorasMes() { return promedioHorasMes; }
        public void setPromedioHorasMes(double promedioHorasMes) { this.promedioHorasMes = promedioHorasMes; }
    }
    
    public static class ProductividadEmpleadoData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String nombreEmpleado;
        private int citasAtendidas;
        private double horasTrabajadas;
        private double eficiencia;
        
        public String getNombreEmpleado() { return nombreEmpleado; }
        public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
        
        public int getCitasAtendidas() { return citasAtendidas; }
        public void setCitasAtendidas(int citasAtendidas) { this.citasAtendidas = citasAtendidas; }
        
        public double getHorasTrabajadas() { return horasTrabajadas; }
        public void setHorasTrabajadas(double horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }
        
        public double getEficiencia() { return eficiencia; }
        public void setEficiencia(double eficiencia) { this.eficiencia = eficiencia; }
    }
    
    public static class DatoGraficoData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String etiqueta;
        private double valor;
        
        public DatoGraficoData(String etiqueta, double valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
        
        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
        
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
} 