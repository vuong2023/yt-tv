package com.liskovsoft.youtubeapi.common.models.V2;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.JsonPath;
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper;
import com.liskovsoft.youtubeapi.common.models.items.Thumbnail;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TileItem {
    public static final String CONTENT_TYPE_UNDEFINED = "UNDEFINED";
    public static final String CONTENT_TYPE_CHANNEL = "TILE_CONTENT_TYPE_CHANNEL";
    public static final String CONTENT_TYPE_PLAYLIST = "TILE_CONTENT_TYPE_PLAYLIST";
    public static final String CONTENT_TYPE_VIDEO = "TILE_CONTENT_TYPE_VIDEO";
    private static final String BADGE_STYLE_LIVE = "LIVE";
    private static final String BADGE_STYLE_UPCOMING = "UPCOMING";
    private static final String BADGE_STYLE_DEFAULT = "DEFAULT";
    private static final String BADGE_STYLE_MOVIE = "BADGE_STYLE_TYPE_YPC";

    @JsonPath({
            "$.header.tileHeaderRenderer",
            "$.header.trackTileHeaderRenderer" // Music playlist
    })
    private Header mHeader;

    @JsonPath({"$.metadata.tileMetadataRenderer",
            "$.header.tileHeaderRenderer.thumbnailOverlays[0].tileMetadataRenderer"}) // V4 search metadata
    private Metadata mMetadata;

    @JsonPath({
            "$.onSelectCommand.watchEndpoint.videoId", // regular video
            "$.onSelectCommand.showMenuCommand.contentId" // rent movie
    })
    private String mVideoId;

    @JsonPath({
            "$.onSelectCommand.watchEndpoint.playlistId",
            "$.onSelectCommand.watchPlaylistEndpoint.playlistId"
    })
    private String mPlaylistId;

    @JsonPath({
            // New videos row in Music section
            "$.menu.menuRenderer.items[*].menuNavigationItemRenderer.navigationEndpoint.watchEndpoint.videoId",
            "$.onLongPressCommand.showMenuCommand.menu.menuRenderer.items[*].menuNavigationItemRenderer.navigationEndpoint.watchEndpoint.videoId" // v2 feedback token
    })
    private List<String> mVideoIds;

    @JsonPath({
            // New videos row in Music section
            "$.menu.menuRenderer.items[*].menuNavigationItemRenderer.navigationEndpoint.watchEndpoint.playlistId",
            "$.onLongPressCommand.showMenuCommand.menu.menuRenderer.items[*].menuNavigationItemRenderer.navigationEndpoint.watchEndpoint.playlistId" // v2 feedback token
    })
    private List<String> mPlaylistIds;

    @JsonPath("$.onSelectCommand.browseEndpoint.params")
    private String mPlaylistParams;

    @JsonPath("$.onSelectCommand.browseEndpoint.browseId")
    private String mChannelId;

    @JsonPath("$.contentType")
    private String mContentType;

    @JsonPath({"$.menu.menuRenderer.items[*].menuServiceItemRenderer.serviceEndpoint.feedbackEndpoint.feedbackToken",
            "$.onLongPressCommand.showMenuCommand.menu.menuRenderer.items[*].menuServiceItemRenderer.serviceEndpoint.feedbackEndpoint.feedbackToken"}) // v2 feedback token
    private List<String> mFeedbackToken;

    public Header getHeader() {
        return mHeader;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public String getVideoId() {
        return mVideoId != null ? mVideoId : ServiceHelper.getFirst(mVideoIds);
    }

    public String getTitle() {
        return mMetadata != null ? mMetadata.getTitle() : mHeader != null ? mHeader.getTitle() : null;
    }

    public String getDescBadgeText() {
        return mMetadata != null && mMetadata.getBadgeLabels() != null ? mMetadata.getBadgeLabels().get(0) : null;
    }

    public String getUserName() {
        return mMetadata != null ? mMetadata.getUserName() : null;
    }

    public String getPublishedTime() {
        return mMetadata != null ? mMetadata.getPublishedTime() : null;
    }

    public String getViewCountText() {
        return mMetadata != null ? mMetadata.getViewCountText() : null;
    }

    public String getUpcomingEventText() {
        return null;
    }

    public List<Thumbnail> getThumbnails() {
        return mHeader != null ? mHeader.getThumbnails() : null;
    }

    public String getBadgeText() {
        return mHeader != null ? mHeader.getBadgeText() : null;
    }

    public int getPercentWatched() {
        return mHeader != null ? mHeader.getPercentWatched() : -1;
    }

    public String getRichThumbnailUrl() {
        return mHeader != null ? mHeader.getMovingThumbnailUrl() : null;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public int getPlaylistIndex() {
        return 0;
    }

    public String getPlaylistId() {
        return mPlaylistId != null ? mPlaylistId : ServiceHelper.getFirst(mPlaylistIds);
    }

    public String getPlaylistParams() {
        return mPlaylistParams;
    }

    public boolean isLive() {
        return Helpers.equalsAny(BADGE_STYLE_LIVE, getBadgeStyles());
    }

    public boolean isUpcoming() {
        return Helpers.equalsAny(BADGE_STYLE_UPCOMING, getBadgeStyles());
    }

    public boolean isMovie() {
        return Helpers.equalsAny(BADGE_STYLE_MOVIE, getBadgeStyles());
    }

    public String getFeedbackToken() {
        return ServiceHelper.getFirst(mFeedbackToken);
    }

    public String getClickTrackingParams() {
        return null;
    }

    public String getContentType() {
        return mContentType != null ? mContentType : CONTENT_TYPE_UNDEFINED;
    }

    @Nullable
    private String[] getBadgeStyles() {
        return mHeader != null ? new String[] {mHeader.getBadgeStyle()} :
                mMetadata != null && mMetadata.getBadgeStyles() != null ? mMetadata.getBadgeStyles().toArray(new String[]{}) : null;
    }
}
