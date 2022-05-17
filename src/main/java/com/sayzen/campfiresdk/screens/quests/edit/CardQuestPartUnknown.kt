package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestPartText
import com.dzen.campfire.api.models.quests.QuestPartUnknown
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewText

class CardQuestPartUnknown(val part: QuestPartUnknown) : CardQuestPart(R.layout.card_quest_part_short_text) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vTitle.text = t(API_TRANSLATE.quests_part_title, t(API_TRANSLATE.quests_part_unknown), part.devLabel)
        vDescription.text = t(API_TRANSLATE.quests_part_unknown_desc)
    }
}