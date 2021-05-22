import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "com.alibaba.testable"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("com.alibaba.testable:testable-all:0.6.6")
    testAnnotationProcessor("com.alibaba.testable:testable-processor:0.6.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    jvmArgs("-javaagent:${classpath.find { it.name.contains("testable-agent") }!!.absolutePath}")
    useJUnitPlatform()
}
