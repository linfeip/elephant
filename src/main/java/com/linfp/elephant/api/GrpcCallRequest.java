package com.linfp.elephant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class GrpcCallRequest {
    @NotEmpty
    public List<String> protos;
    @NotBlank
    public String addr;
    @NotBlank
    public String service;
    @NotBlank
    public String method;
    public String body;
}
