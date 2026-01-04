完整流程如下：

## 完整部署流程（无Jenkins）

### 阶段1：修复代码问题

#### 1.1 修复 `main.js` - 使用相对路径
```javascript
// backend/src/main/resources/static/main.js 第45行
const LOCAL_HOST = window.location.origin;  // 自动适配
```

#### 1.2 修复 `constants.ts` - 生产环境URL
```typescript
// mobile/utils/constants.ts
export const API_BASE_URL = __DEV__ 
  ? 'http://192.168.1.9:8443' 
  : 'https://<EC2_IP>';  // 替换为实际EC2 IP
```

#### 1.3 修复文档错误
- `doc/deployment.md` 第443行：`阶段6` → `阶段7`

---

### 阶段2：修改 docker-compose.yml 添加 image 标签

修改 `docker-compose.yml`：

```yaml
  spring-backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    image: your-username/spring-backend:latest  # 添加这行
    # ... 其他配置保持不变

  puzzle-agent:
    build:
      context: ../scavenger.PuzzleAgent
      dockerfile: Dockerfile
    image: your-username/puzzle-agent:latest  # 添加这行
    # ... 其他配置保持不变

  landmark-processor:
    build:
      context: ../scavenger.LandmarkProcessor
      dockerfile: Dockerfile
    image: your-username/landmark-processor:latest  # 添加这行
    # ... 其他配置保持不变
```

注意：`spring-backend` 目前只有 `image:`，需要添加 `build:` 部分（如果还没有）。

---

### 阶段3：本地构建并推送镜像

#### 3.1 登录 Docker Registry
```bash
# Docker Hub
docker login

# 或 GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin
```

#### 3.2 构建所有镜像
```bash
cd /Users/grexrr/Documents/scavengerHunt

# 先构建Spring Boot JAR
cd backend
mvn clean package -DskipTests
cd ..

# 构建所有Docker镜像（包括两个Flask服务）
docker-compose build
```

#### 3.3 推送所有镜像
```bash
# 一次性推送所有带image标签的服务
docker-compose push
```

这会推送：
- `your-username/spring-backend:latest`
- `your-username/puzzle-agent:latest`
- `your-username/landmark-processor:latest`

---

### 阶段4：EC2 环境准备

#### 4.1 安装 Docker 和 Docker Compose
```bash
# SSH到EC2
ssh ubuntu@<EC2_IP>

# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu
newgrp docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 安装Nginx
sudo apt update
sudo apt install nginx -y
```

#### 4.2 准备部署文件
```bash
# 在EC2上
cd ~
mkdir -p scavengerhunt
cd scavengerhunt

# 从Git clone（或scp传输）
git clone <your-repo-url> .
```

#### 4.3 创建生产环境 docker-compose.yml

在 EC2 上创建 `docker-compose.prod.yml`：

```yaml
version: '3.8'

networks:
  scavenger-net:
    name: scavenger-net
    driver: bridge

volumes:
  mongo-data:
    name: scavenger-mongo-data

services:
  mongo:
    image: mongo:8.0
    container_name: mongo-scavenger
    networks:
      - scavenger-net
    volumes:
      - mongo-data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=scavengerhunt
    restart: unless-stopped

  spring-backend:
    image: your-username/spring-backend:latest  # 从Registry拉取
    container_name: spring-backend
    networks:
      - scavenger-net
    ports:
      - "8443:8080"  # 仅Nginx访问
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongo-scavenger:27017/scavengerhunt
      - JAVA_TOOL_OPTIONS=-Dpuzzle.agent.url=http://puzzle-agent:5000 -Dlandmark.processor.url=http://landmark-processor:5000
    depends_on:
      - mongo
      - puzzle-agent
      - landmark-processor
    restart: unless-stopped

  puzzle-agent:
    image: your-username/puzzle-agent:latest  # 从Registry拉取
    container_name: puzzle-agent
    networks:
      - scavenger-net
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - MONGO_URL=mongodb://mongo-scavenger:27017
      - FLASK_HOST=0.0.0.0
      - FLASK_PORT=5000
    depends_on:
      - mongo
    restart: unless-stopped

  landmark-processor:
    image: your-username/landmark-processor:latest  # 从Registry拉取
    container_name: landmark-processor
    networks:
      - scavenger-net
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - MONGO_URL=mongodb://mongo-scavenger:27017
      - MONGO_DB=scavengerhunt
      - FLASK_HOST=0.0.0.0
      - FLASK_PORT=5000
    depends_on:
      - mongo
    restart: unless-stopped
```

#### 4.4 创建 .env 文件
```bash
# 在EC2上
cd ~/scavengerhunt
nano .env
```

内容：
```bash
OPENAI_API_KEY=sk-your-actual-key-here
```

#### 4.5 拉取并启动服务
```bash
# 在EC2上
cd ~/scavengerhunt

# 创建网络
docker network create scavenger-net 2>/dev/null || true

# 拉取所有镜像
docker-compose -f docker-compose.prod.yml pull

# 启动服务
docker-compose -f docker-compose.prod.yml up -d

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

---

### 阶段5：配置 Nginx HTTPS

#### 5.1 生成 SSL 证书
```bash
# 在EC2上
sudo mkdir -p /etc/nginx/ssl
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/nginx-selfsigned.key \
  -out /etc/nginx/ssl/nginx-selfsigned.crt \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=<EC2_IP>"
```

#### 5.2 配置 Nginx
```bash
sudo nano /etc/nginx/sites-available/scavengerhunt
```

内容：
```nginx
# HTTP重定向到HTTPS
server {
    listen 80;
    server_name <EC2_IP>;
    return 301 https://$server_name$request_uri;
}

# HTTPS配置
server {
    listen 443 ssl http2;
    server_name <EC2_IP>;
    
    ssl_certificate /etc/nginx/ssl/nginx-selfsigned.crt;
    ssl_certificate_key /etc/nginx/ssl/nginx-selfsigned.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    location / {
        proxy_pass http://localhost:8443;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

#### 5.3 启用并启动 Nginx
```bash
sudo ln -s /etc/nginx/sites-available/scavengerhunt /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl enable nginx
```

---

### 阶段6：验证部署

```bash
# 测试健康检查
curl -k https://<EC2_IP>/actuator/health

# 测试Web前端
# 浏览器访问: https://<EC2_IP>/

# 查看容器状态
docker-compose -f docker-compose.prod.yml ps
```

---

## 后续更新流程

当代码更新后：

```bash
# 1. 本地构建并推送
cd /Users/grexrr/Documents/scavengerHunt
cd backend && mvn clean package -DskipTests && cd ..
docker-compose build
docker-compose push

# 2. EC2上更新
ssh ubuntu@<EC2_IP>
cd ~/scavengerhunt
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

---

## 总结

1. 修复代码（API地址）
2. 修改 docker-compose.yml（添加 image 标签）
3. 本地构建并推送镜像
4. EC2 拉取镜像并运行
5. 配置 Nginx HTTPS

需要我生成具体的修改文件内容吗？
