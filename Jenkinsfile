node ('beaware-jenkins-slave') {

    stage ('Deploy') {
      checkout scm
      sh 'kubectl apply -f kubernetes/deploy.yaml -n prod --validate=false'
    }

    stage ('Print-deploy logs') {
        sh 'sleep 60'
        sh 'kubectl  -n prod logs deploy/social-media-analysis -c social-media-analysis'
    }

}
