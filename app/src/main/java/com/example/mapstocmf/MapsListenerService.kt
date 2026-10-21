package com.example.mapstocmf

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

enum class NavDirection(val badge: String) {
    ARRIVED("[ARRIVED]"),
    U_TURN("[U-TURN]"),
    ROUNDABOUT_1("[ROUNDABOUT 1]"),
    ROUNDABOUT_2("[ROUNDABOUT 2]"),
    ROUNDABOUT_3("[ROUNDABOUT 3]"),
    ROUNDABOUT_4("[ROUNDABOUT 4]"),
    ROUNDABOUT("[ROUNDABOUT]"),
    EXIT("[EXIT]"),
    RAMP("[RAMP]"),
    MERGE("[MERGE]"),
    FORK_LEFT("[FORK L]"),
    FORK_RIGHT("[FORK R]"),
    SHARP_LEFT("[SHARP L]"),
    SHARP_RIGHT("[SHARP R]"),
    SLIGHT_LEFT("[SLIGHT L]"),
    SLIGHT_RIGHT("[SLIGHT R]"),
    KEEP_LEFT("[KEEP L]"),
    KEEP_RIGHT("[KEEP R]"),
    LEFT("[LEFT]"),
    RIGHT("[RIGHT]"),
    STRAIGHT("[STRAIGHT]"),
    NONE("");

    companion object {
        fun fromText(rawText: String): NavDirection {
            // 1. Strip landmark directional noise like "(on the left)" or "destination on the right"
            val cleaned = rawText
                .replace(Regex("""(?i)\(on the (?:left|right)\)"""), "")
                .replace(Regex("""(?i)\bpass by [^)]+\)"""), "")
                .replace(Regex("""(?i)\bpass by .*$"""), "")
                .replace(Regex("""(?i)\bdestination (?:will be )?on the (?:left|right)\b"""), "")
                .lowercase()

            // 2. Roundabout exit check
            val exitMatch = Regex("""(\d+)(?:st|nd|rd|th)?\s+exit""").find(cleaned)
            if (exitMatch != null) {
                return when (exitMatch.groupValues[1]) {
                    "1" -> ROUNDABOUT_1
                    "2" -> ROUNDABOUT_2
                    "3" -> ROUNDABOUT_3
                    "4" -> ROUNDABOUT_4
                    else -> ROUNDABOUT
                }
            }

            // 3. High-priority maneuvers
            if (cleaned.contains("arrive") || cleaned.contains("destination")) return ARRIVED
            if (cleaned.contains("u-turn") || cleaned.contains("make a u")) return U_TURN
            if (listOf("roundabout", "traffic circle", "rotary", " cir", " circle").any { cleaned.contains(it) }) return ROUNDABOUT
            if (cleaned.contains("take exit") || cleaned.contains("highway exit")) return EXIT
            if (cleaned.contains("ramp") || cleaned.contains("on-ramp")) return RAMP
            if (cleaned.contains("merge")) return MERGE
            if (cleaned.contains("fork left")) return FORK_LEFT
            if (cleaned.contains("fork right")) return FORK_RIGHT
            if (cleaned.contains("sharp left")) return SHARP_LEFT
            if (cleaned.contains("sharp right")) return SHARP_RIGHT
            if (cleaned.contains("slight left") || cleaned.contains("bear left")) return SLIGHT_LEFT
            if (cleaned.contains("slight right") || cleaned.contains("bear right")) return SLIGHT_RIGHT
            if (cleaned.contains("keep left")) return KEEP_LEFT
            if (cleaned.contains("keep right")) return KEEP_RIGHT

            // 4. Explicit turn phrases
            if (Regex("""\b(?:turn|take a)\s+left\b""").containsMatchIn(cleaned)) return LEFT
            if (Regex("""\b(?:turn|take a)\s+right\b""").containsMatchIn(cleaned)) return RIGHT

            // 5. Standalone Left vs Right (first occurrence determines direction)
            val leftIndex = Regex("""\bleft\b""").find(cleaned)?.range?.first ?: -1
            val rightIndex = Regex("""\bright\b""").find(cleaned)?.range?.first ?: -1

            if (leftIndex != -1 && rightIndex != -1) {
                return if (leftIndex < rightIndex) LEFT else RIGHT
            } else if (leftIndex != -1) {
                return LEFT
            } else if (rightIndex != -1) {
                return RIGHT
            }

            // 6. Straight / Continue
            if (listOf("straight", "continue", "head ").any { cleaned.contains(it) }) return STRAIGHT

            return NONE
        }
    }
}

class MapsListenerService : NotificationListenerService() {

    private val mapsPackage = "com.google.android.apps.maps"
    private val channelId = "cmf_maps_channel"
    private var lastSummary = ""

    private val distanceTokenRegex = Regex("""(?i)\b\d+(?:[.,]\d+)?\s*(?:m|km|ft|mi)\b""")

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == mapsPackage) {
            val extras = sbn.notification.extras

            val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
            val rawSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

            if (rawTitle.isEmpty() && rawText.isEmpty()) return

            val fullText = "$rawTitle $rawText $rawSubText".replace("·", "-").replace("•", "-")
            if (fullText == lastSummary) return
            lastSummary = fullText

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
                .replace(Regex("""(?i)\(pass by [^)]+\)"""), "")
                .replace(Regex("""(?i)\bpass by .*$"""), "")
                .replace(Regex("""(?i)\bdestination will be .*$"""), "")
                .replace(Regex("""(?i)\bgo through \d+ roundabout\b"""), "")
                .trim(' ', '-', '·', '•', ':', ',')

            if (instruction.isEmpty()) instruction = "Continue"

            if (instruction.length > 45) {
                val boundary = instruction.lastIndexOf(' ', 42)
                instruction = if (boundary != -1) instruction.substring(0, boundary) + "..." else instruction.take(42) + "..."
            }

            val cleanEta = rawSubText.replace("·", "|").replace("•", "|").trim(' ', '-', '|')
            val watchBody = if (cleanEta.isNotEmpty()) "$instruction\n[$cleanEta]" else instruction

            val direction = NavDirection.fromText("$rawTitle $rawText $instruction")
            val formattedTitle = when {
                direction != NavDirection.NONE && distance.isNotEmpty() -> "${direction.badge} $distance"
                distance.isNotEmpty() -> distance
                direction != NavDirection.NONE -> "${direction.badge} NOW"
                else -> "NAVIGATION"
            }

            pushToWatch(formattedTitle, watchBody)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName == mapsPackage) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(101)
            lastSummary = ""
        }
    }

    private fun pushToWatch(title: String, text: String) {
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
                .bigText(text)

            val notification = Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setAutoCancel(true)
                .build()

            manager.notify(101, notification)
        }
    }
}