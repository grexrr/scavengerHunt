FROM python:3.12-slim

# 更稳妥的基础设置
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1

WORKDIR /app

# 可选：升级 pip（不要写进 requirements.txt）
RUN python -m pip install --no-cache-dir --upgrade pip

# 先装依赖（利用缓存）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 再拷贝源码
COPY . .

# 容器内监听端口（与 FLASK_PORT 对齐）
EXPOSE 5000

# 用环境变量里的 HOST/PORT 启动；确保代码里用它们
CMD ["python", "app.py"]
