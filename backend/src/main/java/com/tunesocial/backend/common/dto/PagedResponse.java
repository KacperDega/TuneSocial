package com.tunesocial.backend.common.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        Integer nextPage
) {}
