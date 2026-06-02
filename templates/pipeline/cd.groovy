pipeline {
    agent any

    parameters {
        string(name: 'SERVICE_NAME', defaultValue: 'todo', description: 'Service name prefix for image naming')
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag')
        string(name: 'MANIFEST_REGISTRY', defaultValue: 'k3d-cicd-registry:5050', description: 'Registry in K8s manifests')
        string(name: 'MANIFESTS_REPO', defaultValue: 'git@github.com:NoahOno/cicd-demo-manifests.git', description: 'K8s manifests repo URL')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Manifests branch')
        string(name: 'SUBPROJECTS', defaultValue: 'backend,frontend', description: 'Comma-separated subproject dirs')
        booleanParam(name: 'DIRECT_DEPLOY', defaultValue: false, description: 'Run kubectl apply after manifests update')
    }

    environment {
        SERVICE = "${params.SERVICE_NAME}"
        TAG = "${params.IMAGE_TAG}"
        REG = "${params.MANIFEST_REGISTRY}"
    }

    stages {
        stage('Update Manifests') {
            steps {
                sh 'rm -rf manifests'
                checkout([$class: 'GitSCM',
                    branches: [[name: "*/${params.GIT_BRANCH}"]],
                    userRemoteConfigs: [[url: "${params.MANIFESTS_REPO}"]],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'manifests']]
                ])
                script {
                    def projects = params.SUBPROJECTS.split(',').collect{ it.trim() }
                    projects.each { project ->
                        def imageName = "${SERVICE}-${project}"
                        def depFile = "manifests/${project}/deployment.yaml"
                        if (fileExists(depFile)) {
                            sh """
                                sed -i 's|image: .*${imageName}.*|image: ${REG}/${imageName}:${TAG}|g' ${depFile}
                            """
                            echo "[CD] Updated ${depFile} -> ${REG}/${imageName}:${TAG}"
                        } else {
                            echo "[CD] WARNING: ${depFile} not found, skipping"
                        }
                    }
                }
            }
        }

        stage('Commit & Push') {
            steps {
                sh """
                    cd manifests
                    git config user.email "cd@jenkins.local"
                    git config user.name "Jenkins CD"
                    git add .
                    git commit -m "chore: update ${SERVICE} images to ${TAG}" || true
                    git push
                """
            }
        }

        stage('K8s Deploy') {
            when {
                expression { params.DIRECT_DEPLOY }
            }
            steps {
                script {
                    def projects = params.SUBPROJECTS.split(',').collect{ it.trim() }
                    projects.each { project ->
                        sh "kubectl apply -f manifests/${project}/"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "[CD] ${SERVICE}:${TAG} manifests updated and pushed"
            script {
                if (params.DIRECT_DEPLOY) {
                    echo "[CD] K8s resources applied"
                }
            }
        }
        failure {
            echo "[CD] ${SERVICE}:${TAG} deploy FAILED"
        }
    }
}
