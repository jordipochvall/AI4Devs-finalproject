package com.novacasino.common.dto;

/** Framework-agnostic pagination request passed from the web layer to application ports. */
public record PageRequestDto(int page, int size) {
}
