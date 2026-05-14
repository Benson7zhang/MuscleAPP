package com.musclefit.app.ui.assistant;

import com.musclefit.app.auth.AuthRole;
import com.musclefit.app.auth.AuthState;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AIAssistantServicePrivacyTest {

    @Test
    public void buildProfileContext_omitsSensitiveFieldsAndKeepsTrainingInputs() throws Exception {
        AIAssistantService service = allocateWithoutConstructor();
        Method method = AIAssistantService.class.getDeclaredMethod("buildProfileContext", AuthState.class);
        method.setAccessible(true);

        AuthState state = new AuthState(
                true,
                "000123",
                "测试用户",
                "男",
                "13812345678",
                "72",
                "178",
                "2000-05-01",
                AuthRole.USER
        );

        String promptContext = (String) method.invoke(service, state);

        assertTrue(promptContext.contains("性别: 男"));
        assertTrue(promptContext.contains("身高(cm): 178"));
        assertTrue(promptContext.contains("体重(kg): 72"));
        assertTrue(promptContext.contains("年龄: "));
        assertFalse(promptContext.contains("账号ID"));
        assertFalse(promptContext.contains("昵称"));
        assertFalse(promptContext.contains("手机号"));
        assertFalse(promptContext.contains("2000-05-01"));
        assertFalse(promptContext.contains("13812345678"));
        assertFalse(promptContext.contains("测试用户"));
        assertFalse(promptContext.contains("000123"));
    }

    private static AIAssistantService allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (AIAssistantService) allocateInstance.invoke(unsafe, AIAssistantService.class);
    }
}
