void call() {
  pod_container([yaml: resource('pod.yaml'), containerName: 'gradle']) {
    unstash 'gitStash'
    withCredentials([usernamePassword(credentialsId: 'nexus_creds', usernameVariable: 'NexusUser', passwordVariable: 'NexusPassword')]) {
      nexusBaseUrl = 'https://nexus.com'
      mavenProxy = "${nexusBaseUrl}/repository/maven-proxy/"
      gradleProxy = "${nexusBaseUrl}/repository/gradle-proxy/"
      projName = 'project'
      repoName = 'test'
      fileName = "${projName}-${repoName}-${env.SCM_COMMIT}.war"\

      stage('Gradle: Build') {
        sh 'gradle wrapper'
        sh """
          ArchiveFileName='${fileName}' NexusMavenRepo=${mavenProxy} NexusGradlePluginRepo=${gradleProxy} \
          ./gradlew build --exclude-task test
        """
      }

      stage('Gradle: Push Jar/War to Nexus') {
        curlResponse = sh(script: """ \
          curl --insecure -D- -X POST ${nexusBaseUrl}/service/rest/v1/components?repository=artifact-repo \
            -u \${NexusUser}:\${NexusPassword} \
          -F 'raw.directory=${projName}/${repoName}/' \
          -F 'raw.asset1=@build/libs/${fileName};type=application/x-gtar' \
          -F 'raw.asset1.filename=${fileName}'
        """, returnStdout: true)
        println "curl response: ${curlResponse}"
      }
    } //withCredentials
  } //pod_container
}
