package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestEffectBox
import com.dzen.campfire.api.models.quests.QuestEffectBoxReset
import com.dzen.campfire.api.models.quests.QuestEffectVibrate
import com.dzen.campfire.api.models.quests.QuestPartText
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewText

class CardQuestPartText(val part: QuestPartText) : CardQuestPart(R.layout.card_quest_part_short_text) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vTitle.text = t(API_TRANSLATE.quests_part_title, t(API_TRANSLATE.quests_part_text), part.devLabel)

        val desc = StringBuilder()
        desc.append(part.text.substring(0, 100.coerceAtMost(part.text.length)))
        if (part.inputs.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.inputs.joinToString("\n") {
                val inputType = when (it.type) {
                    API.QUEST_TYPE_TEXT -> t(API_TRANSLATE.quests_variable_string)
                    API.QUEST_TYPE_NUMBER -> t(API_TRANSLATE.quests_variable_number)
                    API.QUEST_TYPE_BOOL -> t(API_TRANSLATE.quests_variable_bool)
                    else -> t(API_TRANSLATE.quests_variable_unknown)
                }
                "${it.label ?: t(API_TRANSLATE.quests_part_text_input)} ($inputType)"
            })
        }
        if (part.buttons.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.buttons.joinToString(" | ") { it.label })
        }
        if (part.effects.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.effects.joinToString("\n") {
                when (it) {
                    is QuestEffectBox -> t(API_TRANSLATE.quests_effect_box, it.box.link)
                    is QuestEffectBoxReset -> t(API_TRANSLATE.quests_effect_box_reset)
                    is QuestEffectVibrate -> t(API_TRANSLATE.quests_effect_vibrate)
                    else -> t(API_TRANSLATE.quests_effect_unknown)
                }
            })
        }

        vDescription.text = desc
    }
}