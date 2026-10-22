package com.camsell.shared.api;

import com.camsell.shared.api.dto.ValidationIssue;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.DomainException;
import com.camsell.shared.domain.exception.DuplicatedResourceException;
import com.camsell.shared.domain.exception.OperationNotAllowedException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Traduce excepciones de dominio e infraestructura a respuestas RFC-9457
 * (ProblemDetail), para que el frontend consuma siempre el mismo contrato.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String TYPE_PREFIX = "https://camsell.dev/problems/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail onNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex);
    }

    @ExceptionHandler(DuplicatedResourceException.class)
    public ProblemDetail onDuplicated(DuplicatedResourceException ex) {
        return problem(HttpStatus.CONFLICT, "Recurso duplicado", ex);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ProblemDetail onNotAllowed(OperationNotAllowedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Operacion no permitida", ex);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail onBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onInvalidPayload(MethodArgumentNotValidException ex) {
        List<ValidationIssue> issues = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toIssue)
                .toList();
        ProblemDetail detail = base(HttpStatus.BAD_REQUEST,
                "Payload invalido",
                "La peticion contiene campos invalidos",
                "invalid_payload");
        detail.setProperty("errors", issues);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadablePayload(HttpMessageNotReadableException ex) {
        log.warn("Payload ilegible: {}", ex.getMostSpecificCause().getMessage());
        return base(HttpStatus.BAD_REQUEST,
                "Payload ilegible",
                "El cuerpo de la peticion no es un JSON valido para este recurso",
                "malformed_payload");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail onTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return base(HttpStatus.BAD_REQUEST,
                "Parametro invalido",
                "El parametro '%s' no tiene un valor valido".formatted(ex.getName()),
                "invalid_parameter");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail onAccessDenied(AccessDeniedException ex) {
        return base(HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "No cuentas con permisos para esta operacion",
                "access_denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail onAuthentication(AuthenticationException ex) {
        return base(HttpStatus.UNAUTHORIZED,
                "No autenticado",
                "Credenciales invalidas o sesion expirada",
                "unauthenticated");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return base(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                "Ocurrio un error inesperado procesando la peticion",
                "internal_error");
    }

    private ValidationIssue toIssue(FieldError error) {
        return new ValidationIssue(error.getField(), error.getDefaultMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, DomainException ex) {
        return base(status, title, ex.getMessage(), ex.code());
    }

    private ProblemDetail base(HttpStatus status, String title, String detailMessage, String code) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        detail.setTitle(title);
        detail.setType(URI.create(TYPE_PREFIX + code.replace('_', '-')));
        detail.setProperty("code", code);
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }
}
