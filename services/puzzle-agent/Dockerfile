FROM python:3.12-slim

# 1) Basic Setup
# Do not generate .pyc files
# Output logs in real-time
ENV PYTHONDONTWRITEBYTECODE=1 \  
    PYTHONUNBUFFERED=1

WORKDIR /app

# 2) Copy only requirements first to install dependencies, leveraging cache
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 3) Then copy the source code
COPY . .

# 4) Expose port inside the container
EXPOSE 5000

# 5) Run using HOST/PORT from environment variables by default; ensure your code uses these environment variables
#    Equivalent to: app.run(host=os.getenv("FLASK_HOST","0.0.0.0"), port=int(os.getenv("FLASK_PORT","5000")))
CMD ["python", "app.py"]