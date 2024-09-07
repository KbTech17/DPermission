//import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.dp_th.dpermission"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
}

//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.S01)
//    signAllPublications()
//}

//mavenPublishing {
//    coordinates("com.dp_th.dpermission", "dpermission", "2.1.1")
//
//    pom {
//        name.set("DPermission")
//        description.set("DPermission is an open source Android library that makes handling runtime permissions extremely easy and supports newly devices too.")
//        inceptionYear.set("2024")
//        url.set("https://github.com/KbTech17/DPermission/")
//        licenses {
//            license {
//                name.set("The Apache License, Version 2.0")
//                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//            }
//        }
//        developers {
//            developer {
//                id.set("KbTech17")
//                name.set("Kb Tech")
//                url.set("https://github.com/KbTech17/")
//            }
//        }
//        scm {
//            url.set("https://github.com/KbTech17/DPermission/")
//            connection.set("scm:git:git://github.com/KbTech17/DPermission.git")
//            developerConnection.set("scm:git:ssh://git@github.com/KbTech17/DPermission.git")
//        }
//    }
//}