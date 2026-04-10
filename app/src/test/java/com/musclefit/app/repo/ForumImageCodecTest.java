package com.musclefit.app.repo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ForumImageCodecTest {
    @Test
    public void encodeThenDecodeRoundTrip() {
        List<String> input = Arrays.asList(
                "file:///data/user/0/com.musclefit.app/files/forum_media/a.jpg",
                "file:///data/user/0/com.musclefit.app/files/forum_media/b.jpg"
        );

        String encoded = ForumImageCodec.encode(input);
        List<String> decoded = ForumImageCodec.decode(encoded);

        assertEquals(input, decoded);
    }

    @Test
    public void decodeInvalidJsonReturnsEmpty() {
        List<String> decoded = ForumImageCodec.decode("not_json");
        assertTrue(decoded.isEmpty());
    }

    @Test
    public void encodeSkipsBlankValues() {
        String encoded = ForumImageCodec.encode(Arrays.asList("", "  ", "file:///a.jpg"));
        List<String> decoded = ForumImageCodec.decode(encoded);
        assertEquals(Collections.singletonList("file:///a.jpg"), decoded);
    }
}
