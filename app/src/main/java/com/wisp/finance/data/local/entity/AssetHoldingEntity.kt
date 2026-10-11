package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.AssetKind
import com.wisp.finance.domain.model.CurrencyCode
import java.time.Instant

@Entity(tableName = "asset_holdings", indices = [Index("kind"), Index("archived"), Index("symbol")])
data class AssetHoldingEntity(
    @PrimaryKey val id: String,
    val kind: AssetKind,
    val name: String,
    val symbol: String,
    val quantity: Double,
    @ColumnInfo(name = "average_cost_minor_units") val averageCostMinorUnits: Long,
    @ColumnInfo(name = "current_price_minor_units") val currentPriceMinorUnits: Long,
    val currency: CurrencyCode,
    val note: String,
    val archived: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
