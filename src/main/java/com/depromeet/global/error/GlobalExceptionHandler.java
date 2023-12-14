package com.depromeet.global.error;

import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // @Override
    // protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException
    // ex,
    // 	HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    // 	return super.handleMethodArgumentNotValid(ex, headers, status, request);
    // }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        ErrorResponse errorResponse =
                ErrorResponse.of(HttpStatus.resolve(statusCode.value()), ex.getMessage());
        return super.handleExceptionInternal(ex, errorResponse, headers, statusCode, request);
    }

    /**
     * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다. HttpMessageConverter 에서 등록한
     * HttpMessageConverter binding 못할경우 발생 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @SneakyThrows
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.error("MethodArgumentNotValidException : {}", e.getMessage(), e);
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        Map<String, Object> fieldAndErrorMessages =
                errors.stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField, FieldError::getDefaultMessage));

        String errorsToJsonString = new ObjectMapper().writeValueAsString(fieldAndErrorMessages);
        final ErrorResponse errorResponse =
                ErrorResponse.of(HttpStatus.resolve(status.value()), errorsToJsonString);

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /** Request Param Validation 예외 처리 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.error("ConstraintViolationException : {}", e.getMessage(), e);

        Map<String, Object> bindingErrors = new HashMap<>();
        e.getConstraintViolations()
                .forEach(
                        constraintViolation -> {
                            List<String> propertyPath =
                                    List.of(
                                            constraintViolation
                                                    .getPropertyPath()
                                                    .toString()
                                                    .split("\\."));
                            String path =
                                    propertyPath.stream()
                                            .skip(propertyPath.size() - 1L)
                                            .findFirst()
                                            .orElse(null);
                            bindingErrors.put(path, constraintViolation.getMessage());
                        });

        final ErrorResponse errorResponse =
                ErrorResponse.of(HttpStatus.BAD_REQUEST, bindingErrors.toString());

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /** enum type 일치하지 않아 binding 못할 경우 발생 주로 @RequestParam enum으로 binding 못했을 경우 발생 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException : {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getStatus(), errorCode.getMessage());

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /** 지원하지 않은 HTTP method 호출 할 경우 발생 */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.error("HttpRequestMethodNotSupportedException : {}", e.getMessage(), e);
        final ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getStatus(), errorCode.getMessage());

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /** CustomException 예외 처리 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException : {}", e.getMessage(), e);
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getStatus(), errorCode.getMessage());

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /** 500번대 에러 처리 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Internal Server Error : {}", e.getMessage(), e);
        final ErrorCode internalServerError = ErrorCode.INTERNAL_SERVER_ERROR;
        final ErrorResponse errorResponse =
                ErrorResponse.of(internalServerError.getStatus(), internalServerError.getMessage());

        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }
}
