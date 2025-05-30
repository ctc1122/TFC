package com.example.pruebamongodbcss.Modulos.Facturacion;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador para mostrar los detalles de una factura
 */
public class FacturaDetalleController implements Initializable {

    // Contenedor principal - corregido para coincidir con FXML
    @FXML private BorderPane mainPane;
    
    // Datos generales - corregidos para coincidir con FXML
    @FXML private Label lblTitulo;
    @FXML private Label lblNumeroFactura;
    @FXML private Label lblFechaEmision;
    @FXML private Label lblFechaVencimiento;
    @FXML private Label lblEstado;
    @FXML private Label lblMetodoPago;
    
    // Datos del cliente - corregidos para coincidir con FXML
    @FXML private Label lblNombreCliente;
    @FXML private Label lblDNICliente;
    @FXML private Label lblDireccionCliente;
    @FXML private Label lblTelefonoCliente;
    @FXML private Label lblEmailCliente;
    
    // Datos del paciente - corregidos para coincidir con FXML
    @FXML private Label lblNombrePaciente;
    @FXML private Label lblEspeciePaciente;
    @FXML private Label lblRazaPaciente;
    
    // Datos del veterinario - corregidos para coincidir con FXML
    @FXML private Label lblNombreVeterinario;
    @FXML private Label lblNumeroColegiado;
    
    // Servicios - corregidos para coincidir con FXML
    @FXML private TableView<ModeloFactura.ConceptoFactura> tablaServicios;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioDescripcion;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colServicioCantidad;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioPrecio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioIVA;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioSubtotal;
    
    // Medicamentos - corregidos para coincidir con FXML
    @FXML private TableView<ModeloFactura.ConceptoFactura> tablaMedicamentos;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoNombre;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colMedicamentoCantidad;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoPrecio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoIVA;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoSubtotal;
    
    // Totales - corregidos para coincidir con FXML
    @FXML private Label lblSubtotalServicios;
    @FXML private Label lblSubtotalMedicamentos;
    @FXML private Label lblIVAServicios;
    @FXML private Label lblIVAMedicamentos;
    @FXML private Label lblTotal;
    
    // Observaciones - correcto
    @FXML private TextArea txtObservaciones;
    
    // Botones - correctos
    @FXML private Button btnExportarPDF;
    @FXML private Button btnCerrar;
    @FXML private Button btnEditar;
    @FXML private Button btnImprimir;
    
    // Datos
    private ModeloFactura factura;
    private DecimalFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;
    private ObservableList<ModeloFactura.ConceptoFactura> listaServicios;
    private ObservableList<ModeloFactura.ConceptoFactura> listaMedicamentos;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            formatoMoneda = new DecimalFormat("#,##0.00 €");
            formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            
            // Inicializar listas
            listaServicios = FXCollections.observableArrayList();
            listaMedicamentos = FXCollections.observableArrayList();
            
            // Configurar tablas
            configurarTablas();
            
            // Configurar eventos
            configurarEventos();
            
            // Hacer el área de observaciones de solo lectura
            txtObservaciones.setEditable(false);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de inicialización", "No se pudo inicializar la vista de detalle: " + e.getMessage());
        }
    }
    
    private void configurarTablas() {
        configurarTablaServicios();
        configurarTablaMedicamentos();
    }
    
    private void configurarTablaServicios() {
        colServicioDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colServicioCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colServicioPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecioUnitario();
            return new SimpleStringProperty(formatoMoneda.format(precio));
        });
        colServicioIVA.setCellValueFactory(cellData -> {
            double iva = cellData.getValue().getTipoIva();
            return new SimpleStringProperty(String.format("%.1f%%", iva));
        });
        colServicioSubtotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        tablaServicios.setItems(listaServicios);
        tablaServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarTablaMedicamentos() {
        colMedicamentoNombre.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colMedicamentoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMedicamentoPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecioUnitario();
            return new SimpleStringProperty(formatoMoneda.format(precio));
        });
        colMedicamentoIVA.setCellValueFactory(cellData -> {
            double iva = cellData.getValue().getTipoIva();
            return new SimpleStringProperty(String.format("%.1f%%", iva));
        });
        colMedicamentoSubtotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        tablaMedicamentos.setItems(listaMedicamentos);
        tablaMedicamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarEventos() {
        btnExportarPDF.setOnAction(e -> exportarPDF());
        btnCerrar.setOnAction(e -> cerrarVentana());
    }
    
    /**
     * Establece la factura a mostrar
     */
    public void setFactura(ModeloFactura factura) {
        this.factura = factura;
        if (factura != null) {
            cargarDatosEnVista();
        }
    }
    
    private void cargarDatosEnVista() {
        if (factura == null) return;
        
        // Datos generales
        lblNumeroFactura.setText(factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR");
        lblFechaEmision.setText(factura.getFechaEmision() != null ? formatoFecha.format(factura.getFechaEmision()) : "");
        lblFechaVencimiento.setText(factura.getFechaVencimiento() != null ? formatoFecha.format(factura.getFechaVencimiento()) : "");
        lblEstado.setText(factura.getEstado() != null ? factura.getEstado().getDescripcion() : "");
        lblMetodoPago.setText(factura.getMetodoPago() != null ? factura.getMetodoPago() : "");
        
        // Aplicar estilo según el estado
        aplicarEstiloEstado();
        
        // Datos del cliente
        lblNombreCliente.setText(factura.getNombreCliente() != null ? factura.getNombreCliente() : "");
        lblDNICliente.setText(factura.getDniCliente() != null ? factura.getDniCliente() : "");
        lblDireccionCliente.setText(factura.getDireccionCliente() != null ? factura.getDireccionCliente() : "");
        lblTelefonoCliente.setText(factura.getTelefonoCliente() != null ? factura.getTelefonoCliente() : "");
        lblEmailCliente.setText(factura.getEmailCliente() != null ? factura.getEmailCliente() : "");
        
        // Datos del paciente
        lblNombrePaciente.setText(factura.getNombrePaciente() != null ? factura.getNombrePaciente() : "");
        lblEspeciePaciente.setText(factura.getEspeciePaciente() != null ? factura.getEspeciePaciente() : "");
        lblRazaPaciente.setText(factura.getRazaPaciente() != null ? factura.getRazaPaciente() : "");
        
        // Veterinario
        lblNombreVeterinario.setText(factura.getVeterinarioNombre() != null ? factura.getVeterinarioNombre() : "");
        lblNumeroColegiado.setText(""); // Se puede agregar este campo a ModeloFactura si es necesario
        
        // Servicios y medicamentos
        listaServicios.clear();
        if (factura.getServicios() != null) {
            listaServicios.addAll(factura.getServicios());
        }
        
        listaMedicamentos.clear();
        if (factura.getMedicamentos() != null) {
            listaMedicamentos.addAll(factura.getMedicamentos());
        }
        
        // Totales - calculando desde los datos disponibles
        double subtotalServicios = factura.getServicios() != null ? 
            factura.getServicios().stream().mapToDouble(s -> s.getTotal()).sum() : 0.0;
        double subtotalMedicamentos = factura.getMedicamentos() != null ? 
            factura.getMedicamentos().stream().mapToDouble(m -> m.getTotal()).sum() : 0.0;
            
        lblSubtotalServicios.setText(formatoMoneda.format(subtotalServicios));
        lblSubtotalMedicamentos.setText(formatoMoneda.format(subtotalMedicamentos));
        lblIVAServicios.setText(formatoMoneda.format(factura.getIvaGeneral()));
        lblIVAMedicamentos.setText(formatoMoneda.format(factura.getIvaMedicamentos()));
        lblTotal.setText(formatoMoneda.format(factura.getTotal()));
        
        // Observaciones
        txtObservaciones.setText(factura.getObservaciones() != null ? factura.getObservaciones() : "");
        
        // Ocultar secciones vacías
        ocultarSeccionesVacias();
    }
    
    private void aplicarEstiloEstado() {
        // Limpiar estilos previos
        lblEstado.getStyleClass().removeAll("estado-borrador", "estado-emitida", "estado-pagada", "estado-vencida", "estado-anulada");
        
        if (factura.getEstado() != null) {
            switch (factura.getEstado()) {
                case BORRADOR:
                    lblEstado.getStyleClass().add("estado-borrador");
                    break;
                case EMITIDA:
                    lblEstado.getStyleClass().add("estado-emitida");
                    break;
                case PAGADA:
                    lblEstado.getStyleClass().add("estado-pagada");
                    break;
                case VENCIDA:
                    lblEstado.getStyleClass().add("estado-vencida");
                    break;
                case ANULADA:
                    lblEstado.getStyleClass().add("estado-anulada");
                    break;
            }
        }
    }
    
    private void ocultarSeccionesVacias() {
        // Ocultar tabla de servicios si está vacía
        if (listaServicios.isEmpty()) {
            tablaServicios.setVisible(false);
            tablaServicios.setManaged(false);
        } else {
            tablaServicios.setVisible(true);
            tablaServicios.setManaged(true);
        }
        
        // Ocultar tabla de medicamentos si está vacía
        if (listaMedicamentos.isEmpty()) {
            tablaMedicamentos.setVisible(false);
            tablaMedicamentos.setManaged(false);
        } else {
            tablaMedicamentos.setVisible(true);
            tablaMedicamentos.setManaged(true);
        }
        
        // Ocultar observaciones si están vacías
        if (txtObservaciones.getText().trim().isEmpty()) {
            txtObservaciones.setVisible(false);
            txtObservaciones.setManaged(false);
        } else {
            txtObservaciones.setVisible(true);
            txtObservaciones.setManaged(true);
        }
    }
    
    private void exportarPDF() {
        if (factura == null) {
            mostrarError("Error", "No hay factura para exportar");
            return;
        }
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Factura PDF");
            fileChooser.setInitialFileName("Factura_" + 
                (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR") + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );
            
            Stage stage = (Stage) mainPane.getScene().getWindow();
            File archivo = fileChooser.showSaveDialog(stage);
            
            if (archivo != null) {
                // Usar el generador PDF existente
                GeneradorPDFFactura generador = new GeneradorPDFFactura();
                generador.generarPDF(factura, archivo.getAbsolutePath());
                
                mostrarInfo("Éxito", "Factura exportada correctamente a PDF");
                
                // Preguntar si desea abrir el archivo
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exportación Completada");
                alert.setHeaderText("Factura exportada correctamente");
                alert.setContentText("¿Desea abrir el archivo generado?");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        Desktop.getDesktop().open(archivo);
                    } catch (Exception ex) {
                        mostrarError("Error", "No se pudo abrir el archivo: " + ex.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "Error al exportar PDF: " + e.getMessage());
        }
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    // Métodos FXML para los botones del archivo FXML
    
    /**
     * Método llamado desde el FXML para cerrar la ventana
     */
    @FXML
    private void onCerrar() {
        cerrarVentana();
    }
    
    /**
     * Método llamado desde el FXML para editar la factura
     */
    @FXML
    private void onEditar() {
        // Por ahora mostrar mensaje informativo
        // Se puede implementar lógica para abrir el editor de facturas
        mostrarInfo("Funcionalidad en desarrollo", 
            "La función de editar desde el detalle estará disponible próximamente.\n" +
            "Puede editar la factura desde el listado principal.");
    }
    
    /**
     * Método llamado desde el FXML para exportar PDF
     */
    @FXML
    private void onExportarPDF() {
        exportarPDF();
    }
    
    /**
     * Método llamado desde el FXML para imprimir
     */
    @FXML
    private void onImprimir() {
        // Por ahora mostrar mensaje informativo
        // Se puede implementar lógica de impresión directa
        mostrarInfo("Funcionalidad en desarrollo", 
            "La función de impresión directa estará disponible próximamente.\n" +
            "Puede exportar a PDF e imprimir desde su visor de PDF.");
    }
} 