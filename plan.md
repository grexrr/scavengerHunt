<!-- 1cb34b48-1829-42bb-81cc-d65a7dd4a086 61f135a6-73dc-4b8e-8193-8ccf8aea02e1 -->
# React Native 移动端应用迁移计划

## 项目结构

```
scavengerHunt/
├── mobile/                          # 新建移动端目录
│   ├── app.json                     # Expo 配置
│   ├── package.json                 # 依赖管理
│   ├── tsconfig.json                # TypeScript 配置
│   ├── app/
│   │   ├── _layout.tsx             # 导航根布局
│   │   ├── (auth)/                 # 认证流程
│   │   │   ├── login.tsx
│   │   │   └── register.tsx
│   │   └── (tabs)/                 # 主游戏界面
│   │       ├── index.tsx           # 地图主屏
│   │       └── profile.tsx         # 用户信息
│   ├── components/
│   │   ├── MapView.tsx             # 地图组件（含 GeoJSON 渲染）
│   │   ├── PlayerMarker.tsx        # 玩家位置标记
│   │   ├── ViewCone.tsx            # 视角扇形
│   │   ├── LandmarkPolygon.tsx     # 地标多边形
│   │   ├── GameHUD.tsx             # 游戏信息覆盖层
│   │   ├── CountdownTimer.tsx      # 倒计时组件
│   │   └── AuthForm.tsx            # 认证表单
│   ├── services/
│   │   ├── api/
│   │   │   ├── client.ts           # API 客户端（fetch 封装）
│   │   │   ├── auth.ts             # 认证 API
│   │   │   ├── game.ts             # 游戏 API
│   │   │   └── types.ts            # API 类型定义
│   │   ├── location/
│   │   │   ├── locationService.ts # GPS 定位服务
│   │   │   └── headingService.ts  # 设备方向服务
│   │   └── storage/
│   │       └── storageService.ts   # AsyncStorage 封装
│   ├── hooks/
│   │   ├── useGameSession.ts       # 游戏会话状态管理
│   │   ├── useLocation.ts          # GPS 定位 Hook
│   │   ├── useHeading.ts           # 设备方向 Hook
│   │   └── useAuth.ts              # 认证状态 Hook
│   ├── utils/
│   │   ├── geoUtils.ts             # 地理计算工具
│   │   └── constants.ts           # 常量配置
│   └── types/
│       └── index.ts                # 全局类型定义
```

## 技术栈

### 核心依赖

- **React Native + Expo**: 跨平台框架
- **react-native-maps**: 地图渲染（支持 GeoJSON 多边形）
- **expo-location**: GPS 定位
- **expo-sensors**: 设备方向（Magnetometer/Gyroscope）
- **@react-navigation**: 导航路由
- **@react-native-async-storage**: 本地存储
- **TypeScript**: 类型安全

### API 集成

- 使用原生 `fetch` API 调用后端 REST 端点
- 配置基础 URL（开发/生产环境）
- 统一错误处理和重试机制

## 核心功能模块

### 1. 认证模块 (`app/(auth)/`)

**文件**: `app/(auth)/login.tsx`, `register.tsx`

**功能**:

- 用户注册/登录
- 存储 userId/username/role 到 AsyncStorage
- 导航到主游戏界面

**API 调用**:

- `POST /api/auth/register`
- `POST /api/auth/login`

**移除**: logout 功能（移动端通常不需要）

### 2. 地图渲染模块 (`components/MapView.tsx`)

**核心功能**:

- 集成 `react-native-maps` MapView
- 渲染地标多边形（从 `LandmarkDTO.coordinates` 转换）
- 玩家位置标记（实时更新）
- View Cone 扇形覆盖（60度，50米半径）
- 搜索半径圆圈（开始回合前显示）

**GeoJSON 处理**:

- 后端返回 `coordinates: List<List<Double>>` 格式 `[lat, lng]`
- 转换为 `react-native-maps` 的 `Polygon` 组件所需的坐标格式
- 支持多边形填充和描边样式

**关键代码结构**:

```typescript
// 坐标转换：后端 [lat, lng][] → react-native-maps [{latitude, longitude}]
const convertCoordinates = (coords: number[][]): {latitude: number, longitude: number}[] => {
  return coords.map(([lat, lng]) => ({ latitude: lat, longitude: lng }));
};
```

### 3. 定位服务 (`services/location/`)

**GPS 定位** (`locationService.ts`):

- 使用 `expo-location` 的 `watchPositionAsync`
- 实时更新玩家位置
- 请求精确位置权限（`Location.Accuracy.High`）
- 错误处理（权限拒绝、定位失败）

**设备方向** (`headingService.ts`):

- 使用 `expo-sensors` 的 `Magnetometer` 或 `DeviceMotion`
- 计算设备朝向角度（0-360度，0=北）
- 移除校准逻辑，直接使用绝对方向

**Hooks**:

- `useLocation()`: 返回 `{latitude, longitude, accuracy}`
- `useHeading()`: 返回 `{heading, accuracy}`

### 4. 游戏流程模块 (`hooks/useGameSession.ts`)

**状态管理**:

- 游戏会话状态（未开始/进行中/已完成）
- 当前目标地标
- 谜题内容
- 剩余尝试次数
- 倒计时状态

**游戏流程**:

1. **初始化** (`POST /api/game/init-game`):

   - 发送玩家位置、角度、视野参数
   - 接收地标列表，渲染到地图

2. **开始回合** (`POST /api/game/start-round`):

   - 发送位置、角度、搜索半径、语言/风格偏好
   - 接收第一个目标地标和谜题

3. **提交答案** (`POST /api/game/submit-answer`):

   - 发送 userId、用时、当前位置和角度
   - 接收结果（正确/错误）、下一目标或游戏结束

4. **结束回合** (`POST /api/game/finish-round`):

   - 重置游戏状态

### 5. UI 组件

**GameHUD** (`components/GameHUD.tsx`):

- 浮动信息覆盖层
- 当前目标名称
- 谜题文本框
- 剩余尝试次数
- 倒计时显示
- 提交答案按钮（仅在可提交时显示）

**CountdownTimer** (`components/CountdownTimer.tsx`):

- 30分钟倒计时（1800秒）
- 使用 `useEffect` + `setInterval` 管理
- 时间到自动处理

**LandmarkPolygon** (`components/LandmarkPolygon.tsx`):

- 单个地标多边形渲染
- 已解决状态（蓝色）/未解决状态（灰色）
- 点击交互（可选，用于调试）

## 数据流设计

### API 客户端 (`services/api/client.ts`)

```typescript
const API_BASE_URL = __DEV__ 
  ? 'http://localhost:8443' 
  : 'https://your-production-url.com';

class ApiClient {
  async post<T>(endpoint: string, body: any): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!response.ok) throw new Error(`API Error: ${response.status}`);
    return response.json();
  }
}
```

### 类型定义 (`types/index.ts`)

```typescript
interface LandmarkDTO {
  id: string;
  name: string;
  coordinates: number[][]; // [lat, lng][]
}

interface TargetResponse {
  id: string;
  name: string;
  riddle: string;
  attemptsLeft: number;
}

interface SubmitAnswerResponse {
  isCorrect: boolean;
  gameFinished: boolean;
  message: string;
  target?: TargetResponse;
}
```

## 移除的功能

1. **Admin 模式**: 移除所有 Admin 相关代码和测试功能
2. **设备校准**: 移除 `startCalibration()` / `finishCalibration()` 逻辑
3. **地图旋转**: 移除第一人称视角的地图旋转（移动端不适用）
4. **手动拖拽**: 移除地图点击/拖拽移动玩家位置的功能

## 实现步骤

### Phase 1: 项目初始化

1. 在项目根目录创建 `mobile/` 文件夹
2. 运行 `npx create-expo-app@latest . --template blank-typescript`
3. 安装核心依赖：react-native-maps, expo-location, expo-sensors
4. 配置 `app.json` 中的权限（位置、传感器）

### Phase 2: 基础架构

1. 创建目录结构和文件骨架
2. 实现 API 客户端基础类
3. 实现 AsyncStorage 封装
4. 定义 TypeScript 类型

### Phase 3: 定位和方向

1. 实现 `locationService.ts` GPS 定位
2. 实现 `headingService.ts` 设备方向
3. 创建 `useLocation` 和 `useHeading` Hooks
4. 测试权限请求和实时更新

### Phase 4: 认证流程

1. 实现登录/注册页面
2. 集成 API 调用
3. 实现导航跳转
4. 存储认证状态

### Phase 5: 地图集成

1. 配置 `react-native-maps`（iOS/Android 原生配置）
2. 实现 `MapView.tsx` 基础地图显示
3. 实现坐标转换工具函数
4. 渲染地标多边形（从 `init-game` 响应）
5. 添加玩家位置标记和实时更新

### Phase 6: View Cone 和游戏逻辑

1. 实现 View Cone 扇形渲染（基于 heading）
2. 实现 `useGameSession` Hook
3. 集成游戏 API 调用（init-game, start-round, submit-answer）
4. 实现游戏状态管理

### Phase 7: UI 组件和集成

1. 实现 `GameHUD` 覆盖层
2. 实现 `CountdownTimer`
3. 实现搜索半径圆圈（开始回合前）
4. 集成所有组件到主界面

### Phase 8: 测试和优化

1. iOS/Android 设备测试
2. GPS 精度验证
3. 方向传感器准确性测试
4. 性能优化（地图渲染、API 调用）
5. 错误处理和边界情况

## 关键配置

### `app.json` 权限配置

```json
{
  "expo": {
    "plugins": [
      [
        "expo-location",
        {
          "locationAlwaysAndWhenInUsePermission": "This app uses location to track your position in the scavenger hunt game."
        }
      ]
    ],
    "ios": {
      "infoPlist": {
        "NSLocationWhenInUseUsageDescription": "Location access required for game",
        "NSLocationAlwaysAndWhenInUseUsageDescription": "Location access required for game"
      }
    },
    "android": {
      "permissions": [
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION"
      ]
    }
  }
}
```

### `react-native-maps` 配置

- iOS: 在 `ios/Podfile` 中已包含（Expo 自动处理）
- Android: 需要 Google Maps API Key（`app.json` 中配置）

## 注意事项

1. **地图库选择**: 如果 `react-native-maps` 的 GeoJSON 支持不够，可切换到 `@rnmapbox/maps`（需要 Mapbox 账户和 API key）

2. **坐标系统**: 确保后端返回的坐标顺序与 `react-native-maps` 期望的格式匹配（`[lat, lng]`）

3. **性能**: 大量多边形可能影响性能，考虑使用地图聚类或按视野范围筛选

4. **方向精度**: 移动设备的方向传感器在不同环境下精度不同，需要处理磁场干扰情况

5. **网络**: 实现离线检测和重试机制

### To-dos

- [ ] 初始化 Expo 项目：创建 mobile/ 目录，运行 create-expo-app，配置 TypeScript
- [ ] 安装核心依赖：react-native-maps, expo-location, expo-sensors, @react-navigation, @react-native-async-storage
- [ ] 创建项目目录结构：app/, components/, services/, hooks/, utils/, types/
- [ ] 实现 API 客户端基础类：services/api/client.ts，包含 fetch 封装和错误处理
- [ ] 定义 TypeScript 类型：LandmarkDTO, TargetResponse, SubmitAnswerResponse 等
- [ ] 实现 GPS 定位服务：services/location/locationService.ts，使用 expo-location
- [ ] 实现设备方向服务：services/location/headingService.ts，使用 expo-sensors
- [ ] 创建 useLocation 和 useHeading Hooks：hooks/useLocation.ts, hooks/useHeading.ts
- [ ] 实现认证 API：services/api/auth.ts，包含 register 和 login 方法
- [ ] 实现认证界面：app/(auth)/login.tsx, register.tsx，集成导航
- [ ] 实现游戏 API：services/api/game.ts，包含 init-game, start-round, submit-answer, finish-round
- [ ] 实现基础地图组件：components/MapView.tsx，集成 react-native-maps
- [ ] 实现坐标转换工具：utils/geoUtils.ts，将后端坐标格式转换为地图组件格式
- [ ] 实现地标多边形渲染：components/LandmarkPolygon.tsx，支持坐标转换和样式
- [ ] 实现玩家位置标记：components/PlayerMarker.tsx，实时更新位置
- [ ] 实现 View Cone 扇形：components/ViewCone.tsx，基于 heading 和位置计算扇形区域
- [ ] 实现游戏会话 Hook：hooks/useGameSession.ts，管理游戏状态和 API 调用
- [ ] 实现游戏信息覆盖层：components/GameHUD.tsx，显示目标、谜题、倒计时
- [ ] 实现倒计时组件：components/CountdownTimer.tsx，30分钟倒计时逻辑
- [ ] 实现主游戏界面：app/(tabs)/index.tsx，集成所有组件和游戏流程
- [ ] 实现存储服务：services/storage/storageService.ts，封装 AsyncStorage 操作
- [ ] 实现认证状态 Hook：hooks/useAuth.ts，管理登录状态和用户信息
- [ ] 配置导航路由：app/_layout.tsx，设置认证和主界面导航流程
- [ ] 测试和优化：iOS/Android 设备测试，GPS/方向传感器验证，性能优化