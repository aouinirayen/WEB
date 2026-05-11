package tn.esprit.pidev.controller;

import tn.esprit.pidev.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserSearchController {

    private final UserRepository userRepo;

    public UserSearchController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/search-autocomplete")
    public List<Map<String, String>> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) return List.of();
        
        String query = q.trim().toLowerCase();
        return userRepo.findAll().stream()
            .filter(u -> u.getName().toLowerCase().contains(query) 
                      || u.getUsername().toLowerCase().contains(query)
                      || (u.getCin() != null && u.getCin().toString().contains(query)))
            .limit(8)
            .map(u -> Map.of(
                "name", u.getName() != null ? u.getName() : "",
                "phone", u.getCin() != null ? u.getCin().toString() : "",
                "email", u.getEmail() != null ? u.getEmail() : ""
            ))
            .collect(Collectors.toList());
    }
}
