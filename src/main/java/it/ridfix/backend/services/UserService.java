package it.ridfix.backend.services;

import it.ridfix.backend.dto.AddressDTOs;
import it.ridfix.backend.dto.UserDTOs;
import it.ridfix.backend.entities.Address;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.external.cloudinary.CloudinaryService;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.AddressRepository;
import it.ridfix.backend.repositories.UserRepository;
import it.ridfix.backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository users;
    private final AddressRepository addresses;
    private final CloudinaryService cloudinary;

    public UserService(UserRepository users, AddressRepository addresses, CloudinaryService cloudinary) {
        this.users = users;
        this.addresses = addresses;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public UserDTOs.UserResponse getMe() {
        User u = users.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));
        return MapperUtils.userToResponse(u);
    }

    @Transactional
    public UserDTOs.UserResponse updateMe(UserDTOs.UpdateUserRequest req) {
        User u = users.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));
        u.setName(req.name());
        u.setSurname(req.surname());
        return MapperUtils.userToResponse(u);
    }

    @Transactional
    public UserDTOs.UserResponse uploadProfileImage(MultipartFile file) {
        User u = users.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));
        String url = cloudinary.uploadImage(file);
        u.setProfileImageUrl(url);
        return MapperUtils.userToResponse(u);
    }

    @Transactional(readOnly = true)
    public List<AddressDTOs.AddressResponse> listMyAddresses() {
        return addresses.findByUserIdOrderByTypeAsc(SecurityUtils.currentUserId()).stream()
                .map(MapperUtils::addressToResponse)
                .toList();
    }

    @Transactional
    public AddressDTOs.AddressResponse addAddress(AddressDTOs.AddressRequest req) {
        User u = users.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("User not found"));

        if (req.isDefault()) {
            // ✅ Atomico e veloce: un colpo solo sul DB
            addresses.resetDefaultsForUserAndType(u.getId(), req.type());
        }

        Address a = new Address(
                u,
                req.type(),
                req.street(),
                req.city(),
                req.postalCode(),
                req.country(),
                req.isDefault()
        );

        addresses.save(a);
        return MapperUtils.addressToResponse(a);
    }

    @Transactional
    public void deleteMyAddress(UUID addressId) {
        Address a = addresses.findByIdAndUserId(addressId, SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiExceptions.NotFound("Address not found"));
        addresses.delete(a);
    }
}
