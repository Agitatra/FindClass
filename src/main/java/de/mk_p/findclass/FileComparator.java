package de.mk_p.findclass;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * <p>
 * Eine Klasse zum Vergleichen von Dateien.
 * </p>
 * <p>
 * Ihre Hauptaufgabe ist das zur Verf&uuml;gungstellen von {@linkplain java.util.Comparator Vergleichsoperatoren}
 * f&uuml;r {@linkplain java.util.Arrays#sort(T[], java.util.Comparator) Sortierungen}.
 * </p>
 *
 * @author Mark.Kahl.extern@vdek.com
 *
 */


public class FileComparator implements Comparator <String> {

    /**
     * <p>
     * Eine Option die definiert, dass eine zur&uuml;ckgegebene Werte nach dem Zeitpunkt ihrer letzten Ver&auml;nderung
     * sortiert werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Optionen
     * {@linkplain #SORTEDBY_FILENAME} und {@linkplain #SORTEDBY_PATHNAME} aus.
     * </p>
     */
    public static final long   SORTEDBY_MODIFICATIONDATE =  0x10l;

    /**
     * <p>
     * Eine Option die definiert, dass eine zur&uuml;ckgegebene Dateinamen alphabetisch nach ihrem Namensteil
     * (ohne Ber&uuml;cksichtigung von Gro&szlig;-/Kleinschreibung) sortiert werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Optionen
     * {@linkplain #SORTEDBY_MODIFICATIONDATE} und {@linkplain #SORTEDBY_PATHNAME} aus.
     * </p>
     */
    public static final long   SORTEDBY_FILENAME =          0x20l;

    /**
     * <p>
     * Eine Option die definiert, dass eine zur&uuml;ckgegebene Dateinamen alphabetisch nach ihrem Namensteil
     * (ohne Ber&uuml;cksichtigung von Gro&szlig;-/Kleinschreibung) sortiert werden sollen.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option schlie&szlig;t die gleichzeitige Verwendung der Optionen
     * {@linkplain #SORTEDBY_MODIFICATIONDATE} und {@linkplain #SORTEDBY_FILENAME} aus.
     * </p>
     */
    public static final long   SORTEDBY_PATHNAME =          0x40l;

    /**
     * <p>
     * Eine Option die definiert, dass eine Sortierung absteigend vorgenommen werden soll.
     * </p>
     * <p>
     * Beachte: Die Verwendung dieser Option erfordert die gleichzeitige Verwendung einer der Optionen
     * {@linkplain #SORTEDBY_MODIFICATIONDATE}, {@linkplain #SORTEDBY_PATHNAME} oder
     * {@linkplain #SORTEDBY_FILENAME}.
     * </p>
     */
    public static final long   SORTEDBY_DESCENDING =        0x80l;

    private long               options;

    /**
     * <p>
     * Der Konstruktor initialisiert die {@linkplain java.util.Comparator Vergleichsklasse} um nachfolgende
     * Operationen korrekt durchf&uuml;hren zu k&ouml;nnen.
     * </p>
     *
     * @param  options  Genau ein Wert aus:
     *                  {@linkplain #SORTEDBY_MODIFICATIONDATE}, {@linkplain #SORTEDBY_PATHNAME}, oder
     *                  {@linkplain #SORTEDBY_PATHNAME} eventuell geodert (<q>|</q>) mit:
     *                  {@linkplain #SORTEDBY_DESCENDING}.
     */
    public FileComparator (long options) {
        this.options = options;
    }

    private int compareModificationTime (String name1, String name2) {
        long time1 = new File (name1).lastModified ();
        long time2 = new File (name2).lastModified ();

        if ((options & SORTEDBY_DESCENDING) != 0)
            return ((time1 > time2) ? -1 : (time1 < time2) ? 1 : 0);
        else
            return ((time1 < time2) ? -1 : (time1 > time2) ? 1 : 0);
    }

    private int compareName (String name1, String name2) {
        File file1 =    new File (name1);
        File file2 =    new File (name2);
        int  retVal =   file1.getName ().compareToIgnoreCase (file2.getName ());

        if ((options & SORTEDBY_DESCENDING) != 0)
            return ((retVal < 0) ? 1 : (retVal > 0) ? -1 : 0);
        else
            return (retVal);
    }

    private int comparePath (String name1, String name2) {
        File file1 = new File (name1);
        File file2 = new File (name2);
        int  retVal =   file1.getName ().compareToIgnoreCase (file2.getName ());

        try {
            retVal = file1.getCanonicalPath ().compareToIgnoreCase (file2.getCanonicalPath ());
        }
        catch (IOException ioe) {
            retVal = file1.getAbsolutePath ().compareToIgnoreCase (file2.getAbsolutePath ());
        }
        if ((options & SORTEDBY_DESCENDING) != 0)
            return ((retVal < 0) ? 1 : (retVal > 0) ? -1 : 0);
        else
            return (retVal);
    }

    /**
     * <p>
     * Die Methode vergleicht zwei {@linkplain java.lang.String Zeichenketten} die Dateien aus dem
     * unterliegenden Dateisystem repr&auml;sentieren m&uuml;ssen.
     * </p>
     * <p>
     * Die Art des Vergleichs wird durch die Optionen an den vorhergegangenen Aufruf des
     * {@linkplain #FileComparator Konstruktors} bestimmt.
     * </p>
     *
     * @param  name1    Der Name der ersten Datei die mit der zweiten, gem&auml;&szlig; der eingestellten
     *                  Optionen, verglichen werden soll.
     * @param  name2    Der Name der zweiten Datei die mit der ersten, gem&auml;&szlig; der eingestellten
     *                  Optionen, verglichen werden soll.
     * @return <q>-1</q> sofern die erste Datei, gem&auml;&szlig; der eingestellten Optionen, kleiner als
     *         die zweite ist, <q>1</q> wenn sie gr&ouml;&szlig;er und: <q>0</q> wenn sie identisch ist.
     */
    public int compare (String name1, String name2) {
        if ((options & SORTEDBY_FILENAME) != 0l)
            return (compareName (name1, name2));
        else if ((options & SORTEDBY_PATHNAME) != 0l)
            return (comparePath (name1, name2));
        else
            return (compareModificationTime (name1, name2));
            
    }
}
