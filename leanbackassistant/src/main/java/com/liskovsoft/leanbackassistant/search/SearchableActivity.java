package com.liskovsoft.leanbackassistant.search;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.liskovsoft.leanbackassistant.utils.AppUtil;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.sharedutils.mylogger.Log;

import static androidx.core.content.IntentCompat.EXTRA_START_PLAYBACK;

public class SearchableActivity extends Activity {
    private static final String TAG = "SearchableActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Search data " + getIntent().getData());

        if (getIntent() != null && getIntent().getData() != null) {
            Uri uri = getIntent().getData();

            if (uri.getLastPathSegment() != null) {
                int id = Integer.valueOf(uri.getLastPathSegment());

                boolean startPlayback = getIntent().getBooleanExtra(EXTRA_START_PLAYBACK, false);
                Log.d(TAG, "Should start playback? " + (startPlayback ? "yes" : "no"));

                String url = obtainVideoOrChannelUrl(id);

                if (url != null) {
                    Intent intent = AppUtil.getInstance(this).createAppIntent(url);
                    Log.d(TAG, "Starting search intent: " + intent);

                    startActivity(intent);
                }
            }
        }

        finish();
    }

    private String obtainVideoOrChannelUrl(int id) {
        MediaItem video = VideoContentProvider.findVideoWithId(id);

        if (video != null) {
            return video.getVideoUrl() != null ? video.getVideoUrl() : video.getChannelUrl();
        }

        return null;
    }
}
