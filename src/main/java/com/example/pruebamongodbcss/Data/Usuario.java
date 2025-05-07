package com.example.pruebamongodbcss.Data;

public class Usuario {

    private String nombre;
    private String email;
    private String contraseña;
    private String telefono;
    private String CONTRASENA_ADMIN;

    /* Constructor */
    public Usuario(String nombre, String email, String contraseña, String telefono) throws PatronExcepcion {
        this.setNombre(nombre);
        this.setEmail(email);
        this.setContraseña(contraseña);
        this.setTelefono(telefono);
    }

    public Usuario(String nombre, String email, String contraseña,String telefono,String CONTRASENA_ADMIN) throws Exception {
        this.setNombre(nombre);
        this.setEmail(email);
        this.setContraseña(contraseña);
        this.setTelefono(telefono);
        if (CONTRASENA_ADMIN.equals(Clinica.CONTRASENA_ADMIN)) {
            this.CONTRASENA_ADMIN = CONTRASENA_ADMIN;
        }else{
            throw new Exception("Contraseña no válida");
        }
        
    }
    
    
    

    /* Métodos */
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", contraseña='" + contraseña + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }




    /* Setters y Getters */
    public void setNombre(String nombre) throws PatronExcepcion {
        if (nombre.isEmpty() || nombre.length() < 3) {
            throw new PatronExcepcion("Nombre no válido");
        }
        this.nombre = nombre;
    }

    public void setEmail(String email) throws PatronExcepcion {
        if (email.isEmpty() || !email.contains("@") ||
         !email.contains(".") || email.length() > 30 ||
          email.length() < 5|| !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            throw new PatronExcepcion("Email no válido");
        }
        this.email = email;
    }   


    public void setContraseña(String contraseña) throws PatronExcepcion {
        if (contraseña.isEmpty() || contraseña.length() < 8 || contraseña.length() > 16) {
            throw new PatronExcepcion("Contraseña no válida");
        }
        this.contraseña = contraseña;
    }

    public void setTelefono(String telefono) throws PatronExcepcion {
        if (telefono.isEmpty() || telefono.length() != 9 || !telefono.matches("[0-9]+")) {
            throw new PatronExcepcion("Teléfono no válido");
        }
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getTelefono() {
        return telefono;
    }
    
    
}