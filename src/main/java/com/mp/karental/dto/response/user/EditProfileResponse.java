package com.mp.karental.dto.response.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO representing the response for editing a user profile.
 * Contains user profile details that can be updated.
 *
 * @author AnhHP9
 *
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditProfileResponse {

    String fullName;
    LocalDate dob;
    String phoneNumber;
    String nationalId;
    String drivingLicenseUrl;
    String cityProvince;
    String district;
    String ward;
    String houseNumberStreet;
    String email;

    // SURPLUS-GAP-02: Return edit timestamp in response - SRS UC05 does not mention returning lastEditedAt
    LocalDateTime lastEditedAt;
}
