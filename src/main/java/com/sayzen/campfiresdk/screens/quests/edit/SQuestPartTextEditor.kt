package com.sayzen.campfiresdk.screens.quests.edit

import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.post.create.creators.SplashAdd
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged

class SQuestPartTextEditor(
    val oldText: String = "",
    val onEnter: (String) -> Unit
) : Screen(R.layout.screen_post_create_text) {
    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_text))

        findViewById<LinearLayout>(R.id.vOptionsContainer).visibility = GONE

        vField.hint = t(API_TRANSLATE.quests_edit_text_content_hint)
        vField.isSingleLine = false
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.setText(oldText)
        vField.addTextChangedListener(TextWatcherChanged { update() })
        ControllerMention.startFor(vField)

        vFab.setOnClickListener {
            if (oldText != vField.text.toString()) {
                onEnter(vField.text.toString())
            }
            Navigator.remove(this)
        }

        update()
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    override fun onBackPressed(): Boolean {
        if (oldText == vField.text.toString()) return false
        SplashAdd.showConfirmCancelDialog(this)
        return true
    }

    private fun update() {
        val s = vField.text
        ToolsView.setFabEnabledR(vFab, s.isNotEmpty() && s.length <= API.QUEST_TEXT_TEXT_MAX_L, R.color.green_700)
    }
}