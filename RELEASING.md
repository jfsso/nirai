Release Process
===============

 1. Update version number in `gradle.properties` file.
 2. Commit: `git commit -am "Prepare version X.Y.Z."`
 3. Tag: `git tag -a X.Y.Z -m "Version X.Y.Z"`
 4. Release: `./gradlew clean assemble uploadArchives`
 5. Update version number in `gradle.properties` file to next "SNAPSHOT" version.
 6. Commit: `git commit -am "Prepare next development version."`
 7. Push: `git push && git push --tags`
