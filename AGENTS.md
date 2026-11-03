# AGENTS.md

本文件面向在本仓库工作的 AI 助手与贡献者，说明 GunsmithLib 的构建命令、代码地图、核心架构和既有约定。
**动手改代码前，请至少读完第 4、5、7 节。**

## 0. 重要事项

#### 0. 重中之重

无论如何都不能碰 .git 文件夹！

无论如何都不能碰 .git 文件夹！

无论如何都不能碰 .git 文件夹！

#### 1. Agent 该如何查看源代码

- **查看源代码时优先使用 IntelliJ IDEA MCP 工具**（按符号/引用/文件检索代码）；该工具不可用或取不到结果时，再回退到普通文件读取与 grep。

## 1. 项目是什么

- Minecraft **Forge 1.20.1** 模组，Java 17，是 **TaCZ（Timeless and Classics Zero）** 的扩展库/前置。
- 主要能力：面向枪包作者的数据驱动扩展（枪盾、火控、能量武器、爆炸/破片/空爆、弹种变体、屏幕震动、药水效果、命中粒子……）、Lua 脚本 API（逻辑脚本 + 动画状态机脚本）、以及针对 TaCZ/原版的兼容性修补。
- 许可证 GPL-3.0，作者 ChloePrime。当前版本见 `gradle.properties` 的 `mod_version`（`7.0.0-alpha.10`）。
- 双端模组（common + client）。**任何改动都必须保证专用服务器不会加载客户端类。**

## 2. 构建与运行

使用仓库自带的 Gradle Wrapper（Gradle **8.4**），Windows 下用 `gradlew.bat`，Linux/macOS 下用 `./gradlew`：

```bash
.\gradlew.bat build        # 编译 + Mixin AP 生成 refmap + jarJar 打包
.\gradlew.bat runClient    # 开发客户端（工作目录 run/）
.\gradlew.bat runServer    # 专用服务器（工作目录 run-server/）
.\gradlew.bat runData      # 数据生成，输出到 src/generated/resources/
.\gradlew.bat publish      # 发布到 Chloe Maven，需要 CHLOE_MAVEN_USERNAME / CHLOE_MAVEN_PASSWORD
```

- Wrapper 需要 `gradle/wrapper/gradle-wrapper.jar`（`.gitignore` 用 `!gradle-wrapper.jar` 单独放行，**必须提交**）。该文件缺失时 `gradlew.bat` 会报 `ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain`，可用任意 Gradle 8.4 执行 `gradle wrapper --gradle-version 8.4 --no-validate-url` 重新生成。
- 也可以直接在 IntelliJ 中导入 Gradle 项目并运行同名任务。
- 需要 Java 17（`JAVA_HOME` 或 PATH 上的 `java`），`gradlew` 脚本自身用 `-Xmx64m` 启动。
- 工具链：Java 17（`java.toolchain` + `options.release = 17`，UTF-8 编码）。
- 产物基名：`gunsmithlib-forge-1.20.1-tacz${tacz_version}`；`jar` 任务带 `core` classifier，`jarJar` 任务产主 jar（内嵌 kuroutils、Ritchies Projectile Lib、MixinExtras）。同时生成 sources/javadoc jar。
- Run 配置统一 `-Xms6G -Xmx6G`，`org.gradle.daemon=false`。
- dev 运行目录 `run/` 已被 gitignore，里面是本地测试环境（`run/tacz/` 下放着测试枪包，如 EMX-Arms），**不要提交、不要当作源码改动**。
- 测试侧没有 `src/test`，也没有 GameTest；验证方式见第 7 节。

依赖要点（`build.gradle` / `gradle.properties`）：

| 项 | 值 | 源码 |
| --- | --- | --- |
| Minecraft / Forge | 1.20.1 / 47.3.19 | — |
| Mappings | Parchment 2023.09.03-1.20.1 | — |
| TaCZ | `USE_LOCAL_TACZ=true` 时用 `libs/tacz-1.20.1-1021-162132-all.jar`；否则走 CurseMaven | [MCModderAnchor/TACZ](https://github.com/MCModderAnchor/TACZ) |
| 基础库（KuroUtils） | `cn.chloeprime:kuroutils2`（`cn.chloeprime.commons.*`，含 RPC、TaskScheduler、数学/颜色工具） | [ChloePrime/KuroUtils](https://github.com/ChloePrime/KuroUtils) |
| AAA Particles | 命中/开火/弹道粒子（`cn.chloeprime:aaa_particles-forge-1.20.1`） | [ChloePrime/AAAParticles](https://github.com/ChloePrime/AAAParticles) |
| 其他 | GeckoLib、Ritchies Projectile Lib、MixinExtras 0.5.0 | — |

## 3. 代码地图

> **注意**：Java 包名是 `mod.chloeprime.gunsmithlib`，而 Maven `mod_group_id` 是 `cn.chloeprime.gunsmithlib`。
> 这是历史原因，**不要“顺手修正”包名**。

```
src/main/java/mod/chloeprime/gunsmithlib/
├── GunsmithLib.java        入口：DeferredRegister、配置注册、commonSetup、已知不兼容检测
├── Config.java             COMMON 配置
├── api/                    对外 API（会发布到 Maven，被枪包/其他模组依赖）
│   ├── util/               Gunsmith、GunInfo/AmmoInfo/AttachmentInfo、Rangefinder、AABB
│   ├── common/             事件、属性、Loot Function/Condition、配方、scripting_v2 接口
│   └── client/             枪械 Tooltip 事件、动画常量、客户端 scripting_v2 接口
├── common/                 实现层（多数对外不可见）
│   ├── gunpack_extension/  数据驱动扩展核心：gun/ ammo/ attachment/ shared/
│   ├── scripting/          Lua 绑定与脚本加载（ScriptBakery 等）
│   ├── impl/scripting_v2/  脚本 API 的 Java 实现
│   ├── internal/           内部事件/状态，标注 @ApiStatus.Internal
│   ├── loot/ entity/ block/ item/ compat/ util/
├── client/                 纯客户端实现（HUD、渲染、输入、PAPI、tooltip、显示扩展）
├── mixin/                  common/ 与 client/，再按 minecraft/ tacz/ luaj 分包，bugfix/ 子包
├── data/                   数据生成 Provider
├── network/                SimpleChannel 包（少量；跨端主要用 RPC）
├── compat/                 可选模组检测与代理（ModInstallationStatus、AaaParticleProxy）
└── proxies/                ClientProxy 静态门面 + 仅客户端加载的 ClientProxyImpl
```

资源目录（`src/main/resources/`）：

- `gunsmithlib.mixins.json`：**新增 mixin 必须登记**到 `mixins`（common）或 `client` 数组；`refmap` 与 build.gradle 中 `add sourceSets.main, "gunsmithlib.refmap.json"` 对应。
- `META-INF/accesstransformer.cfg`、`META-INF/mods.toml`（TaCZ 依赖 `[1.1.9,)`、AAA Particles 可选）。
- `assets/gunsmithlib/`：`lang/en_us.json` + `lang/zh_cn.json`、textures、shaders、sounds.json。
- `data/gunsmithlib/`、`src/generated/resources/`（datagen 产物，改完要提交）。
- `docs/`：**面向枪包作者的中文文档，会随 jar 发行**。改行为必须同步更新：
  `gunpack_extensions.md`、`attributes.md`、`scripting.md` / `scripting_v2.md`、`modifiable_properties.md`、`gunpack_display_extensions.md`、`textshow_papi_extension.md`、`changelogs/<version>.md`。

## 4. 核心架构（务必先理解）

### 4.1 枪包数据扩展的三层结构

TaCZ 的 `data` JSON 通过 Gson 反序列化。GunsmithLib 用 Mixin 在 TaCZ 的数据 POJO 上“挂”一个 `gunsmithlib_extension` 字段：
`mixin/common/tacz/MixinGunData` 用 `@Unique` + `@SerializedName("gunsmithlib_extension")` 承载 `GunsmithLibGunDataExtension`，并通过 `EnhancedGunData` 接口暴露出来；`MixinAttachmentData`、`MixinAmmoIndexPojo` 同理（`EnhancedAttachmentData` / `EnhancedAmmoData`）。

三层分工：

1. **挂载层（mixin）**：只负责字段声明 + `gunsmith$getGunsmithLibExtension()` 返回 `Optional`。
2. **数据层（`GunsmithLib*DataExtension`）**：字段用 `@GunpackProperty` 标注（CLASS retention，仅作约定/文档标记，不参与序列化），直接给默认值；`GunsmithLibSharedDataExtension` 是枪械/配件/弹药共享字段的基类。
3. **行为层（`*System` / `*Behavior`）**：`@Mod.EventBusSubscriber` + 静态 `@SubscribeEvent`，或提供静态工具方法。参见 `ShieldData` + `ShieldBehavior`。

数据对象通常提供静态访问器，遵循现有惯例：`XxxData.fromGun(ItemStack)` / `fromGun(GunInfo)` / `fromAttachment(...)`，内部走 `Gunsmith.getGunInfo(...)` + 扩展 getter。

### 4.2 命名前缀约定

| 前缀 | 用途 |
| --- | --- |
| `gunsmith$` | Mixin `@Unique` 成员、给 TaCZ 类型补的 internal 接口方法（如 `gunsmith$getGunsmithLibExtension`） |
| `gunsmithlib$` | `Abstract*` 内部接口的抽象方法（如 `gunsmithlib$getShooter`） |
| `gunsmith_` | Lua API **v1** 方法名（`gunsmith_getGunId` 等，保持兼容不再改名） |
| 无前缀 snake_case | Lua API **v2** 包装器方法（`GunsmithLibCommonScriptExtension` / `GunsmithLibLogicScriptExtension`） |

### 4.3 读取/查找扩展数据

- 统一通过 `api/util/Gunsmith.getGunInfo / getAmmoInfo / getAttachmentInfo` 拿到 `GunInfo/AmmoInfo/AttachmentInfo`，不要自己解析 NBT。
- 通用查找：`GunsmithLibSharedDataExtension.forGun / forAmmo / forAttachment / forGunOrAmmo / allOf`；遍历枪械与全部配件可用 `GunPartIterator`。
- 常用工具集中在 `common/util/GsHelper`（弹种、弹药、属性、Lua 转换等），先看它再写新轮子。

### 4.4 Lua 脚本扩展

- 逻辑脚本：v1 接口在 `api/common/GunScriptAPIExtension`、`CommonScriptingExtension`；v2 包装器 `api/common/scripting_v2/GunsmithLibCommonScriptExtension`、`GunsmithLibLogicScriptExtension`。
- 动画状态机脚本：`api/client/scripting_v2/*`。
- 注入点：`MixinScriptManager`、`MixinLuaStateMachineFactory`、`MixinLuaAnimationStateMachine` 等。
- 新增脚本 API：接口放 `api/**/scripting_v2`，实现放 `common/impl/scripting_v2` 或 `Abstract*` 默认方法；Lua table 参数用 `@TableSchema(XxxData.class)` 标注；**旧方法保留并委托给新实现**，不要直接删。
- 脚本按 `ResourceLocation` 由 `ScriptBakery` 加载（`gunsmithlib_extension` 里的 `script` + `script_param`）。

### 4.5 跨端通信

- 优先用基础库的 RPC：`RPC.call(RPCTarget.to(player), Class::method, args)` + `@RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)`，目标方法为 `private static`。
- 只有需要自定义包体/广播时才在 `network/ModNetwork` 注册 `SimpleChannel` 消息（当前只有 `S2CSyncLockedTarget`）。
- 定时/延迟逻辑用 `TaskScheduler.createTickBased(LogicalSide.CLIENT/SERVER)`（kuroutils），**不要开线程、不要手写 tick 计数器**。

### 4.6 注册与配置

- 注册项集中在 `GunsmithLib` 的内部静态类：`Blocks`、`Items`、`Attributes`、`MobEffects`、`SoundEvents`、`EntityTypes`。
- COMMON 配置在 `Config`，CLIENT 配置在 `client/GunsmithClientConfig`；新增配置项按现有风格在注释末写 `Added in version x.y.z` 并加 `@since`。
- TaCZ 运行期可改属性用 `@ValueModifiableAtRuntime` + `GunsmithLibGunProperties` 常量（`gunsmithlib_*` 前缀）。
- 发版前会检查已知不兼容（`checkKnownIncompatibilities`，如旧的 TaCZ Fire Control Extension）；新增不兼容检测也放这里。

### 4.7 客户端代码的隔离

- 公共代码需要客户端能力时走 `proxies/ClientProxy` 静态门面（如 `ClientProxy.getGameTime()`、`bobCompensation`），实现类 `ClientProxyImpl` 只在客户端加载。
- 客户端事件处理器放 `client/`，用 `@Mod.EventBusSubscriber(value = Dist.CLIENT, ...)`。
- 客户端显示扩展：`client/gunpack_extension/EnhancedGunDisplay*`（由 `MixinGunDisplay` / `MixinGunDisplayInstance` 实现）+ `GunsmithLibGunDisplayExtension`。
- PAPI：`client/papi/XxxPapi.java`，实现 `Papi`/`Vector3dPapi`，在 `GunsmithLibPapas.registerPapas()` 注册。

### 4.8 自定义数据包资源

非 TaCZ 的自定义 JSON（如 `gun_ammo_variant_sets`）使用 `common/util/SimpleCodecResourceReloadListener`：
`AddReloadListenerEvent` 注册 → `OnDatapackSyncEvent` 通过 RPC 把原始 JSON（NBT 编码）同步给客户端。参考 `GunAmmoVariantSetLoader`。注意加锁（`GunVariantRegistry.SINGLEPLAYER_LOCK`）。

## 5. 常见任务的操作步骤

**新增一个枪包扩展字段**

1. 在 `GunsmithLibGunDataExtension` / `GunsmithLibAttachmentDataExtension` / `GunsmithLibAmmoDataExtension` 中加 `@GunpackProperty private @Nullable XxxData xxx;` 和 getter（共享字段加到 `GunsmithLibSharedDataExtension`）。
2. 新建 POJO（放对应 `gunpack_extension/<gun|ammo|attachment|shared>/<feature>/`）：每个字段 `@GunpackProperty`、给默认值、`@Nullable`；枚举用 `snake_case` + `@SerializedName`；getter 用 `MoreObjects.firstNonNull` 或 `Optional`。
3. 不需要改 mixin：整个 `gunsmithlib_extension` 对象由 Gson 反序列化。
4. 在行为层消费（见下一条），并在文档补充 JSON5 示例 + 既有版本 `@since`。

**新增一个系统/行为**

- 在对应 feature 包建 `XxxSystem`/`XxxBehavior`，`@Mod.EventBusSubscriber` + 静态 `@SubscribeEvent`，事件内先判定枪械/弹药/AI 条件，再读扩展数据。
- 需要通知客户端时只用 RPC；客户端表现放 `client/gunpack_extension/...`。

**新增 mixin**

1. 放 `mixin/common|client/<minecraft|tacz|luaj>/`，类名 `MixinXxx`；纯访问器命名 `XxxAccessor`。
2. TaCZ 目标按现有代码多数使用 `@Mixin(value = Xxx.class, remap = false)`；原版目标不加 `remap = false`。
3. 登记进 `gunsmithlib.mixins.json`（common 或 client）。
4. `injectors.defaultRequire = 1`，注入点必须存在，改 TaCZ 版本后要回归所有 mixin。

**新增注册内容（方块/物品/属性/音效/实体）**

- 在 `GunsmithLib` 对应内部类加 DeferredRegister 条目；补 `assets/gunsmithlib/lang/{en_us,zh_cn}.json`（如 `attribute.name.gunsmithlib.xxx` 和可选的 `.desc`）；需要模型/战利品/配方就改 `data/` 下的 Provider 并跑 `.\gradlew.bat runData`。

**新增脚本 API / PAPI**

- 脚本：接口 + 实现 + 文档（`docs/scripting.md`、`docs/scripting_v2.md`），参数用 `@TableSchema`。
- PAPI：`client/papi/` 新类 + `GunsmithLibPapas` 注册 + `docs/textshow_papi_extension.md`。

## 6. 代码风格

- Java 17；大量使用 `var`；工具类用 `final` + 私有构造；4 空格缩进。
- **注释、提交信息、面向作者的文档用中文**；玩家可见文本同时补 `en_us.json` 与 `zh_cn.json`。
- 每个公开类/字段/方法按惯例写 `@since <version>`；弃用写 `@Deprecated(since = "x.y.z")` 并给替代方案（旧入口必须继续可用）。
- `api/**` 视为对外 API，`common/**` 为实现细节；内部/实验性 API 分别标 `@ApiStatus.Internal` / `@ApiStatus.Experimental`。
- 空安全混用 `javax.annotation.Nullable` 与 `org.jetbrains.annotations.Nullable`，跟随所在文件即可；公共入口用 `Objects.requireNonNull`/`@Nonnull`。
- 日志统一 `GunsmithLib.LOGGER`（SLF4J），不要 `System.out`。
- **向量类型统一用 `net.minecraft.world.phys.Vec3`**，不要用 `org.joml.Vector3d`。是直接暴露给 Lua 的 API 是个例外：`mod.chloeprime.gunsmithlib.api.common.scripting_v2` 与 `mod.chloeprime.gunsmithlib.api.client.scripting_v2` 包内类的方法形参保持 `Vector3d`（Lua 侧已有约定，不要改动）；此外只有在接口签名被第三方库强绑定等必要场合（如继承 `Vector3dPapi`）才可使用 `Vector3d`。
- `Component.translatable` 在没有对应翻译资源时会把传入的翻译键原样显示出来，因此需要把任意字面文本包装成 `Component` 时可以直接使用它，无需改用 `Component.literal`。
- 包名用 `snake_case` 表示特性（`ammo_variant`、`fire_control`、`screen_shake`、`raytrace_control`…），类名 `PascalCase`。
- 版本号只在 `gradle.properties` 的 `mod_version` 修改；字符串含 `alpha` 时 `isRelease=false`，`publish` 会发到 snapshots 仓库。

## 7. 验证与提交

项目没有自动化测试，验证靠“编译 + 开发客户端实测”：

1. `.\gradlew.bat build` 通过（同时验证 Mixin AP 与 refmap 生成）。
2. 启动一次 `.\gradlew.bat runClient`，确认没有 `Mixin apply failed` / refmap 缺失；改 mixin、TaCZ 版本、注册表后必须做这一步。
3. 数据扩展用 `run/tacz/` 下的测试枪包或自建测试包验证，日志看 `run/logs/latest.log`、崩溃看 `run/crash-reports/`。
4. 发版前：更新 `mod_version`、补 `docs/changelogs/<version>.md`、确认 jarJar 产物包含 kuroutils / ritchiesprojectilelib / mixinextras。

提交信息沿用现有习惯（中文，方括号前缀）：

```
[1.20.1] 更新 TaCZ 版本至 1021 内测版
[fix][🦊] 修复火控破片只能瞄准正南方的 bug
[doc] 屏幕震动文档
版本 6.4.5
```

## 8. 容易踩的坑

- **包名/groupId 不一致**（`mod.` vs `cn.`）是既成事实，不要全局重命名。
- **TaCZ 版本强耦合**：`libs/` 里的 jar、`gradle.properties` 的 `tacz_version`（只影响产物名）、`mods.toml` 的依赖范围、以及所有 `mixin/common/tacz/*` 的内部类引用必须一起对齐。
- **不要给 TaCZ 类直接加普通字段**，只能通过 `@Unique` mixin 字段；mixin 字段名用 `gunsmith$` 前缀 + `@SerializedName` 才会被 Gson 正确读写。
- **专用服务器安全**：任何 `Minecraft.getInstance()`、渲染、输入代码只能在 `client/` 或 `ClientProxyImpl` 中出现；`ClientProxy` 门面负责按 Dist 分支。
- **RPC 方法签名**：目标方法必须 `private static`，参数需可序列化（基本类型、NBT、可被基础库编解码的类型）。
- **数据包热重载**：自定义 JSON 走 `SimpleCodecResourceReloadListener` 时别忘了 `OnDatapackSyncEvent` 同步，否则单人/联机行为不一致。
- **`gunsmithlib.mixins.json` 的 `compatibilityLevel` 是 `JAVA_8`**（历史配置），不要随手改成 `JAVA_17`，除非已实测。
- **资源改动会进 jar**：`src/main/resources/docs` 会随模组发布，文档里的 JSON5 示例就是给枪包作者看的正式契约。
- **不要提交** `build/`、`run*/`、`.gradle/`、`libs/*.jar`（`.gitignore` 已覆盖）。
