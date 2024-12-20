package kz.nik.socksbackspark.handler;

import kz.nik.socksbackspark.exceptions.FileProcessingException;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.exceptions.InvalidDataFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class MainExceptionHandler {

    @ExceptionHandler(InvalidDataFormatException.class)
    public ResponseEntity<Object> handleInvalidDataFormat(InvalidDataFormatException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(
                false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                         WebRequest request) {
        String message = "Invalid value for parameter: " + ex.getName() + ". Expected type: " +
                ex.getRequiredType().getName();
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, message, request.getDescription(
                false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficientStockException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(
                false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Object> handleFileProcessingError(FileProcessingException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleValidationExceptions(BindException ex, WebRequest request) {
        String errorMessage = "Invalid input data: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, errorMessage, request.getDescription(
                false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}