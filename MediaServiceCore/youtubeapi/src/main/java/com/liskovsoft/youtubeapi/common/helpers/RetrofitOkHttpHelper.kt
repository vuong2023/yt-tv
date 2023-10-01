package com.liskovsoft.youtubeapi.common.helpers

import com.google.net.cronet.okhttptransport.CronetInterceptor
import com.liskovsoft.sharedutils.cronet.CronetManager
import com.liskovsoft.sharedutils.helpers.Helpers
import com.liskovsoft.sharedutils.okhttp.OkHttpCommons
import com.liskovsoft.sharedutils.prefs.GlobalPreferences
import com.liskovsoft.youtubeapi.app.AppConstants
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

internal object RetrofitOkHttpHelper {
    @JvmStatic
    val authHeaders = mutableMapOf<String, String>()

    @JvmStatic
    val client: OkHttpClient by lazy { createClient() }

    var disableCompression: Boolean = false
    var disableAuth: Boolean = false

    private val headers = mapOf(
        "User-Agent" to DefaultHeaders.APP_USER_AGENT,
        // Enable compression in production
        "Accept-Encoding" to DefaultHeaders.ACCEPT_ENCODING,
        "Referer" to "https://www.youtube.com/tv"
    )

    private val apiPrefixes = arrayOf(
        "https://m.youtube.com/youtubei/v1/",
        "https://www.youtube.com/youtubei/v1/",
        "https://www.youtube.com/api/stats/",
        "https://clients1.google.com/complete/"
    )

    private fun createClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        addCommonHeaders(builder)
        OkHttpCommons.setupBuilder(builder)
        //addCronetInterceptor(builder)
        return builder.build()
    }

    private fun addCommonHeaders(builder: OkHttpClient.Builder) {
        builder.addInterceptor { chain ->
            val request = chain.request()
            val headers = request.headers()
            val requestBuilder = request.newBuilder()

            applyHeaders(this.headers, headers, requestBuilder)

            if (Helpers.startsWithAny(request.url().toString(), *apiPrefixes)) {
                if (authHeaders.isEmpty() || disableAuth) {
                    disableAuth = false
                    //applyApiKey(request, requestBuilder)
                    applyQueryKeys(mapOf("key" to AppConstants.API_KEY, "prettyPrint" to "false"), request, requestBuilder)
                } else {
                    applyQueryKeys(mapOf("prettyPrint" to "false"), request, requestBuilder)
                    applyHeaders(authHeaders, headers, requestBuilder)
                }
            }

            chain.proceed(requestBuilder.build())
        }
    }

    private fun applyHeaders(newHeaders: Map<String, String>, oldHeaders: Headers, builder: Request.Builder) {
        for (header in newHeaders) {
            if (disableCompression && header.key == "Accept-Encoding") {
                continue
            }

            // Don't override existing headers
            oldHeaders[header.key] ?: builder.header(header.key, header.value)
        }
    }

    private fun applyQueryKeys(keys: Map<String, String>, request: Request, builder: Request.Builder) {
        val originUrl = request.url()

        var newUrlBuilder: HttpUrl.Builder? = null

        for (entry in keys) {
            // Don't override existing keys
            originUrl.queryParameter(entry.key) ?: run {
                if (newUrlBuilder == null) {
                    newUrlBuilder = originUrl.newBuilder()
                }

                newUrlBuilder?.addQueryParameter(entry.key, entry.value)
            }
        }

        newUrlBuilder?.run {
            builder.url(build())
        }
    }

    private fun addCronetInterceptor(builder: OkHttpClient.Builder) {
        val engine = CronetManager.getEngine(GlobalPreferences.sInstance.context)
        if (engine != null) {
            builder.addInterceptor(CronetInterceptor.newBuilder(engine).build())
        }
    }
}