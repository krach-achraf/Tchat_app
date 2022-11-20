package com.tchat.ms_authentification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class AdminDTO {
    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private boolean isLocked;

    @NotNull
    private boolean isExpired;

    @NotNull
    private String authority;

}
