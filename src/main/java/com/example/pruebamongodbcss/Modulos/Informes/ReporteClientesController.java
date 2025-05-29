package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ReporteClientesController implements Initializable {

    @FXML
    private Label lblTotalClientes;
    
    @FXML
    private Label lblClientesNuevosMes;
    
    @FXML
    private Label lblPromedioMascotas;
    
    @FXML
    private VBox chartContainer;
    
    @FXML
    private TableView<ClienteTopRow> tableTopClientes;
    
    @FXML
    private TableColumn<ClienteTopRow, String> colCliente;
    
    @FXML
    private TableColumn<ClienteTopRow, String> colTotalFacturado;
    
    @FXML
    private TableColumn<ClienteTopRow, Integer> colNumeroFacturas;
    
    @FXML
    private TableColumn<ClienteTopRow, String> colPromedioFactura;
    
    @FXML
    private Button btnGenerar;
    
    @FXML
    private Button btnExportar;
    
    @FXML
    private Button btnVolver;
    
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    private DecimalFormat formatoDecimal = new DecimalFormat("#0.0");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gestorSocket = GestorSocket.getInstance();
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar eventos de botones
        btnGenerar.setOnAction(e -> generarReporte());
        btnExportar.setOnAction(e -> exportarReporte());
        btnVolver.setOnAction(e -> volverAlDashboard());
        
        // Generar reporte inicial
        generarReporte();
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private void configurarTabla() {
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTotalFacturado.setCellValueFactory(new PropertyValueFactory<>("totalFacturadoFormateado"));
        colNumeroFacturas.setCellValueFactory(new PropertyValueFactory<>("numeroFacturas"));
        colPromedioFactura.setCellValueFactory(new PropertyValueFactory<>("promedioFacturaFormateado"));
        
        tableTopClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void generarReporte() {
        try {
            // Obtener análisis de clientes del servidor
            AnalisisClientesData analisis = obtenerAnalisisClientes();
            
            if (analisis != null) {
                // Actualizar métricas (sin clientes activos)
                lblTotalClientes.setText(String.valueOf(analisis.getTotalClientes()));
                lblClientesNuevosMes.setText(String.valueOf(analisis.getClientesNuevosMes()));
                lblPromedioMascotas.setText(formatoDecimal.format(analisis.getPromedioMascotasPorCliente()));
                
                // Actualizar gráfico de evolución de clientes
                actualizarGraficoEvolucion();
                
                // Obtener top de clientes
                List<ClienteTopData> topClientes = obtenerTopClientes(10);
                
                // Actualizar tabla
                actualizarTablaTopClientes(topClientes);
            }
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte de clientes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al generar reporte", 
                        "Ha ocurrido un error al generar el reporte de clientes.");
        }
    }
    
    private void actualizarGraficoEvolucion() {
        chartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Nuevos Clientes");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Nuevos Clientes - " + LocalDate.now().getYear());
        chart.setPrefHeight(400);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Año " + LocalDate.now().getYear());
        
        // Obtener datos de propietarios por mes
        List<DatoGraficoData> datos = obtenerPropietariosPorMes(12);
        for (DatoGraficoData dato : datos) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dato.getEtiqueta(), dato.getValor());
            series.getData().add(dataPoint);
        }
        
        chart.getData().add(series);
        
        // Configurar tooltips dinámicos después de que el gráfico se haya renderizado
        Platform.runLater(() -> {
            configurarTooltipsGrafico(chart, series);
        });
        
        chartContainer.getChildren().add(chart);
    }
    
    /**
     * Configura tooltips dinámicos para el gráfico de clientes
     */
    private void configurarTooltipsGrafico(LineChart<String, Number> chart, XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                // Crear tooltip personalizado
                Tooltip tooltip = new Tooltip();
                
                String mes = data.getXValue();
                int valor = data.getYValue().intValue();
                
                tooltip.setText(mes + " " + LocalDate.now().getYear() + "\nNuevos Clientes: " + valor);
                tooltip.setStyle("-fx-background-color: rgba(0,0,0,0.8); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 6px; " +
                               "-fx-padding: 8px; " +
                               "-fx-font-size: 12px;");
                
                // Instalar el tooltip en el nodo
                Tooltip.install(data.getNode(), tooltip);
                
                // Efectos visuales al pasar el mouse
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-background-color: #FF9800; -fx-background-radius: 6px;");
                    data.getNode().setScaleX(1.3);
                    data.getNode().setScaleY(1.3);
                });
                
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle("-fx-background-color: #2196F3; -fx-background-radius: 4px;");
                    data.getNode().setScaleX(1.0);
                    data.getNode().setScaleY(1.0);
                });
            }
        }
    }
    
    private void actualizarTablaTopClientes(List<ClienteTopData> topClientes) {
        tableTopClientes.getItems().clear();
        
        for (ClienteTopData cliente : topClientes) {
            ClienteTopRow row = new ClienteTopRow(
                cliente.getNombre(),
                cliente.getTotalFacturado(),
                cliente.getNumeroFacturas(),
                cliente.getPromedioFactura(),
                formatoMoneda.format(cliente.getTotalFacturado()),
                formatoMoneda.format(cliente.getPromedioFactura())
            );
            tableTopClientes.getItems().add(row);
        }
    }
    
    private void exportarReporte() {
        // Implementar exportación a PDF/Excel
        mostrarAlert("Información", "Función en desarrollo", 
                    "La función de exportación se implementará próximamente.");
    }
    
    private void volverAlDashboard() {
        try {
            // Obtener el BorderPane principal
            BorderPane mainContainer = (BorderPane) btnVolver.getScene().getRoot();
            
            // Cargar la vista principal de informes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/informes-view.fxml"));
            Parent informesView = loader.load();
            
            // Establecer el usuario en el controlador
            InformesController controller = loader.getController();
            if (controller != null && usuarioActual != null) {
                controller.setUsuarioActual(usuarioActual);
            }
            
            // Cambiar el contenido central
            mainContainer.setCenter(informesView);
            
        } catch (IOException e) {
            System.err.println("Error al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostrarAlert(String titulo, String header, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    // Métodos para peticiones al servidor
    
    private AnalisisClientesData obtenerAnalisisClientes() {
        try {
            String peticion = Protocolo.OBTENER_ANALISIS_CLIENTES + Protocolo.SEPARADOR_CODIGO;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_ANALISIS_CLIENTES_RESPONSE) {
                return (AnalisisClientesData) entrada.readObject();
            } else {
                System.err.println("Error al obtener análisis de clientes");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerAnalisisClientes: " + e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<DatoGraficoData> obtenerPropietariosPorMes(int meses) {
        try {
            String peticion = Protocolo.OBTENER_PROPIETARIOS_POR_MES + Protocolo.SEPARADOR_CODIGO + meses;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_PROPIETARIOS_POR_MES_RESPONSE) {
                return (List<DatoGraficoData>) entrada.readObject();
            } else {
                System.err.println("Error al obtener propietarios por mes");
                return List.of(); // Lista vacía
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerPropietariosPorMes: " + e.getMessage());
            return List.of();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<ClienteTopData> obtenerTopClientes(int limite) {
        try {
            String peticion = Protocolo.OBTENER_TOP_CLIENTES + Protocolo.SEPARADOR_CODIGO + limite;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TOP_CLIENTES_RESPONSE) {
                return (List<ClienteTopData>) entrada.readObject();
            } else {
                System.err.println("Error al obtener top clientes");
                return List.of(); // Lista vacía
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerTopClientes: " + e.getMessage());
            return List.of();
        }
    }
    
    // Clases de datos para la comunicación cliente-servidor
    
    public static class AnalisisClientesData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int totalClientes;
        private int clientesNuevosMes;
        private double promedioMascotasPorCliente;
        
        public int getTotalClientes() { return totalClientes; }
        public void setTotalClientes(int totalClientes) { this.totalClientes = totalClientes; }
        
        public int getClientesNuevosMes() { return clientesNuevosMes; }
        public void setClientesNuevosMes(int clientesNuevosMes) { this.clientesNuevosMes = clientesNuevosMes; }
        
        public double getPromedioMascotasPorCliente() { return promedioMascotasPorCliente; }
        public void setPromedioMascotasPorCliente(double promedioMascotasPorCliente) { this.promedioMascotasPorCliente = promedioMascotasPorCliente; }
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
    
    public static class ClienteTopData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String nombre;
        private double totalFacturado;
        private int numeroFacturas;
        private double promedioFactura;
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public double getTotalFacturado() { return totalFacturado; }
        public void setTotalFacturado(double totalFacturado) { this.totalFacturado = totalFacturado; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioFactura() { return promedioFactura; }
        public void setPromedioFactura(double promedioFactura) { this.promedioFactura = promedioFactura; }
    }
    
    // Clase para las filas de la tabla (sin cambios)
    public static class ClienteTopRow {
        private String nombre;
        private Double totalFacturado;
        private Integer numeroFacturas;
        private Double promedioFactura;
        private String totalFacturadoFormateado;
        private String promedioFacturaFormateado;
        
        public ClienteTopRow(String nombre, Double totalFacturado, Integer numeroFacturas, 
                           Double promedioFactura, String totalFacturadoFormateado, 
                           String promedioFacturaFormateado) {
            this.nombre = nombre;
            this.totalFacturado = totalFacturado;
            this.numeroFacturas = numeroFacturas;
            this.promedioFactura = promedioFactura;
            this.totalFacturadoFormateado = totalFacturadoFormateado;
            this.promedioFacturaFormateado = promedioFacturaFormateado;
        }
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public Double getTotalFacturado() { return totalFacturado; }
        public void setTotalFacturado(Double totalFacturado) { this.totalFacturado = totalFacturado; }
        
        public Integer getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(Integer numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public Double getPromedioFactura() { return promedioFactura; }
        public void setPromedioFactura(Double promedioFactura) { this.promedioFactura = promedioFactura; }
        
        public String getTotalFacturadoFormateado() { return totalFacturadoFormateado; }
        public void setTotalFacturadoFormateado(String totalFacturadoFormateado) { this.totalFacturadoFormateado = totalFacturadoFormateado; }
        
        public String getPromedioFacturaFormateado() { return promedioFacturaFormateado; }
        public void setPromedioFacturaFormateado(String promedioFacturaFormateado) { this.promedioFacturaFormateado = promedioFacturaFormateado; }
    }
} 