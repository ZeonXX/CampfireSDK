package com.sayzen.campfiresdk.screens.quests

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGetDrafts
import com.dzen.campfire.api.requests.quests.RQuestsNew
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardQuestDetails
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.java.libs.eventBus.EventBus
import kotlin.reflect.KClass

class SQuestDrafts : SLoadingRecycler<CardQuestDetails, QuestDetails>() {
    private val eventBus = EventBus
        .subscribe(EventQuestChanged::class) { ev ->
            if (! adapter.get(CardQuestDetails::class).any { it.questDetails.id == ev.quest.id })
                adapter.reloadBottom()
        }

    init {
        setScreenColorBackground()
        setTitle(t(API_TRANSLATE.quests_drafts))
        setTextEmpty(t(API_TRANSLATE.quests_empty))
        setTextProgress(t(API_TRANSLATE.quests_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_2)

        vFab.visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            SplashField()
                .setTitle(t(API_TRANSLATE.quests_new))
                .setHint(t(API_TRANSLATE.quests_title))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.QUEST_TITLE_MIN_L)
                .setMax(API.QUEST_TITLE_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_create)) { splash, title ->
                    ApiRequestsSupporter.executeEnabled(splash, RQuestsNew(title, ControllerApi.getLanguageId())) {
                        EventBus.post(EventQuestChanged(it.quest))
                    }
                }
                .asSheetShow()
        }

        adapter.setBottomLoader { onLoad, cards ->
            val r = RQuestsGetDrafts(cards.size.toLong())
                .onComplete { r -> onLoad(r.quests) }
                .onNetworkError { onLoad(null) }
            r.send(api)
        }
    }

    override fun classOfCard(): KClass<CardQuestDetails> = CardQuestDetails::class

    override fun map(item: QuestDetails): CardQuestDetails {
        return CardQuestDetails(item)
    }
}