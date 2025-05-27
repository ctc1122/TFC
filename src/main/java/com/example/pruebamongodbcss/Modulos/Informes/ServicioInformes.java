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
    
    /**
     * Obtiene estadísticas de usuarios por rol
     */
    public List<DatoGrafico> obtenerUsuariosPorRol() {
        List<DatoGrafico> datos = new ArrayList<>();
        
        try {
            MongoCollection<Document> usuariosCollection = GestorConexion.conectarEmpresa().getCollection("usuarios");
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$rol", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
            );
            
            MongoCursor<Document> cursor = usuariosCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String rol = doc.getString("_id");
                int count = doc.getInteger("count", 0);
                datos.add(new DatoGrafico(rol != null ? rol : "Sin rol", count));
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios por rol: " + e.getMessage());
        }
        
        return datos;
    }
    
    /**
     * Obtiene comparativa anual de ventas
     */
    public ComparativaAnual obtenerComparativaAnual() {
        ComparativaAnual comparativa = new ComparativaAnual();
        LocalDate hoy = LocalDate.now();
        int anoActual = hoy.getYear();
        int anoAnterior = anoActual - 1;
        
        // Ventas por mes del año actual
        List<DatoGrafico> ventasAnoActual = obtenerVentasPorMeses(anoActual);
        List<DatoGrafico> ventasAnoAnterior = obtenerVentasPorMeses(anoAnterior);
        
        comparativa.setVentasAnoActual(ventasAnoActual);
        comparativa.setVentasAnoAnterior(ventasAnoAnterior);
        
        // Calcular totales
        double totalActual = ventasAnoActual.stream().mapToDouble(DatoGrafico::getValor).sum();
        double totalAnterior = ventasAnoAnterior.stream().mapToDouble(DatoGrafico::getValor).sum();
        
        comparativa.setTotalAnoActual(totalActual);
        comparativa.setTotalAnoAnterior(totalAnterior);
        comparativa.setPorcentajeCrecimiento(calcularPorcentajeCambio(totalActual, totalAnterior));
        
        return comparativa;
    }
    
    /**
     * Obtiene estadísticas detalladas de empleados
     */
    public EstadisticasEmpleados obtenerEstadisticasEmpleados() {
        EstadisticasEmpleados stats = new EstadisticasEmpleados();
        
        try {
            MongoCollection<Document> usuariosCollection = GestorConexion.conectarEmpresa().getCollection("usuarios");
            
            // Total de empleados
            long totalEmpleados = usuariosCollection.countDocuments();
            stats.setTotalEmpleados((int) totalEmpleados);
            
            // Empleados por rol
            List<DatoGrafico> empleadosPorRol = obtenerUsuariosPorRol();
            stats.setEmpleadosPorRol(empleadosPorRol);
            
            // Empleados activos (que han fichado en los últimos 30 días)
            LocalDate hace30Dias = LocalDate.now().minusDays(30);
            Date fechaLimite = Date.from(hace30Dias.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long empleadosActivos = fichajesCollection.distinct("empleadoId", 
                Filters.gte("fechaHoraEntrada", fechaLimite), org.bson.types.ObjectId.class).into(new ArrayList<>()).size();
            stats.setEmpleadosActivos((int) empleadosActivos);
            
            // Promedio de horas trabajadas por empleado este mes
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            stats.setPromedioHorasMes(calcularPromedioHorasPorEmpleado(inicioMes, LocalDate.now()));
            
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas de empleados: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Obtiene análisis de productividad por empleado
     */
    public List<ProductividadEmpleado> obtenerProductividadEmpleados(int limite) {
        List<ProductividadEmpleado> productividad = new ArrayList<>();
        
        try {
            MongoCollection<Document> usuariosCollection = GestorConexion.conectarEmpresa().getCollection("usuarios");
            
            // Obtener empleados con rol VETERINARIO
            MongoCursor<Document> empleados = usuariosCollection.find(
                Filters.eq("rol", "VETERINARIO")).iterator();
            
            while (empleados.hasNext()) {
                Document empleado = empleados.next();
                String nombreEmpleado = empleado.getString("nombre");
                String idEmpleado = empleado.getObjectId("_id").toString();
                
                ProductividadEmpleado prod = new ProductividadEmpleado();
                prod.setNombreEmpleado(nombreEmpleado);
                
                // Contar citas atendidas este mes
                LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
                Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaFin = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                long citasAtendidas = citasCollection.countDocuments(Filters.and(
                    Filters.eq("veterinario", nombreEmpleado),
                    Filters.gte("fechaHora", fechaInicio),
                    Filters.lt("fechaHora", fechaFin),
                    Filters.eq("estado", "COMPLETADA")
                ));
                
                prod.setCitasAtendidas((int) citasAtendidas);
                
                // Calcular horas trabajadas este mes
                double horasTrabajadas = calcularHorasEmpleadoMes(idEmpleado, inicioMes, LocalDate.now());
                prod.setHorasTrabajadas(horasTrabajadas);
                
                // Calcular eficiencia (citas por hora)
                if (horasTrabajadas > 0) {
                    prod.setEficiencia(citasAtendidas / horasTrabajadas);
                }
                
                productividad.add(prod);
            }
            empleados.close();
            
            // Ordenar por eficiencia descendente
            productividad.sort((a, b) -> Double.compare(b.getEficiencia(), a.getEficiencia()));
            
            // Limitar resultados
            if (productividad.size() > limite) {
                productividad = productividad.subList(0, limite);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener productividad de empleados: " + e.getMessage());
        }
        
        return productividad;
    }
    
    /**
     * Obtiene análisis de clientes
     */
    public AnalisisClientes obtenerAnalisisClientes() {
        AnalisisClientes analisis = new AnalisisClientes();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioAno = hoy.withDayOfYear(1);
        
        try {
            // Total de clientes
            long totalClientes = propietariosCollection.countDocuments();
            analisis.setTotalClientes((int) totalClientes);
            
            // Clientes nuevos este mes
            Date fechaInicioMes = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(hoy.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long clientesNuevosMes = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicioMes),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            analisis.setClientesNuevosMes((int) clientesNuevosMes);
            
            // Clientes nuevos este año
            Date fechaInicioAno = Date.from(inicioAno.atStartOfDay(ZoneId.systemDefault()).toInstant());
            long clientesNuevosAno = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicioAno),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            analisis.setClientesNuevosAno((int) clientesNuevosAno);
            
            // Clientes activos (con citas en los últimos 3 meses)
            LocalDate hace3Meses = hoy.minusMonths(3);
            Date fecha3Meses = Date.from(hace3Meses.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<String> clientesActivos = citasCollection.distinct("propietario", 
                Filters.gte("fechaHora", fecha3Meses), String.class).into(new ArrayList<>());
            analisis.setClientesActivos(clientesActivos.size());
            
            // Promedio de mascotas por cliente
            long totalMascotas = pacientesCollection.countDocuments();
            if (totalClientes > 0) {
                analisis.setPromedioMascotasPorCliente((double) totalMascotas / totalClientes);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener análisis de clientes: " + e.getMessage());
        }
        
        return analisis;
    }
    
    /**
     * Obtiene top de clientes por facturación
     */
    public List<ClienteTop> obtenerTopClientes(int limite) {
        List<ClienteTop> topClientes = new ArrayList<>();
        
        try {
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("estado", "FINALIZADA")),
                Aggregates.group("$clienteId", 
                    Accumulators.sum("totalFacturado", "$total"),
                    Accumulators.sum("numeroFacturas", 1),
                    Accumulators.first("clienteNombre", "$clienteNombre")),
                Aggregates.sort(Sorts.descending("totalFacturado")),
                Aggregates.limit(limite)
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ClienteTop cliente = new ClienteTop();
                cliente.setNombre(doc.getString("clienteNombre"));
                cliente.setTotalFacturado(doc.getDouble("totalFacturado"));
                cliente.setNumeroFacturas(doc.getInteger("numeroFacturas", 0));
                
                if (cliente.getNumeroFacturas() > 0) {
                    cliente.setPromedioFactura(cliente.getTotalFacturado() / cliente.getNumeroFacturas());
                }
                
                topClientes.add(cliente);
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener top clientes: " + e.getMessage());
        }
        
        return topClientes;
    }
    
    // Métodos auxiliares privados adicionales
    
    private List<DatoGrafico> obtenerVentasPorMeses(int ano) {
        List<DatoGrafico> datos = new ArrayList<>();
        
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate inicioMes = LocalDate.of(ano, mes, 1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            double ventas = calcularVentasPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM"));
            
            datos.add(new DatoGrafico(etiqueta, ventas));
        }
        
        return datos;
    }
    
    private double calcularHorasEmpleadoMes(String empleadoId, LocalDate inicio, LocalDate fin) {
        // Implementación simplificada - en un caso real calcularías las horas exactas
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long fichajes = fichajesCollection.countDocuments(Filters.and(
                Filters.eq("empleadoId", new org.bson.types.ObjectId(empleadoId)),
                Filters.gte("fechaHoraEntrada", fechaInicio),
                Filters.lt("fechaHoraEntrada", fechaFin)
            ));
            
            return fichajes * 8.0; // Estimación de 8 horas por día
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    // Clases internas adicionales para los nuevos reportes
    
    public static class ComparativaAnual {
        private List<DatoGrafico> ventasAnoActual;
        private List<DatoGrafico> ventasAnoAnterior;
        private double totalAnoActual;
        private double totalAnoAnterior;
        private double porcentajeCrecimiento;
        
        // Getters y setters
        public List<DatoGrafico> getVentasAnoActual() { return ventasAnoActual; }
        public void setVentasAnoActual(List<DatoGrafico> ventasAnoActual) { this.ventasAnoActual = ventasAnoActual; }
        
        public List<DatoGrafico> getVentasAnoAnterior() { return ventasAnoAnterior; }
        public void setVentasAnoAnterior(List<DatoGrafico> ventasAnoAnterior) { this.ventasAnoAnterior = ventasAnoAnterior; }
        
        public double getTotalAnoActual() { return totalAnoActual; }
        public void setTotalAnoActual(double totalAnoActual) { this.totalAnoActual = totalAnoActual; }
        
        public double getTotalAnoAnterior() { return totalAnoAnterior; }
        public void setTotalAnoAnterior(double totalAnoAnterior) { this.totalAnoAnterior = totalAnoAnterior; }
        
        public double getPorcentajeCrecimiento() { return porcentajeCrecimiento; }
        public void setPorcentajeCrecimiento(double porcentajeCrecimiento) { this.porcentajeCrecimiento = porcentajeCrecimiento; }
    }
    
    public static class EstadisticasEmpleados {
        private int totalEmpleados;
        private int empleadosActivos;
        private List<DatoGrafico> empleadosPorRol;
        private double promedioHorasMes;
        
        // Getters y setters
        public int getTotalEmpleados() { return totalEmpleados; }
        public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }
        
        public int getEmpleadosActivos() { return empleadosActivos; }
        public void setEmpleadosActivos(int empleadosActivos) { this.empleadosActivos = empleadosActivos; }
        
        public List<DatoGrafico> getEmpleadosPorRol() { return empleadosPorRol; }
        public void setEmpleadosPorRol(List<DatoGrafico> empleadosPorRol) { this.empleadosPorRol = empleadosPorRol; }
        
        public double getPromedioHorasMes() { return promedioHorasMes; }
        public void setPromedioHorasMes(double promedioHorasMes) { this.promedioHorasMes = promedioHorasMes; }
    }
    
    public static class ProductividadEmpleado {
        private String nombreEmpleado;
        private int citasAtendidas;
        private double horasTrabajadas;
        private double eficiencia;
        
        // Getters y setters
        public String getNombreEmpleado() { return nombreEmpleado; }
        public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
        
        public int getCitasAtendidas() { return citasAtendidas; }
        public void setCitasAtendidas(int citasAtendidas) { this.citasAtendidas = citasAtendidas; }
        
        public double getHorasTrabajadas() { return horasTrabajadas; }
        public void setHorasTrabajadas(double horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }
        
        public double getEficiencia() { return eficiencia; }
        public void setEficiencia(double eficiencia) { this.eficiencia = eficiencia; }
    }
    
    public static class AnalisisClientes {
        private int totalClientes;
        private int clientesNuevosMes;
        private int clientesNuevosAno;
        private int clientesActivos;
        private double promedioMascotasPorCliente;
        
        // Getters y setters
        public int getTotalClientes() { return totalClientes; }
        public void setTotalClientes(int totalClientes) { this.totalClientes = totalClientes; }
        
        public int getClientesNuevosMes() { return clientesNuevosMes; }
        public void setClientesNuevosMes(int clientesNuevosMes) { this.clientesNuevosMes = clientesNuevosMes; }
        
        public int getClientesNuevosAno() { return clientesNuevosAno; }
        public void setClientesNuevosAno(int clientesNuevosAno) { this.clientesNuevosAno = clientesNuevosAno; }
        
        public int getClientesActivos() { return clientesActivos; }
        public void setClientesActivos(int clientesActivos) { this.clientesActivos = clientesActivos; }
        
        public double getPromedioMascotasPorCliente() { return promedioMascotasPorCliente; }
        public void setPromedioMascotasPorCliente(double promedioMascotasPorCliente) { this.promedioMascotasPorCliente = promedioMascotasPorCliente; }
    }
    
    public static class ClienteTop {
        private String nombre;
        private double totalFacturado;
        private int numeroFacturas;
        private double promedioFactura;
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public double getTotalFacturado() { return totalFacturado; }
        public void setTotalFacturado(double totalFacturado) { this.totalFacturado = totalFacturado; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioFactura() { return promedioFactura; }
        public void setPromedioFactura(double promedioFactura) { this.promedioFactura = promedioFactura; }
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