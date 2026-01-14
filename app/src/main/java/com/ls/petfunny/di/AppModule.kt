package com.ls.petfunny.di

import android.content.Context
import com.ls.petfunny.MainApp
import com.ls.petfunny.data.AppPreferencesHelper
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


//	@Singleton
//	@Provides
//	fun providerConfigManager(
//		@ApplicationContext context: Context,
//	) = ConfigManager(context, CommonInfo::class.java)

	@Singleton
	@Provides
	fun providerAdsManager(
		@ApplicationContext context: Context,
	) = AdManager(context)

}