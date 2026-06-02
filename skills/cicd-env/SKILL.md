# CI/CD 环境技能

## 基础设施

- **Jenkins**: http://localhost:8088（Docker 容器）
- **K3s 集群**: k3d-my-k3d01（kubectl 上下文）
- **GitHub**: https://github.com/NoahOno

## 仓库

- **应用代码**: https://github.com/NoahOno/cicd-demo
  - 后端：FastAPI（Python）
  - 前端：React + Vite
  - Dockerfile 位于各模块根目录
- **K8s 清单**: https://github.com/NoahOno/cicd-demo-manifests
  - 渲染后的 YAML 文件，用于部署
- **CI/CD 配置**: https://github.com/NoahOno/cicd-copilot（本仓库）
  - 管道模板、K8s 模板、MCP 配置、技能定义

## 工作流

### 构建与部署

1. 通过 MCP 触发 Jenkins 管道
2. 管道阶段：检出 → 测试 → Docker 构建 → 推送到 K3s 镜像仓库
3. 更新清单仓库中的镜像标签
4. 将清单应用到 K3s 集群

### 检查状态

1. 通过 MCP 查询 Jenkins 构建状态
2. 通过 MCP 查询 K3s Pod/Service 状态

## MCP 服务器

- **GitHub MCP**: 仓库管理、文件操作
- **Jenkins MCP**: 触发构建、查看状态、获取日志
- **K8s MCP**: 应用清单、查询 Pod/Service、检查滚动更新

## 镜像仓库

- **本地 K3s 镜像仓库**: k3d-cicd-registry:5050（通过主机端口访问）
- **从 Jenkins 推送**: docker push localhost:5050/...（Docker 信任 localhost 的 HTTP）
- **从 K3s 拉取**: k3d-cicd-registry:5050/...（containerd 镜像到 registry:5000）
