package com.liskovsoft.youtubeapi.common.models.impl.mediagroup

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem
import com.liskovsoft.youtubeapi.browse.v2.gen.*
import com.liskovsoft.youtubeapi.common.models.gen.ItemWrapper
import com.liskovsoft.youtubeapi.common.models.gen.getBrowseId
import com.liskovsoft.youtubeapi.common.models.gen.getBrowseParams
import com.liskovsoft.youtubeapi.common.models.impl.mediaitem.GuideMediaItem
import com.liskovsoft.youtubeapi.common.models.impl.mediaitem.NotificationMediaItem
import com.liskovsoft.youtubeapi.notifications.gen.NotificationsResult
import com.liskovsoft.youtubeapi.notifications.gen.getItems

internal data class BrowseMediaGroup(
    private val browseResult: BrowseResult,
    private val options: MediaGroupOptions = MediaGroupOptions(),
    private val liveResult: BrowseResult? = null
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?> =
        listOfNotNull(liveResult?.getLiveItems(), browseResult.getItems()).flatten()
    override fun getNextPageKeyInt(): String? = browseResult.getContinuationToken()
    override fun getTitleInt(): String? = browseResult.getTitle()
}

internal data class LiveMediaGroup(
    private val liveResult: BrowseResult,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?> = listOfNotNull(liveResult.getLiveItems(), liveResult.getPastLiveItems()).flatten()
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = liveResult.getTitle()
}

internal data class ContinuationMediaGroup(
    private val continuationResult: ContinuationResult,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = continuationResult.getItems()
    override fun getNextPageKeyInt(): String? = continuationResult.getContinuationToken()
    override fun getTitleInt(): String? = null
}

internal data class SectionMediaGroup(
    private val richSectionRenderer: RichSectionRenderer,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = richSectionRenderer.getItems()
    override fun getNextPageKeyInt(): String? = richSectionRenderer.getContinuationToken()
    override fun getTitleInt(): String? = richSectionRenderer.getTitle()
}

internal data class TabMediaGroup(
    private val tabRenderer: TabRenderer,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = tabRenderer.getItems()
    override fun getNextPageKeyInt(): String? = tabRenderer.getContinuationToken()
    override fun getTitleInt(): String? = tabRenderer.getTitle()
    override fun getChannelIdInt(): String? = tabRenderer.endpoint?.getBrowseId()
    override fun getParamsInt(): String? = tabRenderer.endpoint?.getBrowseParams()
}

internal data class KidsSectionMediaGroup(
    private val anchoredSectionRenderer: AnchoredSectionRenderer,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = anchoredSectionRenderer.getItems()
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = anchoredSectionRenderer.getTitle()
}

internal data class ChipMediaGroup(
    private val chipCloudChipRenderer: ChipCloudChipRenderer,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = chipCloudChipRenderer.getContinuationToken()
    override fun getTitleInt(): String? = chipCloudChipRenderer.getTitle()
}

internal data class GuideMediaGroup(
    private val guideResult: GuideResult,
    private val options: MediaGroupOptions = MediaGroupOptions(),
    private val sort: Boolean = false
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
    override val mediaItemList by lazy {
        val result = mutableListOf<MediaItem>()

        guideResult.getFirstSubs()?.forEach {
            it?.let { if (it.thumbnail != null) result.add(GuideMediaItem(it)) } // exclude 'special' items
        }

        guideResult.getCollapsibleSubs()?.forEach {
            it?.let { if (it.thumbnail != null) result.add(GuideMediaItem(it)) } // exclude 'special' items
        }

        if (sort) result.sortBy { it.title?.lowercase() }

        result
    }
}

internal data class RecommendedMediaGroup(
    private val guideItem: GuideItem,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = guideItem.getTitle()
    override fun getChannelIdInt(): String? = guideItem.getBrowseId()
    override fun getParamsInt(): String? = guideItem.getBrowseParams()
}

internal data class ShortsMediaGroup(
    private val items: List<MediaItem?>,
    private val continuation: String? = null,
    private val options: MediaGroupOptions = MediaGroupOptions()
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = continuation
    override fun getTitleInt(): String? = null
    override val mediaItemList = items
}

internal data class NotificationsMediaGroup(
    private val result: NotificationsResult
): BaseMediaGroup(MediaGroupOptions(groupType = MediaGroup.TYPE_NOTIFICATIONS)) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
    override val mediaItemList by lazy { result.getItems()?.mapNotNull { it?.let { NotificationMediaItem(it) } } }
}

internal data class WrapperMediaGroup(
    private val items: List<ItemWrapper?>
): BaseMediaGroup(MediaGroupOptions(false)) {
    override fun getItemWrappersInt(): List<ItemWrapper?> = items
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
}
