package de.mk_p.findclass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * Eine Klasse zur Arbeit mit Dateisystemverzeichnissen.
 * </p>
 *
 * @author Mark.Kahl.extern@vdek.com
 *
 */

public class ZipHelper {

    public static final char   seperatorChar =              '/';
    public static final String seperator =                  "" + seperatorChar;
    public static final String DEFAULT_SUFFIX =             ".zip";

    /**
     * <p>
     * Eine Option f&uuml;r Filterungen die definert, dass nur voreingestellte Optionen verwendet werden sollen.
     * </p>
     */
    public static final long   DEFAULT_FILTER_OPTIONS =     0x0l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Suche nach Eintragsnamen Verzeichniseint&auml;ge mit einbezogen werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #WITHOUT_DIRECTORIES} aus.
     * </p>
     */
    public static final long   WITH_DIRECTORIES =           0x1l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Suche nach Eintragsnamen Verzeichniseint&auml;ge nicht mit einbezogen werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #WITH_DIRECTORIES} aus.
     * </p>
     */
    public static final long   WITHOUT_DIRECTORIES =        0x2l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Evaluierung von Namen gegen Filterkriterien die Gro&szlig;-/Kleinschreibung
     * ber&uuml;cksichtigt werden soll.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #MATCH_CASEINSENSITIVE} aus.
     * </p>
     */
    public static final long   MATCH_CASESENSITIVE =        0x4l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Evaluierung von Namen gegen Filterkriterien die Gro&szlig;-/Kleinschreibung
     * nicht ber&uuml;cksichtigt werden soll.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #MATCH_CASESENSITIVE} aus.
     * </p>
     */
    public static final long   MATCH_CASEINSENSITIVE =      0x8l;

    /**
     * <p>
     * Die Voreinstellung f&uuml;r die Gr&ouml;&szlig;e eine Puffers zum Schreiben und Lesen von Archiveintr&auml;gen.
     * </p>
     */
    public static final int    DEFAULT_BUFERSIZE =          0x1000;

    private static final int   LIST =  0;
    private static final int   PUT =   1;
    private static final int   GET =   2;
    private static final int   PRINT = 3;

    private String             archiveName;

    private ZipFile openZipFile (String archiveName) throws FileNotFoundException, IOException {
        File zipFile = new File (archiveName);

        if (zipFile.exists ())
            return (new ZipFile (archiveName));
        else
            throw new FileNotFoundException (archiveName);
    }

    public ZipHelper (String archiveName) {
        this.archiveName = archiveName;
    }

    public String normaliseEntryname (String entryName) {
        return (entryName.replace ('\\', seperatorChar));
    }

    public String getName () {
        return (archiveName);
    }

    public static void zip (String filename, String archivename) throws FileNotFoundException, IOException {
        ZipHelper zipHelper = new ZipHelper (archivename);
        
        zipHelper.append (filename, new FileInputStream (filename));
    }
    
    public InputStream get (ZipFile archive, String entryName) throws FileNotFoundException, IOException {
        ZipEntry    entry = archive.getEntry (normaliseEntryname (entryName));

        if (entry == null)
            throw new FileNotFoundException (entryName);
        return (archive.getInputStream (entry));
    }

    public InputStream [] getMulti (String entryNamePattern) throws FileNotFoundException, IOException {
        ZipEntry                            entry;
        String []                           entryNames = findEntries (entryNamePattern);
        ZipFile                             archive =    openZipFile (archiveName);
        List <InputStream>                  retVals = new ArrayList <> ();


        for (String entryName: entryNames) {
            entry = archive.getEntry (normaliseEntryname (entryName));
            InputStream is = get (entryName);
            if (entry == null)
                throw new FileNotFoundException (entryName);
            retVals.add (get (entryName));
        }
        return (retVals.toArray (new InputStream [retVals.size ()]));
    }

    public InputStream get (String entryName) throws FileNotFoundException, IOException {
        return (get (openZipFile (archiveName), entryName));
    }

    public StringBuilder getString (String entryName) throws FileNotFoundException, IOException {
        int             len;
        char []         buffer =    new char [DEFAULT_BUFERSIZE];
        StringBuilder   retVal =    new StringBuilder ();
        Reader          entryIn =   new InputStreamReader (get (entryName));

        while ((len = entryIn.read (buffer, 0, buffer.length)) > 0)
            retVal.append (buffer, 0, len);
        return (retVal);
    }

    @SuppressWarnings ("unchecked")
    public <T> T getObject (String entryName, T object)
                            throws FileNotFoundException, IOException, ClassNotFoundException {
        return (object = (T) new ObjectInputStream (get (entryName)).readObject ());
    }

    public String [] addFiles (String [] files) throws IOException {
        int             i;
        int             len;
        String          name;
        File            tempFile =      File.createTempFile (archiveName, null);
        byte []         buffer =        new byte [DEFAULT_BUFERSIZE];
        List <String>   errorFiles =    new ArrayList <String> ();
        List <String>   successFiles =  new ArrayList <String> ();
        File            zipFile =       new File (archiveName);
        ZipEntry        entry;
        ZipInputStream  zipOldStream;
        ZipOutputStream zipOutStream;
        FileInputStream entryIn =       null;

        tempFile.delete ();  // delete it, otherwise you cannot rename your existing zip to it.
        if (!zipFile.renameTo (tempFile))
            throw new IOException ("could not rename the file " + zipFile.getAbsolutePath () + " to " + tempFile.getAbsolutePath ());
        zipOldStream = new ZipInputStream (new FileInputStream (tempFile));
        zipOutStream = new ZipOutputStream (new FileOutputStream (zipFile));
        try {
            for (i = 0; i < files.length; i++) {
                try {
                    entryIn = new FileInputStream (files [i]);
                    successFiles.add (files [i]);
                    zipOutStream.putNextEntry (new ZipEntry (normaliseEntryname (files [i])));
                    while ((len = entryIn.read (buffer)) > 0)
                        zipOutStream.write (buffer, 0, len);
                    zipOutStream.closeEntry ();
                }
                catch (IOException ioe) {
                    // File does not exist or is not readable
                    errorFiles.add (files [i]);
                    continue;
                }
                finally {
                    try {
                        if (entryIn != null)
                            entryIn.close ();
                    }
                    catch (Exception e) {
                        e.printStackTrace ();
                    }
                }
            }
            Arrays.sort (files = successFiles.toArray (new String [] {}));
            for (entry = zipOldStream.getNextEntry (); entry != null; entry = zipOldStream.getNextEntry ()) {
                name = entry.getName ();
                if (Arrays.binarySearch (files, name) < 0) {
                    zipOutStream.putNextEntry (new ZipEntry (name));
                    while ((len = zipOldStream.read (buffer)) > 0)
                        zipOutStream.write (buffer, 0, len);
                }
            
            }
        }
        finally {
            try {
                zipOldStream.close ();
                zipOutStream.close ();
                tempFile.delete ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
        if (errorFiles.size () > 0)
            return (errorFiles.toArray (new String [] {}));
        else
            return (null);
    }

    public int append (String name, InputStream entryIn) throws IOException {
        boolean         doCopy;
        int             retVal =        0;
        int             len;
        String          entryName;
        File            tempFile;
        byte []         buffer =        new byte [DEFAULT_BUFERSIZE];
        File            zipFile =       new File (archiveName);
        ZipEntry        entry;
        ZipInputStream  zipOldStream =  null;
        ZipOutputStream zipOutStream;

        try {
            tempFile = File.createTempFile (archiveName, null);
        }
        catch (IOException ioe) {
            String tempname = new File (archiveName).getName ();
            tempFile = File.createTempFile (tempname, null);
        }
        if (doCopy = zipFile.exists ()) {
            tempFile.delete ();  // delete it, otherwise you cannot rename your existing zip to it.
            if (!zipFile.renameTo (tempFile))
                throw new IOException ("could not rename the file " + zipFile.getAbsolutePath () +
                                       " to " + tempFile.getAbsolutePath ());
            try {
                zipOldStream = new ZipInputStream (new FileInputStream (tempFile));
            }
            catch (Exception ze) {
                ze.printStackTrace ();
                doCopy = false; // Kein ZIP-Archiv, ignoriere Datei.
            }
        }
        zipOutStream = new ZipOutputStream (new FileOutputStream (zipFile));
        try {
            if (doCopy)
                for (entry = zipOldStream.getNextEntry (); entry != null; entry = zipOldStream.getNextEntry ()) {
                    entryName = entry.getName ();
                    if (!entryName.equals (name)) {
                        zipOutStream.putNextEntry (new ZipEntry (entryName));
                        while ((len = zipOldStream.read (buffer)) > 0)
                            zipOutStream.write (buffer, 0, len);
                        retVal++;
                    }
            
                }
            try {
                zipOutStream.putNextEntry (new ZipEntry (normaliseEntryname (name)));
                while ((len = entryIn.read (buffer)) > 0)
                    zipOutStream.write (buffer, 0, len);
            }
            finally {
                try {
                    zipOutStream.closeEntry ();
                    entryIn.close ();
                }
                catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        }
        finally {
            try {
                if (zipOldStream != null)
                    zipOldStream.close ();
                zipOutStream.close ();
                tempFile.delete ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
        return (retVal);
    }

    public int append (String name, Reader entryIn) throws IOException {
        return (append (name, new ReaderInputStream (entryIn)));
    }

    public int appendObject (String name, Serializable object) throws IOException {
        int                 retVal =        0;
        int                 len;
        String              entryName;
        File                tempFile =      File.createTempFile (archiveName, null);
        byte []             buffer =        new byte [DEFAULT_BUFERSIZE];
        File                zipFile =       new File (archiveName);
        ZipEntry            entry;
        ZipInputStream      zipOldStream;
        ZipOutputStream     zipOutStream;
	ObjectOutputStream  objectOutput;

        tempFile.delete ();  // delete it, otherwise you cannot rename your existing zip to it.
        if (!zipFile.renameTo (tempFile))
            throw new IOException ("could not rename the file " + zipFile.getAbsolutePath () + " to " + tempFile.getAbsolutePath ());
        zipOldStream = new ZipInputStream (new FileInputStream (tempFile));
        zipOutStream = new ZipOutputStream (new FileOutputStream (zipFile));
        try {
            for (entry = zipOldStream.getNextEntry (); entry != null; entry = zipOldStream.getNextEntry ()) {
                entryName = entry.getName ();
                if (!entryName.equals (name)) {
                    zipOutStream.putNextEntry (new ZipEntry (name));
                    while ((len = zipOldStream.read (buffer)) > 0)
                        zipOutStream.write (buffer, 0, len);
                    retVal++;
                }
            
            }
            try {
                zipOutStream.putNextEntry (new ZipEntry (normaliseEntryname (name)));
                objectOutput = new ObjectOutputStream (zipOutStream);
                objectOutput.writeObject (object);
            }
            finally {
                try {
                    zipOutStream.closeEntry ();
                }
                catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        }
        finally {
            try {
                zipOldStream.close ();
                zipOutStream.close ();
                tempFile.delete ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
        return (retVal);
    }

    public String [] wrap (String [] files) throws FileNotFoundException, IOException, ZipException {
        int             i;
        int             len;
        FileInputStream entryIn;
        ZipOutputStream zipOut =        new ZipOutputStream (new FileOutputStream (archiveName));
        byte []         buffer =        new byte [DEFAULT_BUFERSIZE];
        List <String>   errorFiles =    new ArrayList <String> ();

        try {
            for (i = 0; i < files.length; i++) {
                try {
                    entryIn = new FileInputStream (files [i]);
                }
                catch (IOException ioe) {
                    // File does not exist or is not readable
                    errorFiles.add (files [i]);
                    continue;
                }
                zipOut.putNextEntry (new ZipEntry (files [i]));
                while ((len = entryIn.read (buffer)) > 0)
                    zipOut.write (buffer, 0, len);
                zipOut.closeEntry ();
                entryIn.close ();
            }
        }
        finally {
            zipOut.close ();
        }
        if (errorFiles.size () > 0)
            return (errorFiles.toArray (new String [] {}));
        else
            return (null);
    }

    public String [] unwrap (String [] files, boolean overwrite) throws FileNotFoundException, IOException, ZipException {
        int             i;
        int             len;
        ZipFile         archive =       openZipFile (archiveName);
        File            unwraped;
        OutputStream    output;
        InputStream     entryIn;
        List <String>   errorFiles =    new ArrayList <String> ();
        byte []         buffer =        new byte [DEFAULT_BUFERSIZE];

        try {
            for (i = 0; i < files.length; i++)
                try {
                    for (String filename : findEntries (files[i])) {
                        try {
                            entryIn = get (archive, filename);
                        }
                        catch (FileNotFoundException fnfe) {
                            try {
                                entryIn = get (archive, new File (filename).getCanonicalPath ());
                            }
                            catch (IOException ioe) {
                                entryIn = get (archive, new File (filename).getAbsolutePath ());
                            }
                        }
                        unwraped = new File (filename);
                        if (unwraped.exists () && !overwrite) {
                            errorFiles.add (filename);
                            continue;
                        }
                        File unwrapDir = Paths.get (unwraped.getAbsolutePath ()).getParent ().toFile ();
                        if (!unwrapDir.exists ())
                            unwrapDir.mkdirs ();
                        output = new FileOutputStream (unwraped);
                        while ((len = entryIn.read (buffer)) > 0)
                            output.write (buffer, 0, len);
                        output.close ();
                        entryIn.close ();

                    }
                }
                catch (FileNotFoundException fnfe) {
                    errorFiles.add (files [i]);
                    continue;
                }
        }
        finally {
            try {
                archive.close ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
        if (errorFiles.size () > 0)
            return (errorFiles.toArray (new String [] {}));
        else
            return (null);
    }

    public static void unzip (String filename, String archivename) throws FileNotFoundException, IOException {
        ZipHelper zipHelper = new ZipHelper (archivename);

        zipHelper.unwrap (new String [] {filename}, true);
    }

    private String [] getNames (String [] filters, long options, boolean create) throws ZipException, FileNotFoundException, IOException {
        int                                 i;
        boolean                             ignoreCase =            ((options & MATCH_CASESENSITIVE) != 0l);
        boolean                             withDirectories =       ((options & WITH_DIRECTORIES) != 0l);
        String                              name;
        File                                newArchive;
        ZipEntry                            entry;
        List <String>                       names =                 new ArrayList <String> ();
        ZipFile                             archive;
        Enumeration <? extends ZipEntry>    entries;
        Matcher                             matcher;
        List <Pattern>                      filterPatternsList;
        Pattern []                          filterPatterns =        null;

        try {
            archive = openZipFile (archiveName);
        }
        catch (IOException ze) {   // Archiv existiert nicht
            if (!create)
                throw ze;
            try {
                newArchive = new File (archiveName);
                newArchive.createNewFile ();
            }
            catch (IOException ioe) {
                ioe.printStackTrace ();
                // This may happen and is no error
            }
            return (new String [] {});
        }
        entries = archive.entries ();
        if (filters != null) {
            filterPatternsList = new ArrayList <Pattern> ();
            for (i = 0; i < filters.length; i++) {
                try {
                    filterPatternsList.add (Pattern.compile (filters [i],
                                                             ((ignoreCase) ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0)));
                }
                catch (PatternSyntaxException pse) {
                    // silently ignore this pattern...
                }
            }
            if (filterPatternsList.size () > 0)
                filterPatterns = filterPatternsList.toArray (new Pattern [] {});
        }
        while (entries.hasMoreElements ()) {
            entry = entries.nextElement ();
            if (withDirectories || !entry.isDirectory ()) {
                name = entry.getName ();
                if (filterPatterns != null) {
                    for (i = 0; i < filterPatterns.length; i++) {
                        matcher = filterPatterns [i].matcher (name);
                        if (matcher.matches ()) {
                            names.add (name);
                            break;  // only one match required.
                        }
                    }
                }
                else
                    names.add (name);
            }
        }
        archive.close ();
        return (names.toArray (new String [] {}));
    }

    public String [] getNames (String [] filters, long options) throws ZipException, IOException {
        return (getNames (filters, options, false));
    }

    public String [] getNamesOrCreate (String [] filters, long options) throws ZipException, IOException {
        return (getNames (filters, options, true));
    }

    public String [] getNames () throws ZipException, IOException {
        return (getNames (null, DEFAULT_FILTER_OPTIONS));
    }

    public String [] list () throws ZipException, FileNotFoundException, IOException {
        StringBuilder                       entryText = new StringBuilder ();
        ZipEntry                            entry;
        List <String>                       names =     new ArrayList <String> ();
        ZipFile                             archive =   openZipFile (archiveName);
        Enumeration <? extends ZipEntry>    entries =   archive.entries ();
        Formatter                           formatter = new Formatter (entryText);

        while (entries.hasMoreElements ()) {
            entry = entries.nextElement ();
            formatter.format ("%s%-54s   Size: (%6d/%6d)   %tc%n", 
                              ((entry.isDirectory ()) ? "+" : " "),
                              entry.getName (), entry.getSize (),
                              entry.getCompressedSize (), entry.getTime ());
            names.add (entryText.toString ());
            entryText.setLength (0);
        }
        archive.close ();
        return (names.toArray (new String [] {}));
    }

    public String [] findEntries (String namePatternString) throws ZipException, FileNotFoundException, IOException {
        ZipEntry                            entry;
        List <String>                       names =       new ArrayList <String> ();
        ZipFile                             archive =     openZipFile (archiveName);
        Enumeration <? extends ZipEntry>    entries =     archive.entries ();
        Pattern                             namePattern = Pattern.compile (namePatternString);

        String [] entryFilter = new String []{namePatternString};
        return (getNames (entryFilter, WITH_DIRECTORIES));
    }

    public StringBuilder [] getEntriesAsString (String namePatternString) throws ZipException, FileNotFoundException, IOException {
        String [] entryNames = findEntries (namePatternString);
        List <StringBuilder> retVal = new ArrayList <> ();

        for (String entryName: entryNames)
            retVal.add (getString (entryName));
        return (retVal.toArray (new StringBuilder[retVal.size ()]));
    }
    public static void main (String args []) throws ZipException, IOException {
        int             i;
        boolean         usage =     false;
        int             action =    LIST;
        String          archiveName =   null;
        List <String>   files =     new ArrayList <String> ();
        ZipHelper       zipper;
        String []       entries;
        String []       errorFiles;

        for (i = 0; i < args.length; i++) {
            if ("-put".startsWith (args [i].toLowerCase ()))
                action = PUT;
            else if ("-get".startsWith (args [i].toLowerCase ()))
                action = GET;
            else if ("-print".startsWith (args [i].toLowerCase ()))
                action = PRINT;
            else if ("-list".startsWith (args [i].toLowerCase ()))
                action = LIST;
            else if (archiveName == null)
                archiveName = args [i];
            else
                files.add (args [i]);
        }
        if (archiveName == null)
            usage = true;
        else if (files.size () <= 0)
            usage = (action != LIST);
        if (usage)
            System.out.println ("usage: java ZipHelper (-list|-put|-get) archive file...");
        else {
            zipper = new ZipHelper (archiveName);
            switch (action) {
                case LIST:
                    entries = zipper.list ();
                    for (i = 0; i < entries.length; i++)
                        System.out.printf ("%s [%02d]:\t%s", archiveName, i, entries[i]);
                    break;
                case PUT:
                    if ((errorFiles = zipper.addFiles (files.toArray (new String[] {}))) != null) {
                        System.out.println ("Error zipping:");
                        for (i = 0; i < errorFiles.length; i++)
                            System.out.println (errorFiles[i]);
                    }
                    break;
                case GET:
                    if ((errorFiles = zipper.unwrap (files.toArray (new String[] {}), false)) != null) {
                        System.out.println ("Error unzipping:");
                        for (i = 0; i < errorFiles.length; i++)
                            System.out.println (errorFiles[i]);
                    }
                    break;
                case PRINT:
                    List <String> errorFilesList = new ArrayList <> ();
                    StringBuilder content = new StringBuilder ();
                    StringBuilder [] contents ;
                    String filename = null;
                    for (int j = 0; j < files.size (); j++) {
                        filename = files.get (j);
                        try {
                            contents = zipper.getEntriesAsString (filename);
                            for (StringBuilder entryContent: contents)
                                content.append (entryContent).append ('\n');
                        }
                        catch (IOException ioe) {
                            errorFilesList.add (filename);
                        }
                    }
                    if (errorFilesList.size () > 0) {
                        System.out.println ("Error printing:");
                        for (String errorFile: errorFilesList)
                            System.out.println (errorFile);
                    }
                    else
                        System.out.println (content);
                    break;
            }
        }
    }
}
