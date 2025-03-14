//
//// src/main/java/com/pokerapp/service/impl/UserRoleServiceImpl.java
//package com.pokerapp.service.impl;
//
//import com.pokerapp.domain.user.Player;
//import com.pokerapp.domain.user.Spectator;
//import com.pokerapp.domain.user.User;
//import com.pokerapp.domain.user.UserType;
//import com.pokerapp.exception.NotFoundException;
//import com.pokerapp.repository.PlayerRepository;
//import com.pokerapp.repository.SpectatorRepository;
//import com.pokerapp.repository.UserRepository;
//import com.pokerapp.service.UserRoleService;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
////import javax.transaction.Transactional;
//
//@Service
//public class UserRoleServiceImpl implements UserRoleService {
//
//    @Autowired
//    private final UserRepository userRepository;
//
//    @Autowired
//    private final PlayerRepository playerRepository;
//
//    @Autowired
//    private final SpectatorRepository spectatorRepository;
//
//    @Autowired
//    public UserRoleServiceImpl(
//            UserRepository userRepository,
//            PlayerRepository playerRepository,
//            SpectatorRepository spectatorRepository) {
//        this.userRepository = userRepository;
//        this.playerRepository = playerRepository;
//        this.spectatorRepository = spectatorRepository;
//    }
//
//    @Override
//    @Transactional
//    public Player convertToPlayer(User user) {
//        if (user.getUserType() == UserType.PLAYER) {
//            return playerRepository.findById(user.getId())
//                    .orElseThrow(() -> new NotFoundException("Player not found"));
//        }
//
//        // Create a new Player and copy attributes
//        Player player = new Player();
//        player.setId(user.getId());
//        player.setUsername(user.getUsername());
//        player.setEmail(user.getEmail());
//        player.setPassword(user.getPassword());
//        player.setBalance(user.getBalance());
//        player.setAvatar(user.getAvatar());
//        player.setRoles(user.getRoles());
//        player.addRole("PLAYER");
//        player.setUserType(UserType.PLAYER);
//
//        return playerRepository.save(player);
//    }
//
//    @Override
//    //@Transactional
//    public Spectator convertToSpectator(User user) {
//        if (user.getUserType() == UserType.SPECTATOR) {
//            return spectatorRepository.findById(user.getId())
//                    .orElseThrow(() -> new NotFoundException("Spectator not found"));
//        }
//
//        // Create a new Spectator and copy attributes
//        Spectator spectator = new Spectator();
//        spectator.setId(user.getId());
//        spectator.setUsername(user.getUsername());
//        spectator.setEmail(user.getEmail());
//        spectator.setPassword(user.getPassword());
//        spectator.setBalance(user.getBalance());
//        spectator.setAvatar(user.getAvatar());
//        spectator.setRoles(user.getRoles());
//        spectator.addRole("SPECTATOR");
//        spectator.setUserType(UserType.SPECTATOR);
//
//        return spectatorRepository.save(spectator);
//    }
//
//    @Override
//    public User getUserWithRoles(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//
//        switch (user.getUserType()) {
//            case PLAYER:
//                return playerRepository.findById(userId)
//                        .orElse(null);
//            case SPECTATOR:
//                return spectatorRepository.findById(userId)
//                        .orElse(null);
//            default:
//                return user;
//        }
//    }
//}