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
import com.novacasino.api.player.exception.InvalidBetException;
import com.novacasino.api.player.exception.InsufficientBalanceException;
import com.novacasino.api.player.exception.ConcurrentSpinException;
import com.novacasino.api.operator.exception.InvalidAmountException;
import com.novacasino.api.operator.exception.PlayerNotFoundException;
import com.novacasino.api.operator.exception.RoundNotFoundException;
import com.novacasino.api.idempotency.IdempotencyConflictException;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.api.math.exception.InvalidSimulationParamsException;
import com.novacasino.api.math.exception.SimulationNotCompletedException;
import com.novacasino.api.math.exception.ExplainerUnavailableException;
import com.novacasino.api.math.validation.ConfigValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Global exception handler. Returns {@code application/problem+json} (RFC 9457) with messages
 * localised according to the client's {@code Accept-Language} header (resolved via Spring's
 * {@code LocaleContextHolder}, which honours the configured default of "es").
 *
 * <p>The language never changes the HTTP status nor the Problem Details structure.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private final MessageSource messages;

    public GlobalExceptionHandler(final MessageSource messages) {
        this.messages = messages;
    }

    // -------------------------------------------------------------------------
    // Bean validation errors — 422
    // -------------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {

        final Locale locale = LocaleContextHolder.getLocale();

        final List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field",   fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"
                ))
                .toList();

        final ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.validation.detail", locale));
        body.setProperty("errors", errors);

        return ResponseEntity.status(status).headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // Malformed JSON — 400
    // -------------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {

        final Locale locale = LocaleContextHolder.getLocale();

        final ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.badRequest.title", locale));
        body.setDetail(msg("error.badRequest.detail", locale));

        return ResponseEntity.status(status).headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // Auth — 422 age, 409 duplicate email, 401 credentials
    // -------------------------------------------------------------------------

    @ExceptionHandler(AgeVerificationException.class)
    ResponseEntity<ProblemDetail> handleAgeVerification(final AgeVerificationException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.auth.ageVerificationFailed.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(final EmailAlreadyRegisteredException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.conflict.title", locale));
        body.setDetail(msg("error.auth.emailAlreadyRegistered.detail", locale));
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> handleInvalidCredentials(final InvalidCredentialsException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.unauthorized.title", locale));
        body.setDetail(msg("error.auth.invalidCredentials.detail", locale));
        return ResponseEntity.status(401).body(body);
    }

    // -------------------------------------------------------------------------
    // Resource not found — 404
    // -------------------------------------------------------------------------

    @ExceptionHandler({GameNotFoundException.class, PlayerNotFoundException.class,
            ConfigNotFoundException.class, SimulationNotFoundException.class, RoundNotFoundException.class})
    ResponseEntity<ProblemDetail> handleNotFound(final RuntimeException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.notFound.title", locale));
        body.setDetail(msg("error.notFound.detail", locale));
        return ResponseEntity.status(404).body(body);
    }

    // -------------------------------------------------------------------------
    // Invalid math config — 422 with per-field errors[]
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConfigValidationException.class)
    ResponseEntity<ProblemDetail> handleConfigValidation(final ConfigValidationException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final List<Map<String, String>> errors = ex.getErrors().stream()
                .map(e -> Map.of("field", e.field(), "message", e.message()))
                .toList();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.config.invalid.detail", locale));
        body.setProperty("errors", errors);
        return ResponseEntity.status(422).body(body);
    }

    // -------------------------------------------------------------------------
    // Invalid simulation parameters (numSpins out of range / bad bet) — 422
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidSimulationParamsException.class)
    ResponseEntity<ProblemDetail> handleInvalidSimulationParams(final InvalidSimulationParamsException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.simulation.spinsOutOfRange.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    // -------------------------------------------------------------------------
    // AI explainability — 422 (not completed), 503 (explainer unavailable)
    // -------------------------------------------------------------------------

    @ExceptionHandler(SimulationNotCompletedException.class)
    ResponseEntity<ProblemDetail> handleSimulationNotCompleted(final SimulationNotCompletedException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.explain.notCompleted.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    @ExceptionHandler(ExplainerUnavailableException.class)
    ResponseEntity<ProblemDetail> handleExplainerUnavailable(final ExplainerUnavailableException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.serviceUnavailable.title", locale));
        body.setDetail(msg("error.explain.unavailable.detail", locale));
        return ResponseEntity.status(503).body(body);
    }

    // -------------------------------------------------------------------------
    // Invalid amount — 422
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidAmountException.class)
    ResponseEntity<ProblemDetail> handleInvalidAmount(final InvalidAmountException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.recharge.invalidAmount.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    // -------------------------------------------------------------------------
    // Spin — invalid bet / insufficient balance — 422
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidBetException.class)
    ResponseEntity<ProblemDetail> handleInvalidBet(final InvalidBetException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.spin.invalidBet.detail", locale));
        return ResponseEntity.status(422).body(body);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ProblemDetail> handleInsufficientBalance(final InsufficientBalanceException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.validation.title", locale));
        body.setDetail(msg("error.spin.insufficientBalance.detail", locale,
                ex.getNeededCents(), ex.getAvailableCents()));
        return ResponseEntity.status(422).body(body);
    }

    // -------------------------------------------------------------------------
    // Idempotency-Key reused with a different payload — 409
    // -------------------------------------------------------------------------

    @ExceptionHandler(IdempotencyConflictException.class)
    ResponseEntity<ProblemDetail> handleIdempotencyConflict(final IdempotencyConflictException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.conflict.title", locale));
        body.setDetail(msg("error.idempotency.conflict.detail", locale));
        return ResponseEntity.status(409).body(body);
    }

    // -------------------------------------------------------------------------
    // Concurrent wallet modification (optimistic lock exhausted) — 409
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConcurrentSpinException.class)
    ResponseEntity<ProblemDetail> handleConcurrentSpin(final ConcurrentSpinException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.conflict.title", locale));
        body.setDetail(msg("error.idempotency.concurrentModification.detail", locale));
        return ResponseEntity.status(409).body(body);
    }

    // -------------------------------------------------------------------------
    // Malformed path/header value (e.g. a non-UUID Idempotency-Key) — 400
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.badRequest.title", locale));
        body.setDetail(msg("error.badRequest.detail", locale));
        return ResponseEntity.status(400).body(body);
    }

    // -------------------------------------------------------------------------
    // Uncaught internal error — 500
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleAll(final Exception ex, final HttpServletRequest request) {
        final Locale locale = LocaleContextHolder.getLocale();
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);

        final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        body.setType(ABOUT_BLANK);
        body.setTitle(msg("error.internalError.title", locale));
        body.setDetail(msg("error.internalError.detail", locale));

        return ResponseEntity.status(500).body(body);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String msg(final String code, final Locale locale) {
        return messages.getMessage(code, null, code, locale);
    }

    private String msg(final String code, final Locale locale, final Object... args) {
        return messages.getMessage(code, args, code, locale);
    }
}
