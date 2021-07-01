package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.wiki.EventWikiCreated
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiList(
        val fandomId: Long,
        val prefLanguageId: Long,
        val itemId: Long,
        val itemName: String
) : SLoadingRecycler<CardWikiItem, WikiTitle>() {

    companion object {

        fun instanceFandomId(fandomId: Long, action: NavigationAction) {
            Navigator.action(action, SWikiList(fandomId, 0, 0, ""))
        }

        fun instanceItemId(wikiItemId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RWikiItemGet(wikiItemId)) { r ->
                SWikiList(r.wikiTitle.fandomId, 0, r.wikiTitle.itemId, r.wikiTitle.getName(ControllerApi.getLanguageCode()))
            }
        }


    }

    private val eventBus = EventBus.subscribe(EventWikiCreated::class) { if (it.item.fandomId == fandomId && it.item.parentItemId == itemId) adapter?.reloadBottom() }

    init {
        disableShadows()
        disableNavigation()

        addToolbarIcon(R.drawable.ic_insert_link_white_24dp) {
            if (itemId > 0) ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiItemId(itemId))
            else ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiFandomId(fandomId))
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        if (itemName.isEmpty()) setTitle(t(API_TRANSLATE.app_wiki)) else setTitle(itemName)
        setTextEmpty(t(API_TRANSLATE.wiki_list_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_29)
        (vFab as View).visibility = if (ControllerApi.can(fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT)) View.VISIBLE else View.GONE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { Navigator.to(SWikiItemCreate(fandomId, itemId)) }

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RWikiListGet(fandomId, itemId, cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.items)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardWikiItem::class

    override fun map(item: WikiTitle) = CardWikiItem(item, prefLanguageId)

}
