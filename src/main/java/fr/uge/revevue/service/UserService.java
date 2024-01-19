package fr.uge.revevue.service;

import com.sun.jdi.request.InvalidRequestStateException;
import fr.uge.revevue.entity.User;
import fr.uge.revevue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(){}

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User signup(String username, String password) {
        var find = userRepository.findByUsername(username);
        if (find.isPresent()){
            throw new IllegalArgumentException("username already used");
        }
        var passwordCrypt = bCryptPasswordEncoder.encode(password);
        var user = new User(username, passwordCrypt);
        userRepository.save(user);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return user.get();
    }

    public User login(String username, String password){
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()){
            throw new InvalidRequestStateException("Invalid credentials");
        }
        var passwordCrypt = bCryptPasswordEncoder.encode(password);
        if (!user.get().getPassword().equals(passwordCrypt)){
            throw new InvalidRequestStateException("Invalid credentials");
        }
        return user.get();

    }

}
