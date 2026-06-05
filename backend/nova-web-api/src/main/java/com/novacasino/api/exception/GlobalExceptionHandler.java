package com.novacasino.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.novacasino.api.auth.exception.AgeVerificationException;
import com.novacasino.api.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.api.auth.exception.InvalidCredentialsException;
import com.novacasino.api.player.exception.GameNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manejador global de excepciones. Devuelve {@code application/problem+json} (RFC 9457)
 * con mensajes internacionalizados según la cabecera {@code Accept-Language} del cliente.
 *
 * <p>AC1: Accept-Language: en → mensajes en inglés.
 * <p>AC2: Accept-Language: es (o sin cabecera) → mensajes en español.
 * <p>AC4: El idioma no altera códigos HTTP ni la estructura del Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    // -------------------------------------------------------------------------
    // Errores de validación de bean — 422
    // -------------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Locale locale = LocaleContextHolder.getLocale();

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field",   fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"
                ))
                .toList();

        ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.validation.detail", locale));
        body.setProperty("errors", errors);

        return ResponseEntity.status(status).headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // JSON mal formado — 400
    // -------------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Locale locale = LocaleContextHolder.getLocale();

        ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.badRequest.title", locale));
        body.setDetail(msg("error.badRequest.detail", locale));

        return ResponseEntity.status(status).headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // Auth — 422 edad, 409 email duplicado, 401 credenciales
    // -------------------------------------------------------------------------

    @ExceptionHandler(AgeVerificationException.class)
    ResponseEntity<ProblemDetail> handleAgeVerification(AgeVerificationException ex,
                                                         HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.auth.ageVerificationFailed.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex,
                                                                HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.conflict.title", locale));
        body.setDetail(msg("error.auth.emailAlreadyRegistered.detail", locale));
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex,
                                                            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.unauthorized.title", locale));
        body.setDetail(msg("error.auth.invalidCredentials.detail", locale));
        return ResponseEntity.status(401).body(body);
    }

    // -------------------------------------------------------------------------
    // Recurso no encontrado — 404
    // -------------------------------------------------------------------------

    @ExceptionHandler(GameNotFoundException.class)
    ResponseEntity<ProblemDetail> handleGameNotFound(GameNotFoundException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.notFound.title", locale));
        body.setDetail(msg("error.notFound.detail", locale));
        return ResponseEntity.status(404).body(body);
    }

    // -------------------------------------------------------------------------
    // Error interno no controlado — 500
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleAll(Exception ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);

        ProblemDetail body = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.internalError.title", locale));
        body.setDetail(msg("error.internalError.detail", locale));

        return ResponseEntity.status(500).body(body);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String msg(String code, Locale locale) {
        return messages.getMessage(code, null, code, locale);
    }
}
