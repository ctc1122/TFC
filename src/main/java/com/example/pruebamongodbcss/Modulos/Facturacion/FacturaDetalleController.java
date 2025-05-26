package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para mostrar los detalles de una factura
 */
public class FacturaDetalleController implements Initializable {

    // Datos generales
    @FXML private VBox rootPane;
    @FXML private Label lblNumeroFactura;
    @FXML private Label lblFechaEmision;
    @FXML private Label lblFechaVencimiento;
    @FXML private Label lblEstado;
    @FXML private Label lblMetodoPago;
    
    // Datos del cliente
    @FXML private Label lblNombreCliente;
    @FXML private Label lblDniCliente;
    @FXML private Label lblDireccionCliente;
    @FXML private Label lblTelefonoCliente;
    @FXML private Label lblEmailCliente;
    
    // Datos del paciente
    @FXML private Label lblNombrePaciente;
    @FXML private Label lblEspeciePaciente;
    @FXML private Label lblRazaPaciente;
    
    // Datos del veterinario
    @FXML private Label lblVeterinario;
    
    // Servicios
    @FXML private TableView<ModeloFactura.ConceptoFactura> tableServicios;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescripcionServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colCantidadServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colPrecioServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescuentoServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colIvaServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colTotalServicio;
    
    // Medicamentos
    @FXML private TableView<ModeloFactura.ConceptoFactura> tableMedicamentos;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescripcionMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colCantidadMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colPrecioMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescuentoMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colIvaMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colTotalMedicamento;
    
    // Totales
    @FXML private Label lblSubtotal;
    @FXML private Label lblIvaGeneral;
    @FXML private Label lblIvaMedicamentos;
    @FXML private Label lblTotal;
    
    // Observaciones
    @FXML private TextArea txtObservaciones;
    
    // Botones
    @FXML private JFXButton btnExportarPDF;
    @FXML private JFXButton btnCerrar;
    
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
        colDescripcionServicio.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidadServicio.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioServicio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecioUnitario();
            return new SimpleStringProperty(formatoMoneda.format(precio));
        });
        colDescuentoServicio.setCellValueFactory(cellData -> {
            double descuento = cellData.getValue().getDescuento();
            return new SimpleStringProperty(String.format("%.1f%%", descuento));
        });
        colIvaServicio.setCellValueFactory(cellData -> {
            double iva = cellData.getValue().getTipoIva();
            return new SimpleStringProperty(String.format("%.1f%%", iva));
        });
        colTotalServicio.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        tableServicios.setItems(listaServicios);
        tableServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarTablaMedicamentos() {
        colDescripcionMedicamento.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidadMedicamento.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioMedicamento.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecioUnitario();
            return new SimpleStringProperty(formatoMoneda.format(precio));
        });
        colDescuentoMedicamento.setCellValueFactory(cellData -> {
            double descuento = cellData.getValue().getDescuento();
            return new SimpleStringProperty(String.format("%.1f%%", descuento));
        });
        colIvaMedicamento.setCellValueFactory(cellData -> {
            double iva = cellData.getValue().getTipoIva();
            return new SimpleStringProperty(String.format("%.1f%%", iva));
        });
        colTotalMedicamento.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        tableMedicamentos.setItems(listaMedicamentos);
        tableMedicamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        lblDniCliente.setText(factura.getDniCliente() != null ? factura.getDniCliente() : "");
        lblDireccionCliente.setText(factura.getDireccionCliente() != null ? factura.getDireccionCliente() : "");
        lblTelefonoCliente.setText(factura.getTelefonoCliente() != null ? factura.getTelefonoCliente() : "");
        lblEmailCliente.setText(factura.getEmailCliente() != null ? factura.getEmailCliente() : "");
        
        // Datos del paciente
        lblNombrePaciente.setText(factura.getNombrePaciente() != null ? factura.getNombrePaciente() : "");
        lblEspeciePaciente.setText(factura.getEspeciePaciente() != null ? factura.getEspeciePaciente() : "");
        lblRazaPaciente.setText(factura.getRazaPaciente() != null ? factura.getRazaPaciente() : "");
        
        // Veterinario
        lblVeterinario.setText(factura.getVeterinarioNombre() != null ? factura.getVeterinarioNombre() : "");
        
        // Servicios y medicamentos
        listaServicios.clear();
        if (factura.getServicios() != null) {
            listaServicios.addAll(factura.getServicios());
        }
        
        listaMedicamentos.clear();
        if (factura.getMedicamentos() != null) {
            listaMedicamentos.addAll(factura.getMedicamentos());
        }
        
        // Totales
        lblSubtotal.setText(formatoMoneda.format(factura.getSubtotal()));
        lblIvaGeneral.setText(formatoMoneda.format(factura.getIvaGeneral()));
        lblIvaMedicamentos.setText(formatoMoneda.format(factura.getIvaMedicamentos()));
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
            tableServicios.setVisible(false);
            tableServicios.setManaged(false);
        } else {
            tableServicios.setVisible(true);
            tableServicios.setManaged(true);
        }
        
        // Ocultar tabla de medicamentos si está vacía
        if (listaMedicamentos.isEmpty()) {
            tableMedicamentos.setVisible(false);
            tableMedicamentos.setManaged(false);
        } else {
            tableMedicamentos.setVisible(true);
            tableMedicamentos.setManaged(true);
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
            
            Stage stage = (Stage) rootPane.getScene().getWindow();
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
        Stage stage = (Stage) rootPane.getScene().getWindow();
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
} 