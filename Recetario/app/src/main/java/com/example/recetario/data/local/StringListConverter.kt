package com.example.recetario.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class StringListConverter {
    @TypeConverter
    fun fromStringList(values: List<String>): String {
        val array = JSONArray()
        values.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return runCatching {
            val array = JSONArray(value)
            List(array.length()) { index -> array.optString(index) }
                .filter { it.isNotBlank() }
        }.getOrElse { emptyList() }
    }
}
