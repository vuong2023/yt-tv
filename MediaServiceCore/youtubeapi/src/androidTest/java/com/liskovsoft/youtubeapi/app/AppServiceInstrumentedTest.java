package com.liskovsoft.youtubeapi.app;

import android.Manifest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Note: Robolectric doesn't support loading native libraries (*.so)
 */
public class AppServiceInstrumentedTest {
    private AppService mAppService;

    @Rule
    public GrantPermissionRule mInternetPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET);

    @Before
    public void setUp() {
        GlobalPreferences.instance(InstrumentationRegistry.getInstrumentation().getContext());
        mAppService = AppService.instance();
    }

    /**
     * Note: Robolectric doesn't support loading native libraries (*.so)
     */
    @Test
    public void testThatItemsDecipheredCorrectly() {
        List<String> ciphered = new ArrayList<>();
        String cipher = "ADBVCGD2934FBBBBBDDDFFF";
        ciphered.add(cipher);
        ciphered.add(cipher);
        ciphered.add(cipher);

        List<String> deciphered = mAppService.decipher(ciphered);

        assertNotNull("Deciphered not null", deciphered);
        assertFalse("Deciphered not empty", deciphered.isEmpty());

        for (String decipher : deciphered) {
             assertNotEquals("Cipher and decipher not the same", decipher, cipher);
        }
    }

    @Test
    public void testPlaybackNonce() {
        String playbackNonce = mAppService.getClientPlaybackNonce();

        assertTrue("Playback nonce not empty", playbackNonce != null && !playbackNonce.isEmpty());
    }

    @Test
    public void testThrottleFunction() {
        List<String> throttled = new ArrayList<>();
        String throttleSignature = "ADBVCGD2934FBBBBBDDDFFF";
        throttled.add(throttleSignature);
        throttled.add(throttleSignature);
        throttled.add(throttleSignature);
        List<String> normalized = mAppService.throttleFix(throttled);

        assertNotNull("Throttled not null", normalized);
        assertFalse("Throttled not empty", normalized.isEmpty());

        //for (String throttle : throttled) {
        //    assertNotEquals("Throttled not the same", throttle, throttleSignature);
        //}
    }
}