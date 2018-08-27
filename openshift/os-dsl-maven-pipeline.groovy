
#!groovy
try {
    def appName = "${APP_NAME}"
    def project = ""
    pipeline {
      agent any

      stages {
        stage('Initialize') {
          steps{
              project = env.PROJECT_NAME
          }
        }
        stage('Checkout'){
          steps{
              checkout([$class: 'GitSCM', branches: [[name: env.GIT_REF]], userRemoteConfigs: [[url: env.GIT_REPOSITORY]]])
        }
        stage('Build') {
          steps{
              sh 'mvn clean package'
              stash name: "jar", includes: "target/spring-boot-sample.jar"
          }
        }
        stage("Build Image") {
          steps{
              unstash name: "jar"
              sh "oc start-build ${APP_NAME}-run --from-file=target/spring-boot-sample.jar -n ${project}"
              openshiftVerifyBuild bldCfg: "${appName}-run", namespace: project, waitTime: '20', waitUnit: 'min'
          }
        }
      }
    }

} catch (err) {
    echo "in catch block"
    echo "Caught:"
    println err
    currentBuild.result = 'FAILURE'
    throw err
}
