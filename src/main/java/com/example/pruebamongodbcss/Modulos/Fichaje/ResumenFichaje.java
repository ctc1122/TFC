package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ResumenFichaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String empleado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long totalMinutosTrabajados;
    private int diasTrabajados;
    private int incidencias;
    private int fichjesIncompletos;
    private double promedioHorasDiarias;
    private List<ModeloFichaje> fichajes;
    
    public ResumenFichaje() {}
    
    public ResumenFichaje(String empleado, LocalDate fechaInicio, LocalDate fechaFin) {
        this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
    
    public void calcularEstadisticas(List<ModeloFichaje> fichajes) {
        this.fichajes = fichajes;
        this.totalMinutosTrabajados = 0;
        this.diasTrabajados = 0;
        this.incidencias = 0;
        this.fichjesIncompletos = 0;
        
        for (ModeloFichaje fichaje : fichajes) {
            if (fichaje.getEstado() == ModeloFichaje.EstadoFichaje.CERRADO && !fichaje.isEsIncidencia()) {
                this.totalMinutosTrabajados += fichaje.getMinutosTrabajoTotal();
                this.diasTrabajados++;
            }
            
            if (fichaje.isEsIncidencia()) {
                this.incidencias++;
            }
            
            if (fichaje.getEstado() == ModeloFichaje.EstadoFichaje.ABIERTO || 
                fichaje.getEstado() == ModeloFichaje.EstadoFichaje.INCOMPLETO) {
                this.fichjesIncompletos++;
            }
        }
        
        this.promedioHorasDiarias = diasTrabajados > 0 ? 
            (double) totalMinutosTrabajados / diasTrabajados / 60.0 : 0.0;
    }
    
    public String getTotalHorasFormateadas() {
        if (totalMinutosTrabajados <= 0) return "0h 0m";
        long horas = totalMinutosTrabajados / 60;
        long minutos = totalMinutosTrabajados % 60;
        return String.format("%dh %dm", horas, minutos);
    }
    
    public String getPromedioHorasFormateadas() {
        if (promedioHorasDiarias <= 0) return "0h 0m";
        long horas = (long) promedioHorasDiarias;
        long minutos = (long) ((promedioHorasDiarias - horas) * 60);
        return String.format("%dh %dm", horas, minutos);
    }
    
    public String getPeriodoFormateado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fechaInicio.format(formatter) + " - " + fechaFin.format(formatter);
    }
    
    // Getters y Setters
    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public long getTotalMinutosTrabajados() { return totalMinutosTrabajados; }
    public void setTotalMinutosTrabajados(long totalMinutosTrabajados) { this.totalMinutosTrabajados = totalMinutosTrabajados; }
    
    public int getDiasTrabajados() { return diasTrabajados; }
    public void setDiasTrabajados(int diasTrabajados) { this.diasTrabajados = diasTrabajados; }
    
    public int getIncidencias() { return incidencias; }
    public void setIncidencias(int incidencias) { this.incidencias = incidencias; }
    
    public int getFichjesIncompletos() { return fichjesIncompletos; }
    public void setFichjesIncompletos(int fichjesIncompletos) { this.fichjesIncompletos = fichjesIncompletos; }
    
    public double getPromedioHorasDiarias() { return promedioHorasDiarias; }
    public void setPromedioHorasDiarias(double promedioHorasDiarias) { this.promedioHorasDiarias = promedioHorasDiarias; }
    
    public List<ModeloFichaje> getFichajes() { return fichajes; }
    public void setFichajes(List<ModeloFichaje> fichajes) { this.fichajes = fichajes; }
} 