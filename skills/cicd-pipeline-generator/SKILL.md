---
name: cicd-pipeline-generator
description: 此技能用于创建或配置 CI/CD 管道文件，实现自动化测试、构建和部署。适用于生成 GitHub Actions 工作流、GitLab CI 配置、CircleCI 配置或其他 CI/CD 平台配置。特别适合为 Node.js/Next.js 应用设置自动化管道，包括代码检查、测试、构建和部署到 Vercel、Netlify 或 AWS 等平台。
---

# CI/CD 管道生成器

## 概述

为各种平台（GitHub Actions、GitLab CI、CircleCI、Jenkins）生成可用于生产的 CI/CD 管道配置文件。本技能提供模板和指导，帮助为现代 Web 应用（特别是 Node.js/Next.js 项目）设置自动化工作流，涵盖代码检查、测试、构建和部署。

## 核心功能

### 1. 平台选择

根据项目需求选择合适的 CI/CD 平台：

- **GitHub Actions**: 最适合 GitHub 托管的项目，原生集成
- **GitLab CI/CD**: 适合需要复杂管道的 GitLab 仓库
- **CircleCI**: 针对 Docker 工作流和快速构建优化
- **Jenkins**: 适合自托管、高度可定制的环境

详细的平台对比、优缺点和用例建议，请参考 `references/platform-comparison.md`。

### 2. 管道配置生成

按以下原则生成管道配置：

#### 管道阶段

按以下标准阶段组织管道：

1. **安装依赖**
   - 从仓库检出代码
   - 设置运行时环境（Node.js 版本）
   - 恢复缓存的依赖
   - 使用 `npm ci` 安装依赖
   - 缓存依赖供后续运行使用

2. **代码检查**
   - 运行 ESLint 检查代码质量
   - 运行 TypeScript 类型检查
   - 代码检查失败时快速终止

3. **测试**
   - 执行单元测试
   - 执行集成测试
   - 生成代码覆盖率报告
   - 上传覆盖率到报告服务（Codecov、Coveralls）

4. **构建**
   - 创建生产构建
   - 验证构建成功
   - 存储构建产物

5. **部署**
   - 部署到预发环境（develop 分支）
   - 部署到生产环境（main 分支）
   - 运行部署后冒烟测试

#### 缓存策略

实施有效的缓存以加速构建：

```yaml
# 基于 package-lock.json 缓存 node_modules
cache:
  key: ${{ hashFiles('package-lock.json') }}
  paths:
    - node_modules/
    - .npm/
```

#### 环境变量

配置必要的环境变量：
- `NODE_ENV`：构建时设置为 `production`
- 平台特定令牌：以密钥形式存储
- 构建时变量：传递给构建过程

### 3. 模板使用

使用 `assets/` 目录中提供的模板：

**GitHub Actions 模板**（`assets/github-actions-nodejs.yml`）：
- 多任务工作流，包含代码检查、测试、构建、部署
- 多 Node.js 版本的矩阵构建（可选）
- Vercel 部署集成
- 构建产物上传
- 代码覆盖率报告

**GitLab CI 模板**（`assets/gitlab-ci-nodejs.yml`）：
- 多阶段管道
- 依赖缓存
- 手动生产部署
- 自动预发部署
- 覆盖率报告

使用模板：
1. 复制合适的模板文件
2. 放置在正确位置：
   - GitHub Actions：`.github/workflows/ci.yml`
   - GitLab CI：`.gitlab-ci.yml`
3. 自定义部署目标、环境变量和分支名称
4. 在平台设置中添加所需的密钥

### 4. 部署配置

#### Vercel 部署

对于 GitHub Actions：
```yaml
- uses: amondnet/vercel-action@v25
  with:
    vercel-token: ${{ secrets.VERCEL_TOKEN }}
    vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
    vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
    vercel-args: '--prod'
```

**所需密钥**：
- `VERCEL_TOKEN`：从 Vercel 账户设置获取
- `VERCEL_ORG_ID`：从 Vercel 项目设置获取
- `VERCEL_PROJECT_ID`：从 Vercel 项目设置获取

#### Netlify 部署

```yaml
- run: |
    npm install -g netlify-cli
    netlify deploy --prod --dir=.next
  env:
    NETLIFY_AUTH_TOKEN: ${{ secrets.NETLIFY_AUTH_TOKEN }}
    NETLIFY_SITE_ID: ${{ secrets.NETLIFY_SITE_ID }}
```

#### AWS S3 + CloudFront

```yaml
- uses: aws-actions/configure-aws-credentials@v4
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: us-east-1

- run: |
    aws s3 sync .next/static s3://${{ secrets.S3_BUCKET }}/static
    aws cloudfront create-invalidation --distribution-id ${{ secrets.CF_DIST_ID }} --paths "/*"
```

### 5. 测试集成

配置带报告的测试执行：

**Jest 配置**：
```yaml
- name: 运行测试并生成覆盖率
  run: npm test -- --coverage --coverageReporters=text --coverageReporters=lcov

- name: 上传覆盖率
  uses: codecov/codecov-action@v4
  with:
    files: ./coverage/lcov.info
    flags: unittests
```

**快速失败策略**：
```yaml
# 先运行快速检查
jobs:
  lint:  # 约30秒内失败
  test:  # 约2分钟内失败
  build: # 约5分钟内失败
    needs: [lint, test]
  deploy:
    needs: [build]
```

### 6. 基于分支的工作流

按分支实现不同行为：

**功能分支 / PR**：
- 仅运行代码检查 + 测试
- 不部署
- 在 PR 中添加测试结果评论

**Develop 分支**：
- 运行代码检查 + 测试 + 构建
- 部署到预发环境
- 自动部署

**Main 分支**：
- 运行代码检查 + 测试 + 构建
- 部署到生产环境
- 手动审批（可选）
- 创建发布标签

**示例**：
```yaml
deploy_staging:
  if: github.ref == 'refs/heads/develop'
  # 部署到预发环境

deploy_production:
  if: github.ref == 'refs/heads/main'
  environment: production  # 需要手动审批
  # 部署到生产环境
```

## 工作流决策树

按以下决策树生成合适的管道：

1. **选择哪个平台？**
   - GitHub → 使用 `assets/github-actions-nodejs.yml`
   - GitLab → 使用 `assets/gitlab-ci-nodejs.yml`
   - CircleCI/Jenkins → 适配 GitHub Actions 模板
   - 不确定 → 查阅 `references/platform-comparison.md`

2. **需要哪些阶段？**
   - 始终包含：代码检查、测试、构建
   - 可选：安全扫描、端到端测试、性能测试
   - 如需从 CI 部署，添加部署阶段

3. **选择哪个部署平台？**
   - Vercel → 使用 Vercel 部署示例
   - Netlify → 使用 Netlify CLI 方式
   - AWS → 使用 AWS Actions/CLI
   - 自定义 → 实现自定义部署脚本

4. **触发条件？**
   - 推送到 main/develop 分支时
   - 创建 Pull Request 时
   - 创建标签时
   - 手动工作流调度

5. **需要哪些环境变量？**
   - 平台令牌（Vercel、Netlify、AWS）
   - 外部服务的 API 密钥
   - 构建时环境变量
   - 功能开关

## 最佳实践

### 安全
- 所有密钥存储在平台密钥管理中（绝不放在代码中）
- 使用最小权限令牌（尽可能只读）
- 定期轮换密钥
- 审计密钥访问权限
- 绝不记录密钥（使用 `***` 掩码）

### 性能
- 积极缓存依赖
- 并行化独立任务
- 使用矩阵构建进行多版本测试
- 快速失败：先运行快速检查，再运行慢速检查
- 优化 Docker 层缓存

### 可靠性
- 固定精确的 Node.js 版本（`18.x` 而非仅 `18`）
- 提交锁定文件（`package-lock.json`）
- 为不稳定的外部服务添加重试逻辑
- 设置合理的超时时间（最长 10-15 分钟）
- 对非关键步骤使用 `continue-on-error`

### 可维护性
- 添加注释解释复杂逻辑
- 使用可复用工作流/模板
- 保持配置 DRY（不重复自己）
- 对所有管道变更进行版本控制
- 在 README 中记录所需的密钥

## 常见模式

### 多环境部署
```yaml
deploy_staging:
  environment: staging
  if: github.ref == 'refs/heads/develop'

deploy_production:
  environment: production
  if: github.ref == 'refs/heads/main'
  needs: [deploy_staging]
```

### 矩阵测试
```yaml
strategy:
  matrix:
    node-version: [16.x, 18.x, 20.x]
    os: [ubuntu-latest, windows-latest]
```

### 条件步骤
```yaml
- name: 部署
  if: github.event_name == 'push' && github.ref == 'refs/heads/main'
  run: npm run deploy
```

### 产物管理
```yaml
- name: 上传构建产物
  uses: actions/upload-artifact@v4
  with:
    name: build-output
    path: .next/
    retention-days: 7

- name: 下载构建产物
  uses: actions/download-artifact@v4
  with:
    name: build-output
```

## 故障排除

### 管道失败
1. 检查 Action/Job 日志中的错误信息
2. 验证环境变量和密钥是否已设置
3. 在本地测试命令后再添加到管道
4. 查阅文档了解平台特定问题

### 构建缓慢
1. 验证缓存是否正常工作（检查缓存命中/未命中日志）
2. 并行化独立任务
3. 如有可能使用更快的运行器
4. 优化依赖安装

### 部署失败
1. 验证部署令牌是否有效
2. 检查平台状态页面
3. 查看部署日志
4. 在本地测试部署命令

## 资源

### 模板（`assets/`）
- `github-actions-nodejs.yml`：完整的 GitHub Actions 工作流
- `gitlab-ci-nodejs.yml`：完整的 GitLab CI 管道

### 参考文档（`references/`）
- `platform-comparison.md`：CI/CD 平台、部署目标、最佳实践和常见模式的详细对比

## 使用示例

**用户需求**："创建一个运行测试并部署到 Vercel 的 GitHub Actions 工作流"

**步骤**：
1. 复制 `assets/github-actions-nodejs.yml` 模板
2. 创建 `.github/workflows/` 目录（如不存在）
3. 保存为 `.github/workflows/ci.yml`
4. 使用 Vercel 凭据更新部署部分
5. 在 GitHub 仓库设置中添加密钥：
   - `VERCEL_TOKEN`
   - `VERCEL_ORG_ID`
   - `VERCEL_PROJECT_ID`
6. 提交并推送以触发工作流

**用户需求**："设置带预发和生产环境的 GitLab CI"

**步骤**：
1. 复制 `assets/gitlab-ci-nodejs.yml` 模板
2. 保存为仓库根目录的 `.gitlab-ci.yml`
3. 配置 GitLab CI/CD 变量：
   - `VERCEL_TOKEN`
   - 其他部署凭据
4. 检查生产环境的手动审批设置
5. 提交以触发管道

## 高级配置

### 单体仓库支持
```yaml
paths:
  - 'apps/frontend/**'
  - 'packages/**'
```

### 定时运行
```yaml
on:
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨 2 点
```

### 外部服务集成
```yaml
- name: 通知 Slack
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 安全扫描
```yaml
- name: 运行安全审计
  run: npm audit --audit-level=moderate

- name: 检查漏洞
  uses: snyk/actions/node@master
  env:
    SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```
