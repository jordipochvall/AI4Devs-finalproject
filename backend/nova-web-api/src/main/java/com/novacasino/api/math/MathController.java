package com.novacasino.api.math;

import com.novacasino.api.math.dto.ConfigCreatedDto;
import com.novacasino.api.math.dto.ConfigDetailDto;
import com.novacasino.api.math.dto.CreateConfigRequest;
import com.novacasino.api.math.dto.MathGameDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Math backoffice (MATH_ANALYST role enforced by SecurityConfig on /math/**).
 * Publishing versions is post-MVP.
 */
@RestController
@RequestMapping("/api/v1/math")
public class MathController {

    private final MathService math;

    public MathController(final MathService math) {
        this.math = math;
    }

    /** GET /math/games — games with their active version. */
    @GetMapping("/games")
    public List<MathGameDto> listGames(@AuthenticationPrincipal final NovaUserDetails principal) {
        return math.listGames(principal.getUser().getOperatorId());
    }

    /** GET /math/configs/{id} — detail of a config version. */
    @GetMapping("/configs/{configId}")
    public ConfigDetailDto getConfig(@PathVariable final Long configId) {
        return math.getConfig(configId);
    }

    /** POST /math/games/{id}/configs — creates a new math version. */
    @PostMapping("/games/{gameId}/configs")
    public ResponseEntity<ConfigCreatedDto> createConfig(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId,
            @Valid @RequestBody final CreateConfigRequest body) {
        final Long operatorId = principal.getUser().getOperatorId();
        final Long mathUserId = principal.getUser().getId();
        final ConfigCreatedDto created = math.createConfig(gameId, operatorId, mathUserId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
