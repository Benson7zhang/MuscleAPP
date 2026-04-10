package com.musclefit.app.auth;

public class AuthState {
    public final boolean loggedIn;
    public final String accountId;
    public final String nickname;
    public final String gender;
    public final String phone;
    public final String weightKg;
    public final String heightCm;
    public final String birthDate;
    public final AuthRole role;

    public AuthState(
            boolean loggedIn,
            String accountId,
            String nickname,
            String gender,
            String phone,
            String weightKg,
            String heightCm,
            String birthDate,
            AuthRole role
    ) {
        this.loggedIn = loggedIn;
        this.accountId = accountId;
        this.nickname = nickname;
        this.gender = gender;
        this.phone = phone;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.birthDate = birthDate;
        this.role = role;
    }

    public static AuthState guest() {
        return new AuthState(false, "", "", "", "", "", "", "", AuthRole.GUEST);
    }
}
