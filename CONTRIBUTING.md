# 🌱 Contributing

This guide is for developers interested in contributing to the Epic Fight project.

> [!NOTE]  
> While we welcome contributions, we highly
> encourage [creating issues on GitHub](https://github.com/Epic-Fight/epicfight//issues/new) before
> working on them—especially for medium to large changes.

## 📜 Code of Conduct

Please review our [Code of Conduct](./CODE_OF_CONDUCT.md) to understand the expected standards of behavior when
participating in this project.

## 📋 Prerequisites

- Linux, macOS, or Windows.
- A [Java JDK](https://adoptium.net/temurin/releases). The exact JDK version depends on the Minecraft
  version, for example:
    - **1.21.1**: [21](https://adoptium.net/temurin/releases?version=21)
    - **1.20.1**: [17](https://adoptium.net/temurin/releases?version=17&os=any&arch=any)
- [git](https://git-scm.com/) for version control.
- [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
  with [Minecraft Development IDE plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) (**optional
  but recommended**).
- [Commit signature verification](https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification)
  set up for your GitHub account (**optional but recommended**).
- [Minecraft account](https://www.minecraft.net/store/minecraft-deluxe-collection-pc) to launch the game and test
  cape physics and online features (**optional**).

## 🍴 Forking & cloning the repository

- Fork the [GitHub repo](https://github.com/Epic-Fight/epicfight/) to your account. If you already have a fork,
  make sure it's up to date.

* Clone your fork:

    ```bash
    git clone git@github.com:YOUR_GITHUB_USERNAME_HERE/epicfight.git
    cd epicfight
    ```

* Add the upstream repo:

    ```bash
    git remote add upstream git@github.com:Epic-Fight/epicfight.git
    ```

  This allows you to fetch updates from the main repository.

* Create a local branch and checkout to it:

  ```bash
  git branch -b YOUR_BRANCH_NAME_HERE
  ```

* Make your changes and commit them (structured commits are a bonus):

  ```bash
  git add .
  git commit -m "YOUR_COMMIT_MESSAGE_HERE"
  ```

* Push local branch:

  ```bash
  git push origin YOUR_BRANCH_NAME_HERE
  ```

* GitHub will prompt you to open a GitHub pull request, if not, you can
  open [this link](http://github.com/Epic-Fight/epicfight/pull/new).

## 🧪 Testing

If this is the first time you're building the project, run the following commands
to build and generate IDE configurations:

```shell
$ ./gradlew ide
$ ./gradlew build
```

To test the game (client):

```shell
$ ./gradlew runClient
```

To test the dedicated server:

```shell
$ ./gradlew runServer
```

## ⚙️ Development Notes

- Update [`CHANGELOG.md`](CHANGELOG.md) whenever you make changes.
    - Follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) for format and style.
    - If the change is **breaking** or affects other mods/addons, document it in the `For Devs` section.

## 🧩 Code Style & Recommendations

### 1. Keep the code independent of mod loader APIs

Avoid unnecessary mod loader-specific APIs like `@OnlyIn`. This annotation exists because the separate Minecraft server
JAR lacks client-only classes, and calling a non-existent class can cause crashes that are hard to debug. `@OnlyIn`
prevents this by crashing the game when used on the wrong distribution.

For mods, this is usually unnecessary, as mods typically provide a single JAR for both server and client.

Relying on such APIs makes the project harder to maintain, port to newer Minecraft versions, or support multiple mod
loaders.

We're still working on fixing this issue, as the current system relies heavily on NeoForge/Forge.  
This makes it difficult to port to other mod loaders like Fabric or future NeoForge-based forks.

> [!IMPORTANT]
> Avoid designing code under the assumption that NeoForge will always be used.  
> NeoForge could be replaced or ported later, as happened with Forge—change is inevitable in software.  
> Design for adaptability and low-maintenance updates, even if it requires a higher initial implementation cost.

### 2. Keep the code design more independent of the Minecraft APIs

Try to not depend on Minecraft APIs directly in many files.

#### 🚫 Avoid

```java
// File1.java

Minecraft.getInstance().player.setGameMode(0);

// File2.java

Minecraft.getInstance().player.setGameMode(1);

// File3.java

Minecraft.getInstance().player.setGameMode(1);
```

#### ✅ Preferred

```java
// MyMinecraft.java

// An example only, we should find a better name depending on the problem we're trying to solve.
final class MyMinecraft {
    private static setPlayerGameMode(int gameMode) {
        Minecraft.getInstance().player.setGameMode(gameMode);
    }
}

// File1.java

MyMinecraft.setGameMode(0);

// File2.java

MyMinecraft.setGameMode(1);

// File3.java

MyMinecraft.setGameMode(1);
```

So if Minecraft ever changes the `setGameMode` method—for example, to use an `enum` (e.g., `GameMode.CREATIVE`) instead
of an `int`—we only need to update one place, which greatly reduces maintenance.

> [!NOTE]
> This is just an example. Do not consider it best practice—always look for better alternatives.
> It's not always a good idea to introduce abstractions around Minecraft APIs.

### 3. Avoid tight-coupling on third-party mods directly when implementing compatibility

When adding compatibility with a third-party mod, avoid using their imports everywhere across the codebase to implement
the compatibility. Always confirm that it's an optional dependency.

If you can't find an Epic Fight API that you could use,
like an event to register inside `ICompatModule`, or a callback,
try to develop an API or refactor the existing code to make it easier to solve
the problem, regardless of the dependency X.

For example, we tried to add controller
support ([full issue report](https://github.com/Epic-Fight/epicfight/issues/2116)). However, since many controller mods
exist and change over time, we introduced [major refactoring](https://github.com/Epic-Fight/epicfight/pull/2122), and
then added [Controlify integration](https://github.com/Epic-Fight/epicfight/pull/2133) with just one class—without any
compromise to support quality. This also made it easier to backport it to an older Minecraft version.

Keep the compatibility modular, if it's not possible to implement the compatibility in a modular way,
simplify the existing code before introducing any dependencies. This also makes it easier to replace the dependency.

It's preferable that any imports specific to a third-party dependency remain inside their own package, such as
`yesman.epicfight.compat.mod_name.x`.

> [!NOTE]
> Remember, this is a long-term project, so we take maintenance seriously,
> introducing changes that "just" work, hacks, workarounds, technical debt, may result in serious
> consequences in the long-term. 

### 4. Always prefer a supported public API

For example, these mods provide a supported API that you should consider whatever possible:

* [Shoulder Surfing](https://github.com/Exopandora/ShoulderSurfing/wiki/API-Documentation-Plugins)
* [Controlify Entrypoint](https://moddedmc.wiki/en/project/controlify/latest/docs/developers/controlify-entrypoint)
* [Iron's Spells](https://iron.wiki/developers/)
* [Curios Inventory](https://docs.illusivesoulworks.com/curios/getting-started)
* [GeckoLib](https://github.com/bernie-g/geckolib/wiki)

You could also refer to the mod's `api` package to see if they provide an event, use mixins as the last option.

If the mod does not provide any public API, then we should consider whether its worth depending on,
or if we could provide the compatibility in a separate mod.

The classic Minecraft modding solution is to use mixins, BUT any mixins on third-party classes should remain under
`yesman.epicfight.compat.mod_name.mixin`, and then create a dedicated mixin JSON file just for that mod,
and only register it if the mod is loaded.

Confirm that it does not spam the log, with or without the third-party mod loaded.

### 5. Keep code modular

Even if it's not a public API—or just a private method or field—keep the code modular.  
Avoid putting everything into a single large method that does it all, as this reduces reusability and leads to future
refactoring, cleanup, breaking changes, and extensive testing to fix later.

#### 🚫 Avoid

```java
private void handleKeyMappings() {
    if (isActionPressed()) {
        // Action implementation details
    }
    // ...
}
```

#### ✅ Preferred

```java
private void handleKeyMappings() {
    if (isActionPressed()) {
        doAction();
    }
    // ...
}

private doAction() {
    // Action implementation details
}
```

The first example mixes the trigger condition with the action logic, making it harder for other mods to inject or extend
behavior (even if they shouldn't, they should still have the option).
It also complicates adding new features, fixing bugs, porting to new versions, maintaining the code, and keeping it
readable.

Maybe the

> [!TIP]
> For a real example, refer to this:
> - [Before](https://github.com/Epic-Fight/epicfight/blob/c40ce6b00a2643927ba1b2f2ab27195cf23168f8/src/main/java/yesman/epicfight/client/events/engine/ControlEngine.java#L102-L393)
> - [After](https://github.com/Epic-Fight/epicfight/blob/1.20.1/src/main/java/yesman/epicfight/client/events/engine/ControlEngine.java#L139-L295)

### 6. Always test the game in a production environment

Even if it works on your development machine, that doesn't mean it will work in a production environment.  
When modifying mixins, always build the JAR file and test it in a fresh instance to ensure it truly works from the
user's perspective.

### 7. Don't rely on temporary understanding

Even if you fully understand the code now, others might not—and in the future, you might not either.

Keep it simple: write clear Javadocs, use meaningful names, and maintain a clear structure that makes the code easy to
revisit at any time. This habit will greatly benefit you as a professional software engineer, beyond a single pull
request.

Try to avoid depending on temporary context whenever possible.
Always take time to ensure your code is simple and easy to understand.

### 8. Document public APIs

Although our existing public APIs lack documentation, we're working to improve this going forward.

### 9. Keep code formatting consistent

Currently, we don't have a GitHub workflow or CI setup to enforce this, but that may change in future releases.
