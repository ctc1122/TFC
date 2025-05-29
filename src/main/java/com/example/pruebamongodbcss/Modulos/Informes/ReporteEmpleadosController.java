package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
    private TableView<ProductividadEmpleadoRow> tableProductividad;
    
    @FXML
    private TableColumn<ProductividadEmpleadoRow, String> colEmpleado;
    
    @FXML
    private TableColumn<ProductividadEmpleadoRow, Integer> colCitas;
    
    @FXML
    private TableColumn<ProductividadEmpleadoRow, String> colHoras;
    
    @FXML
    private TableColumn<ProductividadEmpleadoRow, String> colEficiencia;
    
    @FXML
    private Button btnGenerar;
    
    @FXML
    private Button btnExportar;
    
    private ServicioInformes servicioInformes;
    private Usuario usuarioActual;
    private DecimalFormat formatoHoras = new DecimalFormat("#0.0");
    private DecimalFormat formatoEficiencia = new DecimalFormat("#0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        servicioInformes = new ServicioInformes();
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar eventos
        btnGenerar.setOnAction(e -> generarReporte());
        btnExportar.setOnAction(e -> exportarReporte());
        
        // Generar reporte inicial
        generarReporte();
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private void configurarTabla() {
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        colCitas.setCellValueFactory(new PropertyValueFactory<>("citasAtendidas"));
        colHoras.setCellValueFactory(new PropertyValueFactory<>("horasFormateadas"));
        colEficiencia.setCellValueFactory(new PropertyValueFactory<>("eficienciaFormateada"));
        
        tableProductividad.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void generarReporte() {
        try {
            // Obtener estadísticas de empleados
            ServicioInformes.EstadisticasEmpleados stats = servicioInformes.obtenerEstadisticasEmpleados();
            
            // Actualizar métricas
            lblTotalEmpleados.setText(String.valueOf(stats.getTotalEmpleados()));
            lblEmpleadosActivos.setText(String.valueOf(stats.getEmpleadosActivos()));
            lblPromedioHoras.setText(formatoHoras.format(stats.getPromedioHorasMes()) + " h");
            
            // Actualizar gráfico de empleados por rol
            actualizarGraficoRoles(stats.getEmpleadosPorRol());
            
            // Obtener productividad de empleados
            List<ServicioInformes.ProductividadEmpleado> productividad = 
                servicioInformes.obtenerProductividadEmpleados(10);
            
            // Actualizar tabla
            actualizarTablaProductividad(productividad);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte de empleados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void actualizarGraficoRoles(List<ServicioInformes.DatoGrafico> datos) {
        chartContainer.getChildren().clear();
        
        PieChart chart = new PieChart();
        chart.setTitle("Distribución de Empleados por Rol");
        chart.setPrefHeight(400);
        
        for (ServicioInformes.DatoGrafico dato : datos) {
            PieChart.Data slice = new PieChart.Data(dato.getEtiqueta(), dato.getValor());
            chart.getData().add(slice);
        }
        
        chartContainer.getChildren().add(chart);
    }
    
    private void actualizarTablaProductividad(List<ServicioInformes.ProductividadEmpleado> productividad) {
        tableProductividad.getItems().clear();
        
        for (ServicioInformes.ProductividadEmpleado prod : productividad) {
            ProductividadEmpleadoRow row = new ProductividadEmpleadoRow(
                prod.getNombreEmpleado(),
                prod.getCitasAtendidas(),
                prod.getHorasTrabajadas(),
                prod.getEficiencia(),
                formatoHoras.format(prod.getHorasTrabajadas()),
                formatoEficiencia.format(prod.getEficiencia())
            );
            tableProductividad.getItems().add(row);
        }
    }
    
    private void exportarReporte() {
        // Implementar exportación a PDF/Excel
        System.out.println("Exportando reporte de empleados...");
    }
    
    @FXML
    private void volverAlDashboard() {
        try {
            // Buscar la ventana principal y obtener la escena
            javafx.stage.Window ventanaPrincipal = btnExportar.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar directamente la vista de informes
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
                            centerPane.setCenter(informesView);
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
    
    private void mostrarAlert(String titulo, String header, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    // Clase para las filas de la tabla
    public static class ProductividadEmpleadoRow {
        private String nombreEmpleado;
        private Integer citasAtendidas;
        private Double horasTrabajadas;
        private Double eficiencia;
        private String horasFormateadas;
        private String eficienciaFormateada;
        
        public ProductividadEmpleadoRow(String nombreEmpleado, Integer citasAtendidas, 
                                      Double horasTrabajadas, Double eficiencia,
                                      String horasFormateadas, String eficienciaFormateada) {
            this.nombreEmpleado = nombreEmpleado;
            this.citasAtendidas = citasAtendidas;
            this.horasTrabajadas = horasTrabajadas;
            this.eficiencia = eficiencia;
            this.horasFormateadas = horasFormateadas;
            this.eficienciaFormateada = eficienciaFormateada;
        }
        
        // Getters y setters
        public String getNombreEmpleado() { return nombreEmpleado; }
        public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
        
        public Integer getCitasAtendidas() { return citasAtendidas; }
        public void setCitasAtendidas(Integer citasAtendidas) { this.citasAtendidas = citasAtendidas; }
        
        public Double getHorasTrabajadas() { return horasTrabajadas; }
        public void setHorasTrabajadas(Double horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }
        
        public Double getEficiencia() { return eficiencia; }
        public void setEficiencia(Double eficiencia) { this.eficiencia = eficiencia; }
        
        public String getHorasFormateadas() { return horasFormateadas; }
        public void setHorasFormateadas(String horasFormateadas) { this.horasFormateadas = horasFormateadas; }
        
        public String getEficienciaFormateada() { return eficienciaFormateada; }
        public void setEficienciaFormateada(String eficienciaFormateada) { this.eficienciaFormateada = eficienciaFormateada; }
    }
} 