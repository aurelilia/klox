import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    application
    id("org.graalvm.plugin.truffle-language") version "0.1.0-alpha2"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "xyz.angm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.graalvm.truffle:truffle-api:20.3.0")
    compileOnly("org.graalvm.truffle:truffle-dsl-processor:20.3.0")
    kapt("org.graalvm.truffle:truffle-dsl-processor:20.3.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "xyz.angm.lox.LoxKt"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "xyz.angm.lox.LoxKt"
    }
}

sourceSets["main"].java {
    srcDir("build/generated/source/kapt/main")
}

graal {
    version = "20.3.0"
    languageId = "lox.language"
}
