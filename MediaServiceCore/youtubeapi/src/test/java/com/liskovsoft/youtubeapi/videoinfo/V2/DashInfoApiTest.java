package com.liskovsoft.youtubeapi.videoinfo.V2;

import androidx.annotation.NonNull;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.common.helpers.tests.TestHelpersV1;
import com.liskovsoft.youtubeapi.formatbuilders.utils.MediaFormatUtils;
import com.liskovsoft.youtubeapi.videoinfo.models.DashInfoUrl;
import com.liskovsoft.youtubeapi.videoinfo.models.DashInfoContent;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfo;
import com.liskovsoft.youtubeapi.videoinfo.models.formats.AdaptiveVideoFormat;
import okhttp3.Headers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;
import retrofit2.Call;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DashInfoApiTest {
    private static final String SEQ_NUM = "X-Sequence-Num";
    private static final String STREAM_DUR_MS = "X-Head-Time-Millis";
    private static final String LAST_SEG_TIME_MS = "X-Walltime-Ms";
    private DashInfoApi mService;
    private VideoInfoApi mService2;

    @Before
    public void setUp() throws Exception {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        ShadowLog.stream = System.out; // catch Log class output

        mService = RetrofitHelper.withRegExp(DashInfoApi.class);

        mService2 = RetrofitHelper.withJsonPath(VideoInfoApi.class);
    }

    @Test
    public void testDashInfoUrlNotEmpty() throws IOException {
        VideoInfo videoInfo = getVideoInfo(TestHelpersV1.VIDEO_ID_LIVE);
        Call<DashInfoUrl> dashInfoWrapper = mService.getDashInfoUrl(videoInfo.getDashManifestUrl());

        DashInfoUrl dashInfo = dashInfoWrapper.execute().body();

        assertTrue("start segment not null", dashInfo.getStartSegmentNum() > 0);
    }

    @Test
    public void testDashInfoFormatNotEmpty() throws IOException {
        VideoInfo videoInfo = getVideoInfo(TestHelpersV1.VIDEO_ID_LIVE);
        Call<DashInfoContent> dashInfoWrapper = mService.getDashInfoContent(getSmallestAudio(videoInfo).getUrl());

        DashInfoContent dashInfo = dashInfoWrapper.execute().body();

        assertTrue("start segment not null", dashInfo.getStartSegmentNum() > 0);
        assertTrue("start segment time not null", dashInfo.getStartTimeMs() > 0);
    }

    @Test
    public void testDashInfoFormat2NotEmpty() throws IOException {
        VideoInfo videoInfo = getVideoInfo(TestHelpersV1.VIDEO_ID_LIVE);
        Call<Void> dashInfoWrapper = mService.getDashInfoHeaders(getSmallestAudio(videoInfo).getUrl());

        Headers headers = dashInfoWrapper.execute().headers();
        int lastSegmentNum = Integer.parseInt(headers.get(SEQ_NUM));
        long streamDurationMs = Long.parseLong(headers.get(STREAM_DUR_MS));
        long lastSegmentTimeMs = Long.parseLong(headers.get(LAST_SEG_TIME_MS));

        assertTrue("last segment num not null", lastSegmentNum > 0);
        assertTrue("stream duration not null", streamDurationMs > 0);
        assertTrue("last segment time not null", lastSegmentTimeMs > 0);
    }

    private VideoInfo getVideoInfo(String videoId) throws IOException {
        Call<VideoInfo> wrapper = mService2.getVideoInfo(VideoInfoApiTestHelper.getVideoInfoQuery(videoId));
        return wrapper.execute().body();
    }

    @NonNull
    private AdaptiveVideoFormat getSmallestAudio(VideoInfo videoInfo) {
        AdaptiveVideoFormat format = Helpers.findFirst(videoInfo.getAdaptiveFormats(),
                item -> MediaFormatUtils.isAudio(item.getMimeType())); // smallest format

        return format;
    }
}