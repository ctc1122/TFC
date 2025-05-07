package com.example.pruebamongodbcss.Modulos.Empresa;

/**
 * Script para cargar datos de prueba en la base de datos.
 * Ejecutar esta clase para inicializar los datos de veterinarios y usuarios.
 */
public class CargarDatosPrueba {
    
    public static void main(String[] args) {
        System.out.println("Iniciando carga de datos de prueba...");
        
        try {
            // Crear instancia del servicio
            ServicioEmpresa servicio = new ServicioEmpresa();
            
            // Cargar datos
            servicio.cargarDatosPrueba();
            
            // Mostrar resumen
            System.out.println("Datos cargados correctamente.");
            System.out.println("Usuarios disponibles:");
            System.out.println("- admin / admin (Rol: Administrador)");
            
            // Mostrar usuarios de veterinarios
            for (ModeloVeterinario vet : servicio.obtenerTodosVeterinarios()) {
                String nombreUsuario = (vet.getNombre().toLowerCase().charAt(0) + 
                                        vet.getApellidos().split(" ")[0].toLowerCase());
                System.out.println("- " + nombreUsuario + " / password (Rol: Veterinario - " + vet.getNombreCompleto() + ")");
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 