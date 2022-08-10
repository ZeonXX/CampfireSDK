package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewText

class CardQuestPartText(
    part: QuestPartText,
    container: QuestPartContainer,
) : CardQuestPart(R.layout.card_quest_part_short_text, part, container) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vTitle.text = part.toSelectorString()

        val part = part as QuestPartText

        val desc = StringBuilder()
        desc.append(part.text.substring(0, 100.coerceAtMost(part.text.length)))
        if (part.inputs.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.inputs.joinToString("\n") {
                val inputType = t(API_TRANSLATE.forQuestType(it.type))
                "${it.hint.ifEmpty { t(API_TRANSLATE.quests_part_text_input) }} ($inputType)"
            })
        }
        if (part.buttons.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.buttons.joinToString("\n") {
                val dest = t(API_TRANSLATE.quests_edit_text_button_jump_to, jumpToIdToString(it.jumpToId, container))
                "[${it.label}] $dest"
            })
        }
        if (part.effects.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.effects.joinToString("\n") {
                when (it) {
                    is QuestEffectBox -> t(API_TRANSLATE.fromBox(it.box))
                    is QuestEffectBoxReset -> t(API_TRANSLATE.quests_effect_box_reset)
                    is QuestEffectVibrate -> t(API_TRANSLATE.quests_effect_vibrate)
                    else -> t(API_TRANSLATE.quests_effect_unknown)
                }
            })
        }

        vDescription.text = desc
    }
}