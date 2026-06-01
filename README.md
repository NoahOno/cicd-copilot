# cicd-copilot

CI/CD automation configuration for opencode Skill + MCP ecosystem.

## Repositories

| Repo | Purpose |
|------|---------|
| [cicd-demo](https://github.com/NoahOno/cicd-demo) | Application source code (FastAPI + React) |
| [cicd-demo-manifests](https://github.com/NoahOno/cicd-demo-manifests) | Rendered K8s manifests |
| cicd-copilot (this) | Templates, MCP config, skill definitions |

## Structure

```
opencode.jsonc                        # MCP server registrations
skills/cicd-env/SKILL.md              # CI/CD environment skill
templates/pipeline/Jenkinsfile         # Jenkins Pipeline template
templates/k8s/                        # K8s YAML templates
  backend/deployment.yaml.tmpl
  backend/service.yaml
  frontend/deployment.yaml.tmpl
  frontend/service.yaml
  kustomization.yaml.tmpl
render/                               # Template renderer
  render.sh                           # envsubst-based renderer
  Dockerfile                          # Containerized renderer
```

## Usage

```bash
# Render K8s manifests
REGISTRY=k3d-cicd-registry:5000 IMAGE_TAG=build-42 ./render/render.sh

# Or with Docker
docker build -t render ./render
docker run --rm -v $(pwd):/workspace render \
  /workspace/templates/k8s /workspace/rendered
```
