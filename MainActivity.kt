package com.elix360.android

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://elix360.pythonanywhere.com"
    private val client = OkHttpClient()
    private lateinit var input: EditText
    private lateinit var sendButton: android.widget.Button
    private lateinit var list: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private val prefs by lazy {
        getSharedPreferences("elix_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        list = findViewById(R.id.messagesList)

        adapter = MessageAdapter(messages)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        sendButton.setOnClickListener { sendMessage() }
        input.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        findViewById<android.widget.ImageButton>(R.id.settingsButton)
            .setOnClickListener { showSettings() }

        findViewById<android.widget.ImageButton>(R.id.menuButton)
            .setOnClickListener { showMenu() }

        messages.add(Message("Merhaba! Ben ELIX. 😊", false))
        adapter.notifyItemInserted(messages.lastIndex)
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return

        val apiKey = prefs.getString("api_key", "")?.trim().orEmpty()
        val guestMode = prefs.getBoolean("guest_mode", true)

        messages.add(Message(text, true))
        adapter.notifyItemInserted(messages.lastIndex)
        list.scrollToPosition(messages.lastIndex)
        input.text.clear()
        sendButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = callElix(text, apiKey, guestMode)
                withContext(Dispatchers.Main) {
                    messages.add(Message(result, false))
                    adapter.notifyItemInserted(messages.lastIndex)
                    list.scrollToPosition(messages.lastIndex)
                    sendButton.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messages.add(Message("Bağlantı hatası: ${e.message ?: "ELIX'e ulaşılamadı."}", false))
                    adapter.notifyItemInserted(messages.lastIndex)
                    list.scrollToPosition(messages.lastIndex)
                    sendButton.isEnabled = true
                }
            }
        }
    }

    private fun callElix(text: String, apiKey: String, guestMode: Boolean): String {
        val endpoint = if (guestMode || apiKey.isBlank()) {
            "$baseUrl/api/guest_chat"
        } else {
            "$baseUrl/api/v1/chatb"
        }

        val json = JSONObject().put("message", text)
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val builder = Request.Builder()
            .url(endpoint)
            .post(body)
            .addHeader("Content-Type", "application/json")

        if (!guestMode && apiKey.isNotBlank()) {
            builder.addHeader("X-API-Key", apiKey)
        }

        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val obj = try { JSONObject(raw) } catch (_: Exception) {
                JSONObject().put("error", raw)
            }

            if (!response.isSuccessful) {
                throw Exception(obj.optString("error", "HTTP ${response.code}"))
            }

            return obj.optString("answer", "ELIX cevap vermedi.")
        }
    }

    private fun showSettings() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(45, 10, 45, 0)
        }

        val keyInput = EditText(this).apply {
            hint = "ELIX API anahtarı"
            setSingleLine(true)
            setText(prefs.getString("api_key", ""))
        }

        val modeText = android.widget.TextView(this).apply {
            text = "API anahtarı girersen hesap API'si kullanılır. Boş bırakılırsa misafir modu çalışır."
            textSize = 13f
            setPadding(0, 0, 0, 12)
        }

        box.addView(modeText)
        box.addView(keyInput)

        MaterialAlertDialogBuilder(this)
            .setTitle("ELIX Ayarları")
            .setView(box)
            .setPositiveButton("Kaydet") { _, _ ->
                val key = keyInput.text.toString().trim()
                prefs.edit()
                    .putString("api_key", key)
                    .putBoolean("guest_mode", key.isBlank())
                    .apply()
                Toast.makeText(this, "Ayarlar kaydedildi.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showMenu() {
        val items = arrayOf("Yeni sohbet", "Ayarlar", "ELIX API bilgisi")
        MaterialAlertDialogBuilder(this)
            .setTitle("ELIX")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> newChat()
                    1 -> showSettings()
                    2 -> showApiInfo()
                }
            }
            .show()
    }

    private fun newChat() {
        messages.clear()
        messages.add(Message("Yeni sohbet. Ben ELIX. 😊", false))
        adapter.notifyDataSetChanged()
    }

    private fun showApiInfo() {
        MaterialAlertDialogBuilder(this)
            .setTitle("ELIX API")
            .setMessage(
                "Sunucu:\n$baseUrl\n\n" +
                "Hesap API endpoint'i:\n/api/v1/chatb\n\n" +
                "İstek:\nPOST + JSON {\"message\":\"Selam\"}\n\n" +
                "Kimlik doğrulama:\nX-API-Key"
            )
            .setPositiveButton("Tamam", null)
            .show()
    }
}
