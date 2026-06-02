---
name: kubernetes-specialist
description: 在部署或管理 Kubernetes 工作负载时使用。用于创建部署清单、配置 Pod 安全策略、设置服务账户、定义网络隔离规则、调试 Pod 崩溃、分析资源限制、检查容器日志或调整工作负载大小。适用于 Helm Chart、RBAC 策略、NetworkPolicy、存储配置、性能优化、GitOps 管道和多集群管理。
license: MIT
metadata:
  author: https://github.com/Jeffallan
  version: "1.1.1"
  domain: infrastructure
  triggers: Kubernetes, K8s, kubectl, Helm, container orchestration, pod deployment, RBAC, NetworkPolicy, Ingress, StatefulSet, Operator, CRD, CustomResourceDefinition, ArgoCD, Flux, GitOps, Istio, Linkerd, service mesh, multi-cluster, cost optimization, VPA, spot instances
  role: specialist
  scope: infrastructure
  output-format: manifests
  related-skills: devops-engineer, cloud-architect, sre-engineer, terraform-engineer, security-reviewer, chaos-engineer
---

# Kubernetes 专家技能

## 使用场景

- 部署工作负载（Deployment、StatefulSet、DaemonSet、Job）
- 配置网络（Service、Ingress、NetworkPolicy）
- 管理配置（ConfigMap、Secret、环境变量）
- 设置持久化存储（PV、PVC、StorageClass）
- 创建用于应用打包的 Helm Chart
- 排查集群和工作负载问题
- 实施安全最佳实践

## 核心工作流

1. **分析需求** — 了解工作负载特性、扩缩容需求、安全要求
2. **设计架构** — 选择工作负载类型、网络模式、存储方案
3. **实现清单** — 创建声明式 YAML，包含合理的资源限制和健康检查
4. **安全加固** — 应用 RBAC、NetworkPolicy、Pod 安全标准、最小权限原则
5. **验证** — 运行 `kubectl rollout status`、`kubectl get pods -w` 和 `kubectl describe pod <name>` 确认健康状态；必要时使用 `kubectl rollout undo` 回滚

## 参考指南

根据上下文加载详细指导：

| 主题 | 参考 | 加载时机 |
|------|------|----------|
| 工作负载 | `references/workloads.md` | Deployment、StatefulSet、DaemonSet、Job、CronJob |
| 网络 | `references/networking.md` | Service、Ingress、NetworkPolicy、DNS |
| 配置 | `references/configuration.md` | ConfigMap、Secret、环境变量 |
| 存储 | `references/storage.md` | PV、PVC、StorageClass、CSI 驱动 |
| Helm Chart | `references/helm-charts.md` | Chart 结构、values、模板、钩子、测试、仓库 |
| 故障排除 | `references/troubleshooting.md` | kubectl debug、日志、事件、常见问题 |
| 自定义 Operator | `references/custom-operators.md` | CRD、Operator SDK、controller-runtime、reconciliation |
| 服务网格 | `references/service-mesh.md` | Istio、Linkerd、流量管理、mTLS、金丝雀发布 |
| GitOps | `references/gitops.md` | ArgoCD、Flux、渐进式交付、sealed secrets |
| 成本优化 | `references/cost-optimization.md` | VPA、HPA 调优、竞价实例、配额、合理调整 |
| 多集群 | `references/multi-cluster.md` | Cluster API、联邦、跨集群网络、灾难恢复 |

## 约束条件

### 必须做

- 使用声明式 YAML 清单（避免命令式 kubectl 命令）
- 为所有容器设置资源请求和限制
- 包含存活探针和就绪探针
- 使用 Secret 存储敏感数据（绝不硬编码凭据）
- 应用最小权限 RBAC
- 实施 NetworkPolicy 进行网络隔离
- 使用命名空间进行逻辑隔离
- 一致地标记资源以便组织管理
- 在注解中记录配置决策

### 绝对不做

- 无资源限制就部署到生产环境
- 在 ConfigMap 或明文环境变量中存储密钥
- 应用 Pod 使用默认 ServiceAccount
- 允许无限制的网络访问（默认放行所有）
- 无正当理由以 root 身份运行容器
- 跳过健康检查（存活/就绪探针）
- 生产镜像使用 latest 标签
- 暴露不必要的端口或服务

## 常见 YAML 模式

### 带资源限制、探针和安全上下文的 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
  namespace: my-namespace
  labels:
    app: my-app
    version: "1.2.3"
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
        version: "1.2.3"
    spec:
      serviceAccountName: my-app-sa   # 绝不使用默认 SA
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 2000
      containers:
        - name: my-app
          image: my-registry/my-app:1.2.3   # 绝不使用 latest
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: "100m"
              memory: "128Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
          livenessProbe:
            httpGet:
              path: /healthz
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
          readinessProbe:
            httpGet:
              path: /ready
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
          envFrom:
            - secretRef:
                name: my-app-secret   # 从 Secret 而非 ConfigMap 获取凭据
```

### 最小 RBAC（最小权限）

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-app-sa
  namespace: my-namespace
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: my-app-role
  namespace: my-namespace
rules:
  - apiGroups: [""]
    resources: ["configmaps"]
    verbs: ["get", "list"]   # 仅授予所需权限
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: my-app-rolebinding
  namespace: my-namespace
subjects:
  - kind: ServiceAccount
    name: my-app-sa
    namespace: my-namespace
roleRef:
  kind: Role
  name: my-app-role
  apiGroup: rbac.authorization.k8s.io
```

### NetworkPolicy（默认拒绝 + 显式放行）

```yaml
# 默认拒绝所有入站和出站流量
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: my-namespace
spec:
  podSelector: {}
  policyTypes: ["Ingress", "Egress"]
---
# 仅放行特定流量
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-my-app
  namespace: my-namespace
spec:
  podSelector:
    matchLabels:
      app: my-app
  policyTypes: ["Ingress"]
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: frontend
      ports:
        - protocol: TCP
          port: 8080
```

## 验证命令

部署后，验证健康状态和安全态势：

```bash
# 查看滚动更新完成状态
kubectl rollout status deployment/my-app -n my-namespace

# 实时查看 Pod 事件，捕捉崩溃循环或镜像拉取错误
kubectl get pods -n my-namespace -w

# 检查特定 Pod 的失败原因
kubectl describe pod <pod-name> -n my-namespace

# 检查容器日志
kubectl logs <pod-name> -n my-namespace --previous   # 对已崩溃容器使用 --previous

# 验证资源使用 vs 限制
kubectl top pods -n my-namespace

# 审计 ServiceAccount 的 RBAC 权限
kubectl auth can-i --list --as=system:serviceaccount:my-namespace:my-app-sa

# 回滚失败的部署
kubectl rollout undo deployment/my-app -n my-namespace
```

## 输出模板

实现 Kubernetes 资源时，提供：
1. 结构完整的完整 YAML 清单
2. 所需的 RBAC 配置（ServiceAccount、Role、RoleBinding）
3. 用于网络隔离的 NetworkPolicy
4. 设计决策和安全考虑因素的简要说明

[文档](https://jeffallan.github.io/claude-skills/skills/infrastructure/kubernetes-specialist/)
