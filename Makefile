artifact_name       := officer-delta-processor
version             := unversioned

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(artifact_name).jar
	rm -f ./$(artifact_name)-*.zip
	rm -rf ./build-*
	rm -rf ./build.log-*

.PHONY: test-unit
test-unit: clean
	mvn verify -Dskip.integration.tests=true

.PHONY: test
test: clean test-integration test-unit

# Not available until pipeline docker instance is updated
.PHONY: test-integration
test-integration:
	mvn integration-test verify -Dskip.unit.tests=true failsafe:verify

.PHONY: verify
verify: test-unit test-integration

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	# Temporary workaround for failure on concourse - waiting for artifactory request of new version to be actioned by platform
	mvn org.codehaus.mojo:versions-maven-plugin:2.16.2:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	#mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	$(info Packaging version: $(version))
	@test -s ./$(artifact_name).jar || { echo "ERROR: Service JAR not found"; exit 1; }
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./$(artifact_name).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: build
build:
	# Temporary workaround for failure on concourse - waiting for artifactory request of new version to be actioned by platform
	mvn org.codehaus.mojo:versions-maven-plugin:2.16.2:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	#mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn package -Dmaven.test.skip=true
	cp ./target/$(artifact_name)-$(version).jar ./$(artifact_name).jar

.PHONY: dist
dist: clean build package

.PHONY: sonar
sonar:
	mvn sonar:sonar

.PHONY: sonar-pr-analysis
sonar-pr-analysis:
	mvn sonar:sonar -P sonar-pr-analysis