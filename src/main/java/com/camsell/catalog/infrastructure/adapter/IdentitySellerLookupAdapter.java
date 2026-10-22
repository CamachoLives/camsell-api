package com.camsell.catalog.infrastructure.adapter;

import com.camsell.catalog.domain.port.out.SellerLookupPort;
import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.port.in.UserUseCase;
import org.springframework.stereotype.Component;

/**
 * Puente entre el catalogo y el contexto de identidad: el catalogo solo conoce
 * el puerto {@link SellerLookupPort} y nunca el modelo de usuarios.
 */
@Component
public class IdentitySellerLookupAdapter implements SellerLookupPort {

    private final UserUseCase users;

    public IdentitySellerLookupAdapter(UserUseCase users) {
        this.users = users;
    }

    @Override
    public SellerInfo findSeller(Long userId) {
        User user = users.getById(userId);
        return new SellerInfo(
                user.id(),
                user.fullName(),
                user.phone(),
                user.email(),
                user.role().name());
    }
}
