package com.musclefit.app.ui.assistant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NutritionParserRealismTest {

    @Test
    public void parseCalorie_extremeNumbers_areClampedToRealisticBand() {
        String raw = "BMR: 500 kcal\n"
                + "TDEE: 7000 kcal\n"
                + "建议热量: 800~6000 kcal";

        NutritionCalorieParser.CalorieVisualData data = NutritionCalorieParser.parse(raw);

        assertNotNull(data);
        assertNotNull(data.bmr);
        assertNotNull(data.tdee);
        assertNotNull(data.target);

        assertEquals(900, data.bmr.value);
        assertEquals(1845, data.tdee.value);
        assertEquals(2214, data.target.value);
        assertTrue(data.target.value >= (int) Math.round(data.bmr.value * 0.9d));
        assertTrue(data.target.value <= (int) Math.round(data.tdee.value * 1.2d));
    }

    @Test
    public void parseCalorie_invalidBmrTdeeRelationship_isAdjusted() {
        String raw = "基础代谢 BMR 2200 kcal\n"
                + "维持热量 TDEE 1800 kcal\n"
                + "目标热量 5000 kcal";

        NutritionCalorieParser.CalorieVisualData data = NutritionCalorieParser.parse(raw);

        assertNotNull(data);
        assertNotNull(data.bmr);
        assertNotNull(data.tdee);
        assertNotNull(data.target);

        assertEquals(2200, data.bmr.value);
        assertEquals(2640, data.tdee.value);
        assertEquals(3168, data.target.value);
    }

    @Test
    public void parseMacro_outlierRatios_areRebalancedToHundred() {
        String raw = "蛋白质 80%\n"
                + "碳水 10%\n"
                + "脂肪 10%";

        NutritionMacroParser.MacroVisualData data = NutritionMacroParser.parse(raw);

        assertNotNull(data);
        assertNotNull(data.protein);
        assertNotNull(data.carb);
        assertNotNull(data.fat);

        assertEquals(45, data.protein.percent);
        assertEquals(40, data.carb.percent);
        assertEquals(15, data.fat.percent);
        assertEquals(100, data.sumPercent());
    }

    @Test
    public void parseMacro_highSumRatios_stayWithinBandsAndHundred() {
        String raw = "蛋白质 45%\n"
                + "碳水 65%\n"
                + "脂肪 40%";

        NutritionMacroParser.MacroVisualData data = NutritionMacroParser.parse(raw);

        assertNotNull(data);
        assertNotNull(data.protein);
        assertNotNull(data.carb);
        assertNotNull(data.fat);

        assertEquals(100, data.sumPercent());
        assertTrue(data.protein.percent >= 15 && data.protein.percent <= 45);
        assertTrue(data.carb.percent >= 20 && data.carb.percent <= 65);
        assertTrue(data.fat.percent >= 15 && data.fat.percent <= 40);
    }
}
