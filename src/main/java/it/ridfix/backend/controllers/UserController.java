package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.AddressDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    /**
     * ✅ ENDPOINT RICHIESTO
     * PATCH /api/users/me/image
     *
     * Postman: Body -> form-data -> key = "file" (type File)
     */
    @PatchMapping(
            value = "/me/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserDTOs.UserResponse uploadProfileImage(
            @RequestPart("file") MultipartFile file
    ) {
        validateImage(file);
        return users.uploadProfileImage(file);
    }

    // Addresses
    @GetMapping("/me/addresses")
    public List<AddressDTOs.AddressResponse> listAddresses() {
        return users.listMyAddresses();
    }

    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDTOs.AddressResponse addAddress(@Valid @RequestBody AddressDTOs.AddressRequest req) {
        return users.addAddress(req);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@PathVariable UUID addressId) {
        users.deleteMyAddress(addressId);
    }

    // ---- private helpers ----

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiExceptions.BadRequest("File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiExceptions.BadRequest("Only image files are allowed");
        }
    }
}
