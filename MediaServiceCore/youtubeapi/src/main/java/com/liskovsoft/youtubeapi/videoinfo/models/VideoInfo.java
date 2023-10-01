package com.liskovsoft.youtubeapi.videoinfo.models;

import com.liskovsoft.sharedutils.helpers.DateHelper;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.querystringparser.UrlQueryString;
import com.liskovsoft.sharedutils.querystringparser.UrlQueryStringFactory;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.JsonPath;
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper;
import com.liskovsoft.youtubeapi.common.models.V2.TextItem;
import com.liskovsoft.youtubeapi.videoinfo.models.formats.AdaptiveVideoFormat;
import com.liskovsoft.youtubeapi.videoinfo.models.formats.RegularVideoFormat;

import java.util.List;
import java.util.regex.Pattern;

public class VideoInfo {
    private static final String PARAM_EVENT_ID = "ei";
    private static final String PARAM_VM = "vm";
    private static final String PARAM_OF = "of";
    private static final String STATUS_UNPLAYABLE = "UNPLAYABLE";
    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_OFFLINE = "LIVE_STREAM_OFFLINE";
    private static final String STATUS_LOGIN_REQUIRED = "LOGIN_REQUIRED";
    private static final String STATUS_AGE_CHECK_REQUIRED = "AGE_CHECK_REQUIRED";
    private static final String STATUS_CONTENT_CHECK_REQUIRED = "CONTENT_CHECK_REQUIRED";
    private static final Pattern tagPattern = Pattern.compile("\\(.*\\)$");

    @JsonPath("$.streamingData.formats[*]")
    private List<RegularVideoFormat> mRegularFormats;

    @JsonPath("$.streamingData.adaptiveFormats[*]")
    private List<AdaptiveVideoFormat> mAdaptiveFormats;

    //@JsonPath("$.playabilityStatus.paygatedQualitiesMetadata.restrictedAdaptiveFormats[*]")
    private List<AdaptiveVideoFormat> mRestrictedFormats;

    @JsonPath("$.captions.playerCaptionsTracklistRenderer.captionTracks[*]")
    private List<CaptionTrack> mCaptionTracks;

    @JsonPath("$.captions.playerCaptionsTracklistRenderer.translationLanguages[*]")
    private List<TranslationLanguage> mTranslationLanguages;

    @JsonPath("$.streamingData.hlsManifestUrl")
    private String mHlsManifestUrl;

    @JsonPath("$.playbackTracking.videostatsWatchtimeUrl.baseUrl")
    private String mVideoStatsWatchTimeUrl;

    @JsonPath("$.streamingData.dashManifestUrl")
    private String mDashManifestUrl;

    @JsonPath("$.videoDetails")
    private VideoDetails mVideoDetails;

    @JsonPath("$.playbackTracking.videostatsPlaybackUrl.baseUrl")
    private String mPlaybackUrl;

    @JsonPath("$.playbackTracking.videostatsWatchtimeUrl.baseUrl")
    private String mWatchTimeUrl;

    @JsonPath("$.playabilityStatus.status")
    private String mPlayabilityStatus;

    @JsonPath("$.playabilityStatus.reason")
    private String mPlayabilityReason;

    @JsonPath("$.playabilityStatus.errorScreen.playerErrorMessageRenderer.subreason")
    private TextItem mPlayabilityDescription;

    @JsonPath("$.storyboards.playerStoryboardSpecRenderer.spec")
    private String mStoryboardSpec;

    @JsonPath("$.playabilityStatus.errorScreen.playerLegacyDesktopYpcTrailerRenderer.trailerVideoId")
    private String mTrailerVideoId;

    @JsonPath("$.microformat.playerMicroformatRenderer.liveBroadcastDetails.startTimestamp")
    private String mStartTimestamp;

    @JsonPath("$.microformat.playerMicroformatRenderer.uploadDate")
    private String mUploadDate;

    @JsonPath("$.playerConfig.audioConfig.loudnessDb")
    private float mLoudnessDb;

    // Values used in tracking actions
    private String mEventId;
    private String mVisitorMonitoringData;
    private String mOfParam;

    private long mStartTimeMs;
    private int mStartSegmentNum;
    private int mSegmentDurationUs;
    private boolean mIsStreamSeekable;
    private List<CaptionTrack> mMergedCaptionTracks;

    public List<AdaptiveVideoFormat> getAdaptiveFormats() {
        return mAdaptiveFormats;
    }

    public List<RegularVideoFormat> getRegularFormats() {
        return mRegularFormats;
    }

    public List<AdaptiveVideoFormat> getRestrictedFormats() {
        return mRestrictedFormats;
    }

    public String getHlsManifestUrl() {
        return mHlsManifestUrl;
    }

    public String getVideoStatsWatchTimeUrl() {
        return mVideoStatsWatchTimeUrl;
    }

    public List<CaptionTrack> getCaptionTracks() {
        return mergeCaptionTracks();
    }

    public String getDashManifestUrl() {
        return mDashManifestUrl;
    }

    public void setDashManifestUrl(String dashManifestUrl) {
        mDashManifestUrl = dashManifestUrl;
    }

    public void setHlsManifestUrl(String hlsManifestUrl) {
        mHlsManifestUrl = hlsManifestUrl;
    }

    public void setStoryboardSpec(String storyboardSpec) {
        mStoryboardSpec = storyboardSpec;
    }

    public VideoDetails getVideoDetails() {
        return mVideoDetails;
    }

    public String getEventId() {
        parseTrackingParams();

        return mEventId;
    }

    /**
     * Intended to merge signed and unsigned infos (no-playback fix)
     */
    public void setEventId(String eventId) {
        mEventId = eventId;
    }

    public String getVisitorMonitoringData() {
        parseTrackingParams();

        return mVisitorMonitoringData;
    }

    /**
     * Intended to merge signed and unsigned infos (no-playback fix)
     */
    public void setVisitorMonitoringData(String visitorMonitoringData) {
        mVisitorMonitoringData = visitorMonitoringData;
    }

    public String getOfParam() {
        parseTrackingParams();

        return mOfParam;
    }

    /**
     * Intended to merge signed and unsigned infos (no-playback fix)
     */
    public void setOfParam(String ofParam) {
        mOfParam = ofParam;
    }

    public String getPlaybackUrl() {
        return mPlaybackUrl;
    }

    public String getWatchTimeUrl() {
        return mWatchTimeUrl;
    }

    public boolean isRent() {
        return isUnplayable() && getTrailerVideoId() != null;
    }

    public boolean isUnplayable() {
        return isEmbedRestricted() || isAgeRestricted();
    }

    /**
     * Video cannot be embedded
     */
    public boolean isEmbedRestricted() {
        return ServiceHelper.atLeastOneEquals(mPlayabilityStatus, STATUS_UNPLAYABLE, STATUS_ERROR);
    }

    /**
     * Age restricted video
     */
    public boolean isAgeRestricted() {
        return ServiceHelper.atLeastOneEquals(mPlayabilityStatus, STATUS_LOGIN_REQUIRED, STATUS_AGE_CHECK_REQUIRED, STATUS_CONTENT_CHECK_REQUIRED);
    }

    public boolean isExtendedHlsFormatsBroken() {
        return !isLive() && getHlsManifestUrl() == null && isAdaptiveFullHD();
    }

    public boolean hasExtendedHlsFormats() {
        if (!isLive() && getHlsManifestUrl() != null && isAdaptiveFullHD()) {
            long uploadTimeMs = DateHelper.toUnixTimeMs(getUploadDate());
            // Extended formats may not work during 3 days after publication
            return uploadTimeMs > 0 && System.currentTimeMillis() - uploadTimeMs > 4*24*60*60*1_000;
        }

        return false;
    }

    public boolean isStoryboardBroken() {
        return !isLive() && getStoryboardSpec() == null;
    }

    public boolean isLive() {
        return getVideoDetails() != null && getVideoDetails().isLive();
    }

    public String getPlayabilityStatus() {
        return ServiceHelper.itemsToInfo(mPlayabilityReason, mPlayabilityDescription);
    }

    public String getStoryboardSpec() {
        return mStoryboardSpec;
    }

    public String getTrailerVideoId() {
        return mTrailerVideoId;
    }

    public String getStartTimestamp() {
        return mStartTimestamp;
    }

    public String getUploadDate() {
        return mUploadDate;
    }

    public long getStartTimeMs() {
        return mStartTimeMs;
    }

    public int getStartSegmentNum() {
        return mStartSegmentNum;
    }

    public int getSegmentDurationUs() {
        return mSegmentDurationUs;
    }

    public float getLoudnessDb() {
        return mLoudnessDb;
    }

    public boolean isStreamSeekable() {
        return mIsStreamSeekable;
    }

    public boolean isHfr() {
        return mDashManifestUrl != null && mDashManifestUrl.contains("/hfr/all");
    }

    public boolean isValid() {
        if (STATUS_OFFLINE.equals(mPlayabilityStatus)) {
            return true;
        }

        // Check that history data is present
        return getEventId() != null && getVisitorMonitoringData() != null;
    }

    public void sync(DashInfo dashInfo) {
        if (dashInfo == null) {
            return;
        }

        mSegmentDurationUs = dashInfo.getSegmentDurationUs();
        mStartTimeMs = dashInfo.getStartTimeMs();
        mStartSegmentNum = dashInfo.getStartSegmentNum();
        mIsStreamSeekable = dashInfo.isSeekable();
    }

    private void parseTrackingParams() {
        boolean parseDone = mEventId != null || mVisitorMonitoringData != null;

        if (!parseDone && mWatchTimeUrl != null) {
            UrlQueryString queryString = UrlQueryStringFactory.parse(mWatchTimeUrl);

            mEventId = queryString.get(PARAM_EVENT_ID);
            mVisitorMonitoringData = queryString.get(PARAM_VM);
            mOfParam = queryString.get(PARAM_OF);
        }
    }

    private List<CaptionTrack> mergeCaptionTracks() {
        if (mMergedCaptionTracks == null) {
            mMergedCaptionTracks = mCaptionTracks;

            if (mTranslationLanguages != null && mCaptionTracks != null) {
                CaptionTrack originTrack = findOriginTrack(mCaptionTracks);
                String tag = Helpers.runMultiMatcher(originTrack.getName(), tagPattern);
                for (TranslationLanguage language : mTranslationLanguages) {
                    if (!Helpers.equals(originTrack.getLanguageCode(), language.getLanguageCode())) {
                        mMergedCaptionTracks.add(new TranslatedCaptionTrack(originTrack, language, tag));
                    }
                }
            }
        }

        return mMergedCaptionTracks;
    }

    private CaptionTrack findOriginTrack(List<CaptionTrack> captionTracks) {
        CaptionTrack result = null;

        for (CaptionTrack track : captionTracks) {
            if (!track.isAutogenerated()) {
                result = track;
                break;
            }
        }

        return result != null ? result : captionTracks.get(0);
    }

    private boolean isAdaptiveFullHD() {
        return getAdaptiveFormats() != null && !getAdaptiveFormats().isEmpty() && "1080p".equals(getAdaptiveFormats().get(0).getQualityLabel());
    }
}
