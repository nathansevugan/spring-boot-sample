#!groovy
try {
    timeout(time: 20, unit: 'MINUTES') {
        def appName = "${APP_NAME}"
        def project = ""

        node {
            stage("Initialize") {
                project = env.PROJECT_NAME
            }
        }

        node("maven") {
            stage("Checkout") {
                checkout([$class: 'GitSCM', branches: [[name: env.GIT_REF]], userRemoteConfigs: [[credentialsId: "${project}-${env.GIT_CREDENTIALS}", url: env.GIT_REPOSITORY]]])
            }
            dir("openshift-advanced/spring-boot-sample") {
                stage("Gather buildfacts") {
                    pom = readMavenPom file: 'pom.xml'
                }
                stage("Build JAR") {
                    sh "mvn clean package"
                    stash name: "jar", includes: "target/spring-boot-sample.jar"
                }
            }
        }

        node {
            stage("Build Image") {
                unstash name: "jar"
                sh "oc start-build ${APP_NAME}-run --from-file=target/spring-boot-sample.jar -n ${project}"
                openshiftVerifyBuild bldCfg: "${appName}-run", namespace: project, waitTime: '20', waitUnit: 'min'
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
