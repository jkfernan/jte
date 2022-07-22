void call() {
  dsop_container([yaml: resource('pod.yaml'), containerName: 'gradle']) {
    unstash 'gitStash'
    try {
      withCredentials([usernamePassword(credentialsId: 'nexus_creds', usernameVariable: 'NexusUser', passwordVariable: 'NexusPassword')]) {
        nexusBaseUrl = 'https://nexus.com'
        mavenProxy = "${nexusBaseUrl}/repository/maven-proxy/"
        gradleProxy = "${nexusBaseUrl}/repository/gradle-proxy/"
        stage('Gradle: Unit Tests') {
          sh 'gradle wrapper'
          sh "NexusMavenRepo=${mavenProxy} NexusGradlePluginRepo=${gradleProxy} ./gradlew --no-daemon test"
        }
      }
    } catch (err) {
      error('Test failures present in build.')
    } finally {
      catchError(message: 'Unable to archive unit test', buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
        archiveArtifacts artifacts: 'build/test-results/test/*.xml'
      }
    }
  }
}
