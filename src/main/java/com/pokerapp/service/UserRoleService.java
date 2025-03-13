package com.pokerapp.service;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.User;

public interface UserRoleService {
    Player convertToPlayer(User user);
    Spectator convertToSpectator(User user);
    User getUserWithRoles(Long userId);
}
