package com.example.uthtodo.features

import com.example.uthtodo.data.HttpTaskApi
import org.json.JSONArray
import org.json.JSONObject

object FeatureCodec {
    fun encode(data: LocalTaskData): String = JSONObject().apply {
        put("task", HttpTaskApi.encode(data.task).put("id", data.task.id))
        put("identity", data.identity); put("changed", data.changed); put("deleted", data.deleted)
        put("requested", data.reminderRequested); put("due", data.reminderDue)
        put("version", data.version); put("delivered", data.deliveredVersion)
        data.location?.let { location -> put("location", JSONObject().apply {
            put("lat", location.latitude); put("lon", location.longitude); put("accuracy", location.accuracyMeters)
            put("at", location.capturedAt); put("precise", location.precise); put("label", location.label)
        }) }
        put("photos", JSONArray().apply { data.photos.forEach { photo ->
            put(JSONObject().put("id", photo.id).put("path", photo.path).put("at", photo.createdAt))
        } })
    }.toString()

    fun decode(raw: String): LocalTaskData = JSONObject(raw).let { json ->
        LocalTaskData(HttpTaskApi.decode(json.getJSONObject("task")), json.getString("identity"),
            location = json.optJSONObject("location")?.let {
                TaskLocation(it.getDouble("lat"), it.getDouble("lon"), it.getDouble("accuracy").toFloat(),
                    it.getLong("at"), it.optBoolean("precise"), it.optString("label"))
            }, photos = (json.optJSONArray("photos") ?: JSONArray()).let { photos ->
                List(photos.length()) { index -> photos.getJSONObject(index).let {
                    TaskPhoto(it.getString("id"), it.getString("path"), it.getLong("at"))
                } }
            }, changed = json.optBoolean("changed"), deleted = json.optBoolean("deleted"),
            reminderRequested = json.optBoolean("requested"), reminderDue = json.optLong("due"),
            version = json.optLong("version"), deliveredVersion = json.optLong("delivered", -1))
    }
}
