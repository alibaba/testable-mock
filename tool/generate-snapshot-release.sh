#!/bin/bash

# Release SNAPSHOT version

if [ "${SNAPSHOT_MVN_REPO}" = "" ]; then
    echo "\"SNAPSHOT_MVN_REPO\" environment variable should be setup before execution"
    exit -1
fi

VERSION=$(cat testable-parent/pom.xml | grep '<testable.version>' | sed -e 's/^.*>\([^<]*\)<.*$/\1/g')
MAJOR_VERSION=$(echo $VERSION | sed 's/^\(.*\)\.[0-9]\{1,\}.*/\1/g')
declare -i MINOR_VERSION=$(echo $VERSION | sed 's/^.*\.\([0-9]\{1,\}\)/\1/g')
MINOR_VERSION=$MINOR_VERSION+1
SNAPSHOT="${MAJOR_VERSION}.${MINOR_VERSION}-SNAPSHOT"

echo "Current version: ${VERSION}"
echo "Next snapshot version: ${SNAPSHOT}"
read -p "Confirm (Y/N) ? " confirm && [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]] || exit 1


sed -i '' '/<\/profiles>/,$d' testable-parent/pom.xml
cat << EOF >> testable-parent/pom.xml
        <profile>
            <id>snapshot</id>
            <distributionManagement>
                <repository>
                    <id>releases</id>
                    <url>http://${SNAPSHOT_MVN_REPO}/mvn/releases</url>
                </repository>
                <snapshotRepository>
                    <id>snapshots</id>
                    <url>http://${SNAPSHOT_MVN_REPO}/mvn/snapshots</url>
                </snapshotRepository>
            </distributionManagement>
        </profile>
    </profiles>
</project>
EOF

for pom in testable-all/pom.xml testable-maven-plugin/pom.xml testable-processor/pom.xml testable-agent/pom.xml testable-core/pom.xml testable-parent/pom.xml; do
    sed -i '' "s#<version>${VERSION}</version>#<version>${SNAPSHOT}</version>#g" $pom
done
sed -i '' "s#<testable.version>${VERSION}</testable.version>#<testable.version>${SNAPSHOT}</testable.version>#g" testable-parent/pom.xml

echo "Done. Please setup maven configuration for snapshot repository and manually run \"mvn clean deploy -Psnapshot\""
