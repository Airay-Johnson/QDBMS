"""
Windows Agent — Claude ↔ Windows 桥接
轮询共享目录，执行命令、截图，写回结果。
零外部依赖（仅用 Python 标准库 + Pillow）。
"""
import json, os, sys, time, glob, subprocess, traceback
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

def find_shared_dir():
    """自动发现共享通信目录 outputs/edge-browser/mcp-server/"""
    # 优先使用命令行参数
    if len(sys.argv) > 1:
        path = sys.argv[1]
        if os.path.isdir(path):
            return path

    # 自动搜索 %LOCALAPPDATA%\Claude-3p\...\outputs\edge-browser\mcp-server\
    base = os.path.join(os.environ.get("LOCALAPPDATA", ""), "Claude-3p", "local-agent-mode-sessions")
    if os.path.isdir(base):
        for root, dirs, _ in os.walk(base):
            # 找 outputs\edge-browser\mcp-server 路径
            if root.endswith(os.path.join("outputs", "edge-browser", "mcp-server")):
                return root
            # 限制深度避免搜索太深
            depth = root.replace(base, "").count(os.sep)
            if depth > 6:
                dirs.clear()

    # 回退：用脚本所在目录（向后兼容）
    return SCRIPT_DIR

# 共享目录 = outputs/edge-browser/mcp-server/（VM 可读写）
SHARED_DIR = find_shared_dir()

HEARTBEAT_FILE = os.path.join(SHARED_DIR, "sys-heartbeat.json")
RESULT_FILE = os.path.join(SHARED_DIR, "sys-result.json")
SHOT_RESULT_FILE = os.path.join(SHARED_DIR, "sys-shot-result.png")

def log(msg):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{timestamp}] {msg}"
    print(line)
    sys.stdout.flush()

def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def write_heartbeat():
    write_json(HEARTBEAT_FILE, {
        "status": "alive",
        "timestamp": datetime.now().isoformat(),
        "pid": os.getpid()
    })

def exec_command(cmd_file_path):
    """执行 sys-cmd-*.json 中定义的命令"""
    try:
        with open(cmd_file_path, "r", encoding="utf-8") as f:
            task = json.load(f)
    except Exception as e:
        write_json(RESULT_FILE, {"success": False, "error": f"读取命令文件失败: {e}"})
        os.remove(cmd_file_path)
        return

    command = task.get("command", "")
    timeout = task.get("timeout", 60)
    cwd = task.get("cwd", None)

    log(f"执行命令: {command}")

    try:
        result = subprocess.run(
            command,
            shell=True,
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=timeout,
            encoding="utf-8",
            errors="replace"
        )
        write_json(RESULT_FILE, {
            "success": result.returncode == 0,
            "exitCode": result.returncode,
            "stdout": result.stdout,
            "stderr": result.stderr,
            "timestamp": datetime.now().isoformat()
        })
        log(f"命令完成, exitCode={result.returncode}")
    except subprocess.TimeoutExpired:
        write_json(RESULT_FILE, {
            "success": False,
            "error": f"命令超时 ({timeout}s)",
            "stdout": "",
            "stderr": "",
            "timestamp": datetime.now().isoformat()
        })
        log(f"命令超时")
    except Exception as e:
        write_json(RESULT_FILE, {
            "success": False,
            "error": str(e),
            "stdout": "",
            "stderr": "",
            "timestamp": datetime.now().isoformat()
        })
        log(f"命令异常: {e}")

    try:
        os.remove(cmd_file_path)
    except:
        pass

def take_screenshot(shot_file_path):
    """执行 sys-shot-*.json 中定义的截图请求"""
    try:
        with open(shot_file_path, "r", encoding="utf-8") as f:
            task = json.load(f)
    except Exception as e:
        log(f"读取截图请求失败: {e}")
        try:
            os.remove(shot_file_path)
        except:
            pass
        return

    max_width = task.get("max_width", 1920)
    monitor = task.get("monitor", 0)  # 0 = 全部, 1 = 主显示器

    log(f"截图中... (max_width={max_width})")

    try:
        # 尝试多种截图方式
        img = None
        errors = []

        # 方式1: mss (快速，推荐)
        try:
            import mss
            with mss.mss() as sct:
                if monitor > 0 and monitor <= len(sct.monitors):
                    region = sct.monitors[monitor]
                else:
                    region = sct.monitors[0]  # 0 = 全部显示器
                img_data = sct.grab(region)
                from PIL import Image
                img = Image.frombytes("RGB", img_data.size, img_data.bgra, "raw", "BGRX")
        except ImportError:
            errors.append("mss not installed")
        except Exception as e:
            errors.append(f"mss failed: {e}")

        # 方式2: PIL ImageGrab
        if img is None:
            try:
                from PIL import ImageGrab
                img = ImageGrab.grab(all_screens=True)
            except ImportError:
                errors.append("Pillow not installed")
            except Exception as e:
                errors.append(f"ImageGrab failed: {e}")

        # 方式3: pyautogui
        if img is None:
            try:
                import pyautogui
                img = pyautogui.screenshot()
            except ImportError:
                errors.append("pyautogui not installed")
            except Exception as e:
                errors.append(f"pyautogui failed: {e}")

        if img is None:
            write_json(RESULT_FILE, {
                "success": False,
                "error": f"所有截图方式均失败。请安装: pip install Pillow mss。错误: {'; '.join(errors)}",
                "timestamp": datetime.now().isoformat()
            })
            log(f"截图失败: {'; '.join(errors)}")
        else:
            # 缩放
            w, h = img.size
            if w > max_width:
                ratio = max_width / w
                new_size = (max_width, int(h * ratio))
                img = img.resize(new_size)

            img.save(SHOT_RESULT_FILE, "PNG", optimize=True)
            log(f"截图完成: {img.size[0]}x{img.size[1]}")
            write_json(RESULT_FILE, {
                "success": True,
                "action": "screenshot",
                "size": list(img.size),
                "file": SHOT_RESULT_FILE,
                "timestamp": datetime.now().isoformat()
            })
    except Exception as e:
        write_json(RESULT_FILE, {
            "success": False,
            "error": f"截图异常: {traceback.format_exc()}",
            "timestamp": datetime.now().isoformat()
        })
        log(f"截图异常: {e}")

    try:
        os.remove(shot_file_path)
    except:
        pass

def process_files():
    """扫描并处理所有待处理的请求文件"""
    cmd_files = sorted(glob.glob(os.path.join(SHARED_DIR, "sys-cmd-*.json")))
    shot_files = sorted(glob.glob(os.path.join(SHARED_DIR, "sys-shot-*.json")))

    # 命令优先
    for f in cmd_files:
        exec_command(f)

    # 截图其次
    for f in shot_files:
        take_screenshot(f)

def main():
    log("=" * 50)
    log("Windows Agent 启动")
    log(f"共享目录: {SHARED_DIR}")
    log(f"PID: {os.getpid()}")
 