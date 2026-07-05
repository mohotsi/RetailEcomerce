package za.co.monate.retail.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import za.co.monate.retail.exception.BulkImportException;
import za.co.monate.retail.exception.InsufficientStockException;
import za.co.monate.retail.exception.ResourceNotFoundException;
import za.co.monate.retail.exception.UnauthorizedActionException;
import za.co.monate.retail.exception.dto.ApiErrorResponse;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request.getRequestURI());
    }

    // 400 - Bad Request (For CSV/JSON failures, empty payloads)
    @ExceptionHandler({BulkImportException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad Request error at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    // 403 - Forbidden (Role checks, Assisted Checkout)
    @ExceptionHandler({UnauthorizedActionException.class, SecurityException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        log.warn("Security violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden Action", ex.getMessage(), request.getRequestURI());
    }

    // 401 - Unauthorized (Spring Security Auth Failures)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid email or password", request.getRequestURI());
    }

    // 409 - Conflict (Stock issues)
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInventoryConflict(InsufficientStockException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Inventory Conflict", ex.getMessage(), request.getRequestURI());
    }

    // 500 - Catch-All for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unknown error occurred at {}: ", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected system error occurred", request.getRequestURI());
    }

    // Helper method to build the standardized JSON
    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String error, String message, String path) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
        return new ResponseEntity<>(response, status);
    }
}