package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY isAvailable DESC, isBestSeller DESC, id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY isBestSeller DESC, id ASC")
    fun getAvailableProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: String): Product?

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isAvailable = 1 ORDER BY id ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isBestSeller = 1 AND isAvailable = 1 LIMIT 10")
    fun getBestSellers(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isNewArrival = 1 AND isAvailable = 1 LIMIT 10")
    fun getNewArrivals(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE condition = 'SECOND_HAND' AND isAvailable = 1 ORDER BY id ASC")
    fun getSecondHandProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE isDemoProduct = 1")
    suspend fun deleteDemoProducts(): Int

    @Query("DELETE FROM products WHERE id NOT IN (:ids)")
    suspend fun deleteProductsNotIn(ids: List<String>): Int

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts(): Int

    @Query("UPDATE products SET stock = :newStock, isAvailable = CASE WHEN :newStock > 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Int)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
