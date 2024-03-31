#!/bin/sh

MAVEN_PROJECT="db-messiah"
MAVEN_GROUP="com.urosjarc"
MAVEN_VERSION="0.0.2"
MAVEN_PATH="com/urosjarc/db-messiah"

echo "MAVEN_PROJECT: $MAVEN_PROJECT"
echo "MAVEN_GROUP: $MAVEN_GROUP"
echo "MAVEN_VERSION: $MAVEN_VERSION"
echo "MAVEN_PATH: $MAVEN_PATH"

mkdir -p build/bundle && cd build/bundle || exit

cp -R ~/.m2/repository/"$MAVEN_PATH"/"$MAVEN_VERSION"/* ./

rm -rf ./*.module*
for file in ./*; do md5sum "$file" | awk '{print $1}' > "$file".md5; done
for file in ./*; do sha1sum  "$file" | awk '{print $1}' > "$file".sha1; done
rm -rf ./*.md5.sha1

mkdir -p "$MAVEN_PATH"/"$MAVEN_VERSION"
cp -R ./"$MAVEN_PROJECT"-"$MAVEN_VERSION"* ./"$MAVEN_PATH"/"$MAVEN_VERSION"

rm -rf ./"$MAVEN_PROJECT"*
rm -rf bundle-"$MAVEN_VERSION".zip
zip -r bundle-"$MAVEN_VERSION".zip com

rm -rf io
rm -rf coordinates-"$MAVEN_VERSION".txt
echo "$MAVEN_GROUP":"$MAVEN_PROJECT":"$MAVEN_VERSION" > coordinates-"$MAVEN_VERSION".txt

rm -rf ./bundle*.md5* && rm -rf ./bundle*.sha1*
rm -rf ./coordinates*.md5* && rm -rf ./coordinates*.sha1*
