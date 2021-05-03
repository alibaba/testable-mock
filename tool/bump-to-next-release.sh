#!/bin/bash

# Release next version

VERSION=$(cat testable-parent/pom.xml | grep '<testable.version>' | sed -e 's/^.*>\([^<]*\)<.*$/\1/g')
echo "Current version is: ${VERSION}"
read -p "Next version should be: " NEXT

for pom in testable-all/pom.xml testable-maven-plugin/pom.xml testable-processor/pom.xml testable-agent/pom.xml testable-core/pom.xml testable-parent/pom.xml; do
    sed -i '' "s/<version>${VERSION}<\/version>/<version>${NEXT}<\/version>/" $pom
done
for gradle in demo/java-demo/build.gradle demo/kotlin-demo/build.gradle.kts demo/spock-demo/build.gradle demo/android-demo/app/build.gradle; do
    sed -i '' "s/testable-\([a-z]*\):${VERSION}/testable-\1:${NEXT}/" $gradle
done
for pom in testable-parent/pom.xml demo/java-demo/pom.xml demo/kotlin-demo/pom.xml demo/spock-demo/pom.xml; do
    sed -i '' "s/<testable.version>${VERSION}<\/testable.version>/<testable.version>${NEXT}<\/testable.version>/" $pom
done
for md in docs/zh-cn/doc/setup.md docs/en-us/doc/setup.md; do
    sed -i '' "s/${VERSION}/${NEXT}/" $md
done

echo "Done. Please setup maven configuration for sonatype, and manually run \"mvn clean deploy -Prelease\""
echo "Then commit the new version with \"git add . && git commit -m 'release v${NEXT}' && git tag v${NEXT} && git push --tag\""
