package de.mk_p.findclass;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomHelper {
    private String id;
    private String groupId;
    private String artifactId;
    private String version;

    private static Pattern groupIdPattern = null;
    private static Pattern artifactIdPattern = null;
    private static Pattern versionPattern = null;
    public PomHelper (String id, String groupId, String artifactId, String version) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public PomHelper (StringBuilder pomProperties) {
        String pomPropertiesX = pomProperties.toString ().replace ('\n', ' ');
        if (groupIdPattern == null)
            groupIdPattern = Pattern.compile ("\\bgroupId *= *([^\\p{Space}=]+)(?:[\\p{Space}=]?|$)",
                                              Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
        if (artifactIdPattern == null)
            artifactIdPattern = Pattern.compile ("\\bartifactId *= *([^\\p{Space}=]+)(?:[\\p{Space}=]|$)",
                                                 Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
        if (versionPattern == null)
            versionPattern = Pattern.compile ("\\bversion *= *([^\\p{Space}=]+)(?:[\\p{Space}=]|$)",
                                              Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
        Matcher groupidMatcher = groupIdPattern.matcher (pomPropertiesX);
        Matcher artifactidMatcher = artifactIdPattern.matcher (pomPropertiesX);
        Matcher versionMatcher = versionPattern.matcher (pomPropertiesX);
        this.id = "";
        if (groupidMatcher.find ())
            this.groupId = groupidMatcher.group (1);
        else
            this.groupId = null;
        if (artifactidMatcher.find ())
            this.artifactId = artifactidMatcher.group (1);
        else
            this.artifactId = null;
        if (versionMatcher.find ())
            this.version = versionMatcher.group (1);
        else
            this.version = null;
    }

    public String getId () {
        return (id);
    }

    public String getGroupId () {
        return (groupId);
    }

    public String getArtifactId () {
        return (artifactId);
    }

    public String getVersion () {
        return (version);
    }

    public static PomHelper getInfo (String pomName) {
        MavenXpp3Reader reader = new MavenXpp3Reader ();
        Model model;
        try {
            if ((new File (pomName)).canRead ())
                model = reader.read (new FileReader (pomName));
            else
                model = reader.read
                        (new InputStreamReader
                                (Objects.requireNonNull (PomHelper.class.getResourceAsStream
                                        ("/META-INF/maven/" + PomHelper.class.getPackage ().getName () + "/" +
                                                PomHelper.class.getPackage ().getName () +
                                                "/pom.xml"
                                        ))
                                )
                        );
            return (new PomHelper (model.getId (), model.getGroupId (), model.getArtifactId (), model.getVersion ()));
        }
        catch (XmlPullParserException | IOException | NullPointerException e) {
            return (null);
        }
    }
    public static void main (String[] args) throws IOException, XmlPullParserException {
        PomHelper pomHelper = PomHelper.getInfo ((args.length > 0) ? args [0] : "pom.xml");
        System.out.println("Id: \"" + pomHelper.getId() + "\"");
        System.out.println("GrouptId: \"" + pomHelper.getGroupId() + "\"");
        System.out.println("ArtifactId: \"" + pomHelper.getArtifactId() + "\"");
        System.out.println("Version: \"" + pomHelper.getVersion() + "\"");
    }
}
