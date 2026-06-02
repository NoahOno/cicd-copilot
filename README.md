# cicd-copilot

CI/CD 自动化配置仓库 —— 管理 MCP 服务器定义与 AI 辅助技能，为 [opencode](https://opencode.ai) 提供 CI/CD 场景支持。

## 仓库结构

```
cicd-copilot/
├── README.md              # 本文档
├── skills/                # opencode 技能定义
│   ├── cicd-env/          # 项目环境描述（Jenkins / K3s / 镜像仓库等）
│   ├── cicd-pipeline-generator/  # CI/CD 管道生成器技能
│   ├── jenkins-pipeline/  # Jenkins 管道技能
│   └── kubernetes-specialist/    # Kubernetes 专家技能
└── mcps/                  # MCP 服务器定义（每个服务器一个独立 JSON）
    ├── github.json        # GitHub MCP（gh CLI）
    ├── jenkins.json       # Jenkins MCP（mcp-jenkins）
    └── k8s.json           # Kubernetes MCP（kubernetes-mcp-server）
```

## 关联仓库

| 仓库 | 用途 |
|------|------|
| [cicd-demo](https://github.com/NoahOno/cicd-demo) | 应用源代码（FastAPI 后端 + React 前端） |
| [cicd-demo-manifests](https://github.com/NoahOno/cicd-demo-manifests) | 渲染后的 K8s 部署清单 |
| [cicd-templates](https://github.com/NoahOno/cicd-templates) | Jenkins Pipeline Groovy 模板 |
| cicd-copilot（本仓库） | MCP 服务器定义 + opencode 技能 |

## 使用方法

### 集成 MCP 服务器

将 `mcps/` 目录中的 JSON 配置合并到你的 `opencode.jsonc` 的 `mcpServers` 字段中：

```jsonc
{
  "mcpServers": {
    // 从 mcps/github.json 复制
    "github": {
      "type": "cli",
      "command": "gh",
      "args": []
    },
    // 从 mcps/jenkins.json 复制
    "jenkins": {
      "type": "stdio",
      "command": "/path/to/mcp-jenkins",
      "args": ["--jenkins-url", "http://localhost:8088"]
    },
    // 从 mcps/k8s.json 复制
    "k8s": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "kubernetes-mcp-server"]
    }
  }
}
```

### 加载技能

在 opencode 中通过 `/skill` 命令加载相应技能：

- `cicd-env` — 了解项目基础设施和环境配置
- `cicd-pipeline-generator` — 生成 CI/CD 管道配置
- `jenkins-pipeline` — 编写 Jenkins 管道
- `kubernetes-specialist` — 管理 K8s 工作负载

## 基础设施

| 组件 | 地址 | 说明 |
|------|------|------|
| Jenkins | http://localhost:8088 | CI/CD 自动化引擎 |
| K3s 集群 | k3d-my-k3d01 | 轻量级 Kubernetes 集群 |
| 镜像仓库 | k3d-cicd-registry:5050 | 本地容器镜像仓库 |
