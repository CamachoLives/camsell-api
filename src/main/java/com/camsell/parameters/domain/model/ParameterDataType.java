package com.camsell.parameters.domain.model;

/**
 * Tipo de dato de un grupo de parametros. Determina como el frontend renderiza
 * el campo en el formulario de publicacion y en los filtros de busqueda.
 */
public enum ParameterDataType {
    /** Se elige entre opciones administradas (select / chips). */
    OPTION,
    /** Texto libre. */
    TEXT,
    /** Valor numerico (admite rangos en los filtros). */
    NUMBER,
    /** Si / no. */
    BOOLEAN
}
