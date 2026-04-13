package com.musclefit.app.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AuthManager {
    public static final String ADMIN_ACCOUNT_ID = "000001";

    private static final PresetAccount[] PRESET_ACCOUNTS = new PresetAccount[]{
            new PresetAccount("000001", "admin123", "管理员", AuthRole.ADMIN),
            new PresetAccount("000002", "user002", "训练新手", AuthRole.USER),
            new PresetAccount("000003", "user003", "核心进阶", AuthRole.USER),
            new PresetAccount("000004", "user004", "腿部强化", AuthRole.USER)
    };

    private static final String PREF = "musclefit_auth";
    private static final String KEY_CURRENT_ACCOUNT_ID = "current_account_id";
    private static final String KEY_ACCOUNTS_JSON = "accounts_json";
    private static final String KEY_PRESET_PASSWORD_VERSION = "preset_password_version";
    private static final int PRESET_PASSWORD_VERSION = 2;

    private static volatile AuthManager INSTANCE;

    private final SharedPreferences preferences;
    private final MutableLiveData<AuthState> state = new MutableLiveData<>(AuthState.guest());
    private final Object accountLock = new Object();
    private final SecureRandom secureRandom = new SecureRandom();

    public static final class AuthActionResult {
        public final boolean success;
        public final String message;
        public final String accountId;

        private AuthActionResult(boolean success, String message, String accountId) {
            this.success = success;
            this.message = message == null ? "" : message;
            this.accountId = accountId == null ? "" : accountId;
        }

        public static AuthActionResult success(String message, String accountId) {
            return new AuthActionResult(true, message, accountId);
        }

        public static AuthActionResult fail(String message) {
            return new AuthActionResult(false, message, "");
        }
    }

    private static final class LocalAccount {
        final String accountId;
        final String passwordHash;
        final String nickname;
        final String gender;
        final String phone;
        final String weightKg;
        final String heightCm;
        final String birthDate;
        final AuthRole role;

        private LocalAccount(
                String accountId,
                String passwordHash,
                String nickname,
                String gender,
                String phone,
                String weightKg,
                String heightCm,
                String birthDate,
                AuthRole role
        ) {
            this.accountId = accountId;
            this.passwordHash = passwordHash;
            this.nickname = nickname;
            this.gender = gender;
            this.phone = phone;
            this.weightKg = weightKg;
            this.heightCm = heightCm;
            this.birthDate = birthDate;
            this.role = role == null ? AuthRole.USER : role;
        }
    }

    private static final class PresetAccount {
        final String accountId;
        final String rawPassword;
        final String defaultNickname;
        final AuthRole role;

        private PresetAccount(String accountId, String rawPassword, String defaultNickname, AuthRole role) {
            this.accountId = accountId;
            this.rawPassword = rawPassword;
            this.defaultNickname = defaultNickname;
            this.role = role;
        }
    }

    private AuthManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        synchronized (accountLock) {
            ensurePresetAccountsLocked();
            clearCurrentSessionOnLaunchLocked();
            state.setValue(AuthState.guest());
        }
    }

    public static AuthManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AuthManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<AuthState> observe() {
        return state;
    }

    public AuthState getCurrent() {
        AuthState current = state.getValue();
        return current == null ? AuthState.guest() : current;
    }

    public boolean isLoggedIn() {
        return getCurrent().loggedIn;
    }

    public AuthActionResult login(String accountId, String password) {
        String safeId = accountId == null ? "" : accountId.trim();
        String safePassword = password == null ? "" : password.trim();
        if (safeId.isEmpty() || safePassword.isEmpty()) {
            return AuthActionResult.fail("请输入账号ID或昵称和密码");
        }

        synchronized (accountLock) {
            List<LocalAccount> accounts = readAccountsLocked();
            LocalAccount account = findAccountByIdentifier(accounts, safeId);
            if (account == null) {
                return AuthActionResult.fail("账号不存在");
            }
            String hash = hashPassword(safePassword);
            if (!account.passwordHash.equals(hash)) {
                return AuthActionResult.fail("密码错误");
            }

            preferences.edit().putString(KEY_CURRENT_ACCOUNT_ID, account.accountId).apply();
            state.setValue(toAuthState(account));
            return AuthActionResult.success("登录成功", account.accountId);
        }
    }

    public AuthActionResult register(String nickname, String password) {
        String safeNickname = nickname == null ? "" : nickname.trim();
        String safePassword = password == null ? "" : password.trim();

        if (safePassword.length() < 6) {
            return AuthActionResult.fail("密码至少6位");
        }

        synchronized (accountLock) {
            List<LocalAccount> accounts = readAccountsLocked();
            String generatedId = generateUniqueUserIdLocked(accounts);
            if (safeNickname.isEmpty()) {
                safeNickname = "用户" + generatedId;
            }
            if (nicknameExistsLocked(accounts, safeNickname, null)) {
                return AuthActionResult.fail("昵称已被占用，请更换一个");
            }
            LocalAccount account = new LocalAccount(
                    generatedId,
                    hashPassword(safePassword),
                    safeNickname,
                    "",
                    "",
                    "",
                    "",
                    "",
                    AuthRole.USER
            );
            accounts.add(account);
            saveAccountsLocked(accounts);
            return AuthActionResult.success("注册成功", generatedId);
        }
    }

    public AuthActionResult updateNickname(String nickname) {
        return updateProfile(
                nickname,
                getCurrent().gender,
                getCurrent().phone,
                getCurrent().weightKg,
                getCurrent().heightCm,
                getCurrent().birthDate
        );
    }

    public AuthActionResult updateProfile(
            String nickname,
            String gender,
            String phone,
            String weightKg,
            String heightCm,
            String birthDate
    ) {
        String safeNickname = nickname == null ? "" : nickname.trim();
        if (safeNickname.isEmpty()) {
            return AuthActionResult.fail("昵称不能为空");
        }
        String safeGender = sanitizeGender(gender);
        String safePhone = sanitizePhone(phone);
        if (safePhone == null) {
            return AuthActionResult.fail("手机号格式不正确");
        }
        String safeWeight = sanitizeWeight(weightKg);
        if (safeWeight == null) {
            return AuthActionResult.fail("体重格式不正确");
        }
        String safeHeight = sanitizeHeight(heightCm);
        if (safeHeight == null) {
            return AuthActionResult.fail("身高格式不正确");
        }
        String safeBirth = sanitizeBirthDate(birthDate);
        if (safeBirth == null) {
            return AuthActionResult.fail("出生日期格式应为 YYYY-MM-DD");
        }

        synchronized (accountLock) {
            String currentId = preferences.getString(KEY_CURRENT_ACCOUNT_ID, "");
            if (currentId == null || currentId.trim().isEmpty()) {
                return AuthActionResult.fail("请先登录");
            }

            List<LocalAccount> accounts = readAccountsLocked();
            if (nicknameExistsLocked(accounts, safeNickname, currentId)) {
                return AuthActionResult.fail("昵称已被占用，请更换一个");
            }
            List<LocalAccount> updated = new ArrayList<>(accounts.size());
            LocalAccount target = null;
            for (LocalAccount account : accounts) {
                if (currentId.equals(account.accountId)) {
                    target = new LocalAccount(
                            account.accountId,
                            account.passwordHash,
                            safeNickname,
                            safeGender,
                            safePhone,
                            safeWeight,
                            safeHeight,
                            safeBirth,
                            account.role
                    );
                    updated.add(target);
                } else {
                    updated.add(account);
                }
            }

            if (target == null) {
                preferences.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply();
                state.setValue(AuthState.guest());
                return AuthActionResult.fail("当前账号不存在");
            }

            saveAccountsLocked(updated);
            state.setValue(toAuthState(target));
            return AuthActionResult.success("资料已更新", target.accountId);
        }
    }

    public AuthActionResult changePassword(String oldPassword, String newPassword, String confirmPassword) {
        String safeOld = oldPassword == null ? "" : oldPassword.trim();
        String safeNew = newPassword == null ? "" : newPassword.trim();
        String safeConfirm = confirmPassword == null ? "" : confirmPassword.trim();

        if (safeOld.isEmpty() || safeNew.isEmpty() || safeConfirm.isEmpty()) {
            return AuthActionResult.fail("请填写完整密码信息");
        }
        if (safeNew.length() < 6) {
            return AuthActionResult.fail("新密码至少6位");
        }
        if (!safeNew.equals(safeConfirm)) {
            return AuthActionResult.fail("两次输入的新密码不一致");
        }

        synchronized (accountLock) {
            String currentId = preferences.getString(KEY_CURRENT_ACCOUNT_ID, "");
            if (currentId == null || currentId.trim().isEmpty()) {
                return AuthActionResult.fail("请先登录");
            }

            List<LocalAccount> accounts = readAccountsLocked();
            List<LocalAccount> updated = new ArrayList<>(accounts.size());
            LocalAccount current = null;
            for (LocalAccount account : accounts) {
                if (currentId.equals(account.accountId)) {
                    current = account;
                    break;
                }
            }
            if (current == null) {
                preferences.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply();
                state.setValue(AuthState.guest());
                return AuthActionResult.fail("当前账号不存在");
            }

            String oldHash = hashPassword(safeOld);
            if (!oldHash.equals(current.passwordHash)) {
                return AuthActionResult.fail("旧密码不正确");
            }

            String newHash = hashPassword(safeNew);
            if (newHash.equals(current.passwordHash)) {
                return AuthActionResult.fail("新密码不能与旧密码一致");
            }

            LocalAccount updatedCurrent = null;
            for (LocalAccount account : accounts) {
                if (currentId.equals(account.accountId)) {
                    updatedCurrent = new LocalAccount(
                            account.accountId,
                            newHash,
                            account.nickname,
                            account.gender,
                            account.phone,
                            account.weightKg,
                            account.heightCm,
                            account.birthDate,
                            account.role
                    );
                    updated.add(updatedCurrent);
                } else {
                    updated.add(account);
                }
            }

            saveAccountsLocked(updated);
            state.setValue(toAuthState(updatedCurrent));
            return AuthActionResult.success("密码已更新", currentId);
        }
    }

    public void logout() {
        preferences.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply();
        state.setValue(AuthState.guest());
    }

    private AuthState readFromPrefsLocked() {
        List<LocalAccount> accounts = readAccountsLocked();
        String currentId = preferences.getString(KEY_CURRENT_ACCOUNT_ID, "");
        if (currentId == null || currentId.trim().isEmpty()) {
            return AuthState.guest();
        }
        LocalAccount account = findAccountById(accounts, currentId.trim());
        if (account == null) {
            preferences.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply();
            return AuthState.guest();
        }
        return toAuthState(account);
    }

    private void clearCurrentSessionOnLaunchLocked() {
        String currentId = preferences.getString(KEY_CURRENT_ACCOUNT_ID, "");
        if (currentId == null || currentId.trim().isEmpty()) {
            return;
        }
        preferences.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply();
    }

    private void ensurePresetAccountsLocked() {
        int storedPresetPasswordVersion = preferences.getInt(KEY_PRESET_PASSWORD_VERSION, 0);
        boolean forceResetPresetPasswords = storedPresetPasswordVersion < PRESET_PASSWORD_VERSION;

        List<LocalAccount> accounts = readAccountsLocked();
        List<LocalAccount> updated = new ArrayList<>(accounts.size() + PRESET_ACCOUNTS.length);
        Set<String> mergedIds = new HashSet<>();

        for (LocalAccount account : accounts) {
            if (!mergedIds.add(account.accountId)) {
                continue;
            }
            PresetAccount preset = findPresetById(account.accountId);
            if (preset == null) {
                updated.add(account);
                continue;
            }

            String nickname = account.nickname == null ? "" : account.nickname.trim();
            if (nickname.isEmpty()) {
                nickname = preset.defaultNickname;
            }
            String passwordHash = account.passwordHash == null ? "" : account.passwordHash.trim();
            if (forceResetPresetPasswords || passwordHash.isEmpty()) {
                passwordHash = hashPassword(preset.rawPassword);
            }
            String gender = safeText(account.gender);
            String phone = safeText(account.phone);
            String weightKg = safeText(account.weightKg);
            String heightCm = safeText(account.heightCm);
            String birthDate = safeText(account.birthDate);
            updated.add(new LocalAccount(
                    preset.accountId,
                    passwordHash,
                    nickname,
                    gender,
                    phone,
                    weightKg,
                    heightCm,
                    birthDate,
                    preset.role
            ));
        }

        for (PresetAccount preset : PRESET_ACCOUNTS) {
            if (mergedIds.contains(preset.accountId)) {
                continue;
            }
            mergedIds.add(preset.accountId);
            updated.add(new LocalAccount(
                    preset.accountId,
                    hashPassword(preset.rawPassword),
                    preset.defaultNickname,
                    "",
                    "",
                    "",
                    "",
                    "",
                    preset.role
            ));
        }

        saveAccountsLocked(updated);
        if (forceResetPresetPasswords) {
            preferences.edit().putInt(KEY_PRESET_PASSWORD_VERSION, PRESET_PASSWORD_VERSION).apply();
        }
    }

    private static PresetAccount findPresetById(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return null;
        }
        for (PresetAccount preset : PRESET_ACCOUNTS) {
            if (preset.accountId.equals(accountId)) {
                return preset;
            }
        }
        return null;
    }

    private List<LocalAccount> readAccountsLocked() {
        String raw = preferences.getString(KEY_ACCOUNTS_JSON, "[]");
        List<LocalAccount> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return list;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }
                String accountId = obj.optString("id", "").trim();
                String passwordHash = obj.optString("passwordHash", "").trim();
                String nickname = obj.optString("nickname", "").trim();
                AuthRole role = AuthRole.fromName(obj.optString("role", AuthRole.USER.name()));
                String gender = sanitizeGender(obj.optString("gender", ""));
                String phone = safeText(obj.optString("phone", ""));
                String weightKg = safeText(obj.optString("weightKg", ""));
                String heightCm = safeText(obj.optString("heightCm", ""));
                String birthDate = safeText(obj.optString("birthDate", ""));
                if (!isSixDigitId(accountId) || passwordHash.isEmpty()) {
                    continue;
                }
                if (nickname.isEmpty()) {
                    nickname = "用户" + accountId;
                }
                list.add(new LocalAccount(
                        accountId,
                        passwordHash,
                        nickname,
                        gender,
                        phone,
                        weightKg,
                        heightCm,
                        birthDate,
                        role
                ));
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        return list;
    }

    private void saveAccountsLocked(List<LocalAccount> accounts) {
        JSONArray array = new JSONArray();
        for (LocalAccount account : accounts) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", account.accountId);
                obj.put("passwordHash", account.passwordHash);
                obj.put("nickname", account.nickname);
                obj.put("gender", account.gender);
                obj.put("phone", account.phone);
                obj.put("weightKg", account.weightKg);
                obj.put("heightCm", account.heightCm);
                obj.put("birthDate", account.birthDate);
                obj.put("role", account.role.name());
                array.put(obj);
            } catch (Exception ignored) {
            }
        }
        preferences.edit().putString(KEY_ACCOUNTS_JSON, array.toString()).apply();
    }

    private String generateUniqueUserIdLocked(List<LocalAccount> accounts) {
        Set<String> existing = new HashSet<>();
        for (LocalAccount account : accounts) {
            existing.add(account.accountId);
        }

        for (int i = 0; i < 2000; i++) {
            int value = secureRandom.nextInt(1_000_000);
            String candidate = String.format(Locale.US, "%06d", value);
            if ("000000".equals(candidate) || ADMIN_ACCOUNT_ID.equals(candidate)) {
                continue;
            }
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }

        for (int value = 2; value <= 999_999; value++) {
            String candidate = String.format(Locale.US, "%06d", value);
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("no_available_account_id");
    }

    private static LocalAccount findAccountById(List<LocalAccount> accounts, String accountId) {
        if (accountId == null) {
            return null;
        }
        for (LocalAccount account : accounts) {
            if (accountId.equals(account.accountId)) {
                return account;
            }
        }
        return null;
    }

    private static LocalAccount findAccountByIdentifier(List<LocalAccount> accounts, String input) {
        if (input == null) {
            return null;
        }
        String normalizedInput = input.trim();
        if (normalizedInput.isEmpty()) {
            return null;
        }

        LocalAccount accountById = findAccountById(accounts, normalizedInput);
        if (accountById != null) {
            return accountById;
        }

        String lookupNickname = normalizeNickname(normalizedInput);
        for (LocalAccount account : accounts) {
            if (lookupNickname.equals(normalizeNickname(account.nickname))) {
                return account;
            }
        }
        return null;
    }

    private static boolean nicknameExistsLocked(List<LocalAccount> accounts, String nickname, String excludeAccountId) {
        String normalizedNickname = normalizeNickname(nickname);
        if (normalizedNickname.isEmpty()) {
            return false;
        }
        for (LocalAccount account : accounts) {
            if (excludeAccountId != null && excludeAccountId.equals(account.accountId)) {
                continue;
            }
            if (normalizedNickname.equals(normalizeNickname(account.nickname))) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeNickname(String nickname) {
        if (nickname == null) {
            return "";
        }
        return nickname.trim().toLowerCase(Locale.ROOT);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                sb.append(String.format(Locale.US, "%02x", value));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isSixDigitId(String accountId) {
        if (accountId == null || accountId.length() != 6) {
            return false;
        }
        for (int i = 0; i < accountId.length(); i++) {
            char c = accountId.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static AuthState toAuthState(LocalAccount account) {
        return new AuthState(
                true,
                account.accountId,
                account.nickname,
                account.gender,
                account.phone,
                account.weightKg,
                account.heightCm,
                account.birthDate,
                account.role
        );
    }

    private static String safeText(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    private static String sanitizeGender(String raw) {
        String value = safeText(raw);
        if (value.length() > 8) {
            return value.substring(0, 8);
        }
        return value;
    }

    private static String sanitizePhone(String raw) {
        String value = safeText(raw);
        if (value.isEmpty()) {
            return "";
        }
        if (value.length() < 6 || value.length() > 20) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= '0' && c <= '9') || c == '+' || c == '-' || c == ' ') {
                continue;
            }
            return null;
        }
        return value;
    }

    private static String sanitizeWeight(String raw) {
        String value = safeText(raw);
        if (value.isEmpty()) {
            return "";
        }
        try {
            float num = Float.parseFloat(value);
            if (num <= 0f || num > 500f) {
                return null;
            }
            return String.format(Locale.US, "%.1f", num);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String sanitizeHeight(String raw) {
        String value = safeText(raw);
        if (value.isEmpty()) {
            return "";
        }
        try {
            float num = Float.parseFloat(value);
            if (num <= 0f || num > 300f) {
                return null;
            }
            return String.format(Locale.US, "%.1f", num);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String sanitizeBirthDate(String raw) {
        String value = safeText(raw);
        if (value.isEmpty()) {
            return "";
        }
        if (!value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return null;
        }
        return value;
    }
}
