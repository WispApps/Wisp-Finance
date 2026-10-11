package com.wisp.finance.data.backup

import com.wisp.finance.security.ThreeWayMerge
import org.json.JSONArray
import org.json.JSONObject

class SyncConflict : Exception()
/** No last-writer-wins for money. Concurrent edits/deletions fail without changing either side. */
object SnapshotMerge {
    fun canonical(value: Any?): String = when(value) {
        null, JSONObject.NULL -> "null"
        is JSONObject -> value.keys().asSequence().toList().sorted().joinToString(",", "{", "}") { JSONObject.quote(it)+":"+canonical(value.get(it)) }
        is JSONArray -> (0 until value.length()).joinToString(",", "[", "]") { canonical(value.get(it)) }
        is Number, is Boolean -> value.toString()
        else -> JSONObject.quote(value.toString())
    }
    private fun rows(o: JSONObject?, table: String): Map<String, JSONObject> {
        val a=o?.getJSONArray(table) ?: return emptyMap()
        return (0 until a.length()).associate { a.getJSONObject(it).let { row -> row.getString("id") to row } }
    }
    fun merge(local: JSONObject, remote: JSONObject, base: JSONObject?, policy: Int = ThreeWayMerge.FAIL): JSONObject {
        val result=JSONObject(local.toString())
        for(table in FinanceSnapshot.TABLES) {
            val l=rows(local,table); val r=rows(remote,table); val b=rows(base,table)
            val merged=JSONArray()
            val values=try {
                ThreeWayMerge.merge(l.mapValues { canonical(it.value) },r.mapValues { canonical(it.value) },
                    if(base==null) null else b.mapValues { canonical(it.value) },policy)
            } catch(_: ThreeWayMerge.Conflict) { throw SyncConflict() }
            values.values.forEach { merged.put(JSONObject(it)) }
            result.put(table,merged)
        }
        // Currency and UI language are per-device preferences, never inferred from another device.
        FinanceSnapshot.decode(result)
        return result
    }
    fun dataOnly(o: JSONObject): String = canonical(JSONObject(o.toString()).apply { remove("primaryCurrency") })
}
