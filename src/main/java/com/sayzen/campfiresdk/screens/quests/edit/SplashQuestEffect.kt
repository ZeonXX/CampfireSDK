package com.sayzen.campfiresdk.screens.quests.edit

import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.Splash

class SplashQuestEffect : Splash(R.layout.splash_quest_effect) {
    private val vEffectType: SettingsSelection = findViewById(R.id.vEffectType)

    init {
        setTitle(t(API_TRANSLATE.quests_effect_unknown))

        vEffectType.setTitle()
        vEffectType.add(t(API_TRANSLATE.quests_effect_box))
        vEffectType.add(t(API_TRANSLATE.quests_effect_box_reset))
        vEffectType.add(t(API_TRANSLATE.quests_effect_vibrate))

    }
}