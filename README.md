# cicd-copilot

CI/CD 自动化配置仓库 —— 管理 MCP 服务器定义与 AI 辅助技能，为 [opencode](https://opencode.ai) 提供 CI/CD 全流程支持。

基于 **技能（Skill）+ 模型上下文协议（MCP）** 双轮驱动模式：
- **技能** 提供领域知识和操作指引
- **MCP** 提供与外部系统（GitHub、Jenkins、Kubernetes）的直接交互能力

## 仓库结构

```
cicd-copilot/
├── README.md                       # 本文档
├── skills/                         # opencode 技能定义
│   ├── cicd-env/                   # 环境配置与基础设施信息
│   ├── cicd-pipeline-generator/    # CI/CD 管道生成器
│   ├── jenkins-pipeline/           # Jenkins 管道编写
│   ├── kubernetes-specialist/      # Kubernetes 资源管理
│   └── cicd-workflow/              # ★ 全流程编排（整合所有技能+MCP）
└── mcps/                           # MCP 服务器定义
    ├── github.json                 # GitHub MCP
    ├── jenkins.json                # Jenkins MCP
    └── k8s.json                    # Kubernetes MCP
```

## 技能说明

| 技能 | 作用 | 加载方式 |
|------|------|----------|
| `cicd-env` | 记录当前项目的 Jenkins/K3s/镜像仓库等环境信息 | `/skill cicd-env` |
| `cicd-pipeline-generator` | 生成 CI/CD 管道配置文件（GitHub Actions/GitLab/Jenkins） | `/skill cicd-pipeline-generator` |
| `jenkins-pipeline` | 编写 Jenkins 声明式/脚本化管道 | `/skill jenkins-pipeline` |
| `kubernetes-specialist` | 管理 K8s 工作负载、网络、安全配置 | `/skill kubernetes-specialist` |
| `cicd-workflow` | **元技能**：编排完整 CI/CD 生命周期，自动引用上述技能和 MCP | `/skill cicd-workflow` |

推荐直接从 `cicd-workflow` 入手，它会按需加载子技能。

## 快速开始

### 1. 配置 opencode.jsonc

将 `mcps/` 目录中的 JSON 配置合并到你的 `opencode.jsonc` 中：

```jsonc
{
  "mcpServers": {
    "github": {
      "type": "cli",
      "command": "gh",
      "args": []
    },
    "jenkins": {
      "type": "stdio",
      "command": "/path/to/mcp-jenkins",
      "args": ["--jenkins-url", "http://your-jenkins:8088"]
    },
    "k8s": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "kubernetes-mcp-server"]
    }
  }
}
```

### 2. 定义环境变量

根据你的基础设施设置环境变量（各值替换为实际地址）：

```bash
# 基础设施
export JENKINS_URL="http://jenkins.example.com:8088"
export CLUSTER_NAME="k3d-my-cluster"
export REGISTRY="registry.example.com:5050"

# 仓库
export APP_REPO="your-org/cicd-demo"
export MANIFESTS_REPO="your-org/cicd-demo-manifests"
export TEMPLATES_REPO="your-org/cicd-templates"

# 应用
export APP_NAME="todo-backend"
export NAMESPACE="todo-app"
```

### 3. 启动 CI/CD 全流程

在 opencode 中与 AI 助手对话，例如：

```
你：/skill cicd-workflow
    请设置环境：ENV=staging, APP_NAME=todo-backend, IMAGE_TAG=build-42

你：开始今天的部署流程

AI：好的，按照 cicd-workflow 的编排：
    1. ⏯ 正在通过 Jenkins MCP 触发 CI 构建...
       → Jenkins 作业：cicd-demo-ci
       → 参数：IMAGE_TAG=build-42, ENV=staging
    2. ⏳ 等待 CI 构建完成（预计 2 分钟）...
       → 构建 #15 运行中...
       → 测试阶段 ✅
       → 镜像推送阶段 ✅
    3. ⏯ CD 已自动触发...
       → 克隆 cicd-demo-manifests
       → render.sh 执行完成
       → 已提交并推送
    4. ⏯ 正在通过 K8s MCP 部署...
       → kubectl apply -f stages/staging/
       → rollout 完成 ✅
    5. ✅ 部署成功！
       → Pod todo-backend-xxx 已 Running
       → 就绪探针通过
       → 可通过 http://staging.todo.example.com 访问
```

## 多环境复用

本仓库所有技能和 MCP 定义均设计为**环境无关**，只需替换变量即可复用于不同环境：

| 环境 | 分支策略 | 命名空间 | 典型用途 |
|------|----------|----------|----------|
| `dev` | feature → develop | `{app}-dev` | 每日构建，快速验证 |
| `staging` | develop → staging | `{app}-staging` | 发布前集成测试 |
| `prod` | staging → main | `{app}` | 生产发布 |

详细的环境参数配置参见 `cicd-workflow` 技能中的"多环境策略"章节。

## 关联仓库

| 仓库 | 用途 |
|------|------|
| 应用仓库 | 应用源代码，含 Dockerfile 和单元测试 |
| 清单仓库 | Kustomize 渲染后的 K8s 部署 YAML |
| 模板仓库 | Jenkins Pipeline Groovy 脚本（ci.groovy / cd.groovy） |

每个仓库的职责边界：
- **应用仓库**：只管代码 + 测试 + Dockerfile，不含 CI/CD 配置
- **清单仓库**：只管 K8s 清单 + render.sh + values.yaml，自包含可部署
- **模板仓库**：只管 Pipeline Groovy，纯 Jenkins 逻辑
- **本仓库**：只管 opencode 技能 + MCP 定义，AI 侧配置
