pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag')
        string(name: 'REGISTRY', defaultValue: 'localhost:5050', description: 'Registry for Docker build/push')
        string(name: 'MANIFEST_REGISTRY', defaultValue: 'k3d-cicd-registry:5050', description: 'Registry in K8s manifests')
        string(name: 'GIT_REPO', defaultValue: 'https://github.com/NoahOno/cicd-demo.git', description: 'Application source repo')
        string(name: 'MANIFESTS_REPO', defaultValue: 'https://github.com/NoahOno/cicd-demo-manifests.git', description: 'K8s manifests repo')
    }

    environment {
        REGISTRY = "${params.REGISTRY}"
        MANIFEST_REGISTRY = "${params.MANIFEST_REGISTRY}"
        IMAGE_TAG = "${params.IMAGE_TAG}"
        GIT_REPO = "${params.GIT_REPO}"
        MANIFESTS_REPO = "${params.MANIFESTS_REPO}"
        BACKEND_IMAGE = "${REGISTRY}/todo-backend:${IMAGE_TAG}"
        FRONTEND_IMAGE = "${REGISTRY}/todo-frontend:${IMAGE_TAG}"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: "${GIT_REPO}", branch: 'main'
            }
        }
        stage('Backend Test') {
            steps {
                dir('backend') {
                    sh 'pip install -r requirements.txt --quiet'
                    sh 'python -m pytest tests/ -v'
                }
            }
        }
        stage('Frontend Test') {
            steps {
                dir('frontend') {
                    sh 'npm install --quiet'
                    sh 'npm run test'
                }
            }
        }
        stage('Docker Build & Push') {
            steps {
                dir('backend') {
                    sh "docker build -t ${BACKEND_IMAGE} . && docker push ${BACKEND_IMAGE}"
                }
                dir('frontend') {
                    sh "docker build -t ${FRONTEND_IMAGE} . && docker push ${FRONTEND_IMAGE}"
                }
            }
        }
        stage('Update Manifests') {
            steps {
                sh """
                    git clone ${MANIFESTS_REPO} manifests
                    cd manifests
                    sed -i 's|image: .*todo-backend.*|image: ${MANIFEST_REGISTRY}/todo-backend:${IMAGE_TAG}|g' backend/deployment.yaml
                    sed -i 's|image: .*todo-frontend.*|image: ${MANIFEST_REGISTRY}/todo-frontend:${IMAGE_TAG}|g' frontend/deployment.yaml
                    git add .
                    git commit -m "Update images to ${IMAGE_TAG}"
                    git push
                """
            }
        }
    }
    post {
        success { echo "Build ${IMAGE_TAG} succeeded!" }
        failure { echo "Build ${IMAGE_TAG} failed." }
    }
}
