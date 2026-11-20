package com.payent.delivery.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "payent_delivery_session")

interface SessionManager {
    val authTokenFlow: Flow<String?>
    val partnerIdFlow: Flow<String?>
    val partnerNameFlow: Flow<String?>
    val isDemoSessionFlow: Flow<Boolean>
    val isOnlineStatusFlow: Flow<Boolean>
    val themeModeFlow: Flow<String>
    val languageCodeFlow: Flow<String>
    val orderUpdatesEnabledFlow: Flow<Boolean>
    val orderAlertSoundEnabledFlow: Flow<Boolean>
    val orderAlertVibrationEnabledFlow: Flow<Boolean>

    fun getAuthTokenSync(): String?
    suspend fun saveAuthToken(token: String)
    suspend fun savePartnerSession(partnerId: String, partnerName: String, token: String)
    suspend fun saveDemoSession()
    suspend fun saveOnlineStatus(isOnline: Boolean)
    suspend fun saveThemeMode(mode: String)
    suspend fun saveLanguageCode(code: String)
    suspend fun saveOrderUpdatesEnabled(enabled: Boolean)
    suspend fun saveOrderAlertSoundEnabled(enabled: Boolean)
    suspend fun saveOrderAlertVibrationEnabled(enabled: Boolean)
    suspend fun clearSession()
}

class EncryptedSessionManager(private val context: Context) : SessionManager {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "payent_secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        context.getSharedPreferences("payent_secure_tokens_fallback", Context.MODE_PRIVATE)
    }

    private val _tokenFlow = MutableStateFlow(encryptedPrefs.getString(KEY_TOKEN, null))
    override val authTokenFlow: Flow<String?> = _tokenFlow.asStateFlow()

    companion object {
        private const val KEY_TOKEN = "secure_auth_token"
        private val KEY_PARTNER_ID = stringPreferencesKey("partner_id")
        private val KEY_PARTNER_NAME = stringPreferencesKey("partner_name")
        private val KEY_IS_DEMO = booleanPreferencesKey("is_demo_session")
        private val KEY_IS_ONLINE = booleanPreferencesKey("is_online_status")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val KEY_ORDER_UPDATES_ENABLED = booleanPreferencesKey("order_updates_enabled")
        private val KEY_ORDER_ALERT_SOUND_ENABLED = booleanPreferencesKey("order_alert_sound_enabled")
        private val KEY_ORDER_ALERT_VIBRATION_ENABLED = booleanPreferencesKey("order_alert_vibration_enabled")
    }

    override val partnerIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PARTNER_ID]
    }

    override val partnerNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PARTNER_NAME]
    }

    override val isDemoSessionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_DEMO] ?: false
    }

    override val isOnlineStatusFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_ONLINE] ?: false
    }

    override val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "DARK"
    }

    override val languageCodeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE_CODE] ?: "en"
    }

    override val orderUpdatesEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ORDER_UPDATES_ENABLED] ?: true
    }

    override val orderAlertSoundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ORDER_ALERT_SOUND_ENABLED] ?: true
    }

    override val orderAlertVibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ORDER_ALERT_VIBRATION_ENABLED] ?: true
    }

    override fun getAuthTokenSync(): String? {
        return encryptedPrefs.getString(KEY_TOKEN, null)
    }

    override suspend fun saveAuthToken(token: String) {
        encryptedPrefs.edit().putString(KEY_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    override suspend fun savePartnerSession(partnerId: String, partnerName: String, token: String) {
        encryptedPrefs.edit().putString(KEY_TOKEN, token).apply()
        _tokenFlow.value = token
        context.dataStore.edit { preferences ->
            preferences[KEY_PARTNER_ID] = partnerId
            preferences[KEY_PARTNER_NAME] = partnerName
            preferences[KEY_IS_DEMO] = false
        }
    }

    override suspend fun saveDemoSession() {
        val demoToken = "demo_token_session_active"
        val demoPartnerId = "demo_partner"
        val demoName = "Demo Delivery Partner"

        encryptedPrefs.edit().putString(KEY_TOKEN, demoToken).apply()
        _tokenFlow.value = demoToken

        context.dataStore.edit { preferences ->
            preferences[KEY_PARTNER_ID] = demoPartnerId
            preferences[KEY_PARTNER_NAME] = demoName
            preferences[KEY_IS_DEMO] = true
        }
    }

    override suspend fun saveOnlineStatus(isOnline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_ONLINE] = isOnline
        }
    }

    override suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    override suspend fun saveLanguageCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE_CODE] = code
        }
    }

    override suspend fun saveOrderUpdatesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ORDER_UPDATES_ENABLED] = enabled
        }
    }

    override suspend fun saveOrderAlertSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ORDER_ALERT_SOUND_ENABLED] = enabled
        }
    }

    override suspend fun saveOrderAlertVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ORDER_ALERT_VIBRATION_ENABLED] = enabled
        }
    }

    override suspend fun clearSession() {
        encryptedPrefs.edit().clear().apply()
        _tokenFlow.value = null
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PARTNER_ID)
            preferences.remove(KEY_PARTNER_NAME)
            preferences.remove(KEY_IS_DEMO)
            preferences.remove(KEY_IS_ONLINE)
        }
    }
}
