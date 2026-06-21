package com.novacasino.api.math.dto;

import jakarta.validation.constraints.NotNull;

/** Request to publish (activate) a specific math version of a game (HU-17). */
public record PublishRequest(@NotNull Long configId) {
}
