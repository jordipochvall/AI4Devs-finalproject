package com.novacasino.application.math;

import com.novacasino.common.dto.ConfigCreatedDto;
import com.novacasino.common.dto.ConfigDetailDto;
import com.novacasino.common.dto.ConfigVersionDto;
import com.novacasino.common.dto.MathGameDto;
import com.novacasino.common.dto.PublishResultDto;

import java.util.List;
import java.util.Optional;

/** Output port for math version read/publish/create (HU-17). */
public interface MathConfigPort {

    List<MathGameDto> listGames(Long operatorId);

    Optional<ConfigDetailDto> getConfig(Long configId);

    /** The game if it exists and belongs to the operator, carrying its active config id. */
    Optional<OwnedGame> findOwnedGame(Long gameId, Long operatorId);

    /** Versions of a game (newest first), flagging the active one. */
    List<ConfigVersionDto> listConfigs(Long gameId, Long activeConfigId);

    Optional<ConfigRef> findConfig(Long configId);

    /** Moves {@code games.active_config_id} to the version and records the publication. */
    PublishResultDto applyPublish(Long gameId, Long configId, Long operatorId, Long mathUserId);

    /** Persists a new (immutable) math version with the next version number. */
    ConfigCreatedDto createConfig(Long gameId, Long mathUserId, CreateConfigCommand cmd);

    /** A game owned by the operator, with its active config pointer. */
    record OwnedGame(Long activeConfigId) {}

    /** Lightweight reference to a config version. */
    record ConfigRef(Long id, Long gameId, int version) {}
}
