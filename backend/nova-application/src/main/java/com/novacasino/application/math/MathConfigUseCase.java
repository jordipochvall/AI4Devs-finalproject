package com.novacasino.application.math;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.application.math.MathConfigPort.ConfigRef;
import com.novacasino.application.math.MathConfigPort.OwnedGame;
import com.novacasino.application.math.exception.ConfigAlreadyActiveException;
import com.novacasino.application.math.exception.ConfigNotFoundException;
import com.novacasino.application.math.validation.ConfigValidationException;
import com.novacasino.application.math.validation.ConfigValidator;
import com.novacasino.common.dto.ConfigCreatedDto;
import com.novacasino.common.dto.ConfigDetailDto;
import com.novacasino.common.dto.ConfigVersionDto;
import com.novacasino.common.dto.MathGameDto;
import com.novacasino.common.dto.PublishResultDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Math backoffice use cases (HU-17): querying games/versions, publishing (activating) a version and
 * creating a new immutable version with invariant validation (§3.3.3). Ownership and publish rules
 * are enforced here; persistence and JSON live in the adapter.
 */
public class MathConfigUseCase {

    private static final Logger log = LoggerFactory.getLogger(MathConfigUseCase.class);

    private final MathConfigPort port;
    private final ConfigValidator validator;

    public MathConfigUseCase(final MathConfigPort port, final ConfigValidator validator) {
        this.port = port;
        this.validator = validator;
    }

    @Transactional
    public List<MathGameDto> listGames(final Long operatorId) {
        return port.listGames(operatorId);
    }

    @Transactional
    public ConfigDetailDto getConfig(final Long configId) {
        return port.getConfig(configId).orElseThrow(() -> new ConfigNotFoundException(configId));
    }

    @Transactional
    public List<ConfigVersionDto> listConfigs(final Long gameId, final Long operatorId) {
        final OwnedGame game = ownedGame(gameId, operatorId);
        return port.listConfigs(gameId, game.activeConfigId());
    }

    @Transactional
    public PublishResultDto publish(final Long gameId, final Long configId,
                                    final Long operatorId, final Long mathUserId) {
        final OwnedGame game = ownedGame(gameId, operatorId);
        final ConfigRef config = port.findConfig(configId)
                .filter(c -> c.gameId().equals(gameId))
                .orElseThrow(() -> new ConfigNotFoundException(configId));
        if (configId.equals(game.activeConfigId())) {
            throw new ConfigAlreadyActiveException(configId);
        }
        final PublishResultDto result = port.applyPublish(gameId, config.id(), operatorId, mathUserId);
        log.info("Math version published: gameId={}, configId={}, version={}, operatorId={}, by userId={}",
                gameId, result.activeConfigId(), result.version(), operatorId, mathUserId);
        return result;
    }

    @Transactional
    public ConfigCreatedDto createConfig(final Long gameId, final Long operatorId,
                                         final Long mathUserId, final CreateConfigCommand cmd) {
        ownedGame(gameId, operatorId);
        validator.validate(cmd.config());

        final BigDecimal rtp = cmd.rtpTarget();
        if (rtp == null || rtp.signum() < 0 || rtp.compareTo(BigDecimal.ONE) > 0) {
            throw new ConfigValidationException(List.of(
                    new ConfigValidationException.FieldError("rtpTarget", "must be between 0 and 1")));
        }
        final ConfigCreatedDto created = port.createConfig(gameId, mathUserId, cmd);
        log.info("Math version created: gameId={}, configId={}, version={}, by userId={}",
                gameId, created.id(), created.version(), mathUserId);
        return created;
    }

    private OwnedGame ownedGame(final Long gameId, final Long operatorId) {
        return port.findOwnedGame(gameId, operatorId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
