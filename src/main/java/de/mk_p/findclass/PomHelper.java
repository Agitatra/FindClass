package de.mk_p.findclass;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

public class PomHelper {
    private String id;
    private String groupId;
    private String artifactId;
    private String version;

    private static Pattern groupIdPattern = Pattern.compile ("\\bgroupId *= *([^\\p{Space}=]+)(?:[\\p{Space}=]?|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static Pattern artifactIdPattern = Pattern.compile ("\\bartifactId *= *([^\\p{Space}=]+)(?:[\\p{Space}=]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static Pattern versionPattern = Pattern.compile ("\\bversion *= *([^\\p{Space}=]+)(?:[\\p{Space}=]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
    public PomHelper (String id, String groupId, String artifactId, String version) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public PomHelper (StringBuilder pomProperties) {
        PomHelper copy = parse (pomProperties);
        this.id = copy.getId ();
        this.groupId = copy.getGroupId ();
        this.artifactId = copy.getArtifactId ();
        this.version = copy.getVersion ();
    }

    private static PomHelper parse (StringBuilder pomProperties) {
        String groupId;
        String artifactId;
        String version;
        Matcher groupidMatcher = groupIdPattern.matcher (pomProperties);
        Matcher artifactidMatcher = artifactIdPattern.matcher (pomProperties);
        Matcher versionMatcher = versionPattern.matcher (pomProperties);
        if (groupidMatcher.find ())
            groupId = groupidMatcher.group (1);
        else
            groupId = null;
        if (artifactidMatcher.find ())
            artifactId = artifactidMatcher.group (1);
        else
            artifactId = null;
        if (versionMatcher.find ())
            version = versionMatcher.group (1);
        else
            version = null;
        return new PomHelper ("", groupId, artifactId, version);
    }

    public PomHelper (ZipHelper zipHelper, String archiveName) {
        PomHelper copy;
        StringBuilder [] pomContent;

        try {
            pomContent = zipHelper.getEntriesAsString ("META-INF/maven/.*/pom.properties");
        }
        catch (IOException e) {
            pomContent = null;
        }
        if (pomContent instanceof StringBuilder [] && pomContent.length > 0) {
            copy = parse (pomContent[0]);
        }
        else if ((copy = PomHelper.getInfo (filepathWoExtension (archiveName) + ".pom")) == null)
            copy = PomHelper.getInfo (Paths.get (archiveName).getParent ().toString () + "pom.xml");
        if (copy != null) {
            this.id = copy.getId ();
            this.groupId = copy.getGroupId ();
            this.artifactId = copy.getArtifactId ();
            this.version = copy.getVersion ();
        }
        else {
            this.id = "";
            this.groupId = null;
            this.artifactId = null;
            this.version = null;
        }
    }

    public String getId () {
        return (id);
    }
    public static String filepathWoExtension (String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf (".");

        if (index == -1) {
            return filename;
        } else {
            return filename.substring (0, index);
        }
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
