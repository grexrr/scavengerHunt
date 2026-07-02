yapipeline {
    agent any

    environment {
        CI_PROJECT = "scavenger-ci-${BUILD_NUMBER}"
        COMPOSE_FILE = "docker-compose.yml:docker-compose.ci.yml"
    }

    stages {
        stage('Checkout'){
            steps {
                checkout scm
                echo "Building commit: ${env.GIT_COMMIT}"
            }
        }

        stage('Validate Config') {
            steps {
                sh 'docker compose -f docker-compose.yml -f docker-compose.ci.yml config --quiet'
                echo 'Docker Compose config is valid.'
            }
        }

        stage('Backend Test') {
            steps {
                dir('backend') {
                    sh 'mvn clean test -B'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Python Test') {
            steps {
                dir('services/landmark-processor') {
                    sh 'python3 -m venv .venv'
                    sh '.venv/bin/pip install -r requirements.txt -q'
                    sh '.venv/bin/python -m pytest tests/ -v --tb=short'
                }

                dir('services/puzzle-agent') {
                    sh 'python3 -m venv .venv'
                    sh '.venv/bin/pip install -r requirements.txt -q'
                    sh '.venv/bin/python -m pytest tests/ -v --tb=short || true'
                }
            }
        }

        stage('Package Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests -B'
                }
            }
        }

        stage('Build Images') {
            steps {
                sh 'docker compose -f docker-compose.yml -f docker-compose.ci.yml build'
            }
        }

        stage('Start CI Stack') {
            steps {
                sh 'docker compose -p ${CI_PROJECT} -f docker-compose.yml -f docker-compose.ci.yml up -d'
            }
        }

        stage('Health Checks & Smoke Tests') {
            steps {
                sh 'bash scripts/ci-smoke-test.sh'
            }
        }
    }

    post {
        always {
            sh "docker compose -p ${CI_PROJECT} -f docker-compose.yml -f docker-compose.ci.yml down -v || true"
            echo 'CI stack cleaned up.'
        }
        success {
            echo "Pipeline passed."
        }
        failure {
            sh "docker compose -p ${CI_PROJECT} logs --tail=100 || true"
            echo 'Pipeline failed — logs above.'
        }
    }
}
