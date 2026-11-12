package com.staffforge.skill.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SkillNotFoundException extends RuntimeException {

    public SkillNotFoundException(String message) {
        super(message);
    }

    public SkillNotFoundException(Long id) {
        super("Skill with ID " + id + " not found");
    }
}
