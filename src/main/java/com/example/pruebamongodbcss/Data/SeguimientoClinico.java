package com.example.pruebamongodbcss.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que representa el seguimiento clínico de un paciente
 */
public class SeguimientoClinico {
    private String id;
    private Paciente paciente;
    private List<RegistroMedico> historial;
    
    // Constructor
    public SeguimientoClinico(Paciente paciente) {
        this.id = UUID.randomUUID().toString();
        this.paciente = paciente;
        this.historial = new ArrayList<>();
    }
    
    // Constructor para cargar desde base de datos
    public SeguimientoClinico(String id, Paciente paciente, List<RegistroMedico> historial) {
        this.id = id;
        this.paciente = paciente;
        this.historial = historial;
    }
    
    /**
     * Agrega un nuevo registro al historial médico
     */
    public void agregarRegistro(Cita cita, String diagnostico, String tratamiento, List<String> medicamentos) {
        RegistroMedico registro = new RegistroMedico(cita, diagnostico, tratamiento, medicamentos);
        historial.add(registro);
    }
    
    /**
     * Obtiene todos los registros médicos del paciente
     */
    public List<RegistroMedico> getHistorial() {
        return new ArrayList<>(historial);
    }
    
    /**
     * Obtiene el último registro médico del paciente
     */
    public RegistroMedico getUltimoRegistro() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.get(historial.size() - 1);
    }
    
    /**
     * Obtiene el paciente asociado al seguimiento
     */
    public Paciente getPaciente() {
        return paciente;
    }
    
    /**
     * Obtiene el ID del seguimiento clínico
     */
    public String getId() {
        return id;
    }
    
    /**
     * Clase interna que representa un registro médico individual
     */
    public static class RegistroMedico {
        private String id;
        private Cita cita;
        private LocalDateTime fecha;
        private String diagnostico;
        private String tratamiento;
        private List<String> medicamentos;
        
        // Constructor
        public RegistroMedico(Cita cita, String diagnostico, String tratamiento, List<String> medicamentos) {
            this.id = UUID.randomUUID().toString();
            this.cita = cita;
            this.fecha = LocalDateTime.now();
            this.diagnostico = diagnostico;
            this.tratamiento = tratamiento;
            this.medicamentos = new ArrayList<>(medicamentos);
        }
        
        // Constructor para cargar desde base de datos
        public RegistroMedico(String id, Cita cita, LocalDateTime fecha, String diagnostico, 
                             String tratamiento, List<String> medicamentos) {
            this.id = id;
            this.cita = cita;
            this.fecha = fecha;
            this.diagnostico = diagnostico;
            this.tratamiento = tratamiento;
            this.medicamentos = medicamentos;
        }
        
        // Getters
        public String getId() {
            return id;
        }
        
        public Cita getCita() {
            return cita;
        }
        
        public LocalDateTime getFecha() {
            return fecha;
        }
        
        public String getFechaFormateada() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fecha.format(formatter);
        }
        
        public String getDiagnostico() {
            return diagnostico;
        }
        
        public String getTratamiento() {
            return tratamiento;
        }
        
        public List<String> getMedicamentos() {
            return new ArrayList<>(medicamentos);
        }
        
        // Setters para permitir actualizar información
        public void setDiagnostico(String diagnostico) {
            this.diagnostico = diagnostico;
        }
        
        public void setTratamiento(String tratamiento) {
            this.tratamiento = tratamiento;
        }
        
        public void setMedicamentos(List<String> medicamentos) {
            this.medicamentos = new ArrayList<>(medicamentos);
        }
        
        public void agregarMedicamento(String medicamento) {
            this.medicamentos.add(medicamento);
        }
        
        @Override
        public String toString() {
            return "RegistroMedico{" +
                    "fecha=" + getFechaFormateada() +
                    ", diagnostico='" + diagnostico + '\'' +
                    ", tratamiento='" + tratamiento + '\'' +
                    ", medicamentos=" + medicamentos +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "SeguimientoClinico{" +
                "id='" + id + '\'' +
                ", paciente=" + paciente +
                ", número de registros=" + historial.size() +
                '}';
    }
} 