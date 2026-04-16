#!/bin/bash

# 启动脚本 - macOS/Linux
# 使用说明: chmod +x start.sh && ./start.sh

# 设置项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请先安装Java 21"
    exit 1
fi

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
if [[ "$JAVA_VERSION" != "1.8"* ]] && [[ "$JAVA_VERSION" != "11"* ]] && [[ "$JAVA_VERSION" != "17"* ]] && [[ "$JAVA_VERSION" != "21"* ]]; then
    echo "警告: 推荐使用Java 21，当前版本: $JAVA_VERSION"
fi

# 检查Maven是否安装
if command -v mvn &> /dev/null; then
    echo "正在编译项目..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "编译失败!"
        exit 1
    fi
else
    echo "未检测到Maven，跳过编译步骤"
fi

# 检查JAR文件是否存在
JAR_FILE="target/rag-knowledge-base-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 找不到JAR文件 $JAR_FILE"
    echo "请先执行编译命令: mvn clean package -DskipTests"
    exit 1
fi

# 启动应用
echo "正在启动RAG知识库应用..."
nohup java -jar "$JAR_FILE" > app.log 2>&1 &

# 获取进程ID
PID=$!
echo "应用已启动，进程ID: $PID"
echo "日志文件: app.log"

# 保存PID到文件，便于后续停止
echo $PID > app.pid

echo "启动完成!"