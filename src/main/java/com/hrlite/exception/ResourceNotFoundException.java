package com.hrlite.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCodes.RESOURCE_NOT_FOUND,
                resource + " not found with id: " + id,
                HttpStatus.NOT_FOUND);
    }
}
