package com.example.pruebamongodbcss.Modulos.Informes;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ReporteClientesController implements Initializable {

    @FXML
    private Label lblTotalClientes;
    
    @FXML
    private Label lblClientesNuevosMes;
    
    @FXML
    private Label lblClientesActivos;
    
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
    
    private ServicioInformes servicioInformes;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    private DecimalFormat formatoDecimal = new DecimalFormat("#0.0");
    
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
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTotalFacturado.setCellValueFactory(new PropertyValueFactory<>("totalFacturadoFormateado"));
        colNumeroFacturas.setCellValueFactory(new PropertyValueFactory<>("numeroFacturas"));
        colPromedioFactura.setCellValueFactory(new PropertyValueFactory<>("promedioFacturaFormateado"));
        
        tableTopClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void generarReporte() {
        try {
            // Obtener análisis de clientes
            ServicioInformes.AnalisisClientes analisis = servicioInformes.obtenerAnalisisClientes();
            
            // Actualizar métricas
            lblTotalClientes.setText(String.valueOf(analisis.getTotalClientes()));
            lblClientesNuevosMes.setText(String.valueOf(analisis.getClientesNuevosMes()));
            lblClientesActivos.setText(String.valueOf(analisis.getClientesActivos()));
            lblPromedioMascotas.setText(formatoDecimal.format(analisis.getPromedioMascotasPorCliente()));
            
            // Actualizar gráfico de evolución de clientes
            actualizarGraficoEvolucion();
            
            // Obtener top de clientes
            List<ServicioInformes.ClienteTop> topClientes = servicioInformes.obtenerTopClientes(10);
            
            // Actualizar tabla
            actualizarTablaTopClientes(topClientes);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte de clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void actualizarGraficoEvolucion() {
        chartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Número de Clientes");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Clientes - Últimos 12 Meses");
        chart.setPrefHeight(400);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Clientes Nuevos");
        
        // Obtener datos de clientes por mes
        List<ServicioInformes.DatoGrafico> datos = servicioInformes.obtenerClientesPorMes(12);
        for (ServicioInformes.DatoGrafico dato : datos) {
            series.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
        }
        
        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }
    
    private void actualizarTablaTopClientes(List<ServicioInformes.ClienteTop> topClientes) {
        tableTopClientes.getItems().clear();
        
        for (ServicioInformes.ClienteTop cliente : topClientes) {
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
        System.out.println("Exportando reporte de clientes...");
    }
    
    @FXML
    private void volverAlDashboard() {
        // Este método será llamado desde el FXML
    }
    
    // Clase para las filas de la tabla
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