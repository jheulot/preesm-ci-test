#!groovy

// how many time should we retry resolving dependencies
retryResolveCount = 4

def javaVersion = "1.8"
def javaToolID = "JDK-${javaVersion}"

def mavenVersion = "3.5.0"
def mavenToolID = "Maven-${mavenVersion}"
def mavenOpts = "--errors --strict-checksums --batch-mode -Dmaven.repo.local=m2-repository"
def mavenEnvOpt = "MAVEN_OPT=-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

// tell Jenkins to remove 7 days old artifacts/builds and keep only 7 last ones
//properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '7', daysToKeepStr: '7', numToKeepStr: '7']]]);


pipeline {
    agent {
        docker {
            image 'maven:3-jdk-8'
            args '--dns 10.4.1.79 -u root'
        }
    }

    stages {
        stage('Fetch Source Code') {
        	steps{
            	checkout scm
            }
        }

		parallel {
			stage ('Checkstyle') {
	        	steps{
					sh "releng/run_checkstyle.sh"
				}
			}	
			stage ('Validate POM') {
	        	steps{
					sh "mvn ${mavenOpts} -Dtycho.mode=maven help:help -q"
				}
			}
			stage('Fetch RCPTT') {
	        	steps{
					sh './releng/fetch-rcptt-runner.sh'
				}
			}
		}

		parallel{
			stage ('Resolve Maven Dependencies') {
        		steps{
					sh "mvn ${mavenOpts} dependency:go-offline -Dtycho.mode=maven"
				}
			}
			
			stage ('Resolve P2 Dependencies') {
				// Resolve P2 dependencies
				// note: help:help with arg -q makes a "nop" goal for maven
				// see https://stackoverflow.com/a/27020792/3876938
				// We have to call maven with a nop goal to simply load the
				// tycho P2 resolver that will load all required dependencies
				// This will allow to run next stages in offline mode
        		steps{
					sh "mvn ${mavenOpts} help:help"
				}
			}
		}

		stage ('Build') {
        	steps{
				sh "mvn --offline ${mavenOpts} package -DskipTests=true -Dmaven.test.skip=true"
			}
		}

		stage ('Test') {
			// run tests and findbugs
			// note: findbugs need everything packaged
			// note: fail at end to gather as much traces as possible
        	steps{
				sh "mvn --offline --fail-at-end ${mavenOpts} verify "
			}	
		}
		parallel {
			// run sonar
			// note: run on same node to get test results and findbugs reports
			// note: never fail on that stage (report warning only)
			// note: executing in parallel with 'package' should not interfere
			stage ('Sonar') {
        		steps{
					sh "mvn --offline ${mavenOpts} sonar:sonar"
				}
			}

			stage ('Check Packaging') {
				// final stage to check that the products and site can be packaged
				// noneed to redo all tests there
        		steps{
					sh "mvn --offline ${mavenOpts} -Dmaven.test.skip=true package"
				}
			}
		}	
    }
}


