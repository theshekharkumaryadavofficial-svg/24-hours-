package com.example.bedtime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

/**
 * Full-Screen Red, Black & White RADHA NAAM JAP COUNTER BLOCKER overlay.
 * Whenever a disallowed or newly installed app is opened during any active
 * 24H Dincharya Time Slot, Study Mode, or Bedtime Window, this interactive
 * full-screen Naam Jap Counter covers the app — turning every distraction into
 * permanent Radha Naam Jap!
 */
class BedtimeOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    fun isShowing(): Boolean = overlayView != null

    fun showNaamJapBlockerOverlay(
        blockedAppLabel: String,
        slotTitle: String,
        windowText: String,
        blockedCount: Int,
        initialTodayNaamJap: Int,
        initialAllTimeNaamJap: Int,
        headerBadgeText: String = "24 HRS · SLOT APP LOCK ACTIVE",
        ruleMessage: String = "Only apps enabled for this time slot can open. Tap anywhere to chant Radha Naam!",
        returnButtonText: String = "GO HOME · BACK TO SCHEDULE",
        endButtonText: String = "END ACTIVE GUARD",
        dimScreenForSleep: Boolean = false,
        onIncrementNaamJap: () -> Unit,
        onReturnHome: () -> Unit,
        onEndGuard: () -> Unit
    ): Boolean {
        if (!Settings.canDrawOverlays(context)) return false
        dismiss()

        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

        val fmt = NumberFormat.getNumberInstance(Locale.US)
        var liveToday = initialTodayNaamJap
        var liveTotal = initialAllTimeNaamJap

        val rootBg = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#E51932"),
                Color.parseColor("#C8102E"),
                Color.parseColor("#8C081B")
            )
        )

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = rootBg
            setPadding(dp(24), dp(52), dp(24), dp(36))
            isClickable = true
            isFocusable = true
        }

        // Top jet-black guard badge
        val badgeBg = GradientDrawable().apply {
            setColor(Color.parseColor("#0A0A0C"))
            cornerRadius = dp(50).toFloat()
            setStroke(dp(1), Color.parseColor("#55FFFFFF"))
        }
        val badgeView = TextView(context).apply {
            text = headerBadgeText
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            background = badgeBg
            setPadding(dp(16), dp(7), dp(16), dp(7))
        }
        root.addView(badgeView)

        // Blocked App + Active Slot info strip (Pure White contrast pill)
        val blockedCardBg = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(18).toFloat()
        }
        val blockedCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = blockedCardBg
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
        }
        val blockedTitle = TextView(context).apply {
            text = "BLOCKED: $blockedAppLabel"
            setTextColor(Color.parseColor("#C8102E"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        blockedCard.addView(blockedTitle)

        val slotSubtitle = TextView(context).apply {
            text = "$slotTitle · $windowText  (Blocked #$blockedCount)"
            setTextColor(Color.parseColor("#0A0A0C"))
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(3), 0, 0)
        }
        blockedCard.addView(slotSubtitle)
        root.addView(blockedCard)

        // Center Interactive Full-Screen RADHA NAAM JAP COUNTER Tap Zone
        val japZone = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(dp(8), dp(16), dp(8), dp(16))
        }

        val radhaHindi = TextView(context).apply {
            text = "राधा"
            setTextColor(Color.WHITE)
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        japZone.addView(radhaHindi)

        val radhaEng = TextView(context).apply {
            text = "R A D H A   N A A M   J A P"
            setTextColor(Color.parseColor("#FFE5E9"))
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(18))
        }
        japZone.addView(radhaEng)

        val todayCounterBg = GradientDrawable().apply {
            setColor(Color.parseColor("#660A0A0C"))
            cornerRadius = dp(24).toFloat()
            setStroke(dp(1), Color.parseColor("#66FFFFFF"))
        }
        val todayCounterBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = todayCounterBg
            setPadding(dp(28), dp(14), dp(28), dp(14))
        }

        val todayLabel = TextView(context).apply {
            text = "AAJ KA NAAM JAP"
            setTextColor(Color.parseColor("#FFE5E9"))
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        todayCounterBox.addView(todayLabel)

        val todayCountView = TextView(context).apply {
            text = fmt.format(liveToday)
            setTextColor(Color.WHITE)
            textSize = 54f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        todayCounterBox.addView(todayCountView)

        val malaAllTimeView = TextView(context).apply {
            text = "MALAS: ${liveToday / 108} (${liveToday % 108}/108)  ·  ALL TIME: ${fmt.format(liveTotal)}"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }
        todayCounterBox.addView(malaAllTimeView)
        japZone.addView(todayCounterBox)

        val tapHintView = TextView(context).apply {
            text = "TAP ANYWHERE FOR +1 RADHA (SAVED PERMANENTLY)"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(6))
        }
        japZone.addView(tapHintView)

        val ruleView = TextView(context).apply {
            text = ruleMessage
            setTextColor(Color.parseColor("#FFE5E9"))
            textSize = 12f
            gravity = Gravity.CENTER
        }
        japZone.addView(ruleView)

        val handleChantTap = View.OnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            liveToday += 1
            liveTotal += 1
            todayCountView.text = fmt.format(liveToday)
            malaAllTimeView.text =
                "MALAS: ${liveToday / 108} (${liveToday % 108}/108)  ·  ALL TIME: ${fmt.format(liveTotal)}"
            radhaHindi.animate().cancel()
            radhaHindi.scaleX = 1.12f
            radhaHindi.scaleY = 1.12f
            radhaHindi.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
            onIncrementNaamJap()
        }
        root.setOnClickListener(handleChantTap)
        japZone.setOnClickListener(handleChantTap)
        root.addView(japZone)

        // Primary Jet Black Return Home button
        val homeBtnBg = GradientDrawable().apply {
            setColor(Color.parseColor("#0A0A0C"))
            cornerRadius = dp(16).toFloat()
        }
        val returnBtn = Button(context).apply {
            text = returnButtonText
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            background = homeBtnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
            setOnClickListener {
                dismiss()
                onReturnHome()
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runCatching { context.startActivity(homeIntent) }
            }
        }
        root.addView(returnBtn)

        // Secondary Emergency Exit button
        val exitBtnBg = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(dp(1), Color.WHITE)
            cornerRadius = dp(14).toFloat()
        }
        val exitBtn = Button(context).apply {
            text = endButtonText
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            background = exitBtnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(10)
            }
            setOnClickListener {
                dismiss()
                onEndGuard()
            }
        }
        root.addView(exitBtn)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            if (dimScreenForSleep) {
                screenBrightness = 0.10f
            }
        }

        return try {
            windowManager.addView(root, params)
            overlayView = root
            true
        } catch (_: Exception) {
            overlayView = null
            false
        }
    }

    fun dismiss() {
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        overlayView = null
    }
}
