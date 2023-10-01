package com.liskovsoft.youtubeapi.browse.v2

import com.liskovsoft.youtubeapi.browse.v1.BrowseApiHelper
import com.liskovsoft.youtubeapi.browse.v2.gen.*
import com.liskovsoft.youtubeapi.common.models.impl.mediagroup.KidsSectionMediaGroup
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper
import com.liskovsoft.youtubeapi.common.helpers.RetrofitOkHttpHelper
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper
import com.liskovsoft.youtubeapi.common.helpers.tests.TestHelpersV2
import com.liskovsoft.youtubeapi.common.models.gen.getBrowseParams
import com.liskovsoft.youtubeapi.common.models.gen.getFeedbackToken
import com.liskovsoft.youtubeapi.common.models.gen.getFeedbackToken2
import com.liskovsoft.youtubeapi.common.models.gen.isLive
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class BrowseApiTest {
    /**
     * Authorization should be updated each hour
     */
    private var mService: BrowseApi? = null

    @Before
    fun setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS")
        ShadowLog.stream = System.out // catch Log class output
        mService = RetrofitHelper.withGson(BrowseApi::class.java)
        RetrofitOkHttpHelper.authHeaders["Authorization"] = TestHelpersV2.getAuthorization()
        RetrofitOkHttpHelper.disableCompression = true
    }

    @Test
    fun testThatSubsNotEmpty() {
        val subs = getSubscriptions()

        assertNotNull("Contains videos", subs?.getItems()?.getOrNull(0))
    }

    @Test
    fun testThatSubContainsFeedbackToken() {
        val subs = getSubscriptions()

        assertNotNull("Contains feedback token", subs?.getItems()?.getOrNull(0)?.getFeedbackToken())
    }

    @Test
    fun testThatSubsCanBeContinued() {
        val subs = getSubscriptions()

        assertNotNull("Contains continuation token", subs?.getContinuationToken())

        checkContinuation(subs?.getContinuationToken())
    }

    @Test
    fun testThatHomeNotEmpty() {
        val home = getHome()

        assertNotNull("Contains videos", home?.getItems()?.getOrNull(0))
    }

    @Test
    fun testThatHomeCanBeContinued() {
        val home = getHome()

        assertNotNull("Contains continuation token", home?.getContinuationToken())

        checkContinuation(home?.getContinuationToken())
    }

    @Test
    fun testThatHomeContainsAllTokens() {
        val home = getHome()

        val item = home?.getItems()?.firstOrNull { it?.getFeedbackToken() != null }

        assertNotNull("Home contains feedback token 1", item?.getFeedbackToken())
        assertNotNull("Home contains feedback token 2", item?.getFeedbackToken2())
    }

    @Test
    fun testThatHomeContainsSections() {
        val home = getHome()

        val sections = home?.getSections()
        val tabs = home?.getTabs()
        val chips = home?.getChips()

        val section = sections?.firstOrNull()
        val tab = tabs?.firstOrNull()
        val chip = chips?.firstOrNull()

        assertTrue("Result contains either sections, tabs or chips", section != null || tab != null || chip != null)

        if (section != null) {
            assertNotNull("Section contains title", section.getTitle())
            assertNotNull("Section contains items", section.getItems()?.firstOrNull())
        }

        if (tab != null) {
            //assertNotNull("Section contains title", tab.getTitle())
            assertNotNull("Section contains items", tab.getItems()?.firstOrNull())
        }

        if (chip != null) {
            assertNotNull("Section contains title", chip.getTitle())
            assertNotNull("Section contains items", chip.getContinuationToken() != null)
        }
    }

    @Test
    fun testThatChipsCanBeContinued() {
        val home = getHome()

        val chips = home?.getChips()

        assertNotNull("Contains chips", chips)

        val chip = chips?.getOrNull(2) // first chip is empty, second one is Posts

        assertNotNull("Chip has title", chip?.getTitle())

        checkContinuation(chip?.getContinuationToken(), false) // Chips usually don't support multiple continuation
    }

    @Ignore("Doesn't contains chips")
    @Test
    fun testThatAltHomeNotEmpty() {
        val home = getAltHome()

        assertNotNull("Contains videos", home?.getItems()?.getOrNull(0))
    }

    @Test
    fun testThatKidsHomeNotEmpty() {
        val kidsHome = getKidsHome()

        assertNotNull("Contains sections", kidsHome?.getSections())
    }

    @Test
    fun testThatKidsHomeCanBeContinued() {
        val kidsHome = getKidsHome()

        kidsHome?.getSections()?.forEach {
            if (it?.getItems() == null) {
                val home = getKidsHome(it?.getBrowseParams())
                assertNotNull("Section not empty", home?.getRootSection()?.getItems())
            }
        }
    }

    @Test
    fun testKidsMediaItemConversion() {
        val kidsHome = getKidsHome()

        val mediaGroup = KidsSectionMediaGroup(kidsHome?.getRootSection()!!)

        BrowseTestHelper.checkMediaItem(mediaGroup.mediaItems?.getOrNull(0)!!)
    }

    @Test
    fun testThatGuideNotEmpty() {
        val guide = getGuide()

        assertTrue("Guide contains channels", guide?.getFirstSubs()?.isNotEmpty() == true)
        assertTrue("Guide collapse contains channels", guide?.getCollapsibleSubs()?.size ?: 0 > 20)
    }

    @Test
    fun testThatReelsNotEmpty() {
        val reel = getReel()

        testFirstReelResult(reel)
    }

    @Test
    fun testThatReelDetailsNotEmpty() {
        val reels = getReel()

        val continuation = getReelContinuation(reels?.getContinuationKey())

        testReelContinuation(continuation)

        val next = getReelContinuation(continuation?.getContinuationKey())

        testReelContinuation(next)
    }

    @Test
    fun testThatChannelVideosTabNotEmpty() {
        val videos = getChannelVideos("UC1vCu8GeDC7_UfY7PKqsAzg")

        assertTrue("Contains videos", videos?.getItems()?.size ?: 0 > 10)
        assertNotNull("Has continuation", videos?.getContinuationToken())
    }

    @Test
    fun testThatChannelLiveTabNotEmpty() {
        val videos = getChannelLive("UCjLdcql-zKjeviGljM1RMHA")

        assertTrue("Contains videos", videos?.getItems()?.filter { it?.isLive() == true }?.size ?: 0 > 1)
        assertNotNull("Has continuation", videos?.getContinuationToken())
    }

    @Test
    fun testThatTrendingNotEmpty() {
        val trending = getTrending()

        assertNotNull("Contains videos", trending?.getItems()?.getOrNull(0))

        for (tab in trending!!.getTabs()!!) {
            if (tab!!.content != null) {
                assertTrue("Root tab contains videos", tab.getItems()?.size ?: 0 > 10)
            } else {
                val tabContent = getTrendingTab(tab.endpoint?.getBrowseParams())

                assertTrue("Next tab contains videos", tabContent?.getItems()?.size ?: 0 > 10)
            }
        }
    }

    @Test
    fun testThatChannelPlaylistNotEmpty() {
        val channelId = "VLPLHxc_q5EHiHQX3VxMaUDOdM8NMSTyRjkZ"

        val videos = getChannelVideos(channelId)

        assertTrue("Playlist not empty", videos?.getItems()?.size ?: 0 > 0)
    }

    @Test
    fun testThatChannelPlaylistHasContinuation() {
        val channelId = "VLPLHxc_q5EHiHQX3VxMaUDOdM8NMSTyRjkZ"

        val videos = getChannelVideos(channelId)

        assertNotNull("Playlist has continuation", videos?.getContinuationToken())

        checkContinuation(videos?.getContinuationToken())
    }

    private fun testReelContinuation(continuation: ReelContinuationResult?) {
        val firstEntry = continuation?.getItems()?.getOrNull(0)
        val details = getReelDetails(firstEntry?.videoId, firstEntry?.params)

        assertNotNull("Contains continuation", continuation?.getContinuationKey())

        testReelWatchEndpoint(firstEntry)
        testReelResult(details)
    }

    private fun testFirstReelResult(details: ReelResult?) {
        // Not present
        assertNotNull("Contains video id", details?.getVideoId())
        assertNotNull("Contains thumbs", details?.getThumbnails())
        assertNotNull("Contains title", details?.getTitle())
        assertNotNull("Contains subtitle", details?.getSubtitle())
        assertNotNull("Contains continuation", details?.getContinuationKey())
        assertNotNull("Contains feedback", details?.getFeedbackTokens()?.firstOrNull())
    }

    private fun testReelWatchEndpoint(firstEntry: ReelWatchEndpoint?) {
        assertNotNull("Contains video id", firstEntry?.getVideoId())
        assertNotNull("Contains thumbs", firstEntry?.getThumbnails())
    }

    private fun testReelResult(details: ReelResult?) {
        // Not present
        assertNotNull("Contains title", details?.getTitle())
        assertNotNull("Contains subtitle", details?.getSubtitle())
        assertNotNull("Contains feedback", details?.getFeedbackTokens()?.firstOrNull())
    }

    private fun checkContinuation(token: String?, checkNextToken: Boolean = true) {
        val continuationResult = mService?.getContinuationResult(BrowseApiHelper.getContinuationQueryWeb(token))

        val continuation = RetrofitHelper.get(continuationResult)

        assertNotNull("Contains items", continuation?.getItems()?.getOrNull(0))

        if (checkNextToken) {
            assertNotNull("Contains next token", continuation?.getContinuationToken())
        }
    }

    private fun getSubscriptions(): BrowseResult? {
        val subsResult = mService?.getBrowseResult(BrowseApiHelper.getSubscriptionsQueryWeb())

        return RetrofitHelper.get(subsResult)
    }

    private fun getHome(): BrowseResult? {
        val homeResult = mService?.getBrowseResult(BrowseApiHelper.getHomeQueryWeb())

        return RetrofitHelper.get(homeResult)
    }

    private fun getAltHome(): BrowseResult? {
        val homeResult = mService?.getBrowseResultMobile(BrowseApiHelper.getHomeQueryMWEB())

        return RetrofitHelper.get(homeResult)
    }

    private fun getChannelVideos(channelId: String?): BrowseResult? {
        val homeResult = mService?.getBrowseResult(BrowseApiHelper.getChannelVideosQueryWeb(channelId))

        return RetrofitHelper.get(homeResult)
    }

    private fun getChannelLive(channelId: String?): BrowseResult? {
        val homeResult = mService?.getBrowseResult(BrowseApiHelper.getChannelLiveQueryWeb(channelId))

        return RetrofitHelper.get(homeResult)
    }

    private fun getGuide(): GuideResult? {
        val guideResult = mService?.getGuideResult(ServiceHelper.createQueryWeb(""))

        return RetrofitHelper.get(guideResult)
    }

    private fun getKidsHome(): BrowseResultKids? {
        val kidsResult = mService?.getBrowseResultKids(BrowseApiHelper.getKidsHomeQuery())

        return RetrofitHelper.get(kidsResult)
    }

    private fun getKidsHome(params: String?): BrowseResultKids? {
        val kidsResult = mService?.getBrowseResultKids(BrowseApiHelper.getKidsHomeQuery(params))

        return RetrofitHelper.get(kidsResult)
    }

    private fun getReel(): ReelResult? {
        val reelsResult = mService?.getReelResult(BrowseApiHelper.getReelQuery())

        return RetrofitHelper.get(reelsResult)
    }

    private fun getReelDetails(videoId: String?, params: String?): ReelResult? {
        val details = mService?.getReelResult(BrowseApiHelper.getReelDetailsQuery(videoId, params))

        return RetrofitHelper.get(details)
    }

    private fun getReelContinuation(sequenceParams: String?): ReelContinuationResult? {
        val continuation = mService?.getReelContinuationResult(BrowseApiHelper.getReelContinuationQuery(sequenceParams))

        return RetrofitHelper.get(continuation)
    }

    private fun getReelContinuation2(nextPageKey: String?): ReelContinuationResult? {
        val continuation = mService?.getReelContinuationResult(BrowseApiHelper.getReelContinuation2Query(nextPageKey))

        return RetrofitHelper.get(continuation)
    }

    private fun getTrending(): BrowseResult? {
        val trendingResult = mService?.getBrowseResult(BrowseApiHelper.getTrendingQueryWeb())

        return RetrofitHelper.get(trendingResult)
    }

    private fun getTrendingTab(params: String?): BrowseResult? {
        val trendingResult = mService?.getBrowseResult(BrowseApiHelper.getChannelQueryWeb("FEtrending", params))

        return RetrofitHelper.get(trendingResult)
    }
}