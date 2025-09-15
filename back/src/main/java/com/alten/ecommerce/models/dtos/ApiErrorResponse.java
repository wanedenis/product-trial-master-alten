package com.alten.ecommerce.models.dtos;

import java.util.Map;

public record ApiErrorResponse(
    String message,
    int status,
    String path,
    Map<String, String> errors
) {}