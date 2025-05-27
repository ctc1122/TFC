package com.example.pruebamongodbcss.Modulos.Informes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import Utilidades.GestorConexion;

public class ServicioInformes {
    
    private MongoCollection<Document> facturasCollection;
    private MongoCollection<Document> propietariosCollection;
    private MongoCollection<Document> fichajesCollection;
    private MongoCollection<Document> citasCollection;
    private MongoCollection<Document> pacientesCollection;
    
    public ServicioInformes() {
        this.facturasCollection = GestorConexion.conectarEmpresa().getCollection("facturas");
        this.propietariosCollection = GestorConexion.conectarEmpresa().getCollection("propietarios");
        this.fichajesCollection = GestorConexion.conectarEmpresa().getCollection("fichajes");
        this.citasCollection = GestorConexion.conectarEmpresa().getCollection("citas");
        this.pacientesCollection = GestorConexion.conectarEmpresa().getCollection("pacientes");
    }
    
    /**
     * Obtiene las métricas principales del dashboard
     */
    public DashboardMetricas obtenerMetricasDashboard() {
        DashboardMetricas metricas = new DashboardMetricas();
        
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);
        LocalDate inicioAno = hoy.withDayOfYear(1);
        LocalDate inicioAnoAnterior = inicioAno.minusYears(1);
        
        // Ventas del día
        metricas.setVentasHoy(calcularVentasPorFecha(hoy, hoy));
        
        // Ventas del mes actual vs mes anterior
        double ventasMesActual = calcularVentasPorFecha(inicioMes, hoy);
        double ventasMesAnterior = calcularVentasPorFecha(inicioMesAnterior, finMesAnterior);
        metricas.setVentasMesActual(ventasMesActual);
        metricas.setVentasMesAnterior(ventasMesAnterior);
        metricas.setPorcentajeCambioVentas(calcularPorcentajeCambio(ventasMesActual, ventasMesAnterior));
        
        // Número de facturas del mes
        metricas.setNumeroFacturasMes(contarFacturasPorFecha(inicioMes, hoy));
        metricas.setNumeroFacturasMesAnterior(contarFacturasPorFecha(inicioMesAnterior, finMesAnterior));
        
        // Clientes del año vs año anterior
        int clientesAnoActual = contarClientesPorAno(inicioAno, hoy);
        int clientesAnoAnterior = contarClientesPorAno(inicioAnoAnterior, inicioAno.minusDays(1));
        metricas.setClientesAnoActual(clientesAnoActual);
        metricas.setClientesAnoAnterior(clientesAnoAnterior);
        metricas.setPorcentajeCambioClientes(calcularPorcentajeCambio(clientesAnoActual, clientesAnoAnterior));
        
        // Citas del día
        metricas.setCitasHoy(contarCitasPorFecha(hoy, hoy));
        
        // Promedio de ventas por día del mes
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS.between(inicioMes, hoy) + 1;
        metricas.setPromedioVentasDiarias(ventasMesActual / diasTranscurridos);
        
        return metricas;
    }
    
    /**
     * Obtiene datos para el gráfico de ventas mensuales
     */
    public List<DatoGrafico> obtenerVentasMensuales(int meses) {
        List<DatoGrafico> datos = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = fechaActual.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            double ventas = calcularVentasPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            
            datos.add(new DatoGrafico(etiqueta, ventas));
        }
        
        return datos;
    }
    
    /**
     * Obtiene datos para el gráfico de clientes por mes
     */
    public List<DatoGrafico> obtenerClientesPorMes(int meses) {
        List<DatoGrafico> datos = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = fechaActual.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            int clientes = contarClientesPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            
            datos.add(new DatoGrafico(etiqueta, clientes));
        }
        
        return datos;
    }
    
    /**
     * Obtiene estadísticas de fichajes
     */
    public EstadisticasFichajes obtenerEstadisticasFichajes() {
        EstadisticasFichajes stats = new EstadisticasFichajes();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        
        // Empleados fichados hoy
        stats.setEmpleadosFichadosHoy(contarEmpleadosFichadosHoy());
        
        // Total de horas trabajadas este mes
        stats.setHorasTrabajadasMes(calcularHorasTrabajadasMes(inicioMes, hoy));
        
        // Promedio de horas por empleado
        stats.setPromedioHorasPorEmpleado(calcularPromedioHorasPorEmpleado(inicioMes, hoy));
        
        // Días con más fichajes
        stats.setDiaConMasFichajes(obtenerDiaConMasFichajes(inicioMes, hoy));
        
        return stats;
    }
    
    /**
     * Obtiene el top de servicios más vendidos
     */
    public List<ServicioVendido> obtenerTopServicios(int limite) {
        List<ServicioVendido> servicios = new ArrayList<>();
        
        try {
            // Agregación para contar servicios por tipo
            List<Bson> pipeline = Arrays.asList(
                Aggregates.unwind("$items"),
                Aggregates.group("$items.descripcion", 
                    Accumulators.sum("cantidad", "$items.cantidad"),
                    Accumulators.sum("total", new Document("$multiply", 
                        Arrays.asList("$items.cantidad", "$items.precio")))),
                Aggregates.sort(Sorts.descending("cantidad")),
                Aggregates.limit(limite)
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ServicioVendido servicio = new ServicioVendido();
                servicio.setNombre(doc.getString("_id"));
                servicio.setCantidad(doc.getInteger("cantidad", 0));
                servicio.setTotal(doc.getDouble("total"));
                servicios.add(servicio);
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener top servicios: " + e.getMessage());
        }
        
        return servicios;
    }
    
    /**
     * Obtiene datos para gráfico de citas por estado
     */
    public List<DatoGrafico> obtenerCitasPorEstado() {
        List<DatoGrafico> datos = new ArrayList<>();
        
        try {
            List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$estado", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
            );
            
            MongoCursor<Document> cursor = citasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String estado = doc.getString("_id");
                int count = doc.getInteger("count", 0);
                datos.add(new DatoGrafico(estado != null ? estado : "Sin estado", count));
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener citas por estado: " + e.getMessage());
        }
        
        return datos;
    }
    
    // Métodos auxiliares privados
    
    private double calcularVentasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaCreacion", fechaInicio),
                    Filters.lt("fechaCreacion", fechaFin),
                    Filters.eq("estado", "FINALIZADA")
                )),
                Aggregates.group(null, Accumulators.sum("total", "$total"))
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                return result.getDouble("total");
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al calcular ventas: " + e.getMessage());
        }
        return 0.0;
    }
    
    private int contarFacturasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = facturasCollection.countDocuments(Filters.and(
                Filters.gte("fechaCreacion", fechaInicio),
                Filters.lt("fechaCreacion", fechaFin),
                Filters.eq("estado", "FINALIZADA")
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar facturas: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarClientesPorAno(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicio),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar clientes: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarClientesPorFecha(LocalDate inicio, LocalDate fin) {
        return contarClientesPorAno(inicio, fin);
    }
    
    private int contarCitasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = citasCollection.countDocuments(Filters.and(
                Filters.gte("fechaHora", fechaInicio),
                Filters.lt("fechaHora", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar citas: " + e.getMessage());
            return 0;
        }
    }
    
    private double calcularPorcentajeCambio(double actual, double anterior) {
        if (anterior == 0) return actual > 0 ? 100.0 : 0.0;
        return ((actual - anterior) / anterior) * 100.0;
    }
    
    private int contarEmpleadosFichadosHoy() {
        try {
            LocalDate hoy = LocalDate.now();
            Date fechaInicio = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(hoy.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaHoraEntrada", fechaInicio),
                    Filters.lt("fechaHoraEntrada", fechaFin),
                    Filters.exists("fechaHoraSalida", false)
                )),
                Aggregates.group("$empleadoId"),
                Aggregates.count()
            );
            
            MongoCursor<Document> cursor = fichajesCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                return result.getInteger("count", 0);
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al contar empleados fichados: " + e.getMessage());
        }
        return 0;
    }
    
    private double calcularHorasTrabajadasMes(LocalDate inicio, LocalDate fin) {
        // Implementación simplificada - en un caso real calcularías las horas exactas
        return contarFacturasPorFecha(inicio, fin) * 8.0; // Estimación
    }
    
    private double calcularPromedioHorasPorEmpleado(LocalDate inicio, LocalDate fin) {
        double totalHoras = calcularHorasTrabajadasMes(inicio, fin);
        int empleados = Math.max(1, contarEmpleadosFichadosHoy());
        return totalHoras / empleados;
    }
    
    private String obtenerDiaConMasFichajes(LocalDate inicio, LocalDate fin) {
        // Implementación simplificada
        return "Lunes";
    }
    
    // Clases internas para los datos
    
    public static class DashboardMetricas {
        private double ventasHoy;
        private double ventasMesActual;
        private double ventasMesAnterior;
        private double porcentajeCambioVentas;
        private int numeroFacturasMes;
        private int numeroFacturasMesAnterior;
        private int clientesAnoActual;
        private int clientesAnoAnterior;
        private double porcentajeCambioClientes;
        private int citasHoy;
        private double promedioVentasDiarias;
        
        // Getters y setters
        public double getVentasHoy() { return ventasHoy; }
        public void setVentasHoy(double ventasHoy) { this.ventasHoy = ventasHoy; }
        
        public double getVentasMesActual() { return ventasMesActual; }
        public void setVentasMesActual(double ventasMesActual) { this.ventasMesActual = ventasMesActual; }
        
        public double getVentasMesAnterior() { return ventasMesAnterior; }
        public void setVentasMesAnterior(double ventasMesAnterior) { this.ventasMesAnterior = ventasMesAnterior; }
        
        public double getPorcentajeCambioVentas() { return porcentajeCambioVentas; }
        public void setPorcentajeCambioVentas(double porcentajeCambioVentas) { this.porcentajeCambioVentas = porcentajeCambioVentas; }
        
        public int getNumeroFacturasMes() { return numeroFacturasMes; }
        public void setNumeroFacturasMes(int numeroFacturasMes) { this.numeroFacturasMes = numeroFacturasMes; }
        
        public int getNumeroFacturasMesAnterior() { return numeroFacturasMesAnterior; }
        public void setNumeroFacturasMesAnterior(int numeroFacturasMesAnterior) { this.numeroFacturasMesAnterior = numeroFacturasMesAnterior; }
        
        public int getClientesAnoActual() { return clientesAnoActual; }
        public void setClientesAnoActual(int clientesAnoActual) { this.clientesAnoActual = clientesAnoActual; }
        
        public int getClientesAnoAnterior() { return clientesAnoAnterior; }
        public void setClientesAnoAnterior(int clientesAnoAnterior) { this.clientesAnoAnterior = clientesAnoAnterior; }
        
        public double getPorcentajeCambioClientes() { return porcentajeCambioClientes; }
        public void setPorcentajeCambioClientes(double porcentajeCambioClientes) { this.porcentajeCambioClientes = porcentajeCambioClientes; }
        
        public int getCitasHoy() { return citasHoy; }
        public void setCitasHoy(int citasHoy) { this.citasHoy = citasHoy; }
        
        public double getPromedioVentasDiarias() { return promedioVentasDiarias; }
        public void setPromedioVentasDiarias(double promedioVentasDiarias) { this.promedioVentasDiarias = promedioVentasDiarias; }
    }
    
    public static class DatoGrafico {
        private String etiqueta;
        private double valor;
        
        public DatoGrafico(String etiqueta, double valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
        
        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
        
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
    
    public static class EstadisticasFichajes {
        private int empleadosFichadosHoy;
        private double horasTrabajadasMes;
        private double promedioHorasPorEmpleado;
        private String diaConMasFichajes;
        
        // Getters y setters
        public int getEmpleadosFichadosHoy() { return empleadosFichadosHoy; }
        public void setEmpleadosFichadosHoy(int empleadosFichadosHoy) { this.empleadosFichadosHoy = empleadosFichadosHoy; }
        
        public double getHorasTrabajadasMes() { return horasTrabajadasMes; }
        public void setHorasTrabajadasMes(double horasTrabajadasMes) { this.horasTrabajadasMes = horasTrabajadasMes; }
        
        public double getPromedioHorasPorEmpleado() { return promedioHorasPorEmpleado; }
        public void setPromedioHorasPorEmpleado(double promedioHorasPorEmpleado) { this.promedioHorasPorEmpleado = promedioHorasPorEmpleado; }
        
        public String getDiaConMasFichajes() { return diaConMasFichajes; }
        public void setDiaConMasFichajes(String diaConMasFichajes) { this.diaConMasFichajes = diaConMasFichajes; }
    }
    
    public static class ServicioVendido {
        private String nombre;
        private int cantidad;
        private double total;
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
} 