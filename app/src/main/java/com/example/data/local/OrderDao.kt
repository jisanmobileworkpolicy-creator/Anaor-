package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY createdAt DESC")
    fun getOrdersByPhone(phone: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    fun getOrderByNumber(orderNumber: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumberSync(orderNumber: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, trackingNotes = :notes, updatedAt = :updatedAt WHERE orderNumber = :orderNumber")
    suspend fun updateOrderStatus(orderNumber: String, status: String, notes: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'")
    suspend fun getPendingOrderCount(): Int

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status != 'CANCELLED'")
    suspend fun getTotalSales(): Double?
}
