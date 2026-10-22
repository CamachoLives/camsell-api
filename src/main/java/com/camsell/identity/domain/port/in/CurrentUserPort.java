package com.camsell.identity.domain.port.in;

import java.util.Optional;

/**
 * Puerto publico del contexto de identidad. Los demas modulos resuelven el
 * usuario autenticado a traves de esta interfaz y nunca leen el
 * {@code SecurityContext} de Spring directamente.
 */
public interface CurrentUserPort {

    Optional<Long> currentUserId();

    Long requireCurrentUserId();

    boolean isAdmin();

    /** Verifica propiedad del recurso: el dueno o un administrador. */
    boolean canManage(Long ownerId);
}
