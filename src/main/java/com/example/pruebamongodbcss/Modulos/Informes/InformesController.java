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
import com.example.pruebamongodbcss.theme.ThemeUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import Utilidades1.GestorSocket;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
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
    
    @FXML
    private JFXComboBox<String> comboFiltroTipo;
    
    @FXML
    private JFXComboBox<Integer> comboAno;
    
    @FXML
    private JFXComboBox<String> comboMes;
    
    @FXML
    private Label lblFiltroActual;
    
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    private DecimalFormat formatoPorcentaje = new DecimalFormat("#0.0%");
    
    // Variables para el filtro actual
    private String filtroActual = "ANUAL";
    private int anoActual = LocalDate.now().getYear();
    private Integer mesActual = null;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gestorSocket = GestorSocket.getInstance();
        
        // Reinicializar el módulo completo
        reinicializarModulo();
    }
    
    /**
     * Reinicializa completamente el módulo de informes
     * Útil cuando se vuelve de otros reportes para evitar conflictos de estilos
     */
    public void reinicializarModulo() {
        // Limpiar cualquier estilo previo
        limpiarEstilosPrevios();
        
        // Aplicar clases específicas para el módulo de informes
        aplicarEstilosModulo();
        
        // Configurar controles
        configurarControles();
        
        // Cargar el dashboard inicial
        cargarDashboard();
        
        // Configurar actualización automática cada 5 minutos
        configurarActualizacionAutomatica();
    }
    
    /**
     * Limpia estilos previos que puedan interferir
     */
    private void limpiarEstilosPrevios() {
        try {
            // Limpiar estilos en línea problemáticos
            mainContainer.setStyle("");
            scrollPane.setStyle("");
            contentContainer.setStyle("");
            
            if (metricsContainer != null) {
                metricsContainer.setStyle("");
            }
            if (chartsContainer != null) {
                chartsContainer.setStyle("");
            }
            if (reportsGrid != null) {
                reportsGrid.setStyle("");
            }
            
            // Limpiar clases CSS problemáticas
            Platform.runLater(() -> {
                mainContainer.getStyleClass().removeAll("reporte-container", "reporte-content");
                scrollPane.getStyleClass().removeAll("reporte-container", "reporte-content");
                contentContainer.getStyleClass().removeAll("reporte-container", "reporte-content");
            });
            
        } catch (Exception e) {
            System.err.println("Error al limpiar estilos previos: " + e.getMessage());
        }
    }
    
    /**
     * Aplica los estilos específicos del módulo de informes
     */
    private void aplicarEstilosModulo() {
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
    }
    
    private void configurarControles() {
        // Configurar el botón de actualizar
        configurarBotonRefresh();
        
        // Configurar filtros
        configurarFiltros();
    }
    
    private void configurarFiltros() {
        // Configurar combo tipo de filtro
        if (comboFiltroTipo != null) {
            comboFiltroTipo.getItems().addAll("ANUAL", "MENSUAL");
            comboFiltroTipo.setValue("ANUAL");
            comboFiltroTipo.setOnAction(e -> cambiarTipoFiltro());
        }
        
        // Configurar combo años (últimos 5 años)
        if (comboAno != null) {
            int anoActualTmp = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                comboAno.getItems().add(anoActualTmp - i);
            }
            comboAno.setValue(anoActualTmp);
            comboAno.setOnAction(e -> cambiarAno());
        }
        
        // Configurar combo meses
        if (comboMes != null) {
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            comboMes.getItems().addAll(meses);
            comboMes.setValue(meses[LocalDate.now().getMonthValue() - 1]);
            comboMes.setOnAction(e -> cambiarMes());
            comboMes.setVisible(false); // Oculto por defecto
        }
        
        // Actualizar label del filtro
        actualizarLabelFiltro();
    }
    
    private void cambiarTipoFiltro() {
        if (comboFiltroTipo != null) {
            filtroActual = comboFiltroTipo.getValue();
            if ("MENSUAL".equals(filtroActual)) {
                comboMes.setVisible(true);
                mesActual = comboMes.getSelectionModel().getSelectedIndex() + 1;
            } else {
                comboMes.setVisible(false);
                mesActual = null;
            }
            actualizarLabelFiltro();
            cargarDashboard();
        }
    }
    
    private void cambiarAno() {
        if (comboAno != null) {
            anoActual = comboAno.getValue();
            actualizarLabelFiltro();
            cargarDashboard();
        }
    }
    
    private void cambiarMes() {
        if (comboMes != null && "MENSUAL".equals(filtroActual)) {
            mesActual = comboMes.getSelectionModel().getSelectedIndex() + 1;
            actualizarLabelFiltro();
            cargarDashboard();
        }
    }
    
    private void actualizarLabelFiltro() {
        if (lblFiltroActual != null) {
            String textoFiltro;
            if ("ANUAL".equals(filtroActual)) {
                textoFiltro = "Filtrando por año: " + anoActual;
            } else {
                String nombreMes = comboMes.getValue();
                textoFiltro = "Filtrando por: " + nombreMes + " " + anoActual;
            }
            lblFiltroActual.setText(textoFiltro);
        }
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
                
                // Cargar métricas principales con filtros
                cargarMetricasPrincipalesConFiltros();
                
                // Cargar gráficos con filtros
                cargarGraficosConFiltros();
                
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
    
    private void cargarMetricasPrincipalesConFiltros() {
        // Calcular avance del mes (porcentaje)
        LocalDate hoy = LocalDate.now();
        int diaActual = hoy.getDayOfMonth();
        int diasTotalesMes = hoy.lengthOfMonth();
        double porcentajeAvance = ((double) diaActual / diasTotalesMes) * 100.0;
        
        VBox avanceMes = crearTarjetaMetrica("Avance del Mes", 
            formatoPorcentaje.format(porcentajeAvance / 100.0), 
            "📅", "#4CAF50");
        
        // Ventas según filtro
        double ventas;
        String tituloVentas;
        if ("ANUAL".equals(filtroActual)) {
            ventas = calcularVentasPorAno(anoActual);
            tituloVentas = "Ventas " + anoActual;
        } else {
            ventas = calcularVentasPorMesAno(mesActual, anoActual);
            String nombreMes = comboMes.getValue();
            tituloVentas = "Ventas " + nombreMes + " " + anoActual;
        }
        
        VBox ventasCard = crearTarjetaMetrica(tituloVentas, 
            formatoMoneda.format(ventas), 
            "💰", "#2196F3");
        
        // Pacientes según filtro
        int pacientes;
        String tituloPacientes;
        if ("ANUAL".equals(filtroActual)) {
            pacientes = contarPacientesPorAno(anoActual);
            tituloPacientes = "Pacientes " + anoActual;
        } else {
            pacientes = contarPacientesPorMesAno(mesActual, anoActual);
            String nombreMes = comboMes.getValue();
            tituloPacientes = "Pacientes " + nombreMes + " " + anoActual;
        }
        
        VBox pacientesCard = crearTarjetaMetrica(tituloPacientes, 
            String.valueOf(pacientes), 
            "🐕", "#FF9800");
        
        // Citas hoy (este no cambia)
        int citasHoy = contarCitasPorFecha(hoy, hoy);
        VBox citasCard = crearTarjetaMetrica("Citas Hoy", 
            String.valueOf(citasHoy), 
            "📋", "#9C27B0");
        
        // Fichajes según filtro
        int fichajes;
        String tituloFichajes;
        if ("ANUAL".equals(filtroActual)) {
            fichajes = contarFichajesPorAno(anoActual);
            tituloFichajes = "Fichajes " + anoActual;
        } else {
            fichajes = contarFichajesPorMesAno(mesActual, anoActual);
            String nombreMes = comboMes.getValue();
            tituloFichajes = "Fichajes " + nombreMes + " " + anoActual;
        }
        
        VBox fichajesCard = crearTarjetaMetrica(tituloFichajes, 
            String.valueOf(fichajes), 
            "🕒", "#607D8B");
        
        // Limpiar y configurar el contenedor
        metricsContainer.getChildren().clear();
        metricsContainer.getChildren().addAll(avanceMes, ventasCard, pacientesCard, citasCard, fichajesCard);
        metricsContainer.setSpacing(15);
        metricsContainer.setAlignment(Pos.CENTER);
        metricsContainer.setPadding(new Insets(20));
        
        // Hacer el contenedor transparente
        metricsContainer.setStyle("-fx-background-color: transparent;");
    }
    
    private void cargarGraficosConFiltros() {
        // Gráfico de evolución de ventas con filtros
        LineChart<String, Number> ventasChart = crearGraficoVentasConFiltros();
        
        // Gráfico de barras de usuarios por rol
        BarChart<String, Number> usuariosChart = crearGraficoUsuariosPorRol();
        
        // Contenedores para los gráficos
        VBox ventasContainer = new VBox();
        
        String tituloVentas = "ANUAL".equals(filtroActual) ? 
            "Evolución Ventas " + anoActual : 
            "Ventas Diarias " + comboMes.getValue() + " " + anoActual;
        
        Label ventasTitle = new Label(tituloVentas);
        ventasTitle.getStyleClass().addAll("informes-section-title", "subtitle-label");
        
        ventasContainer.getChildren().addAll(ventasTitle, ventasChart);
        ventasContainer.setSpacing(10);
        ventasContainer.setPrefWidth(400);
        ventasContainer.getStyleClass().add("chart-container");
        
        VBox usuariosContainer = new VBox();
        
        Label usuariosTitle = new Label("Distribución de Usuarios");
        usuariosTitle.getStyleClass().addAll("informes-section-title", "subtitle-label");
        
        usuariosContainer.getChildren().addAll(usuariosTitle, usuariosChart);
        usuariosContainer.setSpacing(10);
        usuariosContainer.setPrefWidth(400);
        usuariosContainer.getStyleClass().add("chart-container");
        
        chartsContainer.getChildren().clear();
        chartsContainer.getChildren().addAll(ventasContainer, usuariosContainer);
        chartsContainer.setSpacing(30);
        chartsContainer.setAlignment(Pos.CENTER);
        chartsContainer.setStyle("-fx-background-color: transparent;");
    }
    
    private LineChart<String, Number> crearGraficoVentasConFiltros() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        if ("ANUAL".equals(filtroActual)) {
            xAxis.setLabel("Mes");
        } else {
            xAxis.setLabel("Día");
        }
        yAxis.setLabel("Ventas (€)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Ventas");
        chart.setPrefHeight(300);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true); // Asegurar que se muestren los símbolos/puntos
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas");
        
        // Por ahora datos de prueba - implementaremos la petición al servidor después
        if ("ANUAL".equals(filtroActual)) {
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            for (int i = 0; i < meses.length; i++) {
                double ventas = calcularVentasPorMesAno(i + 1, anoActual);
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(meses[i], ventas);
                series.getData().add(dataPoint);
            }
        } else {
            // Datos diarios del mes actual - usar datos de cada día del mes
            LocalDate inicioMes = LocalDate.of(anoActual, mesActual, 1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            for (LocalDate fecha = inicioMes; !fecha.isAfter(finMes); fecha = fecha.plusDays(1)) {
                // Para datos diarios usamos la misma petición mensual dividida por días
                // (Los datos diarios requerirían otra petición específica al servidor)
                double ventasDia = calcularVentasPorMesAno(mesActual, anoActual) / finMes.getDayOfMonth();
                String dia = String.valueOf(fecha.getDayOfMonth());
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dia, ventasDia);
                series.getData().add(dataPoint);
            }
        }
        
        chart.getData().add(series);
        
        // Configurar tooltips dinámicos después de que el gráfico se haya renderizado
        Platform.runLater(() -> {
            configurarTooltipsGrafico(chart, series);
        });
        
        return chart;
    }
    
    /**
     * Configura tooltips dinámicos para el gráfico de ventas
     */
    private void configurarTooltipsGrafico(LineChart<String, Number> chart, XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                // Crear tooltip personalizado
                Tooltip tooltip = new Tooltip();
                
                String periodo = "ANUAL".equals(filtroActual) ? 
                    data.getXValue() + " " + anoActual : 
                    "Día " + data.getXValue() + " de " + comboMes.getValue() + " " + anoActual;
                
                double valor = data.getYValue().doubleValue();
                String valorFormateado = formatoMoneda.format(valor);
                
                tooltip.setText(periodo + "\nVentas: " + valorFormateado);
                tooltip.setStyle("-fx-background-color: rgba(0,0,0,0.8); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 6px; " +
                               "-fx-padding: 8px; " +
                               "-fx-font-size: 12px;");
                
                // Instalar el tooltip en el nodo
                Tooltip.install(data.getNode(), tooltip);
                
                // Efectos visuales al pasar el mouse
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-background-color: #FF6B6B; -fx-background-radius: 6px;");
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
    
    private BarChart<String, Number> crearGraficoUsuariosPorRol() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Rol");
        yAxis.setLabel("Cantidad");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Distribución por Roles");
        chart.setPrefHeight(300);
        chart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Usuarios");
        
        // Obtener datos reales del servidor
        try {
            String peticion = Protocolo.OBTENER_USUARIOS_POR_ROL + Protocolo.SEPARADOR_CODIGO;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_USUARIOS_POR_ROL_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ServicioInformes.DatoGrafico> datos = (List<ServicioInformes.DatoGrafico>) entrada.readObject();
                
                for (ServicioInformes.DatoGrafico dato : datos) {
                    series.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
                }
            } else {
                System.err.println("Error al obtener usuarios por rol, usando datos de prueba");
                // Fallback a datos de prueba
                series.getData().add(new XYChart.Data<>("Veterinario", 5));
                series.getData().add(new XYChart.Data<>("Auxiliar", 3)); 
                series.getData().add(new XYChart.Data<>("Recepcionista", 2));
                series.getData().add(new XYChart.Data<>("Administrador", 1));
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerUsuariosPorRol: " + e.getMessage());
            // Fallback a datos de prueba
            series.getData().add(new XYChart.Data<>("Veterinario", 5));
            series.getData().add(new XYChart.Data<>("Auxiliar", 3)); 
            series.getData().add(new XYChart.Data<>("Recepcionista", 2));
            series.getData().add(new XYChart.Data<>("Administrador", 1));
        }
        
        chart.getData().add(series);
        return chart;
    }
    
    // Método simplificado para crear tarjetas de métricas
    private VBox crearTarjetaMetrica(String titulo, String valor, String icono, String color) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().addAll("metric-card", "module-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(8);
        tarjeta.setPadding(new Insets(15));
        tarjeta.setPrefWidth(200);
        tarjeta.setPrefHeight(120);
        tarjeta.setMaxWidth(200);
        tarjeta.setMinWidth(200);
        
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
        
        return tarjeta;
    }
    
    private void cargarTarjetasReportes() {
        // Configurar el grid
        reportsGrid.getChildren().clear();
        reportsGrid.setHgap(20);
        reportsGrid.setVgap(20);
        reportsGrid.setPadding(new Insets(20));
        reportsGrid.setAlignment(Pos.CENTER);
        reportsGrid.setStyle("-fx-background-color: transparent;");
        
        // Crear tarjetas de reportes (solo 3 reportes principales)
        VBox reporteVentas = crearTarjetaReporte("Reporte de Ventas", 
            "Análisis detallado de ventas por período", 
            "/Iconos/iconInvoice1.png", 
            () -> abrirReporteVentas());
            
        VBox reporteClientes = crearTarjetaReporte("Reporte de Clientes", 
            "Estadísticas y análisis de clientes", 
            "/Iconos/IconPruebaClientes.png", 
            () -> abrirReporteClientes());
            
        VBox reporteEmpleados = crearTarjetaReporte("Reporte de Empleados", 
            "Análisis de asistencia y horas trabajadas", 
            "/Iconos/iconClock2.png", 
            () -> abrirReporteFichajes());
        
        // Añadir al grid en disposición 1x3 (una fila, tres columnas)
        reportsGrid.add(reporteVentas, 0, 0);     // Fila 0, Columna 0
        reportsGrid.add(reporteClientes, 1, 0);   // Fila 0, Columna 1
        reportsGrid.add(reporteEmpleados, 2, 0);  // Fila 0, Columna 2
    }
    
    private VBox crearTarjetaReporte(String titulo, String descripcion, String iconPath, Runnable accion) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().addAll("report-card", "module-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setSpacing(15);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(300);
        tarjeta.setPrefHeight(230);
        
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
            // Buscar la ventana principal y obtener la escena
            javafx.stage.Window ventanaPrincipal = btnRefresh.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar la vista del reporte de ventas
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-ventas.fxml"));
                        Parent reporteView = ThemeUtil.loadWithTheme(loader);
                        
                        // Establecer el usuario en el controlador
                        ReporteVentasController controller = loader.getController();
                        if (controller != null && usuarioActual != null) {
                            controller.setUsuarioActual(usuarioActual);
                        }
                        
                        // Buscar el BorderPane central (donde se cargan los módulos)
                        if (mainRoot.getCenter() instanceof BorderPane) {
                            BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                            // IMPORTANTE: Reemplazar todo el contenido, no solo el center
                            centerPane.setTop(null);     // Quitar filtros
                            centerPane.setBottom(null);  // Quitar cualquier footer
                            centerPane.setLeft(null);    // Quitar lateral
                            centerPane.setRight(null);   // Quitar lateral
                            centerPane.setCenter(reporteView); // Poner el reporte completo
                        } else {
                            // Fallback: reemplazar directamente en el centro
                            mainRoot.setCenter(reporteView);
                        }
                        
                        // Aplicar temas para asegurar consistencia
                        javafx.application.Platform.runLater(() -> {
                            ThemeUtil.applyThemeToAllOpenWindows();
                        });
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de ventas: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al cargar reporte", 
                        "Ha ocurrido un error al cargar el reporte de ventas: " + e.getMessage());
        }
    }
    
    private void abrirReporteClientes() {
        try {
            // Buscar la ventana principal y obtener la escena
            javafx.stage.Window ventanaPrincipal = btnRefresh.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar la vista del reporte de clientes
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-clientes.fxml"));
                        Parent reporteView = ThemeUtil.loadWithTheme(loader);
                        
                        // Establecer el usuario en el controlador
                        ReporteClientesController controller = loader.getController();
                        if (controller != null && usuarioActual != null) {
                            controller.setUsuarioActual(usuarioActual);
                        }
                        
                        // Buscar el BorderPane central (donde se cargan los módulos)
                        if (mainRoot.getCenter() instanceof BorderPane) {
                            BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                            // IMPORTANTE: Reemplazar todo el contenido, no solo el center
                            centerPane.setTop(null);     // Quitar filtros
                            centerPane.setBottom(null);  // Quitar cualquier footer
                            centerPane.setLeft(null);    // Quitar lateral
                            centerPane.setRight(null);   // Quitar lateral
                            centerPane.setCenter(reporteView); // Poner el reporte completo
                        } else {
                            // Fallback: reemplazar directamente en el centro
                            mainRoot.setCenter(reporteView);
                        }
                        
                        // Aplicar temas para asegurar consistencia
                        javafx.application.Platform.runLater(() -> {
                            ThemeUtil.applyThemeToAllOpenWindows();
                        });
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de clientes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al cargar reporte", 
                        "Ha ocurrido un error al cargar el reporte de clientes: " + e.getMessage());
        }
    }
    
    private void abrirReporteFichajes() {
        try {
            // Buscar la ventana principal y obtener la escena
            javafx.stage.Window ventanaPrincipal = btnRefresh.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar la vista del reporte de empleados/fichajes
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/reporte-empleados.fxml"));
                        Parent reporteView = ThemeUtil.loadWithTheme(loader);
                        
                        // Establecer el usuario en el controlador
                        ReporteEmpleadosController controller = loader.getController();
                        if (controller != null && usuarioActual != null) {
                            controller.setUsuarioActual(usuarioActual);
                        }
                        
                        // Buscar el BorderPane central (donde se cargan los módulos)
                        if (mainRoot.getCenter() instanceof BorderPane) {
                            BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                            // IMPORTANTE: Reemplazar todo el contenido, no solo el center
                            centerPane.setTop(null);     // Quitar filtros
                            centerPane.setBottom(null);  // Quitar cualquier footer
                            centerPane.setLeft(null);    // Quitar lateral
                            centerPane.setRight(null);   // Quitar lateral
                            centerPane.setCenter(reporteView); // Poner el reporte completo
                        } else {
                            // Fallback: reemplazar directamente en el centro
                            mainRoot.setCenter(reporteView);
                        }
                        
                        // Aplicar temas para asegurar consistencia
                        javafx.application.Platform.runLater(() -> {
                            ThemeUtil.applyThemeToAllOpenWindows();
                        });
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar reporte de empleados: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al cargar reporte", 
                        "Ha ocurrido un error al cargar el reporte de empleados: " + e.getMessage());
        }
    }
    
    private void abrirReporteCitas() {
        // Mostrar análisis comparativo anual
        mostrarAlert("Información", "Funcionalidad en desarrollo", "Esta función se implementará próximamente.");
        // mostrarAnalisisComparativo();
    }
    
    private void mostrarAnalisisComparativo() {
        // Temporalmente deshabilitado hasta simplificar la comunicación
        mostrarAlert("Información", "Funcionalidad en desarrollo", "Esta función se implementará próximamente.");
        
        /* Temporalmente comentado hasta implementar en servidor
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
            Protocolo.ComparativaAnual comparativa = obtenerComparativaAnual();
            
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
        */
    }
    
    private void abrirReporteFinanciero() {
        // Redirigir al reporte de ventas con análisis financiero extendido
        abrirReporteVentas();
    }
    
    private void mostrarAlert(String titulo, String header, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
    
    // Métodos para realizar peticiones al servidor
    
    private double calcularVentasPorAno(int ano) {
        try {
            String peticion = Protocolo.CALCULAR_VENTAS_POR_ANO + Protocolo.SEPARADOR_CODIGO + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CALCULAR_VENTAS_POR_ANO_RESPONSE) {
                return entrada.readDouble();
            } else {
                System.err.println("Error al calcular ventas por año");
                return 0.0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición calcularVentasPorAno: " + e.getMessage());
            return 0.0;
        }
    }
    
    private double calcularVentasPorMesAno(int mes, int ano) {
        try {
            String peticion = Protocolo.CALCULAR_VENTAS_POR_MES_ANO + Protocolo.SEPARADOR_CODIGO + 
                             mes + Protocolo.SEPARADOR_PARAMETROS + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CALCULAR_VENTAS_POR_MES_ANO_RESPONSE) {
                return entrada.readDouble();
            } else {
                System.err.println("Error al calcular ventas por mes y año");
                return 0.0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición calcularVentasPorMesAno: " + e.getMessage());
            return 0.0;
        }
    }
    
    private int contarPacientesPorAno(int ano) {
        try {
            String peticion = Protocolo.CONTAR_PACIENTES_POR_ANO + Protocolo.SEPARADOR_CODIGO + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CONTAR_PACIENTES_POR_ANO_RESPONSE) {
                return entrada.readInt();
            } else {
                System.err.println("Error al contar pacientes por año");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición contarPacientesPorAno: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarPacientesPorMesAno(int mes, int ano) {
        try {
            String peticion = Protocolo.CONTAR_PACIENTES_POR_MES_ANO + Protocolo.SEPARADOR_CODIGO + 
                             mes + Protocolo.SEPARADOR_PARAMETROS + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CONTAR_PACIENTES_POR_MES_ANO_RESPONSE) {
                return entrada.readInt();
            } else {
                System.err.println("Error al contar pacientes por mes y año");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición contarPacientesPorMesAno: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarCitasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            String peticion = Protocolo.CONTAR_CITAS_POR_FECHA + Protocolo.SEPARADOR_CODIGO + 
                             inicio.toString() + Protocolo.SEPARADOR_PARAMETROS + fin.toString();
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CONTAR_CITAS_POR_FECHA_RESPONSE) {
                return entrada.readInt();
            } else {
                System.err.println("Error al contar citas por fecha");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición contarCitasPorFecha: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarFichajesPorAno(int ano) {
        try {
            String peticion = Protocolo.CONTAR_FICHAJES_POR_ANO + Protocolo.SEPARADOR_CODIGO + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CONTAR_FICHAJES_POR_ANO_RESPONSE) {
                return entrada.readInt();
            } else {
                System.err.println("Error al contar fichajes por año");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición contarFichajesPorAno: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarFichajesPorMesAno(int mes, int ano) {
        try {
            String peticion = Protocolo.CONTAR_FICHAJES_POR_MES_ANO + Protocolo.SEPARADOR_CODIGO + 
                             mes + Protocolo.SEPARADOR_PARAMETROS + ano;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.CONTAR_FICHAJES_POR_MES_ANO_RESPONSE) {
                return entrada.readInt();
            } else {
                System.err.println("Error al contar fichajes por mes y año");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Error en petición contarFichajesPorMesAno: " + e.getMessage());
            return 0;
        }
    }
    
    // Métodos temporalmente comentados hasta simplificar la comunicación con el servidor
    /*
    @SuppressWarnings("unchecked")
    private List<Protocolo.DatoGrafico> obtenerEvolucionVentasConFiltro(String tipoFiltro, int ano, Integer mes) {
        try {
            String peticion = Protocolo.OBTENER_EVOLUCION_VENTAS_CON_FILTRO + Protocolo.SEPARADOR_CODIGO + 
                             tipoFiltro + Protocolo.SEPARADOR_PARAMETROS + ano;
            if (mes != null) {
                peticion += Protocolo.SEPARADOR_PARAMETROS + mes;
            }
            
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_EVOLUCION_VENTAS_CON_FILTRO_RESPONSE) {
                return (List<Protocolo.DatoGrafico>) entrada.readObject();
            } else {
                System.err.println("Error al obtener evolución de ventas");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerEvolucionVentasConFiltro: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Protocolo.DatoGrafico> obtenerUsuariosPorRol() {
        try {
            String peticion = Protocolo.OBTENER_USUARIOS_POR_ROL + Protocolo.SEPARADOR_CODIGO;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_USUARIOS_POR_ROL_RESPONSE) {
                return (List<Protocolo.DatoGrafico>) entrada.readObject();
            } else {
                System.err.println("Error al obtener usuarios por rol");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerUsuariosPorRol: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    */
} 