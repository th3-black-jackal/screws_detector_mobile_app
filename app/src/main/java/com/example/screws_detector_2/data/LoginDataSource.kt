package com.example.screws_detector_2.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.HttpException
import com.example.screws_detector.data.remote.NetworkModule
import com.example.screws_detector_2.data.model.LoggedInUser
import com.example.screws_detector_2.data.model.LoginRequest
import com.example.screws_detector_2.data.model.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import kotlin.Result

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(private val appContext: Context) {

    @SuppressLint("NewApi")
    suspend fun login(username: String, password: String): Result<LoggedInUser> =
        withContext(Dispatchers.IO) {
            val api   = NetworkModule.apiService(appContext)
            val prefs = NetworkModule.tokenPrefs(appContext)

            return@withContext try {
                val res: TokenStore = api.login(LoginRequest(username, password))

                // Persist tokens securely
                prefs.edit()
                    .putString("access",  res.access)
                    .putString("refresh", res.refresh)
                    .apply()

                // Use the username as display name, or decode JWT for user id
                val user = LoggedInUser(UUID.randomUUID().toString(), username)
                Result.success(user)
            } catch (@SuppressLint("NewApi") e: HttpException) {
                Result.failure(IOException("Invalid credentials", e))
            } catch (e: IOException) {
                Result.failure(IOException("Network error", e))
            }
        }

    fun logout() {
        // Wipe stored tokens
        NetworkModule.tokenPrefs(appContext).edit().clear().apply()
    }
}