#!/bin/bash

# 停止脚本 - macOS/Linux
# 使用说明: chmod +x stop.sh && ./stop.sh

# 设置项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# 检查PID文件是否存在
PID_FILE="app.pid"
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    echo "正在停止进程ID: $PID"
    
    # 尝试优雅关闭
    kill -TERM "$PID"
    
    # 等待进程结束
    sleep 5
    
    # 检查进程是否还存在
    if kill -0 "$PID" 2>/dev/null; then
        echo "进程仍在运行，强制终止..."
        kill -KILL "$PID"
    fi
    
    # 删除PID文件
    rm -f "$PID_FILE"
    echo "应用已停止"
else
    echo "未找到PID文件，可能应用未运行"
fi

# 清理临时文件
rm -f app.log