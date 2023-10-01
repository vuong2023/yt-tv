package com.liskovsoft.youtubeapi.auth.V2;

import com.liskovsoft.youtubeapi.app.AppService;
import com.liskovsoft.youtubeapi.auth.models.auth.AccessToken;
import com.liskovsoft.youtubeapi.auth.models.auth.RefreshToken;
import com.liskovsoft.youtubeapi.auth.models.auth.UserCode;
import com.liskovsoft.youtubeapi.auth.models.info.AccountInt;
import com.liskovsoft.youtubeapi.auth.models.info.AccountsList;
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.common.helpers.RetrofitOkHttpHelper;
import com.liskovsoft.youtubeapi.common.helpers.tests.TestHelpersV2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AuthApiTest {
    private static final String RAW_JSON_AUTH_DATA = "{\"client_id\":\"861556708454-d6dlm3lh05idd8npek18k6be8ba3oc68.apps.googleusercontent.com\"," +
            "\"client_secret\":\"SboVhoG9s0rNafixCSGGKXAT\"," +
            "\"refresh_token\":\"1//0cXvGwadlFQ4ZCgYIARAAGAwSNwF-L9IrTZKtg_17mTcwUBMsJiSHXTnjWiW6A9Fddq9sHGfKZRIbKSh-7KgJ22ChDOTDtkbsmvU\"," +
            "\"grant_type\":\"refresh_token\"}";
    private static final String CLIENT_ID = "861556708454-d6dlm3lh05idd8npek18k6be8ba3oc68.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "SboVhoG9s0rNafixCSGGKXAT";
    private static final String REFRESH_TOKEN = "1//0cXvGwadlFQ4ZCgYIARAAGAwSNwF-L9IrTZKtg_17mTcwUBMsJiSHXTnjWiW6A9Fddq9sHGfKZRIbKSh-7KgJ22ChDOTDtkbsmvU";
    private AuthApi mService;
    private AppService mAppService;

    @Before
    public void setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        ShadowLog.stream = System.out; // catch Log class output

        mService = RetrofitHelper.withJsonPath(AuthApi.class);
        mAppService = AppService.instance();

        RetrofitOkHttpHelper.getAuthHeaders().put("Authorization", TestHelpersV2.getAuthorization());
    }
    
    @Test
    public void testThatUserIsAuthenticated() throws IOException {
        Call<AccessToken> wrapper = mService.getAccessToken(RAW_JSON_AUTH_DATA);

        Response<AccessToken> execute = wrapper.execute();

        AccessToken token = execute.body();

        assertEquals("Auth type Bearer", "Bearer", token.getTokenType());
        assertTrue("Token not null", token.getAccessToken().length() > 50);
    }
    
    @Test
    public void testThatUserCanRefreshToken() throws IOException {
        Call<AccessToken> wrapper = mService.getAccessToken(
                AuthApiHelper.getAccessTokenQuery(REFRESH_TOKEN, CLIENT_ID, CLIENT_SECRET)
        );

        Response<AccessToken> execute = wrapper.execute();

        AccessToken token = execute.body();

        assertEquals("Auth type Bearer", "Bearer", token.getTokenType());
        assertTrue("Token not null", token.getAccessToken().length() > 50);
    }

    @Test
    public void testThatUserCodeFieldsNotEmpty() throws IOException {
        UserCode code = getUserCode();
        assertTrue(notEmpty(code.getUserCode()));
        assertTrue(notEmpty(code.getDeviceCode()));
    }

    @Test
    public void testThatUserStillNotSignedIn() throws IOException {
        RefreshToken token = getAccessToken();
        assertEquals("authorization_pending", token.getError());
    }

    @Test
    public void testThatAccountsListNotEmpty() {
        Call<AccountsList> wrapper = mService.getAccountsList(AuthApiHelper.getAccountsListQuery());

        AccountsList accountsList = RetrofitHelper.get(wrapper);

        AccountInt firstAccount = accountsList.getAccounts().get(0);

        assertNotNull("Contains Name", firstAccount.getName());
        assertNotNull("Contains Thumbnails", firstAccount.getThumbnails());
        assertTrue("Is selected", firstAccount.isSelected());
    }

    private boolean notEmpty(String userCode) {
        System.out.println("Important code is: " + userCode);
        return userCode != null && userCode.length() > 5;
    }

    private UserCode getUserCode() throws IOException {
        Call<UserCode> userCode = mService.getUserCode(AuthApiHelper.getUserCodeQuery(mAppService.getClientId()));
        Response<UserCode> response = userCode.execute();
        return response.body();
    }

    private RefreshToken getAccessToken() throws IOException {
        UserCode userCode = getUserCode();
        System.out.println("The user code is: " + userCode.getUserCode());

        Call<RefreshToken> token = mService.getRefreshToken(AuthApiHelper.getRefreshTokenQuery(userCode.getDeviceCode(), mAppService.getClientId(), mAppService.getClientSecret()));
        Response<RefreshToken> response = token.execute();
        return response.body();
    }
}