package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.CustomerProfile
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.PromotionalBanner
import com.example.data.model.ShopSettings
import com.example.data.model.WishlistItem
import com.example.data.seed.InitialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Category::class,
        CartItem::class,
        OrderEntity::class,
        PromotionalBanner::class,
        ShopSettings::class,
        WishlistItem::class,
        CustomerProfile::class
    ],
    version = 2,
    exportSchema = false
)
abstract class JisanDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun bannerDao(): BannerDao
    abstract fun settingsDao(): SettingsDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: JisanDatabase? = null

        fun getDatabase(context: Context): JisanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JisanDatabase::class.java,
                    "jisan_mobile.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.categoryDao().getCategoryCount() == 0) {
                            populateInitialData(instance)
                        }
                        if (instance.customerDao().getProfileSync() == null) {
                            instance.customerDao().saveProfile(CustomerProfile())
                        }
                    } catch (_: Exception) {}
                }

                instance
            }
        }

        private suspend fun populateInitialData(database: JisanDatabase) {
            database.categoryDao().insertCategories(InitialData.categories)
            database.bannerDao().insertBanners(InitialData.banners)
            database.settingsDao().insertOrUpdate(InitialData.defaultSettings)
        }
    }
}
