package com.liskovsoft.youtubeapi.chat

import com.liskovsoft.mediaserviceinterfaces.data.ChatItem
import com.liskovsoft.sharedutils.mylogger.Log
import com.liskovsoft.youtubeapi.chat.gen.LiveChatResult
import com.liskovsoft.youtubeapi.chat.gen.getActions
import com.liskovsoft.youtubeapi.chat.gen.getContinuation
import com.liskovsoft.youtubeapi.chat.impl.ChatItemImpl
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

internal class LiveChatServiceInt private constructor() {
    private val mApi = RetrofitHelper.withGson(LiveChatApi::class.java)

    fun openLiveChat(chatKey: String, onChatItem: OnChatItem) {
        // It's common to stream to be interrupted multiple times
        while (true) {
            try {
                openLiveChatInt(chatKey, onChatItem)
                Thread.sleep(5_000) // fix too frequent request
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Connection hanged. Reconnecting...")
            } catch (e: InterruptedIOException) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.")
                break
            } catch (e: InterruptedException) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.")
                break
            } catch (e: NullPointerException) {
                Log.e(TAG, "Oops. Stopping. Got NPE.")
                e.printStackTrace()
                break
            } catch (e: Exception) {
                Log.e(TAG, e.message)
                // Continue to listen whichever is happening.
            }
        }
    }

    private fun openLiveChatInt(chatKey: String, onChatItem: OnChatItem) {
        var continuationKey: String? = chatKey
        var timeoutMs: Int?

        while (true) {
            if (continuationKey.isNullOrEmpty()) {
                break
            }

            val chatResult = getLiveChatResult(continuationKey)
            val continuation = chatResult?.getContinuation()
            continuationKey = continuation?.continuation
            timeoutMs = continuation?.timeoutMs

            val actions = chatResult?.getActions()
            actions?.forEach { it?.let { onChatItem.onChatItem(ChatItemImpl(it)) } }

            timeoutMs?.run { Thread.sleep(timeoutMs.toLong()) }
        }
    }

    private fun getLiveChatResult(chatKey: String): LiveChatResult? {
        val chatQuery = LiveChatApiParams.getLiveChatQuery(chatKey)
        val wrapper = mApi.getLiveChat(chatQuery)
        return RetrofitHelper.get(wrapper)
    }

    interface OnChatItem {
        fun onChatItem(chatItem: ChatItem)
    }

    companion object {
        private val TAG = LiveChatServiceInt::class.simpleName
        private var sInstance: LiveChatServiceInt? = null
        @JvmStatic
        fun instance(): LiveChatServiceInt? {
            if (sInstance == null) {
                sInstance = LiveChatServiceInt()
            }
            return sInstance
        }

        @JvmStatic
        fun unhold() {
            sInstance = null
        }
    }
}