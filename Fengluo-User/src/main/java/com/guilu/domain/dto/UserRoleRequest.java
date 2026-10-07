package com.guilu.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long roleId;
}
