@Library('utils@main')_

def utils = new hee.tis.utils()

node {
    if (env.BRANCH_NAME.startsWith('PR-')) {
        // PR builds are done by GitHub Actions.
        return
    }

    def service = "sync"

    deleteDir()

    stage('Checkout Git Repo') {
      checkout scm
    }

    env.GIT_COMMIT=sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
    def mvn = "${tool 'Maven 3.3.9'}/bin/mvn"
    def workspace = pwd()
    def parent_workspace = pwd()
    def repository = "${env.GIT_COMMIT}".split("TIS-")[-1].split(".git")[0]
    def buildNumber = env.BUILD_NUMBER
    def buildVersion = env.GIT_COMMIT
    def imageName = ""
    def imageVersionTag = ""
    boolean isService = false

    println "[Jenkinsfile INFO] Commit Hash is ${GIT_COMMIT}"

    if (fileExists("$workspace/$service-service/pom.xml")) {
        workspace = "$workspace/$service-service"
        env.WORKSPACE= workspace
        sh 'cd "$workspace"'
        isService = true
    }

    try {

        milestone 1


        stage('Build') {
          sh "'${mvn}' clean install -DskipTests"
        }

        stage('Unit Tests') {
          try {
            sh "'${mvn}' clean test"
          } finally {
            junit '**/target/surefire-reports/TEST-*.xml'
          }
        }

        if (env.BRANCH_NAME == "main") {

        milestone 2

        stage('Dockerise') {
          env.IMAGE_REGISTRY_URL = "430723991443.dkr.ecr.eu-west-2.amazonaws.com"

          // log into aws docker
          sh "aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin 430723991443.dkr.ecr.eu-west-2.amazonaws.com"

          sh "'${mvn}' spring-boot:build-image -DskipTests"
          sh "docker tag ${env.IMAGE_REGISTRY_URL}/${service}:latest ${env.IMAGE_REGISTRY_URL}/${service}:${env.GIT_COMMIT}"
          sh "docker push --all-tags ${env.IMAGE_REGISTRY_URL}/${service}"

          sh "docker rmi ${env.IMAGE_REGISTRY_URL}/${service}:latest ${env.IMAGE_REGISTRY_URL}/${service}:${env.GIT_COMMIT}"

          println "[Jenkinsfile INFO] Stage Dockerize completed..."
        }

          milestone 3

          stage('Staging') {
            node {
              println "[Jenkinsfile INFO] Stage Deploy starting..."

              sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/stage $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
            }
          }

          milestone 4

          stage('Approval') {
            timeout(time:5, unit:'HOURS') {
              input message: 'Deploy to production?', ok: 'Deploy!'
            }
          }

          milestone 5

          stage('Production') {
            node {
              sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/prod $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
              sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/nimdta $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
            }
          }

        }
    } catch (hudson.AbortException ae) {
      // We do nothing for Aborts.
    } catch (err) {
      throw err
    }
}
