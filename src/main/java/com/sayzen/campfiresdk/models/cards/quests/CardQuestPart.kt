package com.sayzen.campfiresdk.models.cards.quests

import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem

abstract class CardQuestPart(
    layout: Int, val partContainer: QuestPartContainer, part: QuestPart
) : Card(layout), NotifyItem {

}