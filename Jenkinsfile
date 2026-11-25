// Declarative pipeline: Git -> Maven build and test -> SonarQube gate -> Docker image -> registry.
// Jenkins checks the repository for changes every ~2 minutes (Poll SCM), so no webhook or public URL is needed.
pipeline {
  agent any

  triggers {
    pollSCM('H/2 * * * *')
  }

  options {
    timestamps()
    timeout(time: 20, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
  }

  tools {
    maven 'maven-3.9'
  }

  parameters {
    string(name: 'REGISTRY', defaultValue: 'localhost:5001',
           description: 'Where to push the image. Use an ECR URI (123456789012.dkr.ecr.ap-south-1.amazonaws.com/greeter) for ECR.')
    booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run the SonarQube analysis and quality gate')
  }

  environment {
    IMAGE_TAG = "${env.BRANCH_NAME ?: 'main'}-${env.GIT_COMMIT?.take(8) ?: env.BUILD_NUMBER}"
  }

  stages {
    stage('Build and test') {
      steps {
        sh 'mvn -B -ntp -f app/pom.xml verify'
      }
      post {
        always {
          junit 'app/target/surefire-reports/*.xml'
          archiveArtifacts artifacts: 'app/target/*.jar', fingerprint: true, allowEmptyArchive: true
        }
      }
    }

    stage('SonarQube analysis') {
      when { expression { params.RUN_SONAR } }
      steps {
        withSonarQubeEnv('sonarqube') {
          sh 'mvn -B -ntp -f app/pom.xml org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=greeter'
        }
      }
    }

    stage('Quality gate') {
      when { expression { params.RUN_SONAR } }
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Docker image') {
      steps {
        sh 'docker build --build-arg APP_VERSION="$IMAGE_TAG" -t "$REGISTRY/greeter:$IMAGE_TAG" app'
      }
    }

    stage('Push') {
      steps {
        sh 'docker push "$REGISTRY/greeter:$IMAGE_TAG"'
      }
    }
  }

  post {
    success { echo "Pushed $REGISTRY/greeter:$IMAGE_TAG" }
    failure { echo 'Build failed: see the stage that turned red.' }
  }
}
