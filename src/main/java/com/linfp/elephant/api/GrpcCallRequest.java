package com.linfp.elephant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GrpcCallRequest {
    @NotEmpty
    private List<String> protos;
    @NotBlank
    private String addr;
    @NotBlank
    private String service;
    @NotBlank
    private String method;
    private String body;
}
