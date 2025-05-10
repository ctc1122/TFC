package com.example.pruebamongodbcss.Modulos.Empresa;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;

/**
 * Script para cargar datos de prueba en la base de datos.
 * Ejecutar esta clase para inicializar los datos de veterinarios y usuarios.
 */
public class CargarDatosPrueba {
    
    public static void main(String[] args) {
        System.out.println("Iniciando carga de datos de prueba...");
        
        try {
            // Crear instancia del servicio
            ServicioUsuarios servicio = new ServicioUsuarios();
            
            // Cargar datos
            servicio.cargarDatosPrueba();
            
            // Mostrar resumen
            System.out.println("Datos cargados correctamente.");
            System.out.println("Usuarios disponibles:");
            System.out.println("- admin / admin (Rol: Administrador)");
            
            // Mostrar usuarios veterinarios
            for (Usuario vet : servicio.buscarUsuariosPorRol(Usuario.Rol.VETERINARIO)) {
                System.out.println("- " + vet.getUsuario() + " / " + vet.getPassword() + " (Rol: Veterinario - " + vet.getNombreCompleto() + ")");
            }
            
            // Mostrar veterinarios desde ModeloVeterinario
            System.out.println("\nModelos de veterinarios:");
            for (ModeloVeterinario vet : servicio.obtenerTodosVeterinarios()) {
                System.out.println("- " + vet.getNombreCompleto() + " - " + vet.getEspecialidad());
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 