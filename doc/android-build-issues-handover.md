# Android 打包问题交接文档

本文档总结了 Android 应用构建和运行过程中遇到的所有问题及其解决方案。

## 目录

1. [已知问题列表](#已知问题列表)
2. [配置文件修改](#配置文件修改)
3. [构建流程](#构建流程)
4. [常见错误及解决方案](#常见错误及解决方案)
5. [待解决问题](#待解决问题)

---

## 已知问题列表

### ❌ 未解决的问题

1. **应用崩溃（SIGABRT）**
   - 问题：应用启动后立即崩溃，日志显示 `Fatal signal 6 (SIGABRT)`
   - 状态：**未解决**
   - 可能原因：
     - 网络请求失败导致未捕获异常
     - Google Maps 初始化失败
     - 资源文件缺失
     - 其他运行时错误

2. **网络请求地址问题**
   - 问题：Android 设备无法访问 `https://454bb8d88e34.ngrok-free.app`
   - 状态：**未解决**
   - 当前配置：`constants.ts` 中 `__DEV__` 时使用 `http://192.168.1.9:8443`，但实际需要访问 `https://454bb8d88e34.ngrok-free.app`
   - 问题：Android 设备无法正确访问 ngrok HTTPS URL

3. **应用图标不显示**
   - 问题：Android 设备上显示默认图标
   - 状态：**未解决**

4. **Splash Screen 显示问题**
   - 问题：启动画面只显示在屏幕中央，未全屏
   - 状态：**未解决**

5. **Google Maps API Key 配置**
   - 问题：虽然已手动添加到 AndroidManifest.xml，但可能仍有问题
   - 状态：**未完全解决**（需要验证是否正常工作）

6. **ProGuard 代码混淆**
   - 问题：Release 构建时可能仍有混淆问题
   - 状态：**未完全解决**（已添加规则，但需要验证）

---

## 配置文件修改

### 1. AndroidManifest.xml

**文件位置**：`mobile/android/app/src/main/AndroidManifest.xml`

**必须添加的配置**：

```xml
<application 
  ...
  android:usesCleartextTraffic="true">
  <!-- Google Maps API Key -->
  <meta-data 
    android:name="com.google.android.geo.API_KEY" 
    android:value="YOUR_GOOGLE_MAPS_API_KEY"/>
  ...
</application>
```

**完整示例**（第16-20行）：
```xml
<application android:name=".MainApplication" android:label="@string/app_name" android:icon="@mipmap/ic_launcher" android:roundIcon="@mipmap/ic_launcher_round" android:allowBackup="true" android:theme="@style/AppTheme" android:supportsRtl="true" android:enableOnBackInvokedCallback="false" android:usesCleartextTraffic="true">
  <meta-data android:name="expo.modules.updates.ENABLED" android:value="false"/>
  <meta-data android:name="expo.modules.updates.EXPO_UPDATES_CHECK_ON_LAUNCH" android:value="ALWAYS"/>
  <meta-data android:name="expo.modules.updates.EXPO_UPDATES_LAUNCH_WAIT_MS" android:value="0"/>
  <meta-data android:name="com.google.android.geo.API_KEY" android:value="AIzaSyCvqE-3bgiojZgdO07B3WLtg8iXyi9S4Uo"/>
  ...
</application>
```

**注意事项**：
- ⚠️ **重要**：运行 `npx expo prebuild --clean` 会覆盖此文件，需要重新添加这些配置
- Google Maps API Key 应该从环境变量读取，但目前需要手动添加

---

### 2. ProGuard 规则

**文件位置**：`mobile/android/app/proguard-rules.pro`

**必须添加的规则**：

```proguard
# react-native-reanimated
-keep class com.swmansion.reanimated.** { *; }
-keep class com.facebook.react.turbomodule.** { *; }

# react-native-maps
-keep class com.airbnb.android.react.maps.** { *; }
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# 保护 API_BASE_URL 常量不被优化
-keepclassmembers class * {
    public static final *** API_BASE_URL;
}
```

**说明**：
- 这些规则防止关键类被 ProGuard/R8 混淆
- 通常不会被 prebuild 覆盖，但建议检查

---

### 3. app.config.js

**文件位置**：`mobile/app.config.js`

**关键配置**：

```javascript
module.exports = {
  expo: {
    name: "UrbanQuest",
    slug: "mobile",
    // ...
    newArchEnabled: false, // 全局设置
    android: {
      package: "com.grexrr.urbanquest", // ⚠️ 必须与 iOS bundleIdentifier 区分
      newArchEnabled: true, // Android 启用新架构
      splash: {
        image: "./assets/splash-icon.png",
        resizeMode: "cover", // 全屏显示
        backgroundColor: "#ffffff"
      },
      config: {
        googleMaps: {
          apiKey: process.env.GOOGLE_MAPS_API_KEY || ""
        }
      },
      adaptiveIcon: {
        foregroundImage: "./assets/adaptive-icon.png",
        backgroundColor: "#ffffff"
      },
      // ...
    },
    ios: {
      bundleIdentifier: "com.grexrr.scavengerhunt", // ⚠️ 与 Android package 不同
      newArchEnabled: true,
      // ...
    }
  }
};
```

**注意事项**：
- Android `package` 和 iOS `bundleIdentifier` 可以不同
- `newArchEnabled` 在 Android 上必须为 `true`（react-native-reanimated 4.x 要求）
- Google Maps API Key 从 `.env` 文件读取

---

### 4. constants.ts

**文件位置**：`mobile/utils/constants.ts`

**当前配置**：

```typescript
export const API_BASE_URL = __DEV__
  ? 'http://192.168.1.9:8443'  // ⚠️ 这个地址在 Android 上无法访问
  : 'https://454bb8d88e34.ngrok-free.app';
```

**问题**：
- Android 设备无法访问 `http://192.168.1.9:8443`
- 实际需要访问的是 `https://454bb8d88e34.ngrok-free.app`，但 Android 设备也无法访问
- **需要修复**：修改 `constants.ts` 让 Android 在开发模式下也使用 ngrok URL，或者配置正确的网络地址

---

## 构建流程

### 标准构建流程

```bash
# 1. 清理并重新生成原生代码
cd mobile
npx expo prebuild --clean --platform android

# 2. 应用手动修改（重要！）
# - 编辑 AndroidManifest.xml：添加 usesCleartextTraffic 和 Google Maps API Key
# - 检查 proguard-rules.pro：确保规则存在

# 3. 构建并运行
npx expo run:android

# 或构建 Release APK
cd android
./gradlew assembleRelease
```

### 快速构建（不清理）

```bash
cd mobile
npx expo run:android
```

---

## 常见错误及解决方案

### 1. `API key not found` (Google Maps)

**错误信息**：
```
java.lang.IllegalStateException: API key not found. Check that <meta-data android:name="com.google.android.geo.API_KEY" android:value="..." /> is in the <application> element of AndroidManifest.xml
```

**解决方案**：
1. 检查 `AndroidManifest.xml` 中是否有 Google Maps API Key 的 meta-data
2. 如果运行了 `prebuild --clean`，需要重新添加
3. 确保 `app.config.js` 中配置了 `android.config.googleMaps.apiKey`

---

### 2. `Network request failed`

**错误信息**：
```
TypeError: Network request failed
```

**可能原因**：
1. AndroidManifest.xml 缺少 `usesCleartextTraffic="true"`
2. Android 设备无法访问开发服务器 IP
3. 防火墙或网络配置问题

**解决方案**：
1. 添加 `android:usesCleartextTraffic="true"` 到 AndroidManifest.xml
2. 使用 `adb reverse` 或修改 `API_BASE_URL` 为 `localhost` 或 `10.0.2.2`
3. 检查网络连接和防火墙设置

---

### 3. `package com.urbanquest does not exist`

**错误信息**：
```
package com.urbanquest does not exist
```

**原因**：
- 包名不匹配：`app.config.js` 中的 `android.package` 与生成的代码不一致

**解决方案**：
1. 确保 `app.config.js` 中 `android.package` 正确
2. 运行 `npx expo prebuild --clean`
3. 清理构建缓存：`cd android && ./gradlew clean`

---

### 4. `Fatal signal 6 (SIGABRT)` - 应用崩溃

**错误信息**：
```
Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 20232 (pool-2-thread-1), pid 20115
```

**可能原因**：
- 未捕获的异常（网络请求失败、资源缺失等）
- Google Maps 初始化失败
- 其他运行时错误

**调试步骤**：
1. 查看完整崩溃日志：
   ```bash
   adb logcat -d | grep -A 50 "Fatal\|AndroidRuntime" | grep -A 50 "com.grexrr.urbanquest"
   ```
2. 检查 React Native 日志：
   ```bash
   adb logcat | grep -i "ReactNative\|JS"
   ```
3. 检查是否有资源文件缺失
4. 尝试在 Debug 模式下运行，查看更详细的错误信息

---

### 5. 应用图标不显示

**问题**：Android 设备上显示默认图标

**解决方案**：
1. 确保 `app.config.js` 中配置了 `icon` 和 `android.adaptiveIcon`
2. 确保图片文件存在：`mobile/assets/icon.png` 和 `mobile/assets/adaptive-icon.png`
3. 运行 `npx expo prebuild --clean` 重新生成资源
4. 检查生成的资源文件：`mobile/android/app/src/main/res/`

---

### 6. Splash Screen 只显示在中央

**问题**：启动画面只占屏幕中央一小块，未全屏

**解决方案**：
1. 确保 `app.config.js` 中 `android.splash.resizeMode` 设置为 `"cover"`
2. 检查图片尺寸和格式
3. 运行 `npx expo prebuild --clean` 重新生成

---

## 待解决问题

### 1. 应用崩溃（高优先级）

**问题描述**：
- 应用启动后立即崩溃，日志显示 `Fatal signal 6 (SIGABRT)`
- 崩溃发生在 `pool-2-thread-1`，可能是后台线程中的错误

**需要调查**：
1. 查看完整的崩溃堆栈（使用 `adb logcat`）
2. 检查是否有未捕获的 Promise rejection
3. 检查 Google Maps 初始化是否成功
4. 检查网络请求是否在应用启动时立即触发
5. 尝试在 `App.tsx` 中添加全局错误处理

**建议的调试步骤**：
```bash
# 1. 查看完整崩溃日志
adb logcat -d | grep -B 20 -A 50 "Fatal\|AndroidRuntime" | tail -200

# 2. 查看 React Native 日志
adb logcat | grep -i "ReactNative\|JS\|Error"

# 3. 在 Debug 模式下运行
npx expo run:android --variant debug

# 4. 检查是否有资源文件问题
ls -la mobile/android/app/src/main/res/mipmap-*/
```

---

### 2. 网络请求地址配置（高优先级）

**问题描述**：
- Android 设备无法访问 `https://454bb8d88e34.ngrok-free.app`
- 当前 `constants.ts` 中 `__DEV__` 时使用 `http://192.168.1.9:8443`，但 Android 设备无法访问
- 需要让 Android 设备能够访问后端 API

**建议的解决方案**：

**方案 A：统一使用 ngrok URL（推荐）**
```typescript
// constants.ts
export const API_BASE_URL = 'https://454bb8d88e34.ngrok-free.app';
```
- 问题：需要确保 ngrok 服务运行，且 Android 设备能访问该 URL
- 可能需要处理 ngrok 的浏览器警告头

**方案 B：使用 adb reverse（USB 调试）**
```bash
adb reverse tcp:8443 tcp:8443
# 然后修改 constants.ts
export const API_BASE_URL = __DEV__
  ? (Platform.OS === 'android' ? 'http://localhost:8443' : 'http://192.168.1.9:8443')
  : 'https://454bb8d88e34.ngrok-free.app';
```

**方案 C：使用 Android 模拟器的特殊 IP**
```typescript
// constants.ts
export const API_BASE_URL = __DEV__
  ? (Platform.OS === 'android' ? 'http://10.0.2.2:8443' : 'http://192.168.1.9:8443')
  : 'https://454bb8d88e34.ngrok-free.app';
```
- 仅适用于 Android 模拟器，不适用于真机

---

### 3. 图标和 Splash Screen（低优先级）

**问题描述**：
- 图标可能不显示
- Splash Screen 可能不占满屏幕

**建议**：
- 检查图片文件格式和尺寸
- 确保运行了 `prebuild` 后资源文件正确生成
- 考虑使用 Expo 的图标生成工具

---

## 重要提醒

### ⚠️ 每次 prebuild 后必须做的修改

运行 `npx expo prebuild --clean` 后，**必须**手动修改以下文件：

1. **AndroidManifest.xml**
   - 添加 `android:usesCleartextTraffic="true"` 到 `<application>` 标签
   - 添加 Google Maps API Key 的 `<meta-data>`

2. **检查 proguard-rules.pro**
   - 确保 ProGuard 规则存在（通常不会被覆盖）

### 📝 环境变量

确保 `mobile/.env` 文件存在并包含：
```
GOOGLE_MAPS_API_KEY=你的API密钥
```

### 🔄 构建前检查清单

- [ ] `.env` 文件存在且包含 `GOOGLE_MAPS_API_KEY`
- [ ] `app.config.js` 中 `android.package` 正确
- [ ] 运行 `prebuild` 后检查 `AndroidManifest.xml`
- [ ] 添加 `usesCleartextTraffic` 和 Google Maps API Key
- [ ] 检查 `proguard-rules.pro` 规则
- [ ] 确保图片资源文件存在

---

## 相关文件

- `mobile/app.config.js` - Expo 主配置文件
- `mobile/.env` - 环境变量（不提交到 Git）
- `mobile/android/app/src/main/AndroidManifest.xml` - Android 清单文件（需要手动修改）
- `mobile/android/app/proguard-rules.pro` - ProGuard 规则（需要手动修改）
- `mobile/utils/constants.ts` - API 基础 URL 配置
- `mobile/App.tsx` - 应用入口
- `mobile/services/client.ts` - API 客户端

---

## 参考链接

- [react-native-maps GitHub Issue #4936](https://github.com/react-native-maps/react-native-maps/issues/4936) - 新架构兼容性问题
- [Android ProGuard 官方文档](https://developer.android.com/guide/developing/tools/proguard.html)
- [Expo Android 配置文档](https://docs.expo.dev/workflow/android/)
- [Android Network Security Config](https://developer.android.com/training/articles/security-config)

---

## 最后更新

- 日期：2025-01-08
- 状态：**构建可通过，但仍有运行时问题**
- 为了让 Android 构建成功已做的修改：
  1. **启用新架构**：`mobile/app.config.js` 中 `newArchEnabled`（expo/android/ios）全部改为 `true`；`mobile/android/gradle.properties` 保持 `newArchEnabled=true`（Reanimated/Worklets 需要）
  2. **修复 autolinking 的包名**：新增 `mobile/react-native.config.js`，强制 `packageName: "com.grexrr.urbanquest"`，解决 `com.urbanquest.BuildConfig` 编译错误
  3. **Google Maps API Key 手动注入**：`mobile/android/app/src/main/AndroidManifest.xml` 增加 `<meta-data android:name="com.google.android.geo.API_KEY" ... />`
  4. **图标资源生成问题**：新增 `mobile/assets/adaptive-icon-clean.png`，并在 `mobile/app.config.js` 指向该文件（避免生成 0 bytes 资源）
  5. **安全区遮挡**：`mobile/components/FloatingActionButton.tsx` 和 `mobile/app/settings.tsx` 增加底部安全区 padding，避免按钮被 Android 导航栏遮住
- 仍待解决/持续问题：
  1. 应用崩溃（SIGABRT，堆栈指向 react-native-maps 的 RNMaps*Props）
  2. Splash 仍为旧图（`mobile/assets/splash-icon.png` 未替换或未更新）
  3. Android 图标仍可能显示异常（需确认生成后的 mipmap 资源）
  4. 登录后 start-round 403（guest 会话未刷新需验证）
