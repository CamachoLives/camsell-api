package com.camsell.parameters.domain.model;

/**
 * Ambito donde aplica un grupo de parametros. Permite que la misma consola
 * administre la configuracion del catalogo de vehiculos y la del contenido
 * editorial sin mezclarlas en las pantallas.
 */
public enum ParameterScope {
    /** Caracteristicas de las publicaciones de vehiculos. */
    LISTING,
    /** Configuracion de la seccion informativa. */
    NEWS,
    /** Catalogos transversales, como ciudades. */
    GENERAL;

    public boolean appliesToListings() {
        return this == LISTING || this == GENERAL;
    }
}
