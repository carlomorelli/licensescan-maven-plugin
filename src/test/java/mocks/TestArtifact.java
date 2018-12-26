package mocks;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class TestArtifact implements Artifact {
    private final String group;
    private final String artifact;
    private final String version;

    public TestArtifact(String group, String artifact, String version) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestArtifact that = (TestArtifact) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (artifact != null ? artifact.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public String getGroupId() {
        return group;
    }

    public String getArtifactId() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {

    }

    public String getScope() {
        return null;
    }

    public String getType() {
        return "test";
    }

    public String getClassifier() {
        return null;
    }

    public boolean hasClassifier() {
        return false;
    }

    public File getFile() {
        return null;
    }

    public void setFile(File destination) {

    }

    public String getBaseVersion() {
        return null;
    }

    public void setBaseVersion(String baseVersion) {

    }

    public String getId() {
        return null;
    }

    public String getDependencyConflictId() {
        return null;
    }

    public void addMetadata(ArtifactMetadata metadata) {

    }

    public Collection<ArtifactMetadata> getMetadataList() {
        return null;
    }

    public void setRepository(ArtifactRepository remoteRepository) {

    }

    public ArtifactRepository getRepository() {
        return null;
    }

    public void updateVersion(String version, ArtifactRepository localRepository) {

    }

    public String getDownloadUrl() {
        return null;
    }

    public void setDownloadUrl(String downloadUrl) {

    }

    public ArtifactFilter getDependencyFilter() {
        return null;
    }

    public void setDependencyFilter(ArtifactFilter artifactFilter) {

    }

    public ArtifactHandler getArtifactHandler() {
        return new DefaultArtifactHandler();
    }

    public List<String> getDependencyTrail() {
        return null;
    }

    public void setDependencyTrail(List<String> dependencyTrail) {

    }

    public void setScope(String scope) {

    }

    public VersionRange getVersionRange() {
        return null;
    }

    public void setVersionRange(VersionRange newRange) {

    }

    public void selectVersion(String version) {

    }

    public void setGroupId(String groupId) {

    }

    public void setArtifactId(String artifactId) {

    }

    public boolean isSnapshot() {
        return false;
    }

    public void setResolved(boolean resolved) {

    }

    public boolean isResolved() {
        return false;
    }

    public void setResolvedVersion(String version) {

    }

    public void setArtifactHandler(ArtifactHandler handler) {

    }

    public boolean isRelease() {
        return false;
    }

    public void setRelease(boolean release) {

    }

    public List<ArtifactVersion> getAvailableVersions() {
        return null;
    }

    public void setAvailableVersions(List<ArtifactVersion> versions) {

    }

    public boolean isOptional() {
        return false;
    }

    public void setOptional(boolean optional) {

    }

    public ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
        return null;
    }

    public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
        return false;
    }

    public int compareTo(Artifact o) {
        return 0;
    }

}
