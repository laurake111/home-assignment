package ee.tuum.assignment.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import ee.tuum.assignment.enums.Currency;
import ee.tuum.assignment.enums.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleEnumErrors(HttpMessageNotReadableException ex) {
		Throwable cause = ex.getCause();
		if (cause instanceof InvalidFormatException err) {
			Class<?> targetType = err.getTargetType();

			if (targetType == Currency.class) {
				return ResponseEntity.badRequest().body("Invalid currency");
			}
			if (targetType == Direction.class) {
				return ResponseEntity.badRequest().body("Invalid direction");
			}
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request payload");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	// for jakarta validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
		return ResponseEntity.badRequest().body(errorMessage);
	}
}
