package com.camsell.identity.domain.model;

/** Roles de la plataforma. */
public enum UserRole {
    /** Persona natural que compra y puede publicar vehiculos propios. */
    USER,
    /** Concesionario o vendedor profesional con publicaciones destacadas. */
    DEALER,
    /** Administra parametros, marcas, moderacion y contenido editorial. */
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
