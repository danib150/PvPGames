plugins {
    id("java")
}

version = "1.0-SNAPSHOT"
group = "it.danielebruni.wildadventure"

repositories {
    mavenCentral()

    maven {
        name = "Boosters"
        url = uri("https://maven.pkg.github.com/danib150/Boosters")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        name = "WildCommons"
        url = uri("https://maven.pkg.github.com/danib150/WildCommons")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        name = "SportPaper"
        url = uri("https://maven.pkg.github.com/Electroid/SportPaper")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        name = "Boosters"
        url = uri("https://maven.pkg.github.com/danib150/Boosters")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    compileOnly("io.github.danib150:boosters:1.0-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("app.ashcon:sportpaper:1.8.8-R0.1-SNAPSHOT")
    compileOnly("it.danielebruni.wildadventure:wildcommons-core:1.0.1")
    compileOnly("io.github.danib150:boosters:1.0-SNAPSHOT")
    implementation("net.citizensnpcs:citizensapi:2.0.41-SNAPSHOT")
}