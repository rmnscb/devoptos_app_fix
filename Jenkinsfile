pipeline {
 agent { label 'master' } 
	
 environment {
  // This can be nexus3 or nexus2
  NEXUS_VERSION = "nexus3"
  // This can be http or https
  NEXUS_PROTOCOL = "http"
  // Where your Nexus is running. In my case:
  NEXUS_URL = "172.25.48.1:8081"
  // Repository where we will upload the artifact
  NEXUS_REPOSITORY = "maven-nexus-repo"
  // Jenkins credential id to authenticate to Nexus OSS
  NEXUS_CREDENTIAL_ID = "nexus-jenkins-user"
// https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#handling-credentials
  NEXUS_COMMON_CREDS = credentials('nexus-jenkins-user')

	//  customWorkspace "${JENKINS_HOME}/workspace/${JOB_NAME}/${BUILD_NUMBER}"


	 /*
	 this actually sets the following three environment variables:
BITBUCKET_COMMON_CREDS - contains a username and a password separated by a colon in the format username:password.
BITBUCKET_COMMON_CREDS_USR - an additional variable containing the username component only.
BITBUCKET_COMMON_CREDS_PSW - an additional variable containing the password component only.
	 */
	 
  /* 
    Windows: set the ip address of docker host. In my case 192.168.99.100.
    to obtains this address : $ docker-machine ip
    Linux: set localhost to SONARQUBE_URL
  */
  SONARQUBE_URL = "http://172.25.48.1"
  SONARQUBE_PORT = "9000"
 }
 options {
  skipDefaultCheckout()

 }
 stages {
  stage('SCM') {
   steps {
    checkout scm
	   echo "Running ${env.GIT_LOCAL_BRANCH} ola ${env.SVN_BRANCH} on ${env.GIT_CHECKOUT_DIR} in ${env.JENKINS_HOME} on ${env.WORKSPACE} in ${env.WORKSPACE_TMP}"
	   
	  
	   
   }
  }
  stage('Build') {
   parallel {
    stage('Compile') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args '-v /root/.m2/repository:/root/.m2/repository'
       // to use the same node and workdir defined on top-level pipeline for all docker agents
       reuseNode true
      }
     }
     steps {
      sh ' mvn clean compile'
     }
    }
    stage('CheckStyle') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args '-v /root/.m2/repository:/root/.m2/repository'
       reuseNode true
      }
     }
     steps {
      sh ' mvn checkstyle:checkstyle'
      /* step([$class: 'CheckStylePublisher',
	  //canRunOnFailed: true,
       defaultEncoding: '',
       healthy: '100',
       pattern: '* * / target/checkstyle-result.xml',
       unHealthy: '90',
       //useStableBuildAsReference: true
      ]) */
     }
    }
   }
  }
  stage('Unit Tests') {
   when {
    anyOf { branch 'master'; branch 'develop' ; branch 'main' }
   }
   agent {
    docker {
     image 'maven:3.6.0-jdk-8-alpine'
     args '-v /root/.m2/repository:/root/.m2/repository'
     reuseNode true
    }
   }
   steps {
    sh 'mvn test'
   }
   post {
    always {
     junit 'target/surefire-reports/**/*.xml'
    }
   }
  }
  stage('Integration Tests') {
   when {
    anyOf { branch 'master'; branch 'develop' ; branch 'main' }
   }
   agent {
    docker {
     image 'maven:3.6.0-jdk-8-alpine'
     args '-v /root/.m2/repository:/root/.m2/repository'
     reuseNode true
    }
   }
   steps {
    sh 'mvn verify -Dsurefire.skip=true'
   }
   post {
    always {
     junit 'target/failsafe-reports/**/*.xml'
    }
    success {
     stash(name: 'artifact', includes: 'target/*.war')
     stash(name: 'pom', includes: 'pom.xml')
     // to add artifacts in jenkins pipeline tab (UI)
     archiveArtifacts 'target/*.war'
    }
   }
  }
  stage('Code Quality Analysis') {
   parallel {
    stage('PMD') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args '-v /root/.m2/repository:/root/.m2/repository'
       reuseNode true
      }
     }
     steps {
      sh ' mvn pmd:pmd'
      // using pmd plugin
     // step([$class: 'PmdPublisher', pattern: '**/target/pmd.xml'])
     }
    }
    stage('Findbugs') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args '-v /root/.m2/repository:/root/.m2/repository'
       reuseNode true
      }
     }
     steps {
      sh ' mvn findbugs:findbugs'
      // using findbugs plugin
     // findbugs pattern: '**/target/findbugsXml.xml'
     }
    }
    stage('JavaDoc') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args '-v /root/.m2/repository:/root/.m2/repository'
       reuseNode true
      }
     }
     steps {
      sh ' mvn javadoc:javadoc'
      // step([$class: 'JavadocArchiver', javadocDir: './target/site/apidocs', keepAll: 'true'])
     }
    }
    stage('SonarQube') {
     agent {
      docker {
       image 'maven:3.6.0-jdk-8-alpine'
       args "-v /root/.m2/repository:/root/.m2/repository"
       reuseNode true
      }
     }
     steps {
      sh " mvn sonar:sonar -Dsonar.host.url=$SONARQUBE_URL:$SONARQUBE_PORT"
     }
    }
   }
   post {
    always {
     // using warning next gen plugin
     recordIssues aggregatingResults: true, tools: [mavenConsole(), java(), javaDoc(), checkStyle(pattern: '**/target/checkstyle-result.xml'), findBugs(pattern: '**/target/findbugsXml.xml', useRankAsPriority: true), pmdParser(pattern: '**/target/pmd.xml')]
    }
   }
  }
  stage('Deploy Artifact To Nexus') {
	  // https://medium.com/appfleet/publishing-artifacts-to-sonatype-nexus-using-jenkins-pipelines-db8c1412dc7
   when {
    anyOf { branch 'master'; branch 'develop' ; branch 'main' }
   }
   steps {
    script {
     unstash 'pom'
     unstash 'artifact'
     // Read POM xml file using 'readMavenPom' step , this step 'readMavenPom' is included in: https://plugins.jenkins.io/pipeline-utility-steps
     pom = readMavenPom file: "pom.xml";
     // Find built artifact under target folder
     filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
     // Print some info from the artifact found
     echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
     // Extract the path from the File found
     artifactPath = filesByGlob[0].path;
     // Assign to a boolean response verifying If the artifact name exists
     artifactExists = fileExists artifactPath;
     if (artifactExists) {
      nexusArtifactUploader(
       nexusVersion: NEXUS_VERSION,
       protocol: NEXUS_PROTOCOL,
       nexusUrl: NEXUS_URL,
       groupId: pom.groupId,
       version: pom.version,
       repository: NEXUS_REPOSITORY,
       credentialsId: NEXUS_CREDENTIAL_ID,
       artifacts: [
        // Artifact generated such as .jar, .ear and .war files.
        [artifactId: pom.artifactId,
         classifier: '',
         file: artifactPath,
         type: pom.packaging
        ],
        // Lets upload the pom.xml file for additional information for Transitive dependencies
        [artifactId: pom.artifactId,
         classifier: '',
         file: "pom.xml",
         type: "pom"
        ]
       ]
      )
     } else {
      error "*** File: ${artifactPath}, could not be found";
     }
    }
   }
  }
  stage('Deploy to Staging Servers') {
   when {
    anyOf { branch 'master'; branch 'develop'  ; branch 'main'}
   }
   agent {
    docker {
     image 'ahmed24khaled/ansible-management'
     reuseNode true
    }
   }
   steps {
    script {

     pom = readMavenPom file: "pom.xml"
     repoPath = "${pom.groupId}".replace(".", "/") + "/${pom.artifactId}"
     version = pom.version
     artifactId = pom.artifactId	    
     withEnv(["ANSIBLE_HOST_KEY_CHECKING=False", "APP_NAME=${artifactId}", "repoPath=${repoPath}", "version=${version}"]) {
      sh '''      
      
        curl --silent --user $NEXUS_COMMON_CREDS_USR:$NEXUS_COMMON_CREDS_PSW  "http://$NEXUS_URL/repository/$NEXUS_REPOSITORY/${repoPath}/${version}/maven-metadata.xml" > tmp &&
        egrep '<value>+([0-9\\-\\.]*)' tmp > tmp2 &&
        tail -n 1 tmp2 > tmp3 &&
        tr -d "</value>[:space:]" < tmp3 > tmp4 &&
        REPO_VERSION=$(cat tmp4) &&

        export APP_SRC_URL="http://${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/${repoPath}/${version}/${APP_NAME}-${REPO_VERSION}.war" &&
        ansible-playbook -v -i ./ansible_provisioning/hosts --extra-vars "host=staging" ./ansible_provisioning/playbook.yml 

       '''

     }
    }
   }
  }
	 // INICION DOS  TESTS
	 
stage('QA testing') {
   parallel {
	   
 stage("QA testing with postman") {
      agent {
        docker {
          image 'postman/newman'
          args '--entrypoint='
        }
      }
      // when {
      //   expression { mapBranch[params.DEPLOY_TO] == "production" }
      // }
      steps {
        sh "newman run \"https://www.getpostman.com/collections/9e8b55b10f6705f5a066\""     
	      
      }
    }
   
stage('QA testing with katalon') {
     agent {
      docker {
		image 'katalonstudio/katalon'
            args "-u root"
      }
     }
     steps {
dir('katalon') { // this app is also a repository of the app	
	     echo "Running ${env.GIT_LOCAL_BRANCH} on ${env.GIT_CHECKOUT_DIR} in ${env.JENKINS_HOME} on ${env.WORKSPACE} in ${env.WORKSPACE_TMP}"

	     
	      sh 'cd katalon && pwd'
	     sh 'ls'     
	     
	    
                sh 'katalonc -noSplash -runMode=console -projectPath="katalon/My First Web UI Project (1).prj" -retry=0 -testSuitePath="Test Suites/New Test Suite" -executionProfile="default" -browserType="Chrome" -apiKey="2fb75599-123b-465b-95b4-753e1841f00c" --config -proxy.auth.option=NO_PROXY -proxy.system.option=NO_PROXY -proxy.system.applyToDesiredCapabilities=true'
            }
    } // aki
   }
  }
}
	 
	 
	 
	 
	 // FIM DOS TESTES	 
   stage('Deploy to Production Servers') {
   when {
    anyOf { branch 'master'  ; branch 'main'}
   }
   agent {
    docker {
     image 'ahmed24khaled/ansible-management'
     reuseNode true
    }
   }
   steps {
    script {

     pom = readMavenPom file: "pom.xml"
     repoPath = "${pom.groupId}".replace(".", "/") + "/${pom.artifactId}"
     version = pom.version
     artifactId = pom.artifactId
     withEnv(["ANSIBLE_HOST_KEY_CHECKING=False", "APP_NAME=${artifactId}", "repoPath=${repoPath}", "version=${version}"]) {
      sh '''
      
        curl --silent  --user $NEXUS_COMMON_CREDS_USR:$NEXUS_COMMON_CREDS_PSW  "$NEXUS_URL/repository/$NEXUS_REPOSITORY/${repoPath}/${version}/maven-metadata.xml" > tmp &&
        egrep '<value>+([0-9\\-\\.]*)' tmp > tmp2 &&
        tail -n 1 tmp2 > tmp3 &&
        tr -d "</value>[:space:]" < tmp3 > tmp4 &&
        REPO_VERSION=$(cat tmp4) &&

        export APP_SRC_URL="http://${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/${repoPath}/${version}/${APP_NAME}-${REPO_VERSION}.war" &&
        ansible-playbook -v -i ./ansible_provisioning/hosts --extra-vars "host=production" ./ansible_provisioning/playbook.yml 

       '''
     }
    }
   }
  }

 }
}
