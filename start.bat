@echo off
echo ========================================
echo 家庭小助手启动脚本
echo ========================================
echo.

REM 检查 .env 文件
if not exist .env (
    echo [警告] .env 文件不存在!
    echo 请复制 .env.example 为 .env 并配置相关参数
    echo.
    pause
    exit /b 1
)

echo [1/3] 检查 Docker 环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未安装或未添加到 PATH
    pause
    exit /b 1
)

echo [2/3] 启动 Milvus 和相关服务...
docker-compose up -d milvus-standalone etcd minio
if errorlevel 1 (
    echo [错误] 服务启动失败
    pause
    exit /b 1
)

echo [3/3] 启动家庭助手...
docker-compose up -d family-assistant
if errorlevel 1 (
    echo [错误] 家庭助手启动失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 启动完成!
echo ========================================
echo.
echo 服务访问地址:
echo - 家庭助手 API: http://localhost:8080
echo - Milvus: localhost:19530
echo - MinIO 控制台: http://localhost:9001
echo.
echo 查看日志: docker-compose logs -f family-assistant
echo 停止服务: docker-compose down
echo.
pause
