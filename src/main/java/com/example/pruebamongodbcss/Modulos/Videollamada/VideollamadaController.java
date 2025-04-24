package com.example.pruebamongodbcss.Modulos.Videollamada;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.util.UUID;

public class VideollamadaController {
    @FXML
    private ListView<Usuario> usuariosListView;
    @FXML
    private TextField buscarTextField;
    @FXML
    private TextField nombreTextField;
    @FXML
    private TextField salaTextField;
    @FXML
    private Label estadoConexionLabel;
    @FXML
    private Label usuarioSeleccionadoLabel;
    @FXML
    private Label estadoUsuarioLabel;
    @FXML
    private Button llamarButton;
    @FXML
    private Button colgarButton;
    @FXML
    private Button crearSalaButton;
    @FXML
    private Button unirseButton;
    @FXML
    private Button copiarButton;
    @FXML
    private VBox llamadaEnCursoBox;
    @FXML
    private VBox infoSalaBox;
    @FXML
    private TextArea infoSalaText;
    @FXML
    private Label tiempoLlamadaLabel;
    @FXML
    private Label estadoLabel;
    
    private JitsiMeetView jitsiView;
    private ObservableList<Usuario> usuarios;
    private FilteredList<Usuario> usuariosFiltrados;
    private Usuario usuarioActual;
    private Usuario usuarioSeleccionado;

    @FXML
    public void initialize() {
        // Inicializar lista de usuarios
        usuarios = FXCollections.observableArrayList();
        usuarios.addAll(
            new Usuario("1", "Dr. Juan Pérez"),
            new Usuario("2", "Dra. María García"),
            new Usuario("3", "Dr. Carlos Rodríguez"),
            new Usuario("4", "Dra. Ana Martínez")
        );
        
        // Configurar filtrado de usuarios
        usuariosFiltrados = new FilteredList<>(usuarios, p -> true);
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            usuariosFiltrados.setPredicate(usuario -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return usuario.getNombre().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        
        // Configurar lista de usuarios
        usuariosListView.setItems(usuariosFiltrados);
        usuariosListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> seleccionarUsuario(newValue));
        
        // Configurar botones
        llamarButton.setOnAction(event -> iniciarLlamada());
        colgarButton.setOnAction(event -> colgarLlamada());
        crearSalaButton.setOnAction(event -> crearNuevaSala());
        unirseButton.setOnAction(event -> unirseASala());
        copiarButton.setOnAction(event -> copiarInformacion());
        
        // Simular usuario actual
        usuarioActual = new Usuario("0", "Dr. Usuario Actual");
        estadoConexionLabel.setText("Conectado como: " + usuarioActual.getNombre());
        
        // Configurar campo de nombre con el usuario actual
        nombreTextField.setText(usuarioActual.getNombre());
    }

    private void seleccionarUsuario(Usuario usuario) {
        usuarioSeleccionado = usuario;
        if (usuario != null) {
            usuarioSeleccionadoLabel.setText(usuario.getNombre());
            estadoUsuarioLabel.setText("Estado: " + usuario.getEstado());
            llamarButton.setDisable(false);
        } else {
            usuarioSeleccionadoLabel.setText("Seleccione un usuario para llamar");
            estadoUsuarioLabel.setText("Estado: -");
            llamarButton.setDisable(true);
        }
    }

    private void iniciarLlamada() {
        try {
            if (usuarioSeleccionado == null) return;

            String roomName = "MedConnect" + UUID.randomUUID().toString()
                    .replaceAll("-", "")
                    .substring(0, 6)
                    .toUpperCase();

            // Actualizar estados
            usuarioActual.setEstado("En llamada");
            usuarioSeleccionado.setEstado("En llamada");
            usuarioActual.setSalaActual(roomName);
            usuarioSeleccionado.setSalaActual(roomName);

            // Iniciar videollamada
            jitsiView = new JitsiMeetView(roomName, usuarioActual.getNombre());

            // Actualizar UI
            llamarButton.setVisible(false);
            colgarButton.setVisible(true);
            llamadaEnCursoBox.setVisible(true);
            infoSalaBox.setVisible(false);
            estadoLabel.setText("Llamando a " + usuarioSeleccionado.getNombre() + "...");

        } catch (Exception e) {
            estadoLabel.setText("Error al iniciar la llamada: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void crearNuevaSala() {
        try {
            String displayName = obtenerNombreUsuario();
            if (displayName == null) return;

            String roomName = "MedConnect" + UUID.randomUUID().toString()
                    .replaceAll("-", "")
                    .substring(0, 6)
                    .toUpperCase();

            // Crear la vista de Jitsi
            jitsiView = new JitsiMeetView(roomName, displayName);

            // Mostrar información de la sala
            mostrarInformacionSala(roomName);
            
            // Actualizar UI
            llamarButton.setVisible(false);
            colgarButton.setVisible(true);
            llamadaEnCursoBox.setVisible(true);
            estadoLabel.setText("Sala creada exitosamente");
            
        } catch (Exception e) {
            estadoLabel.setText("Error al crear la sala: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void unirseASala() {
        try {
            String displayName = obtenerNombreUsuario();
            if (displayName == null) return;

            String roomName = salaTextField.getText().trim();
            if (roomName.isEmpty()) {
                estadoLabel.setText("Por favor, ingrese el ID de la sala");
                return;
            }

            // Unirse a la sala
            jitsiView = new JitsiMeetView(roomName, displayName);
            
            // Actualizar UI
            llamarButton.setVisible(false);
            colgarButton.setVisible(true);
            llamadaEnCursoBox.setVisible(true);
            infoSalaBox.setVisible(false);
            estadoLabel.setText("Conectando a la sala...");
            
        } catch (Exception e) {
            estadoLabel.setText("Error al unirse a la sala: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String obtenerNombreUsuario() {
        String displayName = nombreTextField.getText().trim();
        if (displayName.isEmpty()) {
            estadoLabel.setText("Por favor, ingrese su nombre");
            return null;
        }
        return displayName;
    }

    private void mostrarInformacionSala(String roomName) {
        String info = String.format("""
            ID de la sala: %s
            
            Comparta este ID con la persona a la que desea llamar. 
            Ellos deberán ingresar este ID en el campo "ID de sala" y hacer clic en "Unirse".""",
            roomName);
        
        infoSalaText.setText(info);
        infoSalaBox.setVisible(true);
    }

    private void copiarInformacion() {
        if (infoSalaText.getText().isEmpty()) return;
        
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(infoSalaText.getText());
        clipboard.setContent(content);
        
        estadoLabel.setText("Información copiada al portapapeles");
    }

    private void colgarLlamada() {
        if (jitsiView != null) {
            jitsiView.dispose();
        }

        // Restaurar estados
        usuarioActual.setEstado("Disponible");
        if (usuarioSeleccionado != null) {
            usuarioSeleccionado.setEstado("Disponible");
        }
        usuarioActual.setSalaActual(null);
        if (usuarioSeleccionado != null) {
            usuarioSeleccionado.setSalaActual(null);
        }

        // Actualizar UI
        llamarButton.setVisible(true);
        colgarButton.setVisible(false);
        llamadaEnCursoBox.setVisible(false);
        infoSalaBox.setVisible(false);
        estadoLabel.setText("Llamada finalizada");
    }
} 