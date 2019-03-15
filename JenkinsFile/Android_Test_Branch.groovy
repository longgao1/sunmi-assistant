pipeline{
  agent none
  triggers {
    pollSCM('H/2 * * * *')
  }
  stages{
    stage('Build'){
      agent{node{label('ios')}}
      steps{
        lock('buildResource')
        {
          script{
            try{
              deleteDir()
              git(branch: 'test', credentialsId: 'lukai@sunmi.com', url: 'http://code.sunmi.com/wbu-app/sunmi-assistant-android.git', poll: true)
              sh('''
                export PATH="/usr/local/bin/:$PATH"
                export LC_ALL=en_US.UTF-8
                export LANG=en_US.UTF-8
                export ANDROID_HOME=/Users/admin/Library/Android/sdk
                curl http://api.fir.im/apps/latest/5c048efcca87a826b0c07ece?api_token=b34226983b0b4281c9efad321204ea12 > info.json
                mkdir -p build
                fastlane testEnv
                ''')
              archiveArtifacts(artifacts: 'app/build/outputs/apk/**/app-universal-*.apk', onlyIfSuccessful: true)
              stash(includes: 'app/build/outputs/apk/**/app-universal-*.apk', name: 'apk')
            }catch(e){
              def stageName = 'build'
              if(currentBuild.currentResult == "FAILURE"){
                NotifyBuild(currentBuild.result, stageName)
              }
              currentBuild.result = "FAILURE"
              throw e
            }
          }
        }
      }
    }
    stage('Upload') {
      agent{node{label('ios')}}
      when{
        not{equals(expected:"FAILURE", actual:currentBuild.result)}
      }
      steps{
        script{
          try{
            milestone("${env.BUILD_NUMBER}".toInteger())
            unstash(name: 'apk')
            sh('''
              export PATH="/usr/local/bin/:$PATH"
              export LC_ALL=en_US.UTF-8
              export LANG=en_US.UTF-8
              fir login 8abeee66a3604b68f707d9c2753f7fb4
              fir publish app/build/outputs/apk/**/app-universal-*.apk
              ''')
          }catch(e){
            def stageName = 'release'
            if(currentBuild.currentResult == "FAILURE"){
              NotifyBuild(currentBuild.result, stageName)
            }
            currentBuild.result = "FAILURE"
            throw e
          }
        }
      }
      post{
        success {
          echo "R ${currentBuild.result} C ${currentBuild.currentResult}"
          script{
            def recipient_list = 'lukai@sunmi.com,xiaoxinwu@sunmi.com,gaofei@sunmi.com,lvsiwen@sunmi.com,ningrulin@sunmi.com,yangyan@sunmi.com,zhangshiqiang@sunmi.com,yangshijie@sunmi.com,yangjibin@sunmi.com'
            emailext(attachLog: false, body: '''Download url:   https://fir.im/sf4j''', mimeType: 'text/html', subject: 'Android Test Build Ready', to: recipient_list)
          }
        } 
      }
    }
  }
}

def NotifyBuild(String buildStatus = 'STARTED', String stage){
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESS'
 
  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at "<a href="${env.BUILD_URL}/console">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>"""
 
  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESS') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  def recipient_list = 'lukai@sunmi.com,xiaoxinwu@sunmi.com,yangshijie@sunmi.com,yangjibin@sunmi.com,gaofei@sunmi.com,lvsiwen@sunmi.com,ningrulin@sunmi.com'

  switch(stage){
    case 'build':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Test Branch 构建出错', to: recipient_list)
      break

    case 'deploy':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Test Branch 部署出错', to: recipient_list)
      break

    case 'test':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Test Branch 测试步骤出错', to: recipient_list)
      break

    case 'release':
      emailext(attachLog: false, body: details, mimeType: 'text/html', subject: 'Android Test Branch 上传步骤出错', to: recipient_list)
      break
  }
}