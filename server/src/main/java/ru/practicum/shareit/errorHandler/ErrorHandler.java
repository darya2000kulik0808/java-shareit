package ru.practicum.shareit.errorHandler;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.*;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAbsentObject(final ObjectNotFoundException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Объект не найден", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleExistingObject(final ObjectAlreadyExistsException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Объект уже существует.", e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCommentAccessDenied(final CommentAccessDeniedException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Вы не можете оставлять комментарии.", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleWrongState(final UnknownStateException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Unknown state: UNSUPPORTED_STATUS", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleFailedValidation(final ValidationException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Произошла ошибка!", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBookingAccessDenied(final BookingAccessDeniedException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Бронирование не доступно!", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBookingAccessForOwnerDenied(final BookingAccessDeniedForOwnerException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Бронирование не доступно!", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleFailedMailValidation(final ConstraintViolationException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Такая почта уже есть в базе!", e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingUserIdHeader(final MissingRequestHeaderException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Отсутствует заголовок c идентификатором пользователя.", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleSameStartAndEndTime(final StartTimeAndEndTimeException e) {
        return new ResponseEntity<>(
                new ErrorResponse("Время начала не может быть равно времени окончания.", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(
                new ErrorResponse("Поля объекта заполнены неверно!", getErrorsMap(errors).toString()),
                HttpStatus.BAD_REQUEST
        );
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("Допущенные ошибки: ", errors);
        return errorResponse;
    }
}

