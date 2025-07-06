package dev.sophie.bazaarflipper.api.models

import kotlinx.serialization.Serializable

@Serializable
data class BazaarData(
    val success: Boolean,
    val products: Map<String, ProductData>
)

@Serializable
data class ProductData(
    val product_id: String,
    val quick_status: QuickStatus,
    val sell_summary: List<OrderSummary>,
    val buy_summary: List<OrderSummary>
)

@Serializable
data class QuickStatus(
    val productId: String,
    val sellPrice: Double,
    val sellVolume: Int,
    val sellMovingWeek: Int,
    val sellOrders: Int,
    val buyPrice: Double,
    val buyVolume: Int,
    val buyMovingWeek: Int,
    val buyOrders: Int
)

@Serializable
data class OrderSummary(
    val amount: Int,
    val pricePerUnit: Double,
    val orders: Int
)

data class FlipCandidate(
    val productId: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val profit: Double,
    val profitPercentage: Double,
    val volume: Int
)
