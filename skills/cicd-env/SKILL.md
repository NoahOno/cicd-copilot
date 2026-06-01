# CI/CD Environment Skill

## Infrastructure

- **Jenkins**: http://localhost:8088 (Docker container)
- **K3s Cluster**: k3d-my-k3d01 (kubectl context)
- **GitHub**: https://github.com/NoahOno

## Repositories

- **App Code**: https://github.com/NoahOno/cicd-demo
  - Backend: FastAPI (Python)
  - Frontend: React + Vite
  - Dockerfiles in root of each module
- **K8s Manifests**: https://github.com/NoahOno/cicd-demo-manifests
  - Rendered YAML files for deployment
- **CI/CD Config**: https://github.com/NoahOno/cicd-copilot (this repo)
  - Pipeline templates, K8s templates, MCP config

## Workflow

### Build and Deploy

1. Trigger Jenkins Pipeline via MCP
2. Pipeline stages: Checkout -> Test -> Docker Build -> Push to K3s registry
3. Update manifests repo with new image tag
4. Apply manifests to K3s cluster

### Check Status

1. Query Jenkins build status via MCP
2. Query K3s pod/service status via MCP

## MCP Servers

- **GitHub MCP**: Repository management, file operations
- **Jenkins MCP**: Trigger builds, check status, get logs
- **K8s MCP**: Apply manifests, get pods/services, check rollout

## Image Registry

- **Local K3s Registry**: k3d-cicd-registry:5050 (accessed via host port)
- **Push from Jenkins**: docker push localhost:5050/... (Docker trusts HTTP on localhost)
- **Pull from K3s**: k3d-cicd-registry:5050/... (containerd mirrors to registry:5000)
