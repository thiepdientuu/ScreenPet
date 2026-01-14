plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
	id("dagger.hilt.android.plugin")
	id("org.jetbrains.kotlin.kapt")
}

android {
	namespace = "com.tp.ads"
	compileSdk = 36

	defaultConfig {
		minSdk = 24

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildFeatures {
		buildConfig = true
	}

	buildTypes {
		release {

			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	kotlinOptions {
		jvmTarget = "1.8"
	}
}

dependencies {

	implementation(libs.play.services.ads)
	// Dagger - Hilt
	implementation(libs.hilt.android)
	kapt(libs.hilt.compiler)
	implementation(libs.androidx.lifecycle.process)
	//firebase
	implementation (platform(libs.firebase.bom))
	implementation(libs.firebase.analytics)
	implementation (libs.firebase.config)
	implementation (libs.firebase.common)
	implementation(libs.gson)
	// UMP messaging platform
	implementation (libs.user.messaging.platform)
    implementation(libs.androidx.core.ktx)
}