package com.sayzen.campfiresdk.screens.quests.edit

import androidx.annotation.LayoutRes
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartText
import com.dzen.campfire.api.models.quests.QuestPartUnknown
import com.sup.dev.android.views.cards.Card

abstract class CardQuestPart(layout: Int) : Card(layout) {
    companion object {
        fun instance(questPart: QuestPart): CardQuestPart {
            return when (questPart) {
                is QuestPartText -> CardQuestPartText(questPart)
                else -> CardQuestPartUnknown(QuestPartUnknown())
            }
        }
    }
}