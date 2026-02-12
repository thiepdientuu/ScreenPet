package com.ls.petfunny.di

import android.content.Context
import androidx.room.Room
import com.ls.petfunny.MainApp
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.data.database.AppDatabase
import com.ls.petfunny.data.database.MascotsDao
import com.ls.petfunny.data.database.ShimejiDao
import com.ls.petfunny.data.model.ShimejiListing
import com.ls.petfunny.di.repository.Helper
import com.ls.petfunny.di.repository.MascotsRepository
import com.ls.petfunny.di.repository.ShimejiRepository
import com.ls.petfunny.di.repository.TeamListingService
import com.tp.ads.base.AdManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): MainApp {
        return app as MainApp
    }

    @Singleton
    @Provides
    fun provideAppPreferencesHelper(@ApplicationContext context: Context): AppPreferencesHelper {
        return AppPreferencesHelper(context)
    }


    @Singleton
    @Provides
    fun providerAdsManager(
        @ApplicationContext context: Context,
    ) = AdManager(context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "RoomShimeji.db")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideShimejiDao(database: AppDatabase): ShimejiDao {
        return database.shimejiDao()
    }

    @Provides
    @Singleton
    fun provideMascotsDao(database: AppDatabase): MascotsDao {
        return database.mascotsDao()
    }

    @Provides
    @Singleton
    fun provideHelper(@ApplicationContext context: Context): Helper {
        return Helper(context)
    }

    @Provides
    @Singleton
    fun provideShimejiListing(): ShimejiListing {
        return ShimejiListing()
    }

    @Provides
    @Singleton
    fun provideShimejiRepository(shimejiDao: ShimejiDao, shimejiListing: ShimejiListing): ShimejiRepository {
        return ShimejiRepository(shimejiDao, shimejiListing)
    }

    @Provides
    @Singleton
    fun provideMascotsRepository(mascotsDao: MascotsDao, helper: Helper): MascotsRepository {
        return MascotsRepository(mascotsDao, helper)
    }

    @Provides
    @Singleton
    fun provideTeamListingService(
        shimejiRepository: ShimejiRepository,
        helper: Helper,
        mascotsRepository: MascotsRepository
    ): TeamListingService {
        return TeamListingService(shimejiRepository, helper, mascotsRepository)
    }

}