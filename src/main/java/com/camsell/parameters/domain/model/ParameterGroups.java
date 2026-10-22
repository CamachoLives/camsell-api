package com.camsell.parameters.domain.model;

/**
 * Codigos de grupos que el nucleo del catalogo conoce por nombre. El resto de
 * grupos son totalmente dinamicos y se descubren en tiempo de ejecucion.
 */
public final class ParameterGroups {

    public static final String FUEL_TYPE = "FUEL_TYPE";
    public static final String TRANSMISSION = "TRANSMISSION";
    public static final String BODY_TYPE = "BODY_TYPE";
    public static final String CONDITION = "CONDITION";
    public static final String COLOR = "COLOR";
    public static final String DRIVETRAIN = "DRIVETRAIN";
    public static final String FEATURE = "FEATURE";
    public static final String CITY = "CITY";
    public static final String NEWS_CATEGORY = "NEWS_CATEGORY";

    private ParameterGroups() {
    }
}
