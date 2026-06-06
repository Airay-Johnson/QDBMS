# QDBMS 手动上传 GitHub（无需命令行，全图形界面）

推荐用 **GitHub Desktop**，全程鼠标点击，不需要输入任何命令。

---

## 方式一：GitHub Desktop（推荐，最简单）

### 1. 下载安装

打开 https://desktop.github.com ，点紫色大按钮下载，双击安装。

安装过程中会提示你登录 GitHub 账号，用浏览器授权即可。

### 2. 添加项目

打开 GitHub Desktop 后：

1. 点左上角 **File** → **Add local repository**
2. 点 **Choose...**，选择项目文件夹：
   ```
   D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS
   ```
3. 它会提示 "This directory does not appear to be a Git repository"
4. 点蓝色的 **create a repository** 链接
5. 弹窗里：
   - Name：填 `QDBMS`
   - Description：填 `问卷数据库管理系统`
   - 勾选 "Initialize this repository with a README" **不要勾**
   - Git ignore：选 **None**（项目里已经有 .gitignore 了）
   - License：选 **None**
6. 点 **Create Repository**

### 3. 提交代码

创建完后你会看到界面左侧列出了所有改动的文件（几百个）。

1. 左下角 **Summary** 框里写：`初始化 QDBMS 项目`
2. 点 **Commit to main** 按钮
3. 等待完成（很快）

### 4. 推送到 GitHub

1. 点顶部工具栏的 **Publish repository** 按钮
2. 弹窗里：
   - Name：`QDBMS`（保持默认）
   - Description：`问卷数据库管理系统`
   - **取消勾选** "Keep this code private"（如果你想公开的话）
3. 点 **Publish Repository** 按钮
4. 等待上传完成

### 5. 验证

打开浏览器访问 `https://github.com/你的用户名/QDBMS`，就能看到你的项目了。

---

## 方式二：直接在 GitHub 网页上传（最简单但只适合小文件）

⚠️ 这个方式**不适合本项目**，因为项目文件太多（几百个），网页上传一次只能传 100 个文件，而且不能保留文件夹结构，传完项目跑不起来。

所以还是推荐用 GitHub Desktop。

---

## 方式三：命令行逐条执行（不用脚本）

如果你偏好命令行，在项目文件夹打开 PowerShell，复制粘贴执行（每次粘贴一行，等它跑完再贴下一行）：

```powershell
cd "D:\JetBrains\IntelliJ IDEA 2025.1.3\project\QDBMS"
```

```powershell
Remove-Item -Recurse -Force .git -ErrorAction SilentlyContinue
```

```powershell
git init
```

```powershell
git branch -m main
```

```powershell
git add -A
```

```powershell
git commit -m "初始化 QDBMS 项目"
```

> ⚠️ 下面这行的地址换成你在 GitHub 创建的仓库地址
```powershell
git remote add origin https://github.com/你的用户名/QDBMS.git
```

```powershell
git push -u origin main
```

推送时会弹出浏览器让你授权 GitHub 登录，点一下就行。

---

## 总结

| 方式 | 难度 | 适合人群 |
|------|------|----------|
| GitHub Desktop | ⭐ 零门槛 | 没用过 Git 的人 |
| 命令行逐条执行 | ⭐⭐ 需要复制粘贴 | 想了解每一步在做什么 |
| push-to-github.ps1 脚本 | ⭐ 一键完成 | 已经有 GitHub 仓库地址 |

**零基础强烈推荐 GitHub Desktop**，全中文、纯点击、不用记任何命令。
