package com.liskovsoft.youtubeapi.notifications

import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper
import com.liskovsoft.youtubeapi.common.helpers.RetrofitOkHttpHelper
import com.liskovsoft.youtubeapi.common.helpers.tests.TestHelpersV2
import com.liskovsoft.youtubeapi.notifications.gen.NotificationsResult
import com.liskovsoft.youtubeapi.notifications.gen.getItems
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import junit.framework.Assert.assertNotNull

@RunWith(RobolectricTestRunner::class)
class NotificationsApiTest {
    /**
     * Authorization should be updated each hour
     */
    private var mService: NotificationsApi? = null

    @Before
    fun setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS")
        ShadowLog.stream = System.out // catch Log class output
        mService = RetrofitHelper.withGson(NotificationsApi::class.java)
        RetrofitOkHttpHelper.authHeaders["Authorization"] = TestHelpersV2.getAuthorization()
        RetrofitOkHttpHelper.disableCompression = true
    }

    @Test
    fun testThatNotificationsContainNeededItems() {
        val notifications: NotificationsResult? = getNotifications()

        assertNotNull("Contains content", notifications?.getItems())
    }

    private fun getNotifications(): NotificationsResult? {
        val result = mService?.getNotifications(NotificationsApiHelper.getNotificationsQuery())

        return RetrofitHelper.get(result)
    }
}