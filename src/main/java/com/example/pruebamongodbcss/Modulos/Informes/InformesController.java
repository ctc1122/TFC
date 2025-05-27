package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.theme.ThemeUtil;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class InformesController implements Initializable {

    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private VBox contentContainer;
    
    @FXML
    private HBox metricsContainer;
    
    @FXML
    private HBox chartsContainer;
    
    @FXML
    private GridPane reportsGrid;
    
    @FXML
    private JFXButton btnRefresh;
    
    private ServicioInformes servicioInformes;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    private DecimalFormat formatoPorcentaje = new DecimalFormat("#0.0%");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        servicioInformes = new ServicioInformes();
        
        // Configurar el scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Configurar el botón de actualizar
        configurarBotonRefresh();
        
        // Cargar el dashboard inicial
        cargarDashboard();
        
        // Configurar actualización automática cada 5 minutos
        configurarActualizacionAutomatica();
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private void configurarBotonRefresh() {
        try {
            ImageView refreshIcon = new ImageView(new Image(getClass().getResourceAsStream("/Iconos/iconRefresh.png")));
            refreshIcon.setFitHeight(20);
            refreshIcon.setFitWidth(20);
            btnRefresh.setGraphic(refreshIcon);
        } catch (Exception e) {
            btnRefresh.setText("🔄");
        }
        
        btnRefresh.setOnAction(e -> {
            // Animación de rotación del botón
            animarRefresh();
            cargarDashboard();
        });
    }
    
    private void animarRefresh() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), btnRefresh);
        fade.setFromValue(1.0);
        fade.setToValue(0.5);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.play();
    }
    
    private void cargarDashboard() {
        Platform.runLater(() -> {
            try {
                // Limpiar contenido anterior
                metricsContainer.getChildren().clear();
                chartsContainer.getChildren().clear();
                reportsGrid.getChildren().clear();
                
                // Cargar métricas principales
                cargarMetricasPrincipales();
                
                // Cargar gráficos
                cargarGraficos();
                
                // Cargar tarjetas de reportes
                cargarTarjetasReportes();
                
            } catch (Exception e) {
                System.err.println("Error al cargar dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void cargarMetricasPrincipales() {
        ServicioInformes.DashboardMetricas metricas = servicioInformes.obtenerMetricasDashboard();
        
        // Crear tarjetas de métricas
        VBox ventasHoy = crearTarjetaMetrica("Ventas Hoy", 
            formatoMoneda.format(metricas.getVentasHoy()), 
            "💰", "#4CAF50");
            
        VBox ventasMes = crearTarjetaMetrica("Ventas del Mes", 
            formatoMoneda.format(metricas.getVentasMesActual()), 
            "📈", "#2196F3",
            formatoPorcentaje.format(metricas.getPorcentajeCambioVentas() / 100.0));
            
        VBox clientesAno = crearTarjetaMetrica("Clientes Este Año", 
            String.valueOf(metricas.getClientesAnoActual()), 
            "👥", "#FF9800",
            formatoPorcentaje.format(metricas.getPorcentajeCambioClientes() / 100.0));
            
        VBox citasHoy = crearTarjetaMetrica("Citas Hoy", 
            String.valueOf(metricas.getCitasHoy()), 
            "📅", "#9C27B0");
            
        VBox promedioVentas = crearTarjetaMetrica("Promedio Diario", 
            formatoMoneda.format(metricas.getPromedioVentasDiarias()), 
            "📊", "#607D8B");
        
        metricsContainer.getChildren().addAll(ventasHoy, ventasMes, clientesAno, citasHoy, promedioVentas);
        metricsContainer.setSpacing(20);
        metricsContainer.setAlignment(Pos.CENTER);
    }
    
    private VBox crearTarjetaMetrica(String titulo, String valor, String icono, String color) {
        return crearTarjetaMetrica(titulo, valor, icono, color, null);
    }
    
    private VBox crearTarjetaMetrica(String titulo, String valor, String icono, String color, String cambio) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("metric-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(10);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(200);
        tarjeta.setPrefHeight(120);
        
        // Estilo de la tarjeta
        tarjeta.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);" +
            "-fx-border-radius: 10;"
        );
        
        // Icono y título
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(10);
        
        Label iconLabel = new Label(icono);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label titleLabel = new Label(titulo);
        titleLabel.getStyleClass().add("metric-title");
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        // Valor principal
        Label valueLabel = new Label(valor);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        tarjeta.getChildren().addAll(header, valueLabel);
        
        // Cambio porcentual si se proporciona
        if (cambio != null) {
            Label changeLabel = new Label(cambio);
            changeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                (cambio.startsWith("-") ? "#f44336" : "#4CAF50") + ";");
            tarjeta.getChildren().add(changeLabel);
        }
        
        return tarjeta;
    }
    
    private void cargarGraficos() {
        // Gráfico de ventas mensuales
        LineChart<String, Number> ventasChart = crearGraficoVentas();
        
        // Gráfico de citas por estado
        PieChart citasChart = crearGraficoCitas();
        
        // Contenedores para los gráficos
        VBox ventasContainer = new VBox();
        ventasContainer.getChildren().addAll(
            new Label("Ventas Mensuales"),
            ventasChart
        );
        ventasContainer.setSpacing(10);
        ventasContainer.setPrefWidth(400);
        
        VBox citasContainer = new VBox();
        citasContainer.getChildren().addAll(
            new Label("Distribución de Citas"),
            citasChart
        );
        citasContainer.setSpacing(10);
        citasContainer.setPrefWidth(400);
        
        chartsContainer.getChildren().addAll(ventasContainer, citasContainer);
        chartsContainer.setSpacing(30);
        chartsContainer.setAlignment(Pos.CENTER);
    }
    
    private LineChart<String, Number> crearGraficoVentas() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Ventas (€)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Ventas");
        chart.setPrefHeight(300);
        chart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas");
        
        List<ServicioInformes.DatoGrafico> datos = servicioInformes.obtenerVentasMensuales(6);
        for (ServicioInformes.DatoGrafico dato : datos) {
            series.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
        }
        
        chart.getData().add(series);
        return chart;
    }
    
    private PieChart crearGraficoCitas() {
        PieChart chart = new PieChart();
        chart.setTitle("Estados de Citas");
        chart.setPrefHeight(300);
        
        List<ServicioInformes.DatoGrafico> datos = servicioInformes.obtenerCitasPorEstado();
        for (ServicioInformes.DatoGrafico dato : datos) {
            PieChart.Data slice = new PieChart.Data(dato.getEtiqueta(), dato.getValor());
            chart.getData().add(slice);
        }
        
        return chart;
    }
    
    private void cargarTarjetasReportes() {
        // Configurar el grid
        reportsGrid.setHgap(20);
        reportsGrid.setVgap(20);
        reportsGrid.setPadding(new Insets(20));
        
        // Crear tarjetas de reportes
        VBox reporteVentas = crearTarjetaReporte("Reporte de Ventas", 
            "Análisis detallado de ventas por período", 
            "/Iconos/iconInvoice1.png", 
            () -> abrirReporteVentas());
            
        VBox reporteClientes = crearTarjetaReporte("Reporte de Clientes", 
            "Estadísticas y análisis de clientes", 
            "/Iconos/IconPruebaClientes.png", 
            () -> abrirReporteClientes());
            
        VBox reporteFichajes = crearTarjetaReporte("Reporte de Fichajes", 
            "Análisis de asistencia y horas trabajadas", 
            "/Iconos/iconClock2.png", 
            () -> abrirReporteFichajes());
            
        VBox reporteCitas = crearTarjetaReporte("Reporte de Citas", 
            "Estadísticas de citas y servicios", 
            "/Iconos/iconInicio4.png", 
            () -> abrirReporteCitas());
            
        VBox reporteFinanciero = crearTarjetaReporte("Reporte Financiero", 
            "Análisis financiero completo", 
            "/Iconos/iconAdministrador2.png", 
            () -> abrirReporteFinanciero());
            
        VBox reporteInventario = crearTarjetaReporte("Reporte de Inventario", 
            "Control de stock y productos", 
            "/Iconos/iconPet2.png", 
            () -> abrirReporteInventario());
        
        // Añadir al grid
        reportsGrid.add(reporteVentas, 0, 0);
        reportsGrid.add(reporteClientes, 1, 0);
        reportsGrid.add(reporteFichajes, 2, 0);
        reportsGrid.add(reporteCitas, 0, 1);
        reportsGrid.add(reporteFinanciero, 1, 1);
        reportsGrid.add(reporteInventario, 2, 1);
    }
    
    private VBox crearTarjetaReporte(String titulo, String descripcion, String iconPath, Runnable accion) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("report-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(15);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(250);
        tarjeta.setPrefHeight(180);
        
        // Estilo de la tarjeta
        tarjeta.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);" +
            "-fx-border-radius: 15;" +
            "-fx-cursor: hand;"
        );
        
        // Efecto hover
        tarjeta.setOnMouseEntered(e -> {
            tarjeta.setStyle(
                "-fx-background-color: #f5f5f5;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);" +
                "-fx-border-radius: 15;" +
                "-fx-cursor: hand;"
            );
        });
        
        tarjeta.setOnMouseExited(e -> {
            tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);" +
                "-fx-border-radius: 15;" +
                "-fx-cursor: hand;"
            );
        });
        
        // Icono
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            icon.setPreserveRatio(true);
            tarjeta.getChildren().add(icon);
        } catch (Exception e) {
            Label iconLabel = new Label("📊");
            iconLabel.setStyle("-fx-font-size: 48px;");
            tarjeta.getChildren().add(iconLabel);
        }
        
        // Título
        Label titleLabel = new Label(titulo);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        
        // Descripción
        Label descLabel = new Label(descripcion);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        
        tarjeta.getChildren().addAll(titleLabel, descLabel);
        
        // Acción al hacer clic
        tarjeta.setOnMouseClicked(e -> accion.run());
        
        return tarjeta;
    }
    
    private void configurarActualizacionAutomatica() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> cargarDashboard()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    // Métodos para abrir diferentes reportes
    private void abrirReporteVentas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-ventas.fxml"));
            Parent reporteView = ThemeUtil.loadWithTheme(loader);
            
            ReporteVentasController controller = loader.getController();
            if (controller != null) {
                controller.setUsuarioActual(usuarioActual);
            }
            
            mainContainer.setCenter(reporteView);
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de ventas: " + e.getMessage());
        }
    }
    
    private void abrirReporteClientes() {
        // Implementar reporte de clientes
        System.out.println("Abriendo reporte de clientes...");
    }
    
    private void abrirReporteFichajes() {
        // Implementar reporte de fichajes
        System.out.println("Abriendo reporte de fichajes...");
    }
    
    private void abrirReporteCitas() {
        // Implementar reporte de citas
        System.out.println("Abriendo reporte de citas...");
    }
    
    private void abrirReporteFinanciero() {
        // Implementar reporte financiero
        System.out.println("Abriendo reporte financiero...");
    }
    
    private void abrirReporteInventario() {
        // Implementar reporte de inventario
        System.out.println("Abriendo reporte de inventario...");
    }
    
    @FXML
    private void volverAlDashboard() {
        cargarDashboard();
    }
} 