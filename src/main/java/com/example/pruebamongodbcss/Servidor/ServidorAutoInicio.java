package com.example.pruebamongodbcss.Servidor;

/**
 * Clase para iniciar el servidor automáticamente sin interfaz gráfica
 * Esta clase se usa para los scripts de automatización
 */
public class ServidorAutoInicio {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    SERVIDOR CLÍNICA - INICIO AUTOMÁTICO");
        System.out.println("===========================================");
        System.out.println();
        
        try {
            // Crear una instancia del servidor
            Servidor servidor = new Servidor();
            
            // Agregar un shutdown hook para cerrar el servidor limpiamente
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Cerrando servidor...");
                servidor.detener();
                System.out.println("✅ Servidor cerrado correctamente");
            }));
            
            System.out.println("🚀 Iniciando servidor en puerto 50002...");
            System.out.println("📋 Para detener el servidor, presiona Ctrl+C o cierra esta ventana");
            System.out.println();
            
            // Iniciar el servidor (esto bloquea el hilo hasta que se cierre)
            servidor.iniciar();
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 