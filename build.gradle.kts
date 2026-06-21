plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.detekt)
	java
}

buildscript {
	dependencies {
		classpath(libs.kotlin.gradle)
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

subprojects {
	// Configure linting
	apply<io.gitlab.arturbosch.detekt.DetektPlugin>()
	detekt {
		buildUponDefaultConfig = true
		ignoreFailures = true
		config = files("$rootDir/detekt.yaml")
		basePath = rootDir.absolutePath

		reports {
			sarif.enabled = true
		}
	}

	// Configure default Kotlin compiler options
	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
		compilerOptions {
			jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
		}
	}

	// Configure default Android options
	plugins.withId("com.android.application") {
		configure<com.android.build.api.dsl.ApplicationExtension> {
			compileOptions {
				sourceCompatibility = JavaVersion.VERSION_21
				targetCompatibility = JavaVersion.VERSION_21
			}
		}
	}

	plugins.withId("com.android.library") {
		configure<com.android.build.api.dsl.LibraryExtension> {
			compileOptions {
				sourceCompatibility = JavaVersion.VERSION_21
				targetCompatibility = JavaVersion.VERSION_21
			}
		}
	}
}

tasks.withType<Test> {
	// Ensure Junit emits the full stack trace when a unit test fails through gradle
	useJUnit()

	testLogging {
		events(
			org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
			org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
			org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
		)
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true
	}
}
