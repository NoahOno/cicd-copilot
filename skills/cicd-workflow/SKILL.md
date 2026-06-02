---
name: cicd-workflow
description: >
  CI/CD 全流程编排技能。整合 cicd-env、cicd-pipeline-generator、jenkins-pipeline、
  kubernetes-specialist 四大技能以及 GitHub/Jenkins/K8s 三大 MCP 服务器，
  实现从代码提交到生产部署的端到端自动化。支持多环境（开发/预发/生产）复用。
---

# CI/CD 全流程编排技能

## 概述

本技能是 **元技能（meta-skill）**，整合仓库中所有技能和 MCP 服务器，编排完整的 CI/CD 生命周期：

```
代码提交 → CI 构建测试 → Docker 镜像推送 → CD 渲染清单 → K8s 部署 → 健康验证
```

## 环境模型

本技能支持任意环境，通过变量替换实现复用：

| 变量 | 说明 | 示例值 |
|------|------|--------|
| `${ENV}` | 环境名称 | `dev` / `staging` / `prod` |
| `${REGISTRY}` | 镜像仓库地址 | `registry.example.com:5050` |
| `${CLUSTER_NAME}` | K8s 集群名称 | `k3d-my-k3d01` |
| `${NAMESPACE}` | K8s 命名空间 | `todo-app` |
| `${APP_NAME}` | 应用名称 | `todo-backend` / `todo-frontend` |
| `${IMAGE_TAG}` | 镜像标签 | `build-42` / `v1.2.3` |
| `${JENKINS_URL}` | Jenkins 地址 | `http://jenkins.example.com:8088` |
| `${MANIFESTS_REPO}` | 清单仓库 | `example-corp/cicd-demo-manifests` |

## 全流程分步指南

### 第一阶段：代码提交与 CI 触发

**输入**：开发者推送代码到应用仓库的 `${ENV}` 分支

**操作流程**：

1. **检查代码质量** — 使用 GitHub MCP 查看提交内容
   ```
   > 通过 GitHub MCP 获取最新 commit 信息
   > 检查变更文件列表，确认影响范围
   ```

2. **触发 CI 管道** — 使用 Jenkins MCP 启动构建
   ```
   > 通过 Jenkins MCP 触发 ${APP_NAME} CI 作业
   > 参数：IMAGE_TAG=build-${BUILD_NUMBER}, ENV=${ENV}
   ```

3. **参考 skill**：
   - `cicd-pipeline-generator` — 理解管道阶段构成
   - `jenkins-pipeline` — 编写与调试 Jenkinsfile
   - `cicd-env` — 查看仓库地址和环境配置

**注意**：本阶段不涉及手动代码检查 lint 等操作，Jenkins 管道内已包含 test/lint 阶段。

---

### 第二阶段：CI 管道执行

**输入**：Jenkins CI 作业被触发

**内部流程**（Jenkins 自动执行，通过 Jenkins MCP 监控）：

1. **检出代码** — 从应用仓库克隆 `${ENV}` 分支
2. **运行测试** — 执行单元测试和集成测试
3. **构建镜像** — Docker 构建并推送至 `${REGISTRY}`
4. **触发 CD** — 以参数形式传递 `IMAGE_TAG` 和 `SERVICE_NAME` 给 CD 作业

**查看进度**：
```
> 通过 Jenkins MCP 查询构建日志
> 通过 Jenkins MCP 等待构建完成
```

**参考 skill**：
- `jenkins-pipeline` — 管道语法与插件使用
- `cicd-env` — 镜像仓库配置与推送地址

---

### 第三阶段：CD 渲染与提交

**输入**：CI 完成，CD 作业被触发

**操作流程**：

1. **检出清单仓库** — 克隆 `${MANIFESTS_REPO}`
2. **渲染清单** — 运行 `render.sh`，利用 Kustomize 生成部署 YAML
   - 更新镜像标签为 `${REGISTRY}/${APP_NAME}:${IMAGE_TAG}`
   - 根据 `${ENV}` 调整副本数和资源限制
3. **提交推送** — 将渲染后的清单提交回 `${MANIFESTS_REPO}`

**参考 skill**：
- `kubernetes-specialist` — K8s 清单结构与字段含义
- `cicd-env` — 仓库分支与提交策略

---

### 第四阶段：K8s 部署

**输入**：清单仓库已更新

**操作流程**：

1. **应用清单** — 使用 K8s MCP 部署到集群
   ```
   > 切换 kubectl 上下文到 ${CLUSTER_NAME}
   > kubectl apply -f stages/${ENV}/
   ```

2. **验证部署** — 检查应用健康状态
   ```
   > 通过 K8s MCP 查看 Pod 状态
   > kubectl rollout status deployment/${APP_NAME} -n ${NAMESPACE}
   > kubectl get pods -n ${NAMESPACE} -w
   ```

3. **回滚准备** — 监控部署异常
   ```
   > 如 Pod 启动失败，执行 kubectl rollout undo
   > 检查日志：kubectl logs <pod-name> -n ${NAMESPACE}
   ```

**参考 skill**：
- `kubernetes-specialist` — Deployment 调试、NetworkPolicy、RBAC
- `cicd-env` — 集群上下文与命名空间

---

### 第五阶段：验证与巡检

**输入**：应用已部署运行

**操作流程**：

1. **服务可达性检查**
   ```
   > 通过 K8s MCP 获取 Service Endpoint
   > curl http://${SERVICE_URL}/health
   ```

2. **资源监控**
   ```
   > kubectl top pods -n ${NAMESPACE}
   > kubectl describe pod <pod-name> -n ${NAMESPACE}
   ```

3. **巡检清单**
   - [ ] 所有 Pod 处于 Running 状态
   - [ ] 就绪探针通过
   - [ ] 资源使用未超限
   - [ ] 镜像标签正确

**参考 skill**：
- `kubernetes-specialist` — 故障排除与资源优化
- `cicd-env` — 服务端口与域名信息

---

## 多环境策略

### 环境参数映射

| 环境 | 分支 | 命名空间 | 副本数 | 部署方式 |
|------|------|----------|--------|----------|
| `dev` | `develop` | `${APP_NAME}-dev` | 1 | 自动部署 |
| `staging` | `staging` | `${APP_NAME}-staging` | 2 | 自动部署 |
| `prod` | `main` | `${APP_NAME}` | 3 | CI 成功后自动触发 |

### 环境定制示例

```yaml
# dev 环境 — 最小资源，快速迭代
replicas: 1
resources:
  requests: { cpu: "50m", memory: "64Mi" }
  limits:   { cpu: "200m", memory: "256Mi" }

# staging 环境 — 接近生产
replicas: 2
resources:
  requests: { cpu: "100m", memory: "128Mi" }
  limits:   { cpu: "500m", memory: "512Mi" }

# prod 环境 — 高可用
replicas: 3
resources:
  requests: { cpu: "200m", memory: "256Mi" }
  limits:   { cpu: "1000m", memory: "1Gi" }
```

---

## 快捷操作

### 一键部署全流程

```
1. 加载本技能：/skill cicd-workflow
2. 设置环境变量：ENV=staging, APP_NAME=todo-backend, IMAGE_TAG=build-42
3. 执行全流程：
   → 触发 Jenkins CI（使用 Jenkins MCP）
   → 等待构建完成
   → 确认清单已更新（使用 GitHub MCP 查看 manifests 仓库）
   → 应用 K8s 清单（使用 K8s MCP）
   → 验证部署状态
```

### 常用 MCP 命令速查

| 操作 | MCP 命令 |
|------|----------|
| 触发 Jenkins 构建 | `通过 Jenkins MCP 触发 job ${JOB_NAME}，参数：IMAGE_TAG=...` |
| 查看构建日志 | `通过 Jenkins MCP 获取 job ${JOB_NAME} 最后一次构建的控制台输出` |
| 获取 Pod 列表 | `通过 K8s MCP 查询 namespace ${NAMESPACE} 下的所有 Pod` |
| 查看文件变更 | `通过 GitHub MCP 获取仓库 ${MANIFESTS_REPO} 的最新 commit 变更` |

---

## 技能依赖关系

```
cicd-workflow（本技能 — 全流程编排）
├── cicd-env              # 环境配置信息
├── cicd-pipeline-generator  # 管道设计参考
├── jenkins-pipeline      # Jenkins 管道编写
├── kubernetes-specialist # K8s 资源管理
├── GitHub MCP            # 仓库操作
├── Jenkins MCP           # 构建触发与监控
└── K8s MCP               # 集群操作
```

使用本技能时，上述子技能和 MCP 会自动按需加载。
