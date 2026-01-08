package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.AddressDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/me")
    public UserDTOs.UserResponse me() {
        return users.getMe();
    }

    @PatchMapping("/me")
    public UserDTOs.UserResponse updateMe(@Valid @RequestBody UserDTOs.UpdateUserRequest req) {
        return users.updateMe(req);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTOs.UserResponse uploadProfileImage(@RequestPart("file") MultipartFile file) {
        return users.uploadProfileImage(file);
    }

    // Addresses
    @GetMapping("/me/addresses")
    public List<AddressDTOs.AddressResponse> listAddresses() {
        return users.listMyAddresses();
    }

    @PostMapping("/me/addresses")
    public AddressDTOs.AddressResponse addAddress(@Valid @RequestBody AddressDTOs.AddressRequest req) {
        return users.addAddress(req);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public void deleteAddress(@PathVariable UUID addressId) {
        users.deleteMyAddress(addressId);
    }
}
