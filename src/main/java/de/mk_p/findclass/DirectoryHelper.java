package de.mk_p.findclass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * Eine Klasse zur Arbeit mit Dateisystemverzeichnissen.
 * </p>
 *
 * @author Mark.Kahl.extern@vdek.com
 *
 */

public class DirectoryHelper {



    /**
     * <p>
     * Der voreingestellte Filter gegen den gefundene Dateinamen evaluiert werden soll.
     * Die Voreinstellung sind alle Dateien.
     * </p>
     */
    public static final String DEFAULTFILTER  =             "^.*$";

    /**
     * <p>
     * Das voreingestellte Verzeichnis in dem nach Dateien gesucht werden soll.
     * Die Voreinstellung ist das aktuelle Verzeichnis.
     * </p>
     */
    public static final String DEFAULTDIRECTORY =           ".";

    /**
     * <p>
     * Eine Option f&uuml;r Filterungen die definert, dass nur voreingestellte Optionen verwendet werden sollen.
     * </p>
     */
    public static final long   DEFAULT_FILTER_OPTIONS =     0x0l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Suche nach Dateien Unterverzeichnisse mit einbezogen werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #RECURSE_MATCHEDDIRECTORIES} aus.
     * </p>
     */
    public static final long   RECURSE_DIRECTORIES =        0x1l;

    /**
     * <p>
     * Eine Option die definiert, dass bei der Suche nach Dateien Unterverzeichnisse mit einbezogen werden sollen
     * deren Namen zuvor positiv gegen Filterkriterien evaluiert wurden.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Option
     * {@linkplain #RECURSE_DIRECTORIES} aus.
     * </p>
     */
    public static final long   RECURSE_MATCHEDDIRECTORIES = 0x2l;

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
     * Die Methode akzeptiert den Namen eines Dateisystemordners und mehrere
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} die verwendet werden um im &uuml;bergebenen Ordner
     * und je nach Optionen m&ouml;glicherweise in Unterordnern nach Dateien zu suchen.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner und m&ouml;glicherweise Unterordner auf Dateien die einem oder mehreren
     * Filterausdr&uuml;cken entsprechen und gibt deren Namen zur�ck.
     * Gro&szlig;-/Kleinschreibung wird je nach Optionen ber&uuml;cksichtigt (Windows: ja, andere: nein).
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  directory    Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filters      Ein Feld mit {@linkplain java.lang.String Zeichenkette} die als {@linkplain java.util.regex.Pattern Filter}
     *                      zur Suche nach Dateinamen verwendet werden sollen.
     * @param  options      Ein Wert der aus der logischen <q>Veroderung</q> einer oder mehrerer der folgenden Optionen entsteht.
     *                      <dl>
     *                      <dt>{@linkplain #RECURSE_DIRECTORIES RECURSE_DIRECTORIES}</dt>
     *                      <dd>Bei der Suche sollen Unterverzeichnisse mit einbezogen werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #RECURSE_MATCHEDDIRECTORIES}</q> angegeben werden.
     *                      </dd>
     *                      <dt>{@linkplain #RECURSE_MATCHEDDIRECTORIES RECURSE_MATCHEDDIRECTORIES}</dt>
     *                      <dd>Bei der Suche sollen Unterverzeichnisse, deren Namen einem der Filterkriterien entsprechen,
     *                          mit einbezogen werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #RECURSE_DIRECTORIES}</q> angegeben werden.
     *                      </dd>
     *                      <dt>{@linkplain #MATCH_CASESENSITIVE MATCH_CASESENSITIVE}</dt>
     *                      <dd>Beim Vergleich der Dateinamen mit den Filterkriterien soll die Gro&szlig;-/Kleinschreibung
     *                          ber&uuml;cksichtigt werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q> angegeben werden.
     *                          Wenn weder: <q>{@linkplain #MATCH_CASESENSITIVE}</q> noch: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q>
     *                          angegeben werden, h&auml;ngt das Verhalten vom Betriebssystem ab (Windows: ja, andere: nein).
     *                      </dd>
     *                      <dt>{@linkplain #MATCH_CASEINSENSITIVE MATCH_CASEINSENSITIVE}</dt>
     *                      <dd>Beim Vergleich der Dateinamen mit den Filterkriterien soll die Gro&szlig;-/Kleinschreibung
     *                          nicht ber&uuml;cksichtigt werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #MATCH_CASESENSITIVE}</q> angegeben werden.
     *                          Wenn weder: <q>{@linkplain #MATCH_CASESENSITIVE}</q> noch: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q>
     *                          angegeben werden, h&auml;ngt das Verhalten vom Betriebssystem ab (Windows: ja, andere: nein).
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_MODIFICATIONDATE FileComparator.SORTEDBY_MODIFICATIONDATE}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach dem Zeitpunkt der letzten Modifikation sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_FILENAME FileComparator.SORTEDBY_FILENAME}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach den enthaltenen Dateinamen sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_PATHNAME FileComparator.SORTEDBY_PATHNAME}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach den enthaltenen Pfadnamen sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      </dl>
     * @param  comparator   Die Implementierung einer {@linkplain java.util.Comparator Vergleichsklasse} f&uuml;r
     *                      {@linkplain java.lang.String Zeichenketten} die zur Sortierung der R&uuml;ckgabewerte verwendet  wird.
     *                      Sofern dieser Parameter &uuml;bergeben wird, finden die entsprechenden Flags im Parameter: <q>options</q>
     *                      keine Beachtung.
     * @return Ein Feld mit einer {@linkplain java.lang.String Zeichenkette} f�r jede gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     */

    public static String [] list (String directory, String [] filters, long options, Comparator <String> comparator) {
        boolean         recurseDirectories;
        boolean         recurseMatchedDirectories;
        boolean         ignoreCase;
        int             i;
        int             j;
        int             flags;
        File            dir;
        String          path;
        String []       filenames;
        Matcher         matcher;
        List <Pattern>  filterPatternsList;
        Pattern []      filterPatterns;
        List <String>   retValList =    new ArrayList <String> ();
        String []       retVal;

        if ((options & RECURSE_DIRECTORIES) != 0l)
            recurseMatchedDirectories = !(recurseDirectories = true);
        else if ((options & RECURSE_MATCHEDDIRECTORIES) != 0l)
            recurseDirectories = !(recurseMatchedDirectories = true);
        else
            recurseDirectories = recurseMatchedDirectories = false;
        if ((options & MATCH_CASESENSITIVE) != 0l)
            ignoreCase = false;
        else if ((options & MATCH_CASEINSENSITIVE) != 0l)
            ignoreCase = true;
        else
            ignoreCase = (System.getProperty ("os.name").substring (0, 3).equalsIgnoreCase ("win"));
        flags = (ignoreCase) ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        dir = new File (((directory != null) && (directory.length () > 0)) ? directory : System.getProperty ("user.dir"));
        if (dir.isDirectory ()) { // check to make sure it is a directory
            filterPatternsList = new ArrayList <Pattern> ();
            if (filters != null)
                for (i = 0; i < filters.length; i++) {
                    try {
                        filterPatternsList.add (Pattern.compile (filters [i], flags));

                    }
                    catch (PatternSyntaxException pse) {
                        // silently ignore this pattern...
                    }
                }
            if (filterPatternsList.size () <= 0)  // no filters delivered or none did compile
                try {
                    filterPatternsList.add (Pattern.compile (DEFAULTFILTER, flags));
                }
                catch (PatternSyntaxException pse) {
                    // This should not happen...
                    pse.printStackTrace ();
                }
            filterPatterns = filterPatternsList.toArray (new Pattern [] {});
            filenames = dir.list ();
            if (filenames != null) {
                OUTER: for (i = 0; i < filenames.length; i++) {
                    path = directory + File.separator + filenames [i];
                    if (recurseDirectories && (new File (path)).isDirectory ())
                        retValList.addAll (Arrays.asList (list (path, filters, options)));
                    else
                        for (j = 0; j < filterPatterns.length; j++) {
                            matcher = filterPatterns [j].matcher (filenames [i]);
                            if (matcher.matches ()) {
                                if (recurseMatchedDirectories && (new File (path)).isDirectory ())
                                    retValList.addAll (Arrays.asList (list (path, filters, options)));
                                else {
                                    try {
                                        path = (new File (path)).getCanonicalPath ();
                                    }
                                    catch (IOException ioe) {
                                        path = (new File (path)).getAbsolutePath ();
                                    }
                                    retValList.add (path);
                                }
                                continue OUTER;   // only one match required
                            }
                        }
                }
            }
        }
        retVal = retValList.toArray (new String [] {});
        if (comparator != null)
            Arrays.sort (retVal, comparator);
        else if ((options &
             (FileComparator.SORTEDBY_MODIFICATIONDATE | FileComparator.SORTEDBY_PATHNAME | FileComparator.SORTEDBY_FILENAME)) != 0l)
            Arrays.sort (retVal, new FileComparator (options));
        return (retVal);
    }




    /**
     * <p>
     * Die Methode akzeptiert den Namen eines Dateisystemordners und mehrere
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} die verwendet werden um im &uuml;bergebenen Ordner
     * und je nach Optionen m&ouml;glicherweise in Unterordnern nach Dateien zu suchen.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner und m&ouml;glicherweise Unterordner auf Dateien die einem oder mehreren
     * Filterausdr&uuml;cken entsprechen und gibt deren Namen zur�ck.
     * Gro&szlig;-/Kleinschreibung wird je nach Optionen ber&uuml;cksichtigt (Windows: ja, andere: nein).
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  directory    Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filters      Ein Feld mit {@linkplain java.lang.String Zeichenkette} die als {@linkplain java.util.regex.Pattern Filter}
     *                      zur Suche nach Dateinamen verwendet werden sollen.
     * @param  options      Ein Wert der aus der logischen <q>Veroderung</q> einer oder mehrerer der folgenden Optionen entsteht.
     *                      <dl>
     *                      <dt>{@linkplain #RECURSE_DIRECTORIES RECURSE_DIRECTORIES}</dt>
     *                      <dd>Bei der Suche sollen Unterverzeichnisse mit einbezogen werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #RECURSE_MATCHEDDIRECTORIES}</q> angegeben werden.
     *                      </dd>
     *                      <dt>{@linkplain #RECURSE_MATCHEDDIRECTORIES RECURSE_MATCHEDDIRECTORIES}</dt>
     *                      <dd>Bei der Suche sollen Unterverzeichnisse, deren Namen einem der Filterkriterien entsprechen,
     *                          mit einbezogen werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #RECURSE_DIRECTORIES}</q> angegeben werden.
     *                      </dd>
     *                      <dt>{@linkplain #MATCH_CASESENSITIVE MATCH_CASESENSITIVE}</dt>
     *                      <dd>Beim Vergleich der Dateinamen mit den Filterkriterien soll die Gro&szlig;-/Kleinschreibung
     *                          ber&uuml;cksichtigt werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q> angegeben werden.
     *                          Wenn weder: <q>{@linkplain #MATCH_CASESENSITIVE}</q> noch: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q>
     *                          angegeben werden, h&auml;ngt das Verhalten vom Betriebssystem ab (Windows: ja, andere: nein).
     *                      </dd>
     *                      <dt>{@linkplain #MATCH_CASEINSENSITIVE MATCH_CASEINSENSITIVE}</dt>
     *                      <dd>Beim Vergleich der Dateinamen mit den Filterkriterien soll die Gro&szlig;-/Kleinschreibung
     *                          nicht ber&uuml;cksichtigt werden.
     *                          Darf nicht gleichzeitig mit: <q>{@linkplain #MATCH_CASESENSITIVE}</q> angegeben werden.
     *                          Wenn weder: <q>{@linkplain #MATCH_CASESENSITIVE}</q> noch: <q>{@linkplain #MATCH_CASEINSENSITIVE}</q>
     *                          angegeben werden, h&auml;ngt das Verhalten vom Betriebssystem ab (Windows: ja, andere: nein).
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_MODIFICATIONDATE FileComparator.SORTEDBY_MODIFICATIONDATE}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach dem Zeitpunkt der letzten Modifikation sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_FILENAME FileComparator.SORTEDBY_FILENAME}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach den enthaltenen Dateinamen sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      <dt>{@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_PATHNAME FileComparator.SORTEDBY_PATHNAME}</dt>
     *                      <dd>Das zur&uuml;ckgegebene Feld ist nach den enthaltenen Pfadnamen sortiert.
     *                          Die Liste ist aufsteigend, es sei denn es wurde zus&auml;tzlich die Option:
     *                          {@linkplain com.vdek.dafin.utils.io.file.FileComparator.SORTEDBY_DESCENDING FileComparator.SORTEDBY_DESCENDING}
     *                          angegeben.
     *                      </dd>
     *                      </dl>
     * @return Ein Feld mit einer {@linkplain java.lang.String Zeichenkette} f�r jede gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     */

    public static String [] list (String directory, String [] filters, long options) {
        return (list (directory, filters, options, null));
    }


    /**
     * <p>
     * Die Methode akzeptiert den Namen eines Dateisystemordners und mehrere
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} die verwendet werden um im &uuml;bergebenen Ordner nach Dateien
     * zu suchen.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner auf Dateien die einem oder mehreren Filterausdr&uuml;cken entsprechen und
     * gibt deren Namen zur�ck.
     * Gro&szlig;-/Kleinschreibung wird je nach Betriebssystem ber&uuml;cksichtigt (Windows: ja, andere: nein).
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  directory    Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filters      Ein Feld mit {@linkplain java.lang.String Zeichenkette} die als {@linkplain java.util.regex.Pattern Filter}
     *                      zur Suche nach Dateinamen verwendet werden sollen.
     * @return Ein Feld mit einer {@linkplain java.lang.String Zeichenkette} f�r jede gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     */

    public static String [] list (String directory, String [] filters) {
        return (list (directory, filters, DEFAULT_FILTER_OPTIONS));
    }


    /**
     * <p>
     * Die Methode akzeptiert den Namen eines Dateisystemordners und einen
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;ck} der verwendet wird um im &uuml;bergebenen Ordner nach Dateien
     * zu suchen.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner auf Dateien die dem Filterausdr&uuml;ck entsprechen und
     * gibt deren Namen zur�ck.
     * Gro&szlig;-/Kleinschreibung wird je nach Betriebssystem ber&uuml;cksichtigt (Windows: ja, andere: nein).
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  directory    Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filter       Eine {@linkplain java.lang.String Zeichenkette} die als {@linkplain java.util.regex.Pattern Filter}
     *                      zur Suche nach Dateinamen verwendet wird.
     * @return Ein Feld mit einer {@linkplain java.lang.String Zeichenkette} f�r jede gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     */

    public static String [] list (String directory, String filter) {
        return (list (directory, new String [] {filter}, DEFAULT_FILTER_OPTIONS));
    }


    /**
     * <p>
     * Die Methode akzeptiert den Namen eines Dateisystemordners und mehrere
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} die verwendet werden um im &uuml;bergebenen Ordner nach Dateien
     * zu suchen.
     * Sofern keine entsprechenden Dateien gefunden werden, wartet sie bis eine solche Datei vorhanden ist, oder bis sie
     * {@linkplain java.lang.InterruptedException unterbrochen} wird.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner auf Dateien die den einem oder mehreren Filterausdr&uuml;cken entsprechen und
     * gibt den Namen der ersten gefundenen Datei zur&uuml;ck.
     * Der Name wird vollst�ndig mit allen Ordnern zur&uuml;ckgeliefert.
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  milliseconds         Das Intervall das gewartet werden soll bis der Ordner wieder nach Dateien durchsucht wird.
     * @param  directory            Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filters              Ein Feld mit {@linkplain java.lang.String Zeichenkette} die als
     *                              {@linkplain java.util.regex.Pattern Filter} zur Suche nach Dateinamen verwendet werden sollen.
     * @param  callback             Eine Implementierung der Methode:
     *                              {@linkplain com.vdek.dafin.utils.io.file.IOHelperCallback#terminate() terminate},
     *                              die vor der Wartezeit gefragt wird ob weiter gewartet werden soll.
     *                              Falls der Parameter den Wert: <q>null</q> hat wird er ignoriert.
     * @throws InterruptedException Falls der aktuelle {@linkplain java.lang.Thread Thread} von Au&szlig;en unterbrochen wurde.
     * @return Der {@linkplain java.lang.String Name} der ersten gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     *         Sofern die Methode durch die vom Parameter: <q>callback</q> definierte Methode:
     *         {@linkplain com.vdek.dafin.utils.io.file.IOHelperCallback#terminate() terminate} beendet wurde, ohne da&szlig;
     *         ein Ergebnis gefunden wurde, gibt sie: <q>null</q> zur&uuml;ck.
     */

    public static String waitForFile (int milliseconds, String directory, String [] filters, IOHelperCallback callback)
                                                                                                  throws InterruptedException {
        String [] filenames;

        while (((filenames = list (directory, filters)).length <= 0) && ((callback == null) || !callback.terminate ()))
            TimeUnit.MILLISECONDS.sleep (milliseconds);
        return ((filenames.length <= 0) ? null : filenames [0]);
    }

    
    
    /**
     * <p>
     * Die Methode akzeptiert den Namen eines Dateisystemordners und mehrere
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} die verwendet werden um im &uuml;bergebenen Ordner nach Dateien
     * zu suchen.
     * Sofern keine entsprechenden Dateien gefunden werden, wartet sie bis eine solche Datei vorhanden ist, oder bis sie
     * {@linkplain java.lang.InterruptedException unterbrochen} wird.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner auf Dateien die den einem oder mehreren Filterausdr&uuml;cken entsprechen und
     * gibt den Namen der ersten gefundenen Datei zur&uuml;ck.
     * Der Name wird vollst�ndig mit allen Ordnern zur&uuml;ckgeliefert.
     * </p>
     * <p>
     * Sofern kein Wert f�r den Ordner oder kein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * </p>
     *
     * @param  milliseconds         Das Intervall das gewartet werden soll bis der Ordner wieder nach Dateien durchsucht wird.
     * @param  directory            Der Name des Dateisystemordners der durchsucht werden soll.
     * @param  filters              Ein Feld mit {@linkplain java.lang.String Zeichenkette} die als
     *                              {@linkplain java.util.regex.Pattern Filter} zur Suche nach Dateinamen verwendet werden sollen.
     * @throws InterruptedException Falls der aktuelle {@linkplain java.lang.Thread Thread} von Au&szlig;en unterbrochen wurde.
     * @return Der {@linkplain java.lang.String Name} der ersten gefundenen Datei.
     *         Der Name ist {@linkplain java.io.File#getCanonicalPath() kanonisch} oder
     *         {@linkplain java.io.File#getAbsolutePath() absolut}, je nachdem ob die kanonische Namensbildung eine
     *         {@linkplain java.io.IOException Ausnahme} wirft oder nicht.
     */
    
    public static String waitForFile (int milliseconds, String directory, String [] filters) throws InterruptedException {
    	return (waitForFile (milliseconds, directory, filters, null));
    }
    
    /**
     * <p>
     * Die Hauptmethode akzeptiert {@linkplain java.lang.String Zeichenketten} und eine oder keine {@linkplain java.lang.Integer Ganzzahl}.
     * Die erste gefundene Zeichenkette wird als Verweis auf einen Ordner gewertet, alle weiteren als
     * {@linkplain java.util.regex.Pattern regul&auml;re Filterausdr&uuml;cke}.
     * </p>
     * <p>
     * Die Methode durchsucht den angegebenen Ordner auf Dateien die einem oder mehreren Filterausdr&uuml;cken entsprechen und
     * zeigt sie an.
     * Sofern eine Ganzzahl &uuml;bergeben wurde, wartet die Methode, in Intervallen entsprechend deren Wert, bis mindestens eine Datei
     * im angegebenen Ordner zu finden ist, die einem oder mehreren Filterausdr&uuml;cken entspricht und beendet sich
     * anschlie&szlig;end.
     * </p>
     * Sofern keine Werte f�r den Ordner oder ein Filterkriterium angegeben werden, wird von: <q>.</q> und: <q>^.*$</q> ausgegangen.
     * <p>
     * </p>
     * <p>
     * Die Methode dient ausschlie�lich Testzwecken.
     * </p>
     */

    public static void main (String args []) {
        int             i;
        Integer         latency =       null;
        String          directory =     null;
        String []       files;
        List <String>   filters =       new ArrayList <String> ();

        // Turn tracing on?
        for (i = 0; i < args.length; i++) {
            if (latency == null)
                try {
                    latency = Integer.decode (args [i]);
                    continue;
                }
                catch (NumberFormatException nfe) {
                    // Obviously no valid value for an integer; treat it as a file entry.
                }
            if (directory == null)
                directory = args [i];
            else
                filters.add (args [i]);
        }
        if (directory == null)
            directory = ".";
        if (filters.size () <= 0)
            filters.add (DEFAULTFILTER);
        if (latency == null) {
            files = DirectoryHelper.list (directory, filters.toArray (new String [] {}));
            System.out.println ("found " + files.length + " files matching your criteria.");
            for (i = 0; i < files.length; i++)
                System.out.println (i + "\t\"" + files [i] + "\"");
            files = DirectoryHelper.list (directory, filters.toArray (new String [] {}),
                                          FileComparator.SORTEDBY_FILENAME | FileComparator.SORTEDBY_DESCENDING);
            System.out.println ("found " + files.length + " files matching your criteria.");
            for (i = 0; i < files.length; i++)
                System.out.println (i + "\t\"" + files [i] + "\"");
            files = DirectoryHelper.list (directory, filters.toArray (new String [] {}), FileComparator.SORTEDBY_MODIFICATIONDATE);
            System.out.println ("found " + files.length + " files matching your criteria.");
            for (i = 0; i < files.length; i++)
                System.out.println (i + "\t\"" + files [i] + "\"");
        }
        else
            try {
                System.out.println ("Waited for: \"" +
                                    DirectoryHelper.waitForFile (latency.intValue (), directory,
                                                                 filters.toArray (new String [] {})) + "\".");
            }
            catch (InterruptedException ie) {
                ie.printStackTrace ();
            }
    }
}
