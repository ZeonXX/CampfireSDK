package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.screens.quests.SQuest
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus

class CardQuestDetails constructor(
    var questDetails: QuestDetails,
    private val onClick: (() -> Unit)? = null,
    private val onPublish: (() -> Unit)? = null,
) : CardPublication(R.layout.card_quest_details, questDetails) {
    private val eventBus = EventBus
        .subscribe(EventQuestChanged::class) {
            if (it.quest.id == questDetails.id) {
                questDetails = it.quest
                update()
            }
        }
        .subscribe(EventPublicationRemove::class) {
            if (it.publicationId == questDetails.id) {
                adapter.remove(this)
            }
        }
        .subscribe(EventPostStatusChange::class) {
            if (it.publicationId == questDetails.id && it.status != API.STATUS_PUBLIC) {
                adapter.remove(this)
            }
        }

    override fun bindView(view: View) {
        super.bindView(view)
        val vTouch: LinearLayout = view.findViewById(R.id.vTouch)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)
        val vPublish: ViewButton = view.findViewById(R.id.vPublish)

        vAvatar.setTitle(questDetails.title)
        vAvatar.setSubtitle(t(API_TRANSLATE.quest))
        XAccount().apply {
            setAccount(questDetails.creator)
            setView(vAvatar.vAvatar)
        }

        if (onPublish != null) {
            vPublish.visibility = View.VISIBLE
            vPublish.text = t(API_TRANSLATE.app_publish)
            vPublish.setOnClickListener { onPublish.invoke() }
        } else {
            vPublish.visibility = View.GONE
        }

        vDescription.text = questDetails.description.ifEmpty { "Нет описания" }
        ControllerLinks.makeLinkable(vDescription)

        if (onClick != null) vTouch.setOnClickListener { onClick.invoke() }
        else vTouch.setOnClickListener { Navigator.to(SQuest(questDetails, 0)) }
    }

    override fun updateAccount() {}
    override fun updateFandom() {}
    override fun updateKarma() {}
    override fun updateComments() {}
    override fun updateReports() {}
    override fun updateReactions() {}
    override fun notifyItem() {}
}