package com.musclefit.app.auth;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static org.junit.Assert.assertNotEquals;

public class AuthManagerSecurityTest {

    @Test
    public void presetAccounts_doNotShipWithAdminRole() throws Exception {
        Field presetAccountsField = AuthManager.class.getDeclaredField("PRESET_ACCOUNTS");
        presetAccountsField.setAccessible(true);
        Object presetAccounts = presetAccountsField.get(null);

        Field roleField = null;
        int length = Array.getLength(presetAccounts);
        for (int i = 0; i < length; i++) {
            Object preset = Array.get(presetAccounts, i);
            if (roleField == null) {
                roleField = preset.getClass().getDeclaredField("role");
                roleField.setAccessible(true);
            }
            AuthRole role = (AuthRole) roleField.get(preset);
            assertNotEquals("Preset accounts must not ship with admin privileges", AuthRole.ADMIN, role);
        }
    }
}
