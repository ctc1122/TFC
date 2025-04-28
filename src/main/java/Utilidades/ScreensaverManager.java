package Utilidades;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.pruebamongodbcss.Modulos.Carrusel.FondosPantalla;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScreensaverManager {
    private static final int INACTIVITY_TIMEOUT = 600000; // 3 seconds for testing
    private Timer inactivityTimer;
    private Stage screensaverStage;
    private Stage activeStage; // Referencia a la ventana activa
    private Point lastMousePosition;
    private long lastActivityTime;
    private AtomicBoolean isScreensaverActive;
    private FondosPantalla fondosPantalla;
    private AtomicBoolean isRunning;
    private Robot robot;
    private Point lastMouseCheckPosition;
    private long lastMouseCheckTime;

    public ScreensaverManager(Stage mainStage) {
        System.out.println("ScreensaverManager creado");
        this.activeStage = mainStage;
        isScreensaverActive = new AtomicBoolean(false);
        isRunning = new AtomicBoolean(true);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Error al crear Robot: " + e.getMessage());
        }
        lastMouseCheckPosition = MouseInfo.getPointerInfo().getLocation();
        lastMouseCheckTime = System.currentTimeMillis();
    }

    // Método para actualizar la ventana activa
    public void updateActiveStage(Stage newStage) {
        System.out.println("Actualizando ventana activa...");
        this.activeStage = newStage;
    }

    private void initializeScreensaver() {
        if (screensaverStage == null) {
            System.out.println("Inicializando salvapantallas...");
            Platform.runLater(() -> {
                try {
                    screensaverStage = new Stage();
                    screensaverStage.initStyle(StageStyle.UNDECORATED);
                    screensaverStage.setFullScreen(true);
                    
                    // Inicializar el carrusel de fondos de pantalla solo cuando sea necesario
                    fondosPantalla = new FondosPantalla();
                    fondosPantalla.start(screensaverStage);
                    
                    // Añadir listener de teclado a la escena
                    Scene scene = screensaverStage.getScene();
                    if (scene != null) {
                        scene.setOnKeyPressed(this::handleKeyPress);
                    }
                    
                    System.out.println("Salvapantallas inicializado correctamente");
                } catch (Exception e) {
                    System.err.println("Error al inicializar el salvapantallas: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private void handleKeyPress(KeyEvent event) {
        System.out.println("Tecla presionada: " + event.getCode());
        // Ignorar la tecla ESC
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            return;
        }
        if (isScreensaverActive.get()) {
            hideScreensaver();
        }
    }

    public void startInactivityMonitoring() {
        System.out.println("Iniciando monitoreo de inactividad...");
        lastActivityTime = System.currentTimeMillis();
        lastMousePosition = MouseInfo.getPointerInfo().getLocation();
        
        // Crear un nuevo Timer para asegurar que no hay timers anteriores
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
            inactivityTimer.purge();
        }
        inactivityTimer = new Timer("ScreensaverTimer", true);
        
        inactivityTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning.get()) {
                    this.cancel();
                    return;
                }
                checkInactivity();
            }
        }, 1000, 1000); // Check every second
    }

    private void checkInactivity() {
        if (!isRunning.get()) return;

        Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();
        long currentTime = System.currentTimeMillis();
        long tiempoInactivo = currentTime - lastActivityTime;
        
        System.out.println("Tiempo inactivo: " + tiempoInactivo + "ms");
        
        // Verificar si ha habido movimiento del ratón
        boolean mouseMoved = !currentMousePosition.equals(lastMousePosition);
        
        // Actualizar la posición del ratón solo si ha pasado suficiente tiempo
        // para evitar falsos positivos por pequeños movimientos
        if (currentTime - lastMouseCheckTime > 100) { // 100ms entre comprobaciones
            if (!currentMousePosition.equals(lastMouseCheckPosition)) {
                mouseMoved = true;
                lastMouseCheckPosition = currentMousePosition;
                lastMouseCheckTime = currentTime;
            }
        }
        
        lastMousePosition = currentMousePosition;
        
        if (mouseMoved) {
            lastActivityTime = currentTime;
            if (isScreensaverActive.get()) {
                System.out.println("Movimiento detectado, ocultando salvapantallas");
                hideScreensaver();
            }
        } else if (tiempoInactivo >= INACTIVITY_TIMEOUT && !isScreensaverActive.get()) {
            System.out.println("Inactividad detectada, mostrando salvapantallas");
            showScreensaver();
        }
    }

    private void showScreensaver() {
        System.out.println("Intentando mostrar salvapantallas...");
        Platform.runLater(() -> {
            try {
                if (screensaverStage == null) {
                    initializeScreensaver();
                }
                isScreensaverActive.set(true);
                
                // Ocultar la ventana activa
                if (activeStage != null) {
                    activeStage.hide();
                }
                
                screensaverStage.show();
                screensaverStage.toFront();
                System.out.println("Salvapantallas mostrado");
            } catch (Exception e) {
                System.err.println("Error al mostrar salvapantallas: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void hideScreensaver() {
        System.out.println("Ocultando salvapantallas...");
        Platform.runLater(() -> {
            try {
                isScreensaverActive.set(false);
                if (screensaverStage != null) {
                    screensaverStage.hide();
                }
                
                // Mostrar la ventana activa
                if (activeStage != null) {
                    activeStage.show();
                    activeStage.toFront();
                }
                
                lastActivityTime = System.currentTimeMillis();
                System.out.println("Salvapantallas ocultado");
            } catch (Exception e) {
                System.err.println("Error al ocultar salvapantallas: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        System.out.println("Deteniendo salvapantallas...");
        isRunning.set(false);
        
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
            inactivityTimer.purge();
            inactivityTimer = null;
        }
        
        Platform.runLater(() -> {
            if (screensaverStage != null) {
                screensaverStage.close();
                screensaverStage = null;
            }
            if (activeStage != null) {
                activeStage.show();
            }
        });
    }

    public void resetInactivityTimer() {
        System.out.println("Reiniciando temporizador de inactividad");
        lastActivityTime = System.currentTimeMillis();
        if (isScreensaverActive.get()) {
            hideScreensaver();
        }
    }
} 