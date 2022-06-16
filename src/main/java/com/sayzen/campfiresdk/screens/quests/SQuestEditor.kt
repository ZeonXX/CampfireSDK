package com.sayzen.campfiresdk.screens.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.dzen.campfire.api.models.quests.QuestPartText
import com.dzen.campfire.api.requests.quests.RQuestsModify
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardQuestDetails
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.screens.quests.edit.CardQuestVariables
import com.sayzen.campfiresdk.screens.quests.edit.SQuestPartTextCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.CardTitle
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.splash.SplashFieldTwo
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json

class SQuestEditor(
    private var questDetails: QuestDetails,
) : SRecycler() {
    companion object {
        const val partsOffset = 3
    }

    val eventBus = EventBus
        .subscribe(EventQuestChanged::class) {
            if (it.quest.id == questDetails.id) {
                questDetails = it.quest
                onDetailsUpdated()
            }
        }

    private val adapter = RecyclerCardAdapter()

    init {
        setScreenColorBackground()

        onDetailsUpdated()
        addTitleCards()

        vFab.visibility = VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            openNewQuestPart()
        }

        vRecycler.adapter = adapter
    }

    private fun onDetailsUpdated() {
        setTitle(questDetails.title)

        val cardDetails = CardQuestDetails(questDetails) {
            openDetailsEditor()
        }
        if (!adapter.isEmpty) adapter.replace(0, cardDetails)
        else adapter.add(cardDetails)
    }

    private fun addTitleCards() {
        adapter.add(CardQuestVariables(questDetails))
        adapter.add(CardTitle(t(API_TRANSLATE.quests_contents)))
    }

    private fun openDetailsEditor() {
        SplashFieldTwo()
            .setTitle(t(API_TRANSLATE.quests_edit_details))
            .setHint_1(t(API_TRANSLATE.quests_title))
            .setText_1(questDetails.title)
            .setMin_1(API.QUEST_TITLE_MIN_L)
            .setMax_1(API.QUEST_TITLE_MAX_L)
            .setLinesCount_1(1)

            .setHint_2(t(API_TRANSLATE.app_description))
            .setText_2(questDetails.description)
            .setMax_2(API.QUEST_DESCRIPTION_MAX_L)
            .setMultiLine_2()

            .setOnEnter(t(API_TRANSLATE.app_change)) { _, title, description ->
                editQuestDetails(questDetails) {
                    it.title = title
                    it.description = description
                }
            }
            .asSheetShow()
    }

    private val container = object : QuestPartContainer {
        override fun getParts(): Array<QuestPart> = emptyArray()
    }

    private fun openNewQuestPart() {
        SplashMenu()
            .add(t(API_TRANSLATE.quests_part_text)) {
                Navigator.to(SQuestPartTextCreate(
                    questDetails,
                    container,
                    QuestPartText()
                ) {})
            }
            .add(t(API_TRANSLATE.quests_part_condition))
            .add(t(API_TRANSLATE.quests_part_action))
            .asSheetShow()
    }
}

fun editQuestDetails(questDetails: QuestDetails, modify: (QuestDetails) -> Unit) {
    val newDetails = QuestDetails()
    // i'm sorry
    questDetails.jsonDB = questDetails.jsonDB(true, Json())
    newDetails.json(false, questDetails.json(true, Json()))
    modify(newDetails)
    newDetails.jsonDB = newDetails.jsonDB(true, Json())
    ApiRequestsSupporter.executeProgressDialog(RQuestsModify(newDetails)) { resp ->
        ToolsToast.show(t(API_TRANSLATE.app_done))
        EventBus.post(EventQuestChanged(resp.quest))
    }.onApiError(RQuestsModify.E_INVALID_VARS) {
        if (newDetails.variables.size > API.QUEST_VARIABLES_MAX)
            ToolsToast.show(t(API_TRANSLATE.quests_variable_too_many))
        else
            ToolsToast.show(t(API_TRANSLATE.quests_variable_too_long))
    }.onApiError(RQuestsModify.E_INVALID_NAME) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_name))
    }.onApiError(RQuestsModify.E_INVALID_DESCRIPTION) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_description))
    }.onApiError(RQuestsModify.E_NOT_DRAFT) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_not_draft))
    }
}
