package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGetParts
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.screens.quests.SQuestEditor
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus

class CardQuestDetails constructor(
    var questDetails: QuestDetails,
    private val onClick: (() -> Unit)? = null,
) : Card(R.layout.card_quest_details) {
    private val eventBus = EventBus
        .subscribe(EventQuestChanged::class) {
            if (it.quest.id == questDetails.id) {
                questDetails = it.quest
                update()
            }
        }

    override fun bindView(view: View) {
        super.bindView(view)
        val vTouch: LinearLayout = view.findViewById(R.id.vTouch)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vAvatar.setTitle(questDetails.title)
        vAvatar.setSubtitle(t(API_TRANSLATE.quest))
        XAccount().apply {
            setAccount(questDetails.creator)
            setView(vAvatar.vAvatar)
        }

        vDescription.text = questDetails.description.ifEmpty { "Нет описания" }
        ControllerLinks.makeLinkable(vDescription)

        vTouch.setOnClickListener {
            if (onClick != null) onClick.invoke()
            else {
                ApiRequestsSupporter.executeProgressDialog(RQuestsGetParts(questDetails.id)) { resp ->
                    Navigator.to(SQuestEditor(questDetails, resp.parts))
                }
            }
        }
    }
}