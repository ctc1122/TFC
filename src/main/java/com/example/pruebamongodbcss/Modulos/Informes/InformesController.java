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
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
        
        // Aplicar clases específicas para el módulo de informes
        mainContainer.getStyleClass().add("informes-view");
        scrollPane.getStyleClass().add("informes-container");
        contentContainer.getStyleClass().add("informes-container");
        
        // Aplicar IDs para que los selectores CSS funcionen
        mainContainer.setId("mainContainer");
        scrollPane.setId("scrollPane");
        contentContainer.setId("contentContainer");
        
        // Aplicar clases CSS a los contenedores de métricas, gráficos y reportes
        if (metricsContainer != null) {
            metricsContainer.getStyleClass().add("metrics-container");
        }
        if (chartsContainer != null) {
            chartsContainer.getStyleClass().add("charts-container");
        }
        if (reportsGrid != null) {
            reportsGrid.getStyleClass().add("reports-grid");
        }
        
        // Hacer todos los contenedores transparentes con estilos inline como respaldo
        aplicarTransparenciaForzada();
        
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
    
    private void aplicarTransparenciaForzada() {
        // Aplicar transparencia con estilos inline como respaldo
        String transparentStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0px;";
        
        mainContainer.setStyle(transparentStyle);
        scrollPane.setStyle(transparentStyle);
        contentContainer.setStyle(transparentStyle);
        
        if (metricsContainer != null) {
            metricsContainer.setStyle(transparentStyle);
        }
        if (chartsContainer != null) {
            chartsContainer.setStyle(transparentStyle);
        }
        if (reportsGrid != null) {
            reportsGrid.setStyle(transparentStyle);
        }
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
                
                // Reaplica transparencia después de cargar contenido
                aplicarTransparenciaForzada();
                
            } catch (Exception e) {
                System.err.println("Error al cargar dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void cargarMetricasPrincipales() {
        ServicioInformes.DashboardMetricas metricas = servicioInformes.obtenerMetricasDashboard();
        
        // Crear tarjetas de métricas
        VBox ventasHoy = crearTarjetaMetrica("Ventas Hoy", formatoMoneda.format(metricas.getVentasHoy()), "💰", "#4CAF50", null);
            
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
            "📅", "#9C27B0", null);
            
        VBox promedioVentas = crearTarjetaMetrica("Promedio Diario", 
            formatoMoneda.format(metricas.getPromedioVentasDiarias()), 
            "📊", "#607D8B", null);
        
        // Limpiar y configurar el contenedor
        metricsContainer.getChildren().clear();
        metricsContainer.getChildren().addAll(ventasHoy, ventasMes, clientesAno, citasHoy, promedioVentas);
        metricsContainer.setSpacing(15);
        metricsContainer.setAlignment(Pos.CENTER);
        metricsContainer.setPadding(new Insets(20));
        
        // Hacer el contenedor transparente
        metricsContainer.setStyle("-fx-background-color: transparent;");
    }
    
    // Método sobrecargado para métricas sin cambio porcentual
    private VBox crearTarjetaMetrica(String titulo, String valor, String icono, String color) {
        return crearTarjetaMetrica(titulo, valor, icono, color, null);
    }
    
    private VBox crearTarjetaMetrica(String titulo, String valor, String icono, String color, String cambio) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().addAll("metric-card", "module-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(8);
        tarjeta.setPadding(new Insets(15));
        tarjeta.setPrefWidth(200);
        tarjeta.setPrefHeight(120);
        tarjeta.setMaxWidth(200);
        tarjeta.setMinWidth(200);
        
        // NO aplicar transparencia - dejar que el CSS maneje el estilo
        // tarjeta.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // Icono y título en la misma línea
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(8);
        header.setMaxWidth(Double.MAX_VALUE);
        
        Label iconLabel = new Label(icono);
        iconLabel.getStyleClass().add("metric-icon");
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label titleLabel = new Label(titulo);
        titleLabel.getStyleClass().addAll("metric-title", "small-label");
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        // Valor principal centrado
        Label valueLabel = new Label(valor);
        valueLabel.getStyleClass().addAll("metric-value", "title-label");
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center;");
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        
        tarjeta.getChildren().addAll(header, valueLabel);
        
        // Cambio porcentual si se proporciona
        if (cambio != null && !cambio.trim().isEmpty()) {
            Label changeLabel = new Label(cambio);
            changeLabel.getStyleClass().add("metric-change");
            changeLabel.setStyle("-fx-font-size: 11px; -fx-text-alignment: center;");
            changeLabel.setAlignment(Pos.CENTER);
            changeLabel.setMaxWidth(Double.MAX_VALUE);
            if (cambio.startsWith("-")) {
                changeLabel.setStyle(changeLabel.getStyle() + "; -fx-text-fill: #e74c3c;");
            } else {
                changeLabel.setStyle(changeLabel.getStyle() + "; -fx-text-fill: #2ecc71;");
            }
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
        
        Label ventasTitle = new Label("Ventas Mensuales");
        ventasTitle.getStyleClass().addAll("informes-section-title", "subtitle-label");
        
        ventasContainer.getChildren().addAll(ventasTitle, ventasChart);
        ventasContainer.setSpacing(10);
        ventasContainer.setPrefWidth(400);
        ventasContainer.getStyleClass().add("chart-container");
        // NO aplicar transparencia - dejar que el CSS maneje el estilo
        // ventasContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        VBox citasContainer = new VBox();
        
        Label citasTitle = new Label("Distribución de Citas");
        citasTitle.getStyleClass().addAll("informes-section-title", "subtitle-label");
        
        citasContainer.getChildren().addAll(citasTitle, citasChart);
        citasContainer.setSpacing(10);
        citasContainer.setPrefWidth(400);
        citasContainer.getStyleClass().add("chart-container");
        // NO aplicar transparencia - dejar que el CSS maneje el estilo
        // citasContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        chartsContainer.getChildren().clear();
        chartsContainer.getChildren().addAll(ventasContainer, citasContainer);
        chartsContainer.setSpacing(30);
        chartsContainer.setAlignment(Pos.CENTER);
        chartsContainer.setStyle("-fx-background-color: transparent;");
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
        reportsGrid.getChildren().clear();
        reportsGrid.setHgap(20);
        reportsGrid.setVgap(20);
        reportsGrid.setPadding(new Insets(20));
        reportsGrid.setAlignment(Pos.CENTER);
        reportsGrid.setStyle("-fx-background-color: transparent;");
        
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
        tarjeta.getStyleClass().addAll("report-card", "module-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(15);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(250);
        tarjeta.setPrefHeight(180);
        
        // NO aplicar transparencia - dejar que el CSS maneje el estilo
        // tarjeta.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        
        // Agregar clase para efectos hover
        tarjeta.getStyleClass().add("report-card-hover");
        
        // Icono
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            icon.setPreserveRatio(true);
            tarjeta.getChildren().add(icon);
        } catch (Exception e) {
            Label iconLabel = new Label("📊");
            iconLabel.getStyleClass().add("report-icon");
            iconLabel.setStyle("-fx-font-size: 32px;");
            tarjeta.getChildren().add(iconLabel);
        }
        
        // Título
        Label titleLabel = new Label(titulo);
        titleLabel.getStyleClass().addAll("report-title", "subtitle-label");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-text-alignment: center; -fx-font-weight: bold;");
        
        // Descripción
        Label descLabel = new Label(descripcion);
        descLabel.getStyleClass().addAll("report-description", "small-label");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 11px;");
        
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-clientes.fxml"));
            Parent reporteView = ThemeUtil.loadWithTheme(loader);
            
            ReporteClientesController controller = loader.getController();
            if (controller != null) {
                controller.setUsuarioActual(usuarioActual);
            }
            
            mainContainer.setCenter(reporteView);
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de clientes: " + e.getMessage());
        }
    }
    
    private void abrirReporteFichajes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-empleados.fxml"));
            Parent reporteView = ThemeUtil.loadWithTheme(loader);
            
            ReporteEmpleadosController controller = loader.getController();
            if (controller != null) {
                controller.setUsuarioActual(usuarioActual);
            }
            
            mainContainer.setCenter(reporteView);
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de empleados: " + e.getMessage());
        }
    }
    
    private void abrirReporteCitas() {
        // Mostrar análisis comparativo anual
        mostrarAnalisisComparativo();
    }
    
    private void mostrarAnalisisComparativo() {
        try {
            // Crear una vista de análisis comparativo
            VBox comparativoContainer = new VBox();
            comparativoContainer.setSpacing(20);
            comparativoContainer.getStyleClass().addAll("module-main-container", "informes-view");
            comparativoContainer.setPadding(new Insets(20));
            
            // Aplicar tema al contenedor
            ThemeUtil.applyModuleTheme(comparativoContainer);
            
            // Título
            Label titulo = new Label("Análisis Comparativo Anual");
            titulo.getStyleClass().addAll("informes-section-title", "title-label");
            
            // Botón volver
            Button btnVolver = new Button("← Volver al Dashboard");
            btnVolver.getStyleClass().addAll("btn-secondary");
            btnVolver.setOnAction(e -> cargarDashboard());
            
            HBox header = new HBox();
            header.setSpacing(20);
            header.getChildren().addAll(titulo, btnVolver);
            
            // Obtener datos comparativos
            ServicioInformes.ComparativaAnual comparativa = servicioInformes.obtenerComparativaAnual();
            
            // Métricas comparativas
            HBox metricas = new HBox();
            metricas.setSpacing(40);
            metricas.setAlignment(Pos.CENTER);
            
            VBox metricaActual = new VBox();
            metricaActual.setAlignment(Pos.CENTER);
            metricaActual.setSpacing(10);
            metricaActual.getStyleClass().addAll("module-card", "metric-card");
            metricaActual.setPadding(new Insets(20));
            ThemeUtil.applyCardTheme(metricaActual);
            
            Label labelActual = new Label("Año Actual");
            labelActual.getStyleClass().addAll("metric-title", "subtitle-label");
            Label valorActual = new Label(formatoMoneda.format(comparativa.getTotalAnoActual()));
            valorActual.getStyleClass().addAll("metric-value", "title-label");
            
            metricaActual.getChildren().addAll(labelActual, valorActual);
            
            VBox metricaAnterior = new VBox();
            metricaAnterior.setAlignment(Pos.CENTER);
            metricaAnterior.setSpacing(10);
            metricaAnterior.getStyleClass().addAll("module-card", "metric-card");
            metricaAnterior.setPadding(new Insets(20));
            ThemeUtil.applyCardTheme(metricaAnterior);
            
            Label labelAnterior = new Label("Año Anterior");
            labelAnterior.getStyleClass().addAll("metric-title", "subtitle-label");
            Label valorAnterior = new Label(formatoMoneda.format(comparativa.getTotalAnoAnterior()));
            valorAnterior.getStyleClass().addAll("metric-value", "title-label");
            
            metricaAnterior.getChildren().addAll(labelAnterior, valorAnterior);
            
            VBox metricaCrecimiento = new VBox();
            metricaCrecimiento.setAlignment(Pos.CENTER);
            metricaCrecimiento.setSpacing(10);
            metricaCrecimiento.getStyleClass().addAll("module-card", "metric-card");
            metricaCrecimiento.setPadding(new Insets(20));
            ThemeUtil.applyCardTheme(metricaCrecimiento);
            
            Label labelCrecimiento = new Label("Crecimiento");
            labelCrecimiento.getStyleClass().addAll("metric-title", "subtitle-label");
            Label valorCrecimiento = new Label(formatoPorcentaje.format(comparativa.getPorcentajeCrecimiento() / 100.0));
            valorCrecimiento.getStyleClass().addAll("metric-value", "title-label");
            
            metricaCrecimiento.getChildren().addAll(labelCrecimiento, valorCrecimiento);
            
            metricas.getChildren().addAll(metricaActual, metricaAnterior, metricaCrecimiento);
            
            // Gráfico comparativo
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Comparativa de Ventas por Mes");
            chart.setPrefHeight(400);
            
            XYChart.Series<String, Number> serieActual = new XYChart.Series<>();
            serieActual.setName("Año Actual");
            
            XYChart.Series<String, Number> serieAnterior = new XYChart.Series<>();
            serieAnterior.setName("Año Anterior");
            
            for (ServicioInformes.DatoGrafico dato : comparativa.getVentasAnoActual()) {
                serieActual.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
            }
            
            for (ServicioInformes.DatoGrafico dato : comparativa.getVentasAnoAnterior()) {
                serieAnterior.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
            }
            
            chart.getData().addAll(serieActual, serieAnterior);
            
            VBox chartContainer = new VBox();
            chartContainer.getStyleClass().addAll("module-card", "chart-container");
            chartContainer.setPadding(new Insets(20));
            ThemeUtil.applyCardTheme(chartContainer);
            chartContainer.getChildren().add(chart);
            
            comparativoContainer.getChildren().addAll(header, metricas, chartContainer);
            
            mainContainer.setCenter(comparativoContainer);
            
        } catch (Exception e) {
            System.err.println("Error al mostrar análisis comparativo: " + e.getMessage());
        }
    }
    
    private void abrirReporteFinanciero() {
        // Redirigir al reporte de ventas con análisis financiero extendido
        abrirReporteVentas();
    }
    
    private void abrirReporteInventario() {
        // Implementar reporte de inventario/servicios
        System.out.println("Abriendo análisis de servicios más demandados...");
        mostrarAnalisisServicios();
    }
    
    private void mostrarAnalisisServicios() {
        try {
            // Crear una vista rápida de análisis de servicios
            VBox analisisContainer = new VBox();
            analisisContainer.setSpacing(20);
            analisisContainer.getStyleClass().addAll("module-main-container", "informes-view");
            analisisContainer.setPadding(new Insets(20));
            
            // Aplicar tema al contenedor
            ThemeUtil.applyModuleTheme(analisisContainer);
            
            // Título
            Label titulo = new Label("Análisis de Servicios Más Demandados");
            titulo.getStyleClass().addAll("informes-section-title", "title-label");
            
            // Botón volver
            Button btnVolver = new Button("← Volver al Dashboard");
            btnVolver.getStyleClass().addAll("btn-secondary");
            btnVolver.setOnAction(e -> cargarDashboard());
            
            HBox header = new HBox();
            header.setSpacing(20);
            header.getChildren().addAll(titulo, btnVolver);
            
            // Obtener datos de servicios
            List<ServicioInformes.ServicioVendido> topServicios = servicioInformes.obtenerTopServicios(10);
            
            // Crear tabla de servicios
            TableView<ServicioInformes.ServicioVendido> tablaServicios = new TableView<>();
            tablaServicios.getStyleClass().addAll("module-card");
            ThemeUtil.applyCardTheme(tablaServicios);
            
            TableColumn<ServicioInformes.ServicioVendido, String> colNombre = new TableColumn<>("Servicio");
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colNombre.setPrefWidth(300);
            
            TableColumn<ServicioInformes.ServicioVendido, Integer> colCantidad = new TableColumn<>("Cantidad");
            colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
            colCantidad.setPrefWidth(150);
            
            TableColumn<ServicioInformes.ServicioVendido, String> colTotal = new TableColumn<>("Total Facturado");
            colTotal.setCellValueFactory(cellData -> {
                double total = cellData.getValue().getTotal();
                return new SimpleStringProperty(formatoMoneda.format(total));
            });
            colTotal.setPrefWidth(150);
            
            tablaServicios.getColumns().addAll(colNombre, colCantidad, colTotal);
            tablaServicios.getItems().addAll(topServicios);
            
            analisisContainer.getChildren().addAll(header, tablaServicios);
            
            mainContainer.setCenter(analisisContainer);
            
        } catch (Exception e) {
            System.err.println("Error al mostrar análisis de servicios: " + e.getMessage());
        }
    }
    
    @FXML
    private void volverAlDashboard() {
        cargarDashboard();
    }
    
    /**
     * Método público para reaplica transparencia después de cambios de tema
     * Puede ser llamado desde ThemeManager o otros controladores
     */
    public void reaplicarTransparencia() {
        Platform.runLater(() -> {
            aplicarTransparenciaForzada();
            
            // También reaplica las clases CSS
            mainContainer.getStyleClass().removeAll("informes-view");
            mainContainer.getStyleClass().add("informes-view");
            
            scrollPane.getStyleClass().removeAll("informes-container");
            scrollPane.getStyleClass().add("informes-container");
            
            contentContainer.getStyleClass().removeAll("informes-container");
            contentContainer.getStyleClass().add("informes-container");
        });
    }
} 