pipeline {
    agent any

    options {
        skipDefaultCheckout()
    }

    parameters {
        string(name: 'SERVICE_NAME', defaultValue: 'todo', description: 'Service name prefix for image naming')
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag')
        string(name: 'REGISTRY', defaultValue: 'localhost:5050', description: 'Registry for Docker build/push')
        string(name: 'GIT_REPO', defaultValue: 'https://github.com/NoahOno/cicd-demo.git', description: 'Business repo URL')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to build')
        string(name: 'SUBPROJECTS', defaultValue: 'backend,frontend', description: 'Comma-separated subproject dirs')
        string(name: 'CD_JOB_NAME', defaultValue: 'cicd-demo-cd', description: 'CD Jenkins job name to trigger (if TRIGGER_CD=true)')
        booleanParam(name: 'TRIGGER_CD', defaultValue: false, description: 'Auto-trigger CD pipeline after CI')
    }

    environment {
        SERVICE = "${params.SERVICE_NAME}"
        TAG = "${params.IMAGE_TAG}"
        REG = "${params.REGISTRY}"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: "${params.GIT_REPO}", branch: "${params.GIT_BRANCH}"
            }
        }

        stage('Backend Lint & Test') {
            when {
                expression { params.SUBPROJECTS.contains('backend') }
            }
            steps {
                dir('backend') {
                    sh 'pip install --default-timeout=120 --break-system-packages -r requirements.txt'
                    sh 'python3 -m pytest tests/ -v'
                }
            }
        }

        stage('Frontend Lint & Test') {
            when {
                expression { params.SUBPROJECTS.contains('frontend') }
            }
            steps {
                dir('frontend') {
                    sh 'npm install --no-audit --no-fund'
                    sh 'npm run test'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    def projects = params.SUBPROJECTS.split(',').collect{ it.trim() }
                    projects.each { project ->
                        dir(project) {
                            def image = "${REG}/${SERVICE}-${project}:${TAG}"
                            sh "docker build -t ${image} . && docker push ${image}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "[CI] ${SERVICE}:${TAG} built and pushed successfully"
            script {
                if (params.TRIGGER_CD) {
                    build job: params.CD_JOB_NAME,
                        parameters: [
                            string(name: 'IMAGE_TAG', value: params.IMAGE_TAG),
                            string(name: 'SERVICE_NAME', value: params.SERVICE_NAME)
                        ],
                        wait: false
                    echo "[CI] Triggered CD job: ${params.CD_JOB_NAME}"
                }
            }
        }
        failure {
            echo "[CI] ${SERVICE}:${TAG} FAILED"
        }
    }
}
