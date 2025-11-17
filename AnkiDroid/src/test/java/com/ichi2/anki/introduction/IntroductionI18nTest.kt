package com.ichi2.anki.introduction

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.R
import com.ichi2.anki.RobolectricTest
import com.ichi2.utils.LanguageUtil.getStringByLocale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class IntroductionI18nTest : RobolectricTest() {

    @Test
    fun stringsEnglish() {
        val ctx = targetContext
        val title = ctx.getStringByLocale(R.string.intro_before_continuing, Locale.ENGLISH)
        assertEquals("Before continuing!", title)

        val donation = ctx.getStringByLocale(R.string.intro_donation_notice, Locale.ENGLISH)
        assertEquals("This app is a fork of AnkiDroid; please consider donating to the AnkiDroid team to support their work. The creator of Anki has kindly allowed the use of AnkiWeb sync. If you'd like to support him, please consider buying the iPhone version of Anki.", donation)

        val contact = ctx.getStringByLocale(R.string.intro_contact_notice, Locale.ENGLISH)
        assertEquals("If you have any issues with this version, please contact me and not the AnkiDroid team. Happy memorizing!", contact)

        val donate = ctx.getStringByLocale(R.string.intro_donate_ankidroid, Locale.ENGLISH)
        assertEquals("Donate to AnkiDroid", donate)
    }

    @Test
    fun stringsJapanese() {
        val ctx = targetContext
        val titleJa = ctx.getStringByLocale(R.string.intro_before_continuing, Locale.JAPANESE)
        assertEquals("続ける前に！", titleJa)

        val donationJa = ctx.getStringByLocale(R.string.intro_donation_notice, Locale.JAPANESE)
        assertEquals("このアプリはAnkiDroidのフォークです。AnkiDroidチームの活動を支援するために寄付をご検討ください。Ankiの作者はAnkiWeb同期の使用を快く許可してくれました。作者を支援したい場合は、AnkiのiPhone版の購入をご検討ください。", donationJa)

        val contactJa = ctx.getStringByLocale(R.string.intro_contact_notice, Locale.JAPANESE)
        assertEquals("このバージョンに問題がある場合は、AnkiDroidチームではなく私にご連絡ください。学習をお楽しみください！", contactJa)

        val donateJa = ctx.getStringByLocale(R.string.intro_donate_ankidroid, Locale.JAPANESE)
        assertEquals("AnkiDroidに寄付する", donateJa)
    }
}
