package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Clase para generar estadísticas, gráficos y KPIs del módulo de fichaje
 */
public class EstadisticasFichaje {
    
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Crea un panel con estadísticas completas que se puede integrar en la ventana principal
     */
    public static VBox crearPanelEstadisticasCompletas(List<ModeloFichaje> fichajes, String titulo) {
        // Crear panel principal
        VBox panelPrincipal = new VBox(10);
        panelPrincipal.setPadding(new Insets(10));
        panelPrincipal.getStyleClass().add("fichaje-main-panel");
        
        // Header con título y botón de volver
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("header-container");
        
        Button btnVolver = new Button("← Volver");
        btnVolver.getStyleClass().add("back-button");
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("module-title");
        
        header.getChildren().addAll(btnVolver, lblTitulo);
        
        // Crear TabPane para organizar las diferentes vistas
        TabPane tabPane = new TabPane();
        
        // Tab 1: KPIs y Resumen
        Tab tabKPIs = new Tab("KPIs y Resumen");
        tabKPIs.setClosable(false);
        tabKPIs.setContent(crearPanelKPIs(fichajes));
        
        // Tab 2: Gráficos de Barras
        Tab tabGraficos = new Tab("Gráficos de Barras");
        tabGraficos.setClosable(false);
        tabGraficos.setContent(crearPanelGraficosBarras(fichajes));
        
        // Tab 3: Gráficos de Líneas
        Tab tabLineas = new Tab("Tendencias");
        tabLineas.setClosable(false);
        tabLineas.setContent(crearPanelGraficosLineas(fichajes));
        
        // Tab 4: Gráficos Circulares
        Tab tabCirculares = new Tab("Distribuciones");
        tabCirculares.setClosable(false);
        tabCirculares.setContent(crearPanelGraficosCirculares(fichajes));
        
        tabPane.getTabs().addAll(tabKPIs, tabGraficos, tabLineas, tabCirculares);
        
        // Panel de filtros
        HBox panelFiltros = crearPanelFiltros(fichajes, tabPane);
        
        panelPrincipal.getChildren().addAll(header, panelFiltros, tabPane);
        
        return panelPrincipal;
    }
    
    /**
     * Método de compatibilidad que detecta el panel activo y setea el contenido
     */
    public static void mostrarEstadisticasCompletas(List<ModeloFichaje> fichajes, String tituloVentana) {
        // Crear el panel de estadísticas
        VBox panelEstadisticas = crearPanelEstadisticasCompletas(fichajes, tituloVentana);
        
        // Intentar encontrar el BorderPane principal de la aplicación
        try {
            // Buscar la ventana principal activa
            javafx.stage.Window ventanaPrincipal = javafx.stage.Stage.getWindows().stream()
                .filter(w -> w instanceof javafx.stage.Stage)
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElse(null);
            
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null && scene.getRoot() instanceof BorderPane) {
                    BorderPane root = (BorderPane) scene.getRoot();
                    
                    // Buscar el BorderPane central (donde se cargan los módulos)
                    if (root.getCenter() instanceof BorderPane) {
                        BorderPane centerPane = (BorderPane) root.getCenter();
                        
                        // Configurar el botón volver para restaurar la vista anterior
                        Button btnVolver = (Button) panelEstadisticas.lookup(".back-button");
                        if (btnVolver != null) {
                            // Guardar el contenido anterior
                            javafx.scene.Node contenidoAnterior = centerPane.getCenter();
                            
                            btnVolver.setOnAction(e -> {
                                // Restaurar el contenido anterior
                                centerPane.setCenter(contenidoAnterior);
                            });
                        }
                        
                        // Setear el panel de estadísticas en el centro
                        centerPane.setCenter(panelEstadisticas);
                        
                        // Aplicar estilos CSS
                        if (!scene.getStylesheets().contains("/Estilos/fichaje-styles.css")) {
                            scene.getStylesheets().add(EstadisticasFichaje.class.getResource("/Estilos/fichaje-styles.css").toExternalForm());
                        }
                        
                        return; // Éxito, salir del método
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al integrar estadísticas en ventana principal: " + e.getMessage());
        }
        
        // Fallback: crear ventana modal si no se puede integrar
        crearVentanaModalEstadisticas(fichajes, tituloVentana);
    }
    
    /**
     * Método fallback para crear ventana modal (comportamiento anterior)
     */
    private static void crearVentanaModalEstadisticas(List<ModeloFichaje> fichajes, String tituloVentana) {
        Stage ventanaEstadisticas = new Stage();
        ventanaEstadisticas.initModality(Modality.APPLICATION_MODAL);
        ventanaEstadisticas.setTitle(tituloVentana);
        ventanaEstadisticas.setWidth(1000);
        ventanaEstadisticas.setHeight(700);
        
        VBox panelEstadisticas = crearPanelEstadisticasCompletas(fichajes, tituloVentana);
        
        // Remover el botón volver en modo modal
        HBox header = (HBox) panelEstadisticas.getChildren().get(0);
        header.getChildren().remove(0); // Remover botón volver
        
        Scene scene = new Scene(panelEstadisticas);
        scene.getStylesheets().add(EstadisticasFichaje.class.getResource("/Estilos/fichaje-styles.css").toExternalForm());
        
        ventanaEstadisticas.setScene(scene);
        ventanaEstadisticas.showAndWait();
    }
    
    /**
     * Crea el panel de filtros para las estadísticas
     */
    private static HBox crearPanelFiltros(List<ModeloFichaje> fichajes, TabPane tabPane) {
        HBox panelFiltros = new HBox(10);
        panelFiltros.setAlignment(Pos.CENTER_LEFT);
        panelFiltros.setPadding(new Insets(10));
        panelFiltros.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5px;");
        
        Label lblFiltros = new Label("Filtros:");
        lblFiltros.setStyle("-fx-font-weight: bold;");
        
        DatePicker dpInicio = new DatePicker();
        dpInicio.setPromptText("Fecha inicio");
        
        DatePicker dpFin = new DatePicker();
        dpFin.setPromptText("Fecha fin");
        
        ComboBox<String> cmbEmpleado = new ComboBox<>();
        cmbEmpleado.setPromptText("Todos los empleados");
        cmbEmpleado.getItems().add("Todos los empleados");
        
        // Obtener lista única de empleados
        List<String> empleados = fichajes.stream()
            .map(ModeloFichaje::getNombreEmpleado)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        cmbEmpleado.getItems().addAll(empleados);
        cmbEmpleado.setValue("Todos los empleados");
        
        Button btnAplicarFiltros = new Button("Aplicar Filtros");
        btnAplicarFiltros.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Button btnLimpiarFiltros = new Button("Limpiar");
        btnLimpiarFiltros.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        
        // Eventos de filtros
        btnAplicarFiltros.setOnAction(e -> {
            List<ModeloFichaje> fichajesFiltrados = aplicarFiltros(fichajes, dpInicio.getValue(), 
                dpFin.getValue(), cmbEmpleado.getValue());
            actualizarGraficos(tabPane, fichajesFiltrados);
        });
        
        btnLimpiarFiltros.setOnAction(e -> {
            dpInicio.setValue(null);
            dpFin.setValue(null);
            cmbEmpleado.setValue("Todos los empleados");
            actualizarGraficos(tabPane, fichajes);
        });
        
        panelFiltros.getChildren().addAll(lblFiltros, dpInicio, dpFin, cmbEmpleado, 
            btnAplicarFiltros, btnLimpiarFiltros);
        
        return panelFiltros;
    }
    
    /**
     * Aplica filtros a la lista de fichajes
     */
    private static List<ModeloFichaje> aplicarFiltros(List<ModeloFichaje> fichajes, 
            LocalDate fechaInicio, LocalDate fechaFin, String empleado) {
        return fichajes.stream()
            .filter(f -> {
                boolean cumpleFecha = true;
                if (fechaInicio != null && f.getFechaHoraEntrada() != null) {
                    cumpleFecha = !f.getFechaHoraEntrada().toLocalDate().isBefore(fechaInicio);
                }
                if (fechaFin != null && f.getFechaHoraEntrada() != null && cumpleFecha) {
                    cumpleFecha = !f.getFechaHoraEntrada().toLocalDate().isAfter(fechaFin);
                }
                
                boolean cumpleEmpleado = empleado == null || "Todos los empleados".equals(empleado) 
                    || empleado.equals(f.getNombreEmpleado());
                
                return cumpleFecha && cumpleEmpleado;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Crea el panel de KPIs y resumen
     */
    private static ScrollPane crearPanelKPIs(List<ModeloFichaje> fichajes) {
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        
        // Título
        Label titulo = new Label("📊 INDICADORES CLAVE DE RENDIMIENTO (KPIs)");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        // Grid de KPIs
        GridPane gridKPIs = new GridPane();
        gridKPIs.setHgap(20);
        gridKPIs.setVgap(15);
        gridKPIs.setPadding(new Insets(10));
        
        // Calcular KPIs
        Map<String, Object> kpis = calcularKPIs(fichajes);
        
        // Crear tarjetas de KPIs
        int fila = 0, columna = 0;
        
        // Total de fichajes
        VBox cardTotal = crearTarjetaKPI("📋 Total Fichajes", 
            kpis.get("totalFichajes").toString(), "#4CAF50");
        gridKPIs.add(cardTotal, columna++, fila);
        
        // Empleados activos
        VBox cardEmpleados = crearTarjetaKPI("👥 Empleados Activos", 
            kpis.get("empleadosActivos").toString(), "#2196F3");
        gridKPIs.add(cardEmpleados, columna++, fila);
        
        // Promedio horas diarias
        VBox cardPromedio = crearTarjetaKPI("⏰ Promedio Horas/Día", 
            kpis.get("promedioHorasDiarias").toString(), "#FF9800");
        gridKPIs.add(cardPromedio, columna++, fila);
        
        // Nueva fila
        fila++; columna = 0;
        
        // Total incidencias
        VBox cardIncidencias = crearTarjetaKPI("⚠️ Total Incidencias", 
            kpis.get("totalIncidencias").toString(), "#f44336");
        gridKPIs.add(cardIncidencias, columna++, fila);
        
        // Puntualidad
        VBox cardPuntualidad = crearTarjetaKPI("✅ Puntualidad", 
            kpis.get("porcentajePuntualidad").toString() + "%", "#4CAF50");
        gridKPIs.add(cardPuntualidad, columna++, fila);
        
        // Horas totales trabajadas
        VBox cardHorasTotales = crearTarjetaKPI("🕐 Horas Totales", 
            kpis.get("horasTotales").toString(), "#9C27B0");
        gridKPIs.add(cardHorasTotales, columna++, fila);
        
        contenido.getChildren().addAll(titulo, gridKPIs);
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }
    
    /**
     * Crea una tarjeta de KPI
     */
    private static VBox crearTarjetaKPI(String titulo, String valor, String color) {
        VBox tarjeta = new VBox(5);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(15));
        tarjeta.setStyle(String.format(
            "-fx-background-color: white; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);",
            color
        ));
        tarjeta.setPrefWidth(180);
        tarjeta.setPrefHeight(100);
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        lblTitulo.setWrapText(true);
        lblTitulo.setAlignment(Pos.CENTER);
        
        Label lblValor = new Label(valor);
        lblValor.setStyle(String.format("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: %s;", color));
        
        tarjeta.getChildren().addAll(lblTitulo, lblValor);
        return tarjeta;
    }
    
    /**
     * Calcula los KPIs principales
     */
    private static Map<String, Object> calcularKPIs(List<ModeloFichaje> fichajes) {
        Map<String, Object> kpis = new HashMap<>();
        
        // Total de fichajes
        kpis.put("totalFichajes", fichajes.size());
        
        // Empleados únicos
        long empleadosActivos = fichajes.stream()
            .map(ModeloFichaje::getEmpleadoId)
            .distinct()
            .count();
        kpis.put("empleadosActivos", empleadosActivos);
        
        // Promedio de horas diarias (convertir de minutos a horas)
        double promedioHoras = fichajes.stream()
            .filter(f -> f.getMinutosTrabajoTotal() > 0)
            .mapToDouble(f -> f.getMinutosTrabajoTotal() / 60.0)
            .average()
            .orElse(0.0);
        kpis.put("promedioHorasDiarias", String.format("%.1f", promedioHoras));
        
        // Total de incidencias
        long totalIncidencias = fichajes.stream()
            .filter(f -> f.getEstado() == ModeloFichaje.EstadoFichaje.INCIDENCIA_AUTO ||
                        f.getEstado() == ModeloFichaje.EstadoFichaje.INCOMPLETO)
            .count();
        kpis.put("totalIncidencias", totalIncidencias);
        
        // Porcentaje de puntualidad (fichajes normales vs total)
        long fichajesNormales = fichajes.stream()
            .filter(f -> f.getEstado() == ModeloFichaje.EstadoFichaje.CERRADO)
            .count();
        double porcentajePuntualidad = fichajes.isEmpty() ? 0 : 
            (fichajesNormales * 100.0) / fichajes.size();
        kpis.put("porcentajePuntualidad", String.format("%.1f", porcentajePuntualidad));
        
        // Horas totales trabajadas (convertir de minutos a horas)
        double horasTotales = fichajes.stream()
            .mapToDouble(f -> f.getMinutosTrabajoTotal() / 60.0)
            .sum();
        kpis.put("horasTotales", String.format("%.1f", horasTotales));
        
        return kpis;
    }
    
    /**
     * Crea el panel de gráficos de barras
     */
    private static ScrollPane crearPanelGraficosBarras(List<ModeloFichaje> fichajes) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(20));
        
        // Gráfico de horas por empleado
        BarChart<String, Number> graficoHorasEmpleado = crearGraficoHorasPorEmpleado(fichajes);
        
        // Gráfico de fichajes por día de la semana
        BarChart<String, Number> graficoFichajesDia = crearGraficoFichajesPorDia(fichajes);
        
        contenido.getChildren().addAll(graficoHorasEmpleado, graficoFichajesDia);
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }
    
    /**
     * Crea gráfico de horas trabajadas por empleado
     */
    private static BarChart<String, Number> crearGraficoHorasPorEmpleado(List<ModeloFichaje> fichajes) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Empleados");
        yAxis.setLabel("Horas Trabajadas");
        
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("📊 Horas Trabajadas por Empleado");
        grafico.setPrefHeight(300);
        
        // Agrupar por empleado y sumar horas (convertir de minutos a horas)
        Map<String, Double> horasPorEmpleado = fichajes.stream()
            .collect(Collectors.groupingBy(
                ModeloFichaje::getNombreEmpleado,
                Collectors.summingDouble(f -> f.getMinutosTrabajoTotal() / 60.0)
            ));
        
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas Trabajadas");
        
        horasPorEmpleado.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        grafico.getData().add(serie);
        return grafico;
    }
    
    /**
     * Crea gráfico de fichajes por día de la semana
     */
    private static BarChart<String, Number> crearGraficoFichajesPorDia(List<ModeloFichaje> fichajes) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Día de la Semana");
        yAxis.setLabel("Número de Fichajes");
        
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("📅 Fichajes por Día de la Semana");
        grafico.setPrefHeight(300);
        
        // Contar fichajes por día de la semana
        Map<String, Long> fichajesPorDia = fichajes.stream()
            .filter(f -> f.getFechaHoraEntrada() != null)
            .collect(Collectors.groupingBy(
                f -> f.getFechaHoraEntrada().getDayOfWeek().toString(),
                Collectors.counting()
            ));
        
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Fichajes");
        
        // Orden de días de la semana
        String[] diasSemana = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        String[] diasEspanol = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        
        for (int i = 0; i < diasSemana.length; i++) {
            Long count = fichajesPorDia.getOrDefault(diasSemana[i], 0L);
            serie.getData().add(new XYChart.Data<>(diasEspanol[i], count));
        }
        
        grafico.getData().add(serie);
        return grafico;
    }
    
    /**
     * Crea el panel de gráficos de líneas (tendencias)
     */
    private static ScrollPane crearPanelGraficosLineas(List<ModeloFichaje> fichajes) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(20));
        
        // Gráfico de tendencia de horas por mes
        LineChart<String, Number> graficoTendencia = crearGraficoTendenciaHoras(fichajes);
        
        contenido.getChildren().add(graficoTendencia);
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }
    
    /**
     * Crea gráfico de tendencia de horas trabajadas por mes
     */
    private static LineChart<String, Number> crearGraficoTendenciaHoras(List<ModeloFichaje> fichajes) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Promedio Horas Diarias");
        
        LineChart<String, Number> grafico = new LineChart<>(xAxis, yAxis);
        grafico.setTitle("📈 Tendencia de Horas Trabajadas por Mes");
        grafico.setPrefHeight(400);
        
        // Agrupar por mes y calcular promedio (convertir de minutos a horas)
        Map<String, Double> promedioHorasPorMes = fichajes.stream()
            .filter(f -> f.getFechaHoraEntrada() != null && f.getMinutosTrabajoTotal() > 0)
            .collect(Collectors.groupingBy(
                f -> f.getFechaHoraEntrada().getYear() + "-" + 
                     String.format("%02d", f.getFechaHoraEntrada().getMonthValue()),
                Collectors.averagingDouble(f -> f.getMinutosTrabajoTotal() / 60.0)
            ));
        
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Promedio Horas");
        
        promedioHorasPorMes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        grafico.getData().add(serie);
        return grafico;
    }
    
    /**
     * Crea el panel de gráficos circulares
     */
    private static ScrollPane crearPanelGraficosCirculares(List<ModeloFichaje> fichajes) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(20));
        
        // Gráfico de distribución de estados
        PieChart graficoEstados = crearGraficoDistribucionEstados(fichajes);
        
        // Gráfico de distribución de tipos de entrada
        PieChart graficoTipos = crearGraficoDistribucionTipos(fichajes);
        
        contenido.getChildren().addAll(graficoEstados, graficoTipos);
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }
    
    /**
     * Crea gráfico circular de distribución de estados
     */
    private static PieChart crearGraficoDistribucionEstados(List<ModeloFichaje> fichajes) {
        PieChart grafico = new PieChart();
        grafico.setTitle("🎯 Distribución de Estados de Fichajes");
        grafico.setPrefHeight(400);
        
        Map<String, Long> estadosCounts = fichajes.stream()
            .filter(f -> f.getEstado() != null)
            .collect(Collectors.groupingBy(
                f -> f.getEstado().getDescripcion(),
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        estadosCounts.forEach((estado, count) -> {
            pieChartData.add(new PieChart.Data(estado + " (" + count + ")", count));
        });
        
        grafico.setData(pieChartData);
        return grafico;
    }
    
    /**
     * Crea gráfico circular de distribución de tipos de entrada
     */
    private static PieChart crearGraficoDistribucionTipos(List<ModeloFichaje> fichajes) {
        PieChart grafico = new PieChart();
        grafico.setTitle("🚪 Distribución de Tipos de Entrada");
        grafico.setPrefHeight(400);
        
        Map<String, Long> tiposCounts = fichajes.stream()
            .filter(f -> f.getTipoEntrada() != null)
            .collect(Collectors.groupingBy(
                f -> f.getTipoEntrada().getDescripcion(),
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        tiposCounts.forEach((tipo, count) -> {
            pieChartData.add(new PieChart.Data(tipo + " (" + count + ")", count));
        });
        
        grafico.setData(pieChartData);
        return grafico;
    }
    
    /**
     * Actualiza todos los gráficos con nuevos datos
     */
    private static void actualizarGraficos(TabPane tabPane, List<ModeloFichaje> fichajes) {
        try {
            // Actualizar cada tab con los nuevos datos
            tabPane.getTabs().get(0).setContent(crearPanelKPIs(fichajes));
            tabPane.getTabs().get(1).setContent(crearPanelGraficosBarras(fichajes));
            tabPane.getTabs().get(2).setContent(crearPanelGraficosLineas(fichajes));
            tabPane.getTabs().get(3).setContent(crearPanelGraficosCirculares(fichajes));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al actualizar gráficos");
            alert.setContentText("No se pudieron actualizar los gráficos: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 