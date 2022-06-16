package com.sayzen.campfiresdk.screens.quests.edit

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartText
import com.dzen.campfire.api.models.quests.QuestPartUnknown
import com.dzen.campfire.api.models.translate.Translate
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

fun API_TRANSLATE.forQuestType(type: Long): Translate =
    when (type) {
        API.QUEST_TYPE_TEXT -> this.quests_variable_string
        API.QUEST_TYPE_NUMBER -> this.quests_variable_number
        API.QUEST_TYPE_BOOL -> this.quests_variable_bool
        else -> this.quests_variable_unknown
    }
