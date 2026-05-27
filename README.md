# 智炼引擎

## 1. 项目名称
**智炼引擎**（Android 原生健身训练应用）

---

## 2. 项目简介（Overview）
智炼引擎是一个面向健身新手与进阶用户的 Android 本地化训练应用，核心目标是把“找动作、学动作、记录交流、获得 AI 建议”整合到一个离线可用的 App 中。

它主要解决的问题：
- 新手不知道某个肌群该练什么动作
- 训练动作分散、筛选成本高
- 健身建议不够个性化（尤其是训练/营养方案）
- 社区互动和个人训练场景割裂

核心功能（基于当前代码）：
- 交互式人体肌肉图（男女 + 正背切换 + 热区点击跳转）
- 动作库筛选与动作详情（分类/关键词过滤、收藏点赞、图片预览）
- AI 双助手（训练助手 + 营养助手，登录后可用）
- 论坛图文发帖与互动（点赞/收藏/评论/举报，账号绑定）
- 本地账号系统（登录/注册、资料编辑、主题切换、密码修改、收藏按账号隔离）

---

## 3. 项目演示
当前 App 底部导航为 5 个主入口：
- `首页`：点击肌肉区域，自动跳转动作库并携带筛选参数
- `动作库`：按关键词+类别查找动作，进入详情查看步骤与注意事项
- `AI训练`：在训练助手/营养助手间切换，支持会话历史、复制回复、营养可视化
- `论坛`：瀑布流帖子，支持写帖（底部弹窗+最多3图）、互动与详情页管理
- `我的`：登录注册、资料编辑、收藏入口、设置抽屉（主题与账号安全）

---

## 4. 技术栈（Tech Stack）

### 编程语言
- **Java（主）**
- **Kotlin（辅）**：当前仅少量 Kotlin 文件（如 `MuscleExercisesActivity.kt`）

### 框架与架构
- Android 原生（Activity + Fragment）
- MVVM（ViewModel + Repository + LiveData）
- Room 本地数据库
- ViewBinding
- 部分 Compose 依赖与页面（项目已启用 `buildFeatures.compose = true`）

### UI 组件
- Material Components / Material3
- RecyclerView / BottomSheetDialog / Chip / FAB

### 网络与数据处理
- OkHttp
- org.json（JSON 解析）
- Coil（图片加载与预览）

### 构建工具
- AGP `8.13.2`
- Gradle Wrapper `8.13`
- Kotlin 插件 `2.2.0`
- JDK `17`

---

## 5. 项目结构（Project Structure）

```text
Android_app/
├── app/
│   ├── build.gradle                  # App 模块构建配置（含 BuildConfig 字段）
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   └── muscle_exercises.json
│       │   ├── java/com/musclefit/app/
│       │   │   ├── auth/             # 本地账号、角色与权限
│       │   │   ├── data/db/          # Room 实体、DAO、数据库迁移、Seed 数据
│       │   │   ├── data/model/       # 列表卡片与详情数据模型
│       │   │   ├── repo/             # Repository 数据访问层
│       │   │   ├── theme/            # 深浅色主题管理
│       │   │   ├── ui/
│       │   │   │   ├── home/         # 首页肌肉图交互
│       │   │   │   ├── exercise/     # 动作库与动作详情
│       │   │   │   ├── assistant/    # AI 训练/营养助手
│       │   │   │   ├── forum/        # 论坛与帖子详情
│       │   │   │   ├── profile/      # 我的、登录注册、收藏
│       │   │   │   └── muscle/       # 额外肌肉动作页（Kotlin/Compose）
│       │   │   └── ZhiLianEngineApp.java # Application 初始化入口
│       │   └── res/                  # XML 布局、主题、颜色、图标、肌肉图资源
│       └── test/
│           └── java/...              # 单元测试（Repo/解析器/防抖逻辑）
├── docs/
│   ├── environment_setup.md          # 环境说明
│   ├── data_pipeline.md              # 数据导入流程说明
│   └── mysql_schema.sql              # 内容管理侧 MySQL 参考 schema
├── run_stable_install.sh             # 稳健安装脚本（处理 emulator offline 场景）
├── build.gradle                      # 根构建配置
├── settings.gradle                   # 根项目名与模块声明
└── README.md
```

---

## 6. 安装与运行（Getting Started）

### 6.1 环境要求
- Android Studio（最新稳定版）
- JDK 17
- Android SDK：
  - `compileSdk 35`
  - `targetSdk 35`
  - `minSdk 29`

### 6.2 获取项目
```bash
git clone <your-repo-url>
cd Android_app
```

### 6.3 Android Studio 运行
1. 用 Android Studio 打开项目根目录
2. 等待 Gradle Sync 完成
3. 选择模拟器或真机
4. 点击 Run 运行 `app`

### 6.4 命令行构建
```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

### 6.5 安装到设备
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

若遇到模拟器 `device offline`，可使用项目内脚本：
```bash
chmod +x run_stable_install.sh
./run_stable_install.sh
```

---

## 7. 核心功能说明（Features）

### 7.1 首页：交互式肌肉图
- 支持男/女切换、正面/后背切换
- 支持类别 BottomSheet（全部/自重/哑铃/杠铃/器械）
- 热区点击后高亮并跳转动作库
- 跳转时携带 `muscleGroup`、`trainingCategory` 以及兼容筛选字段 `keyword`、`category`

### 7.2 动作库
- 支持关键词检索与类别筛选
- 动作卡片支持点赞/收藏（需登录）
- 点击卡片进入动作详情页，展示动作说明、注意事项、肌群强度信息
- 图片支持大图预览（`ImagePreviewActivity`）

### 7.3 AI 训练（双助手）
- 登录后可使用：训练助手 / 营养助手
- 通过 Longcat 接口请求 AI 回复
- 会话按账号隔离持久化（Room `assistant_message`）
- 营养助手支持热量与三大营养素可视化展示
- AI 回复支持右上角复制按钮

### 7.4 论坛
- 瀑布流帖子列表（StaggeredGrid）
- 筛选：全部/我的/我收藏；排序：最新/点赞量/收藏量
- 发帖方式：FAB 打开底部写帖面板，支持最多 3 张图
- 帖子详情：点赞、收藏、评论、举报、编辑、删除
- 权限规则：
  - 游客可浏览
  - 登录用户可互动与发帖
  - 作者/管理员可管理帖子与评论

### 7.5 账号与个人中心
- 本地账号体系（SharedPreferences）
- 登录支持 `账号ID或昵称 + 密码`
- 注册自动生成 6 位数字账号 ID（唯一），昵称不可重复
- 支持资料编辑：昵称、性别、手机号、体重、身高、生日
- 支持密码修改、深浅主题切换
- 收藏列表与账号绑定

---

## 8. API 文档（如果有）

> 本项目当前**没有自建后端 REST API**。  
> 网络接口主要用于调用外部 AI 服务（Longcat）。

### 8.1 外部 AI 接口（Longcat Chat Completions）
- **Method**: `POST`
- **URL 生成逻辑**：由 `LONGCAT_BASE_URL` 推导，默认 Longcat 场景会拼接到  
  `.../openai/v1/chat/completions`
- **Headers**:
  - `Authorization: Bearer <LONGCAT_API_KEY>`
  - `Content-Type: application/json`

### 8.2 请求体（示例）
```json
{
  "model": "LongCat-Flash-Chat",
  "messages": [
    {"role": "system", "content": "系统提示词"},
    {"role": "user", "content": "用户问题"}
  ],
  "temperature": 0.7
}
```

### 8.3 响应解析
- 从 `choices[0].message.content` 提取文本回复
- 营养助手回复会进一步做本地解析，用于热量/比例可视化与现实性校验提示

---

## 9. 使用示例（Usage）

### 示例 1：首页到动作库
1. 打开 `首页`
2. 选择训练类别（例如：哑铃）
3. 点击肌肉图上的“肩部”
4. 自动跳转到 `动作库`，并按“肩 + 哑铃”筛选

### 示例 2：论坛发帖
1. 登录账号
2. 进入 `论坛`
3. 点击右下角 `+`
4. 填写标题/正文，选择 0~3 张图片
5. 发布后帖子出现在列表中，可在详情继续评论与管理

### 示例 3：AI 营养助手
1. 登录账号并完善身高/体重/生日/性别
2. 进入 `AI训练` -> 切到 `营养助手`
3. 询问“我想减脂，给我每日热量建议”
4. 查看文本建议 + 热量/营养素可视化条

### 示例 4：预置账号（开发测试）
- 用户：`000001 / user001`
- 用户：`000002 / user002`
- 用户：`000003 / user003`
- 用户：`000004 / user004`

---

## 10. 配置说明（Configuration）

### 10.1 关键构建配置
位于 `app/build.gradle`：
- `LONGCAT_API_KEY`
- `LONGCAT_BASE_URL`
- `LONGCAT_MODEL`

这些值通过 `buildConfigField` 注入 `BuildConfig`，由 `AIAssistantService` 使用。

### 10.2 本地配置文件
`local.properties` 主要用于：
- `sdk.dir`
- 可放置本地 Longcat 相关字段（当前代码默认读取 `BuildConfig` 中值）

### 10.3 数据存储
- Room DB：`musclefit.db`
- SharedPreferences：
  - 账号信息（`musclefit_auth`）
  - 主题配置（`musclefit_theme`）

### 10.4 Seed 数据
- 应用首次启动会通过 `SeedData` 注入训练动作、强度与示例论坛帖子
- 参考文档：`docs/data_pipeline.md`、`docs/mysql_schema.sql`

---

## 11. 未来优化方向（TODO / Roadmap）

- [ ] 将 AI Key 管理改为更安全的本地/CI 注入方案（避免明文硬编码）
- [ ] 增加仪器测试（UI/Espresso）覆盖关键用户流程
- [ ] 论坛与收藏支持云端同步（多设备一致性）
- [ ] AI 回复结构化程度增强（可配置模板、可追溯引用）
- [ ] 动作与论坛内容运营后台化（当前以本地种子数据为主）
- [ ] 完善异常上报与性能监控（崩溃、慢请求、ANR）

---

## 12. 贡献指南（Contributing）

欢迎贡献代码与建议，推荐流程：

1. Fork 仓库并创建分支
2. 实现功能或修复问题（尽量遵循现有包结构和命名）
3. 本地验证：
   ```bash
   ./gradlew :app:assembleDebug
   ./gradlew :app:testDebugUnitTest
   ./gradlew :app:lintDebug
   ```
4. 提交 PR，并说明：
   - 变更动机
   - 主要修改点
   - 验证方式与结果
5. 涉及 UI 变更时，建议附截图或录屏说明

---

## 13. 许可证（License）

本项目采用 MIT License，详情请见 [LICENSE](LICENSE)。
