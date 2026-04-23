package com.kunal.healthkriya.data.model.auth;

import com.kunal.healthkriya.data.model.UserModel;

public class AuthResult {

    private final UserModel user;
    private final String errorCode;
    private final String message;

    private AuthResult(UserModel user, String errorCode, String message) {
        this.user = user;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static AuthResult success(UserModel user) {
        return new AuthResult(user, null, null);
    }

    public static AuthResult error(String errorCode, String message) {
        return new AuthResult(null, errorCode, message);
    }

    public boolean isSuccess() {
        return user != null;
    }

    public UserModel getUser() {
        return user;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
