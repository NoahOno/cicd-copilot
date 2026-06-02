---
name: jenkins-pipeline
description: >
  使用声明式和脚本化管道构建 Jenkins 管道，包括阶段、代理、
  参数和插件。实现多分支管道和部署自动化。
---

# Jenkins 管道

## 目录

- [概述](#概述)
- [使用场景](#使用场景)
- [快速入门](#快速入门)
- [参考指南](#参考指南)
- [最佳实践](#最佳实践)

## 概述

使用声明式和脚本化方法创建企业级 Jenkins 管道，自动化构建、测试和部署流程，支持高级控制流。

## 使用场景

- 企业 CI/CD 基础设施
- 复杂多阶段构建
- 本地部署自动化
- 参数化构建

## 快速入门

最小工作示例：

```groovy
pipeline {
    agent { label 'linux-docker' }
    environment {
        REGISTRY = 'docker.io'
        IMAGE_NAME = 'myapp'
    }
    parameters {
        string(name: 'DEPLOY_ENV', defaultValue: 'staging')
    }
    stages {
        stage('检出') { steps { checkout scm } }
        stage('安装') { steps { sh 'npm ci' } }
        stage('代码检查') { steps { sh 'npm run lint' } }
        stage('测试') {
            steps {
                sh 'npm run test:coverage'
                junit 'test-results.xml'
            }
        }
        stage('构建') {
            steps {
                sh 'npm run build'
                archiveArtifacts artifacts: 'dist/**/*'
            }
        }
// ...（完整实现请参见参考指南）
```

## 参考指南

`references/` 目录中包含详细实现：

| 指南 | 内容 |
|---|---|
| [声明式管道 (Jenkinsfile)](references/declarative-pipeline-jenkinsfile.md) | 声明式管道 (Jenkinsfile) |
| [脚本化管道](references/scripted-pipeline.md) | 脚本化管道 (Groovy)、多分支管道、参数化管道、带凭据管道 |

## 最佳实践

### ✅ 应该做

- 优先使用声明式管道，清晰易读
- 使用凭据插件管理密钥
- 归档构建产物和报告
- 生产环境部署设置审批门禁
- 保持管道模块化和可复用

### ❌ 不应该做

- 在管道代码中硬编码凭据
- 忽略管道错误
- 跳过测试覆盖率报告
- 使用已弃用的插件
