package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo que representa un diagnóstico médico de la base de datos UMLS.
 * Corresponde a registros de la tabla Diagnosticos en MariaDB.
 */
public class ModeloDiagnosticoUMLS {
    private final StringProperty cui;
    private final StringProperty lat; 
    private final StringProperty ts;
    private final StringProperty lui;
    private final StringProperty stt;
    private final StringProperty sui;
    private final StringProperty ispref;
    private final StringProperty aui;
    private final StringProperty saui;
    private final StringProperty scui;
    private final StringProperty sdui;
    private final StringProperty sab;
    private final StringProperty tty;
    private final StringProperty code;
    private final StringProperty str; // Descripción del diagnóstico
    private final StringProperty srl;
    private final StringProperty suppress;
    private final StringProperty cvf;
    
    /**
     * Constructor con valores por defecto.
     */
    public ModeloDiagnosticoUMLS() {
        this.cui = new SimpleStringProperty();
        this.lat = new SimpleStringProperty();
        this.ts = new SimpleStringProperty();
        this.lui = new SimpleStringProperty();
        this.stt = new SimpleStringProperty();
        this.sui = new SimpleStringProperty();
        this.ispref = new SimpleStringProperty();
        this.aui = new SimpleStringProperty();
        this.saui = new SimpleStringProperty();
        this.scui = new SimpleStringProperty();
        this.sdui = new SimpleStringProperty();
        this.sab = new SimpleStringProperty();
        this.tty = new SimpleStringProperty();
        this.code = new SimpleStringProperty();
        this.str = new SimpleStringProperty();
        this.srl = new SimpleStringProperty();
        this.suppress = new SimpleStringProperty();
        this.cvf = new SimpleStringProperty();
    }
    
    /**
     * Constructor con parámetros básicos.
     * @param cui Identificador único del concepto
     * @param str Descripción del diagnóstico
     * @param sab Fuente de la que proviene el término
     * @param code Código del diagnóstico
     */
    public ModeloDiagnosticoUMLS(String cui, String str, String sab, String code) {
        this();
        this.cui.set(cui);
        this.str.set(str);
        this.sab.set(sab);
        this.code.set(code);
    }
    
    /**
     * Constructor completo con todos los campos.
     */
    public ModeloDiagnosticoUMLS(
            String cui, String lat, String ts, String lui, String stt, 
            String sui, String ispref, String aui, String saui, String scui, 
            String sdui, String sab, String tty, String code, String str, 
            String srl, String suppress, String cvf) {
        this();
        this.cui.set(cui);
        this.lat.set(lat);
        this.ts.set(ts);
        this.lui.set(lui);
        this.stt.set(stt);
        this.sui.set(sui);
        this.ispref.set(ispref);
        this.aui.set(aui);
        this.saui.set(saui);
        this.scui.set(scui);
        this.sdui.set(sdui);
        this.sab.set(sab);
        this.tty.set(tty);
        this.code.set(code);
        this.str.set(str);
        this.srl.set(srl);
        this.suppress.set(suppress);
        this.cvf.set(cvf);
    }
    
    /**
     * Devuelve una representación corta del diagnóstico.
     */
    @Override
    public String toString() {
        return str.get() + " (" + cui.get() + ")";
    }
    
    // Getters y setters como propiedades para JavaFX
    
    public String getCui() {
        return cui.get();
    }
    
    public StringProperty cuiProperty() {
        return cui;
    }
    
    public void setCui(String cui) {
        this.cui.set(cui);
    }
    
    public String getLat() {
        return lat.get();
    }
    
    public StringProperty latProperty() {
        return lat;
    }
    
    public void setLat(String lat) {
        this.lat.set(lat);
    }
    
    public String getTs() {
        return ts.get();
    }
    
    public StringProperty tsProperty() {
        return ts;
    }
    
    public void setTs(String ts) {
        this.ts.set(ts);
    }
    
    public String getLui() {
        return lui.get();
    }
    
    public StringProperty luiProperty() {
        return lui;
    }
    
    public void setLui(String lui) {
        this.lui.set(lui);
    }
    
    public String getStt() {
        return stt.get();
    }
    
    public StringProperty sttProperty() {
        return stt;
    }
    
    public void setStt(String stt) {
        this.stt.set(stt);
    }
    
    public String getSui() {
        return sui.get();
    }
    
    public StringProperty suiProperty() {
        return sui;
    }
    
    public void setSui(String sui) {
        this.sui.set(sui);
    }
    
    public String getIspref() {
        return ispref.get();
    }
    
    public StringProperty isprefProperty() {
        return ispref;
    }
    
    public void setIspref(String ispref) {
        this.ispref.set(ispref);
    }
    
    public String getAui() {
        return aui.get();
    }
    
    public StringProperty auiProperty() {
        return aui;
    }
    
    public void setAui(String aui) {
        this.aui.set(aui);
    }
    
    public String getSaui() {
        return saui.get();
    }
    
    public StringProperty sauiProperty() {
        return saui;
    }
    
    public void setSaui(String saui) {
        this.saui.set(saui);
    }
    
    public String getScui() {
        return scui.get();
    }
    
    public StringProperty scuiProperty() {
        return scui;
    }
    
    public void setScui(String scui) {
        this.scui.set(scui);
    }
    
    public String getSdui() {
        return sdui.get();
    }
    
    public StringProperty sduiProperty() {
        return sdui;
    }
    
    public void setSdui(String sdui) {
        this.sdui.set(sdui);
    }
    
    public String getSab() {
        return sab.get();
    }
    
    public StringProperty sabProperty() {
        return sab;
    }
    
    public void setSab(String sab) {
        this.sab.set(sab);
    }
    
    public String getTty() {
        return tty.get();
    }
    
    public StringProperty ttyProperty() {
        return tty;
    }
    
    public void setTty(String tty) {
        this.tty.set(tty);
    }
    
    public String getCode() {
        return code.get();
    }
    
    public StringProperty codeProperty() {
        return code;
    }
    
    public void setCode(String code) {
        this.code.set(code);
    }
    
    public String getStr() {
        return str.get();
    }
    
    public StringProperty strProperty() {
        return str;
    }
    
    public void setStr(String str) {
        this.str.set(str);
    }
    
    public String getSrl() {
        return srl.get();
    }
    
    public StringProperty srlProperty() {
        return srl;
    }
    
    public void setSrl(String srl) {
        this.srl.set(srl);
    }
    
    public String getSuppress() {
        return suppress.get();
    }
    
    public StringProperty suppressProperty() {
        return suppress;
    }
    
    public void setSuppress(String suppress) {
        this.suppress.set(suppress);
    }
    
    public String getCvf() {
        return cvf.get();
    }
    
    public StringProperty cvfProperty() {
        return cvf;
    }
    
    public void setCvf(String cvf) {
        this.cvf.set(cvf);
    }
} 