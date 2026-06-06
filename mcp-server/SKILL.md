
# Windows 桌面操作 — 查看桌面 + 执行命令

通过 Windows Agent 桥接，Claude 可以直接截取用户桌面、执行 CMD/PowerShell 命令。全自动，无需用户手动复制粘贴。

## 架构

```
Claude (VM)  ──写 JSON──▶  共享目录  ──轮询──▶  Windows Agent (Python)
     ◀──读结果──                            ◀──执行命令/截图──
```

Agent 在 Windows 上运行（双击 `start-agent.bat`），轮询共享目录中的 JSON 请求文件，执行后把结果写回。

## 前置条件

**Agent 必须正在运行。** 检查方法：

```bash
cat /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-heartbeat.json
```

如果文件不存在或 `status` 不是 `"alive"`，提醒用户双击启动 Agent：
- Agent 位置：`QDBMS/mcp-server/start-agent.bat`（首次需要 `pip install Pillow mss`）

## 核心能力

### 1. 截取桌面（查看用户在做什么）

**用途**：看用户的屏幕、确认操作结果、验证 UI 状态。

**操作**：写入截图请求文件，等待 3 秒，读取截图：

```bash
# 步骤 1：发截图请求（编号递增避免冲突）
echo '{"max_width":1920}' > /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-shot-1.json

# 步骤 2：等待 Agent 处理
sleep 3

# 步骤 3：查看结果
cat /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-result.json

# 步骤 4：读取截图图片
# 截图保存在同目录的 sys-shot-result.png
```

**截图参数**：
- `max_width`：最大宽度（默认 1920），图片等比缩放
- `monitor`：显示器编号（0=全部, 1=主显示器），默认 0

### 2. 执行 Windows 命令（CMD / PowerShell）

**用途**：运行任何 Windows 命令——构建项目、启动服务、操作文件、查询系统信息等。

**操作**：

```bash
# 步骤 1：发命令请求
echo '{"command":"dir C:\\Users\\17605\\Desktop","timeout":30}' > /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-cmd-1.json

# 步骤 2：等待执行
sleep 4

# 步骤 3：读取结果
cat /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-result.json
```

**命令参数**：
- `command`：要执行的命令（字符串，必需）
- `timeout`：超时秒数（默认 60）
- `cwd`：工作目录（可选）

**结果格式**：
```json
{
  "success": true/false,
  "exitCode": 0,
  "stdout": "命令输出...",
  "stderr": "错误输出...",
  "timestamp": "2026-06-09T12:00:00"
}
```

### 3. 检查 Agent 状态

```bash
cat /sessions/<session>/mnt/outputs/edge-browser/mcp-server/sys-heartbeat.json
```

在线时返回：
```json
{"status": "alive", "timestamp": "2026-06-09T12:00:00", "pid": 12345}
```

## 完整工作流程

### 场景 1：查看桌面 + 执行操作

```
用户: "看看我桌面上有什么，然后帮我清理一下"
→ 先截图看桌面 → 分析桌面上有什么 → 执行清理命令
```

### 场景 2：运行项目构建

```
用户: "帮我构建 QDBMS 项目"
→ 执行 Maven/Node 构建命令 → 检查结果 → 截图验证
```

### 场景 3：系统诊断

```
用户: "我的电脑为什么这么慢"
→ 查看系统信息（tasklist, systeminfo）→ 截图任务管理器 → 分析
```

## 路径映射（重要）

| Windows 路径 | VM 路径 |
|-------------|---------|
| `D:\JetBrains\...\QDBMS\` | `/sessions/<session>/mnt/QDBMS/` |
| `%LOCALAPPDATA%\...\outputs\` | `/sessions/<session>/mnt/outputs/` |
| `C:\Users\17605\Desktop\` | 无直接映射（通过 Agent 命令访问） |

## 依赖安装

Agent 首次运行需要 Python 截图库：

```powershell
pip install Pillow mss
```

`start-agent.bat` 会自动检测并安装。如果自动安装失败，手动运行上述命令。

## 故障排查

| 问题 | 解决方案 |
|------|---------|
| Agent 无响应 | 检查窗口是否仍在运行；重新双击 start-agent.bat |
| 截图失败 | `pip install Pillow mss` 后重启 Agent |
| 命令超时 | 增大 timeout 参数；检查命令是否会等待输入 |
| JSON 文件未被消费 | 等待久一些（5-10秒）；检查 Agent 窗口日志 |
| heartbeat 文件不存在 | Agent 未启动或已崩溃 |
