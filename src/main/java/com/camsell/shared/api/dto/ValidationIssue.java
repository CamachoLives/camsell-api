package com.camsell.shared.api.dto;

/** Detalle de un campo invalido devuelto junto al ProblemDetail. */
public record ValidationIssue(String field, String message) {
}
