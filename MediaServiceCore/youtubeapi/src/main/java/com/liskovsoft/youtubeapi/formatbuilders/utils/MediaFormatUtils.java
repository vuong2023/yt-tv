package com.liskovsoft.youtubeapi.formatbuilders.utils;

import com.liskovsoft.mediaserviceinterfaces.data.MediaFormat;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaFormatUtils {
    public static final String MIME_WEBM_AUDIO = "audio/webm";
    public static final String MIME_WEBM_VIDEO = "video/webm";
    public static final String MIME_MP4_AUDIO = "audio/mp4";
    public static final String MIME_MP4_VIDEO = "video/mp4";
    private static final Pattern CODECS_PATTERN = Pattern.compile(".*codecs=\\\"(.*)\\\"");

    public static String getHeight(MediaFormat format) {
        String size = format.getSize();

        if (size == null) {
            return "";
        }

        String[] widthHeight = size.split("x");

        if (widthHeight.length != 2) {
            return "";
        }

        return widthHeight[1];
    }

    public static String getWidth(MediaFormat format) {
        String size = format.getSize();

        if (size == null) {
            return "";
        }

        String[] widthHeight = size.split("x");

        if (widthHeight.length != 2) {
            return "";
        }

        return widthHeight[0];
    }

    public static boolean isDash(MediaFormat format) {
        if (format.getITag() == null) {
            return false;
        }

        if (format.getGlobalSegmentList() != null) {
            return true;
        }

        String id = format.getITag();

        return Helpers.isDash(id);
    }

    public static boolean checkMediaUrl(MediaFormat format) {
        return format != null && format.getUrl() != null;
    }

    public static String extractMimeType(MediaFormat format) {
        if (format.getGlobalSegmentList() != null) {
            return format.getMimeType();
        }

        String codecs = extractCodecs(format);

        if (codecs.startsWith("vorbis") ||
                codecs.startsWith("opus")) {
            return MIME_WEBM_AUDIO;
        }

        if (codecs.startsWith("vp9")) {
            return MIME_WEBM_VIDEO;
        }

        if (codecs.startsWith("mp4a") ||
                codecs.startsWith("ec-3") ||
                codecs.startsWith("ac-3")) {
            return MIME_MP4_AUDIO;
        }

        if (codecs.startsWith("avc") ||
                codecs.startsWith("av01")) {
            return MIME_MP4_VIDEO;
        }

        return null;
    }

    public static String extractCodecs(MediaFormat format) {
        // input example: video/mp4;+codecs="avc1.640033"
        Matcher matcher = CODECS_PATTERN.matcher(format.getMimeType());
        matcher.find();
        return matcher.group(1);
    }

    public static boolean isLiveMedia(MediaFormat format) {
        boolean isLive =
                format.getUrl().contains("live=1") ||
                        format.getUrl().contains("yt_live_broadcast");

        return isLive;
    }

    public static boolean isAudio(String mimeType) {
        return Helpers.startsWith(mimeType, "audio");
    }

    public static boolean isVideo(String mimeType) {
        return Helpers.startsWith(mimeType, "video");
    }
}
