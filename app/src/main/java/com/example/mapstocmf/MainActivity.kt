package com.example.mapstocmf

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val channelId = "cmf_maps_channel"
    private val distanceTokenRegex = Regex("""(?i)\b\d+(?:[.,]\d+)?\s*(?:m|km|ft|mi)\b""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }
        scrollView.addView(layout)

        val btnPermission = Button(this).apply {
            text = "⚙️ Check Notification Access"
            setOnClickListener {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
        layout.addView(btnPermission)

        val lblTests = TextView(this).apply {
            text = "\n🧪 Generic Route Tests:"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }
        layout.addView(lblTests)

        // 1. Right Turn with opposite-side landmark
        layout.addView(createTestButton("1. [RIGHT] Grand Avenue (100m)") {
            simulateMapsNotification(
                rawTitle = "Turn right onto Grand Avenue",
                rawText = "100 m · Pass by Central Park (on the left)",
                rawSubText = "12 min · 3.5 km · 5:30 PM"
            )
        })

        // 2. Roundabout 2nd Exit
        layout.addView(createTestButton("2. [ROUNDABOUT 2] Main Street (250m)") {
            simulateMapsNotification(
                rawTitle = "At the roundabout, take the 2nd exit onto Main Street",
                rawText = "250 m",
                rawSubText = "8 min · 2.1 km · 5:34 PM"
            )
        })

        // 3. Left Turn with distance in title
        layout.addView(createTestButton("3. [LEFT] Market Street (500m)") {
            simulateMapsNotification(
                rawTitle = "500 m · Turn left onto Market Street",
                rawText = "Pass by City Library (on the right)",
                rawSubText = "3 min · 800 m · 5:39 PM"
            )
        })

        setContentView(scrollView)
    }

    private fun createTestButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        }
    }

    private fun simulateMapsNotification(rawTitle: String, rawText: String, rawSubText: String) {
        val cleanTitle = rawTitle.replace("·", " - ").replace("•", " - ").trim()
        val cleanText = rawText.replace("·", " - ").replace("•", " - ").trim()

        val distInTitle = distanceTokenRegex.find(cleanTitle)?.value
        val distInText = distanceTokenRegex.find(cleanText)?.value
        val distance = distInTitle ?: distInText ?: ""

        var instruction = when {
            cleanTitle.contains(" - ") -> {
                cleanTitle.split(" - ")
                    .filter { !it.matches(Regex("""(?i).*\b\d+\s*(m|km|ft|mi)\b.*""")) }
                    .joinToString(" ")
                    .ifEmpty { cleanText }
            }
            distInTitle != null && cleanText.isNotEmpty() -> cleanText
            distInText != null && cleanTitle.isNotEmpty() -> cleanTitle
            else -> "$cleanTitle $cleanText".trim()
        }

        if (distance.isNotEmpty()) {
            instruction = instruction.replace(distance, "")
        }

        instruction = instruction
            .replace(Regex("""(?i)\bin\b"""), "")
            .trim(' ', '-', '·', '•', ':', ',')

        if (instruction.isEmpty()) instruction = "Continue"

        val cleanEta = rawSubText.replace("·", "|").replace("•", "|").trim(' ', '-', '|')
        val watchBody = if (cleanEta.isNotEmpty()) "$instruction\n[$cleanEta]" else instruction

        val direction = NavDirection.fromText("$rawTitle $rawText $instruction")
        val formattedTitle = when {
            direction != NavDirection.NONE && distance.isNotEmpty() -> "${direction.badge} $distance"
            distance.isNotEmpty() -> distance
            direction != NavDirection.NONE -> "${direction.badge} NOW"
            else -> "NAVIGATION"
        }

        sendNotification(formattedTitle, watchBody)
        Toast.makeText(this, "Sent: $formattedTitle", Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Navigation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)

            val bigTextStyle = Notification.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(body)

            val notification = Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(bigTextStyle)
                .setAutoCancel(true)
                .build()

            manager.notify(999, notification)
        }
    }
}