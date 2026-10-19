package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class SupabaseClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    var projectUrl: String = ""
        private set
    var anonKey: String = ""
        private set
    var authToken: String = ""
        private set
    var currentUserId: String = ""
        private set

    companion object {
        fun normalizeUrl(raw: String): String {
            var u = raw.trim().trim('"', '\'', '`').replace("\\s+".toRegex(), "")
            if (u.isBlank()) return ""
            if (!u.startsWith("http://") && !u.startsWith("https://")) {
                u = "https://$u"
            }
            u = u.removeSuffix("/")
            if (u.contains("/rest/v1")) {
                u = u.substringBefore("/rest/v1")
            } else if (u.contains("/auth/v1")) {
                u = u.substringBefore("/auth/v1")
            }
            return u.trim().removeSuffix("/")
        }

        fun sanitizeKey(raw: String): String {
            return raw.trim().trim('"', '\'', '`').replace("\\s+".toRegex(), "")
        }
    }

    fun configure(url: String, key: String, token: String = "", userId: String = "") {
        projectUrl = normalizeUrl(url)
        anonKey = sanitizeKey(key)
        authToken = sanitizeKey(token)
        currentUserId = userId.trim()
    }

    val isConfigured: Boolean
        get() = projectUrl.isNotBlank() && anonKey.isNotBlank() && (projectUrl.startsWith("http://") || projectUrl.startsWith("https://"))

    private fun buildHeaders(requestBuilder: Request.Builder): Request.Builder {
        val cleanKey = sanitizeKey(anonKey)
        val tokenToUse = if (authToken.isNotBlank()) sanitizeKey(authToken) else cleanKey

        return requestBuilder
            .header("apikey", cleanKey)
            .header("Authorization", "Bearer $tokenToUse")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Prefer", "return=representation")
    }

    suspend fun ping(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Pair(false, "Please provide a valid Supabase Project URL and Anon Public Key.")
        }

        val testUrl = "$projectUrl/rest/v1/subjects?select=id&limit=1"
        try {
            val request = buildHeaders(
                Request.Builder()
                    .url(testUrl)
                    .get()
            ).build()

            client.newCall(request).execute().use { response ->
                val code = response.code
                val responseBody = response.body?.string()?.trim() ?: ""

                if (response.isSuccessful) {
                    Pair(true, "HTTP $code OK: Connected to Supabase! Public subjects verified ✅")
                } else {
                    val errorDetail = if (responseBody.isNotBlank()) {
                        try {
                            val jsonObj = JSONObject(responseBody)
                            jsonObj.optString("message", jsonObj.optString("error_description", responseBody))
                        } catch (_: Exception) {
                            responseBody.take(160)
                        }
                    } else "No response body"

                    val msg = when (code) {
                        401, 403 -> "HTTP $code (Unauthorized/Forbidden): $errorDetail. Please verify your anon public API key."
                        404 -> "HTTP $code (Not Found): 'subjects' table not found at $testUrl. Check Project URL or run supabase_setup.sql."
                        else -> "HTTP $code: Supabase error: $errorDetail"
                    }
                    Pair(false, msg)
                }
            }
        } catch (e: UnknownHostException) {
            Pair(false, "DNS Resolution Failed: Could not resolve Supabase host ($projectUrl). Check your Project URL.")
        } catch (e: ConnectException) {
            Pair(false, "Connection Refused ($projectUrl): Check your internet connection.")
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Ping exception", e)
            Pair(false, "Connection error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    suspend fun signInWithPassword(emailOrUser: String, pass: String): Triple<Boolean, String, String> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Triple(false, "Supabase is not configured yet.", "")
        try {
            val email = if (!emailOrUser.contains("@")) "$emailOrUser@twohearts.app" else emailOrUser
            val payload = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$projectUrl/auth/v1/token?grant_type=password")
                .header("apikey", sanitizeKey(anonKey))
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(respStr)
                    val token = json.optString("access_token", "")
                    val userObj = json.optJSONObject("user")
                    val userId = userObj?.optString("id", "") ?: ""
                    if (token.isNotBlank()) {
                        authToken = token
                        currentUserId = userId
                    }
                    Triple(true, "Authentication successful", userId)
                } else {
                    val errJson = try { JSONObject(respStr) } catch (_: Exception) { null }
                    val msg = errJson?.optString("error_description")
                        ?: errJson?.optString("msg")
                        ?: "Sign in failed (HTTP ${response.code})"
                    Triple(false, msg, "")
                }
            }
        } catch (e: Exception) {
            Triple(false, e.localizedMessage ?: "Network error during sign in", "")
        }
    }

    suspend fun fetchUserProfile(userId: String): JSONObject? = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isBlank()) return@withContext null
        try {
            val request = buildHeaders(
                Request.Builder()
                    .url("$projectUrl/rest/v1/profiles?id=eq.$userId&select=*")
                    .get()
            ).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val raw = response.body?.string() ?: "[]"
                    val array = JSONArray(raw)
                    if (array.length() > 0) array.getJSONObject(0) else null
                } else null
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "fetchUserProfile error", e)
            null
        }
    }

    suspend fun getJsonArray(table: String, queryParams: String = "select=*"): JSONArray? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val request = buildHeaders(
                Request.Builder()
                    .url("$projectUrl/rest/v1/$table?$queryParams")
                    .get()
            ).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val raw = response.body?.string() ?: "[]"
                    JSONArray(raw)
                } else {
                    Log.w("SupabaseClient", "getJsonArray on $table returned ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "getJsonArray error on $table", e)
            null
        }
    }

    suspend fun postJson(table: String, jsonObject: JSONObject): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val body = jsonObject.toString().toRequestBody("application/json".toMediaType())
            val request = buildHeaders(
                Request.Builder()
                    .url("$projectUrl/rest/v1/$table")
                    .post(body)
            ).build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "postJson error on $table", e)
            false
        }
    }

    suspend fun patchJson(table: String, filterQuery: String, jsonObject: JSONObject): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val body = jsonObject.toString().toRequestBody("application/json".toMediaType())
            val request = buildHeaders(
                Request.Builder()
                    .url("$projectUrl/rest/v1/$table?$filterQuery")
                    .patch(body)
            ).build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "patchJson error on $table", e)
            false
        }
    }
}
