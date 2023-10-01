package com.liskovsoft.youtubeapi.app;

import com.liskovsoft.youtubeapi.common.helpers.DefaultHeaders;
import com.liskovsoft.youtubeapi.app.models.AppInfo;
import com.liskovsoft.youtubeapi.app.models.PlayerData;
import com.liskovsoft.youtubeapi.app.models.clientdata.ClientData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AppApiTest {
    private AppApiWrapper mAppApiWrapper;

    @Before
    public void setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        ShadowLog.stream = System.out; // catch Log class output

        mAppApiWrapper = new AppApiWrapper();
    }

    @Test
    public void testThatAppInfoContainsAllRequiredFields() throws IOException {
        String playerUrl = getPlayerUrl(DefaultHeaders.USER_AGENT_TV);
        assertTrue("Player url should ends with js", playerUrl.endsWith(".js"));
    }

    @Test
    public void testThatDecipherFunctionIsValid() {
        String playerUrl = getPlayerUrl(DefaultHeaders.USER_AGENT_TV);

        PlayerData playerData = mAppApiWrapper.getPlayerData(playerUrl);

        assertNotNull("Decipher result not null", playerData);

        String decipherFunctionContent = playerData.getDecipherFunction();
        assertNotNull("Decipher function not null", decipherFunctionContent);
        assertFalse("Decipher function is not empty", decipherFunctionContent.isEmpty());
        assertTrue("Decipher function has proper content",
                decipherFunctionContent.startsWith(";var ") && decipherFunctionContent.contains("function ") &&
                        decipherFunctionContent.endsWith(".join(\"\")}"));
    }

    @Test
    public void testThatPlaybackNonceFunctionIsValid() {
        String playerUrl = getPlayerUrl(DefaultHeaders.USER_AGENT_TV);

        PlayerData clientPlaybackNonceFunction = mAppApiWrapper.getPlayerData(playerUrl);

        assertNotNull("Playback nonce result not null", clientPlaybackNonceFunction);

        String playbackNonceFunctionContent = clientPlaybackNonceFunction.getClientPlaybackNonceFunction();
        assertNotNull("Playback nonce function not null", playbackNonceFunctionContent);
        assertFalse("Playback nonce function not empty", playbackNonceFunctionContent.isEmpty());
        assertTrue("Playback nonce has valid content", playbackNonceFunctionContent.startsWith("function ") &&
                playbackNonceFunctionContent.contains("function getClientPlaybackNonce") && playbackNonceFunctionContent.endsWith("}"));
    }

    @Test
    public void testThrottleFunctionIsValid() {
        String playerUrl = getPlayerUrl(DefaultHeaders.USER_AGENT_TV);

        PlayerData playerData = mAppApiWrapper.getPlayerData(playerUrl);

        assertNotNull("PlayerData not null", playerData);

        String throttleFunction = playerData.getThrottleFunction();
        assertNotNull("Throttle function not null", throttleFunction);
        assertFalse("Throttle function not empty", throttleFunction.isEmpty());
        assertTrue("Throttle function has valid content", throttleFunction.startsWith("function throttleSignature")
                && throttleFunction.endsWith(".join(\"\")}"));
    }

    @Test
    public void testThatClientIdAndSecretNotEmpty() {
        testThatClientIdAndSecretNotEmpty(DefaultHeaders.USER_AGENT_TV);
    }

    public void testThatClientIdAndSecretNotEmpty(String userAgent) {
        String baseUrl = getBaseUrl(userAgent);

        ClientData baseData = mAppApiWrapper.getBaseData(baseUrl);

        assertNotNull("Base data not null", baseData);

        assertNotNull("Client id not empty", baseData.getClientId());
        assertNotNull("Client secret not empty", baseData.getClientSecret());
    }

    private String getPlayerUrl(String userAgent) {
        AppInfo appInfo = mAppApiWrapper.getAppInfo(userAgent);

        assertNotNull("AppInfo not null", appInfo);

        String playerUrl = appInfo.getPlayerUrl();

        assertNotNull("Player url not null", playerUrl);

        return playerUrl;
    }

    private String getBaseUrl(String userAgent) {
        AppInfo appInfo = mAppApiWrapper.getAppInfo(userAgent);

        assertNotNull("AppInfo not null", appInfo);

        String baseUrl = appInfo.getBaseUrl();

        assertNotNull("Base url not null", baseUrl);

        return baseUrl;
    }
}