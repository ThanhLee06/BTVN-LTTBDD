package com.example.uthtodo.data

import com.example.uthtodo.BuildConfig
import com.example.uthtodo.model.TodoSubtask
import com.example.uthtodo.model.TodoTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface TaskApi {
    suspend fun list(): List<TodoTask>
    suspend fun detail(id: String): TodoTask
    suspend fun create(task: TodoTask): TodoTask
    suspend fun update(task: TodoTask)
    suspend fun delete(id: String)
}

class TaskApiException(val status: Int, serverMessage: String? = null) : IOException(serverMessage ?: when (status) {
    401, 403 -> "Không thể xác thực API. Kiểm tra cấu hình access token."
    404 -> "Không tìm thấy công việc."
    else -> "Máy chủ chưa xử lý được yêu cầu (HTTP $status). Thử lại sau."
})

class HttpTaskApi(private val token: String = BuildConfig.MOCK_API_TOKEN) : TaskApi {
    override suspend fun list(): List<TodoTask> = JSONArray(request("GET", "")).let { array ->
        List(array.length()) { decode(array.getJSONObject(it)) }
    }
    override suspend fun detail(id: String): TodoTask = decode(JSONObject(request("GET", path(id))))
    override suspend fun create(task: TodoTask): TodoTask = decode(JSONObject(request("POST", "", encode(task))))
    override suspend fun update(task: TodoTask) { request("PATCH", path(task.id), encode(task)) }
    override suspend fun delete(id: String) { request("DELETE", path(id)) }

    private fun path(id: String): String {
        require(id.matches(Regex("[0-9]+"))) { "Mã công việc không hợp lệ." }
        return "/$id"
    }

    private suspend fun request(method: String, path: String, body: JSONObject? = null): String = withContext(Dispatchers.IO) {
        check(token.isNotBlank()) { "Chưa cấu hình MOCK_API_TOKEN cho ứng dụng." }
        val connection = URL("https://share.tryapps.store/mock/teach/tasks$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            if (status !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                val serverMessage = runCatching { JSONObject(errorBody.orEmpty()).optString("message") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }?.replace(token, "[token]")
                throw TaskApiException(status, serverMessage)
            }
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally { connection.disconnect() }
    }

    companion object {
        fun decode(json: JSONObject) = TodoTask(
            id = json.get("id").toString(), title = json.getString("title"),
            category = if (json.getString("category") == "study") "Học tập" else "Cuộc sống",
            time = json.getString("time"), isCompleted = json.optBoolean("done"),
            date = json.getString("date"), note = json.optString("note", ""),
            priority = json.optString("priority", "medium"), remind = json.optBoolean("remind"),
            subtasks = (json.optJSONArray("subtasks") ?: JSONArray()).let { list ->
                List(list.length()) { index -> list.getJSONObject(index).let {
                    TodoSubtask(it.getString("title"), it.optBoolean("done"))
                } }
            }
        )

        fun encode(task: TodoTask): JSONObject = JSONObject().apply {
            put("title", task.title.trim()); put("category", if (task.category == "Học tập") "study" else "life")
            put("date", task.date); put("time", task.time); put("note", task.note)
            put("priority", task.priority); put("remind", task.remind); put("done", task.isCompleted)
            put("subtasks", JSONArray().apply { task.subtasks.forEach {
                put(JSONObject().put("title", it.title).put("done", it.done))
            } })
        }
    }
}
