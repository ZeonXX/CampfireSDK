package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.chat.ChatTag
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import com.sup.dev.java.tools.ToolsCollections

class MChatMessagesPool(
        var chatTag:ChatTag,
        var showInNavigation:Boolean = true,
        var messages:Array<String> = emptyArray(),
        var count:Int = 0
) : JsonParsable{

    val MAX_MESSAGES = 10

    override fun json(inp: Boolean, json: Json): Json {
        messages = json.m(inp, "messages", messages)
        if(messages.size > MAX_MESSAGES) messages = ToolsCollections.subarray(messages, messages.size - MAX_MESSAGES, MAX_MESSAGES)
        chatTag = json.m(inp, "chatTag", chatTag)
        count = json.m(inp, "count", count)
        showInNavigation = json.m(inp, "showInNavigation", showInNavigation)
        return json
    }

    fun add(message:String):MChatMessagesPool{
        count++
        messages = ToolsCollections.add(message, messages)
        if(messages.size > MAX_MESSAGES) messages = ToolsCollections.subarray(messages, messages.size - MAX_MESSAGES, MAX_MESSAGES)
        return this
    }

    fun setCount(count:Int):MChatMessagesPool{
        this.count = count
        return this
    }

    fun text():String{
        var text = ""
        var b = false
        for(i in messages) {
            if(i.trim().isEmpty()) break
            if(!b) b = true else text+="\n"
            text += i
        }
        return text
    }

    fun count() = count
    fun isEmpty() = count == 0
    fun isNotEmpty() = count > 0


}