plugins {
    java
    kotlin("jvm") version "1.7.20"
    `maven-publish`
}

group = "me.koddydev"
version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://m2.dv8tion.net/releases/")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("redis.clients:jedis:4.2.3")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    compileOnly("org.slf4j:slf4j-log4j12:2.0.1")
    compileOnly(files("C:\\Users\\Koddy\\Desktop\\Jars\\mkUtils.jar"))
    compileOnly(files("C:\\Users\\Koddy\\Desktop\\Jars\\Vault.jar"))
    compileOnly(files("C:\\Users\\Koddy\\Desktop\\Jars\\EduardAPI-1.0-all.jar"))
}

tasks {
    jar {
        destinationDirectory
            .set(file("C:\\Users\\Koddy\\Desktop\\Jars\\Plugins"))
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}