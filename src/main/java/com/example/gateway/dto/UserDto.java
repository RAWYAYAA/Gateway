package com.example.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {
    private Long id;
    private String password;
    private String email;
    private String FirstNAme;
    private String LastName;
}
