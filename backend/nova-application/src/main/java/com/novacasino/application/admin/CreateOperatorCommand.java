package com.novacasino.application.admin;

/** Command to onboard a new operator and its initial OPERATOR user (HU-25). */
public record CreateOperatorCommand(String code, String name, String operatorEmail, String operatorPassword) {
}
