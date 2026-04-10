package com.musclefit.app.ui.assistant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AiResponseCardParserTest {

    @Test
    public void parse_structuredMessage_extractsSectionsAndConfidence() {
        String raw = ""
                + "【置信等级】A\n"
                + "【资料解读】\n"
                + "资料较完整。\n"
                + "【可执行方案】\n"
                + "每周训练4天。\n"
                + "【依据说明】\n"
                + "依据超负荷渐进原则。\n"
                + "【风险与禁忌】\n"
                + "避免连续两天练同肌群。\n"
                + "【需补充信息】\n"
                + "训练目标。";

        AiResponseCardParser.ParsedAiResponse parsed = AiResponseCardParser.parse(raw);

        assertEquals("A", parsed.confidenceLevel);
        assertTrue(parsed.structured);
        assertTrue(parsed.primaryText.contains("资料解读"));
        assertTrue(parsed.primaryText.contains("可执行方案"));
        assertTrue(parsed.evidenceText.contains("超负荷渐进"));
    }

    @Test
    public void parse_missingConfidenceButGenericAssumption_fallsBackToC() {
        String raw = ""
                + "【资料解读】\n"
                + "资料不足，以下方案基于通用假设。\n"
                + "【可执行方案】\n"
                + "全身训练每周3天。";

        AiResponseCardParser.ParsedAiResponse parsed = AiResponseCardParser.parse(raw);

        assertEquals("C", parsed.confidenceLevel);
        assertTrue(parsed.structured);
    }

    @Test
    public void parse_plainText_fallsBackToBAndKeepsRawText() {
        String raw = "今天先做热身，再做下肢复合动作。";

        AiResponseCardParser.ParsedAiResponse parsed = AiResponseCardParser.parse(raw);

        assertEquals("B", parsed.confidenceLevel);
        assertEquals(raw, parsed.primaryText);
        assertEquals("", parsed.evidenceText);
        assertTrue(!parsed.structured);
    }
}
