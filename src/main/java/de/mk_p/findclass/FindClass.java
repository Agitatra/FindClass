package de.mk_p.findclass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

/**
 * <p>
 * Die Klasse sucht nach einer oder mehreren Klassen in einem oder mehreren Java Archiven
 * in einem Wurzelverzeichnis und allen Unterverzeichenissen und zeigt die Java Archive an, die
 * die Klassen enthalten.
 * </p>
 */

public class FindClass {
    public static final String  DEFAULTJARFILTER =  "^.*\\.[JjWwEe][AaJj][RrBb]$";

    public static boolean isClassFilename (String filename) {
        return (filename.toLowerCase ().endsWith (".class"));
    }

    /**
     * <p>
     * Die Methode sucht nach Klassen in Java Archiven.
     * Sie akzeptiert die folgenden Aufrufparameter:
     * <dl>
     * <dt>-jarfilter</dt>
     * <dd>
     * Alle nachfolgenden Argumente, die keine Schalter sind, werden als
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} betrachtet.
     * Diese werden verwendet um die zu durchsuchenden Java Archive einzuschr&auml;nken.
     * Es k&ouml;nnen beliebig viele Filterausdr&uuml;cke angegeben werden.<br />
     * <strong>Beachte</strong>: Bei den
     * {@linkplain java.util.regex.Pattern regul&auml;ren Ausdr&uuml;cken}
     * handelt es sich nicht um die &uuml;blichen: <q>Wildcards</q>
     * (z.B. <q>?</q> und <q>*</q>) sondern um die entsprechenden Java-Ausdr&uuml;cke.
     * Anstatt <q>?</q> ist also: <q>.</q> zu schreiben, <q>*</q> wird durch: <q>.*</q>
     * spezifiziert und das Trennzeichen f�r ein Datei-Suffix muss als: <q>\.</q> angegeben
     * werden.
     * Au&szlig;erdem muss - eventuell durch Maskieren - sichergestellt werden, dass alle
     * relevanten Zeichen auch in den Filterausdr&uuml;cken ankommen, beispielsweise ist in
     * einigen Situationen: <q>\\.</q> anstatt: <q>\.</q> zu schreiben.
     * </dd>
     * <dt>-packagefilter</dt>
     * <dd>
     * Alle nachfolgenden Argumente, die keine Schalter sind, werden als
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} betrachtet.
     * Diese werden verwendet um die Klassenpakete zu definieren nach denen die Java Archive 
     * durchsucht werden sollen.
     * Es k&ouml;nnen beliebig viele Filterausdr&uuml;cke angegeben werden.<br />
     * <strong>Beachte</strong>: Paketfilterangaben werden nicht mit Klassenfilterangaben (<q>-classfilter</q>)
     * <q>geodert</q>, sie beschr&auml;nken also nicht die zu suchenden Klassen sondern geben zus&auml;tliche
     * Klassen aus.<br />
     * <strong>Beachte</strong>: F&uuml;r das Schreiben der
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke}
     * gelten die gleichen Bedingungen die f&uuml;r die mit: <q>-jarfilter</q>
     * spezifizierten Suchfilter.
     * </dd>
     * <dt>-classfilter</dt>
     * <dd>
     * Alle nachfolgenden Argumente, die keine Schalter sind, werden als
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke} betrachtet.
     * Diese werden verwendet um die Klassen zu definieren nach denen die Java Archive 
     * durchsucht werden sollen.
     * Es k&ouml;nnen beliebig viele Filterausdr&uuml;cke angegeben werden.<br />
     * <strong>Beachte</strong>: F&uuml;r das Schreiben der
     * {@linkplain java.util.regex.Pattern regul&auml;re Ausdr&uuml;cke}
     * gelten die gleichen Bedingungen die f&uuml;r die mit: <q>-jarfilter</q>
     * spezifizierten Suchfilter.
     * </dd>
     * <dt>-verbose</dt>
     * <dd>
     * Es werden w&auml;hrend der Suche Zusatzinformationen ausgegeben.
     * </dd>
     * <dt>Andere Ausdr&uuml;cke</dt>
     * <dd>
     * Andere Ausdr&uuml;cke werden je nach Position unterschiedlich zugeordnet.
     * <ol>
     * <li>
     * Wenn noch kein Schalter: <q>-jarfilter</q>, <q>-packagefilter</q> oder: <q>-classfilter</q>
     * spezifiziert wurde, wird der erste Ausdruck als Wurzelverzeichnis angesehen,
     * welches zusammen mit allen Unterverzeichnissen, nach den angegebenen Java-Archiven
     * durchsucht werden soll.
     * Dies gilt nur, wenn der Ausdruck nicht das letzte Argument ist und noch kein
     * Klassenfilter definiert wurde.
     * In diesem Fall wird der Ausdruck als Filter f&uuml;r Klassennamen betrachtet.
     * Als Wurzelverzeichnis wird in diesem Fall das aktuelle verwendet.
     * </li>
     * <li>
     * Wenn ein Schalter: <q>-jarfilter</q> angegeben wurde, werden alle nachfolgenden 
     * Argumente, die keine Schalter sind, als Filter f�r die Suche nach Java Archiven
     * betrachtet.
     * </li>
     * <li>
     * Wenn ein Schalter: <q>-classfilter</q> angegeben wurde, werden alle nachfolgenden 
     * Argumente, die keine Schalter sind, als Filter f�r die Suche nach Klassenamen in
     * Java-Archiven betrachtet.
     * </li>
     * </ol>
     * </dd>
     * </dl>
     * </p>
     * <h3>
     * Beispiele
     * </h3>
     * <p>
     * Das folgende Beispiel sucht nach der Klasse: <q>String</q> in allen Java-Archiven im aktuellen Verzeichnis und darunter.
     * <ul><li><samp>
     * java FindClass String
     * </samp></li></ul>
     * </p>
     * <p>
     * Das folgende Beispiel sucht nach allen Klassen aus dem Paket: <q>java.io</q> in allen Java-Archiven mit Namen: <q>palle.jar</q>,
     * <q>pille.jar</q> und <q>pulle.jar</q> im aktuellen Verzeichnis und darunter.  Das Beispiel geht davon aus, dass der Aufruf von einer
     * Unix-Shell aus stattfindet und daher <q>Backslashes</q> doppelt angegeben werden m&uuml;ssen.
     * <ul><li><samp>
     * java FindClass -jarfilter "p[aiu]lle&#92;&#92;.jar" -packagefilter "java.io"
     * </samp></li></ul>
     * </p>
     * <p>
     * Das folgende Beispiel sucht nach allen Klassen dem Paket: <q>java.io</q> und der Klasse <q>java.lang.String</q>in allen
     * Java-Archiven mit Namen: <q>palle.jar</q>, <q>pille.jar</q>, <q>pulle.jar</q>, <q>bla.jar</q>, <q>blub.jar</q> und
     * <q>blubber.jar</q> im Verzeichnis: <q>/usr/java/lib</q> und darunter.
     * Die Ausf&uuml;hrung gibt zus&auml;tzliche Informationen aus.
     * Das Beispiel geht davon aus, dass der Aufruf von einer
     * Unix-Shell aus stattfindet und daher <q>Backslashes</q> doppelt angegeben werden m&uuml;ssen.
     * <ul><li><samp>
     * java FindClass /usr/java/lib -verbose -jarfilter "p[aiu]lle&#92;&#92;.jar" "bl(a|ub|ubber)&#92;&#92;.jar"
     *                         -packagefilter "^java.io" -classfilter "java.lang.String"
     * </samp></li></ul>
     * </p>
     */
    public static void main (String [] args) throws IOException {
        int             i;
        int             j;
        boolean         jarFilter =         false;
        boolean         classFilter =       false;
        boolean         packageFilter =     false;
        boolean         verbose =           false;
        String          directory =         null;
        String []       classes;
        String []       entries;
        String []       archives;
        String []       classFilterArray;
        List <String>   jarFilters =        new ArrayList <> ();
        List <String>   classFiles =        new ArrayList <> ();
        List <String>   classFilters =      new ArrayList <> ();
        List <String>   packageFilters =    new ArrayList <> ();
        ZipHelper       zipHelper;
        FindClass       finder =            new FindClass ();
        PomHelper       pom;

        for (i = 0; i < args.length; i++) {
            if ("-jarfilter".startsWith (args [i].toLowerCase ()))
                packageFilter = classFilter = !(jarFilter = true);
            else if ("-packagefilter".startsWith (args [i].toLowerCase ()))
                jarFilter = classFilter = !(packageFilter = true);
            else if ("-classfilter".startsWith (args [i].toLowerCase ()))
                packageFilter = jarFilter = !(classFilter = true);
            else if ("-verbose".startsWith (args [i].toLowerCase ()))
                verbose = true;
            else if (!jarFilter && !classFilter && (i < (args.length - 1)) && (directory == null))
                directory = args [i];
            else if (packageFilter)
                packageFilters.add ("^.*" + args [i] + ".*\\.[Cc][Ll][Aa][Ss][Ss]$");
            else if (classFilter || (i >= (args.length - 1)))
                classFilters.add ("^.*" + (isClassFilename(args [i])
                        ? args [i] : args [i] + "\\.[Cc][Ll][Aa][Ss][Ss]") + "$");
            else
                jarFilters.add (args [i]);
        }
        classFilters.addAll (packageFilters);
        classFiles.addAll (classFilters);
        if (classFilters.size () <= 0)
            System.out.println ("usage: java " + finder.getClass ().getName () +
                    "[directory] [[-jarfilter ]jar-filter...] [[-classfilter ]classFilter...] classfilter");
        else {
            if (directory == null)
                directory = ".";
            if (jarFilters.size () <= 0)
                jarFilters.add  (FindClass.DEFAULTJARFILTER);
            if (verbose) {
                System.out.println ("looking for class:");
                for (i = 0; i < classFilters.size (); i++)
                    System.out.println ("\t\t" + classFilters.get (i));
                System.out.println ("\tin JAR:");
                for (i = 0; i < jarFilters.size (); i++)
                    System.out.println ("\t\t" + jarFilters.get (i));
                System.out.println ("\tin directory: " + directory);
            }
            archives = DirectoryHelper.list (directory, jarFilters.toArray (new String [] {}), DirectoryHelper.RECURSE_DIRECTORIES);
            classFilterArray = classFilters.toArray (new String [] {});
            for (i = 0; i < archives.length; i++) {
                if (verbose)
                    System.out.println (i + "\t\"" + archives [i] + "\"");
                zipHelper = new ZipHelper (archives [i]);
                try {
                    entries = zipHelper.getNames (classFilterArray, ZipHelper.WITHOUT_DIRECTORIES);
                    if (entries.length > 0) {
                        System.out.print (archives [i]);
                        pom = new PomHelper (zipHelper,archives[i]);
                        if (pom != null)
                            System.out.print ("; Group: " +
                                    ((pom.getGroupId () != null) ? pom.getGroupId () : "[inherited]" ) +
                                    ", Artifact: " + pom.getArtifactId () +
                                    ", Version: " + pom.getVersion () + ".");
                        System.out.println ();
                        for (j = 0; j < entries.length; j++)
                            System.out.println ("\t[" + j + "]:\t\"" + entries[j] + "\"");
                    }
                }
                catch (FileNotFoundException fnfe) {
                    System.out.println ("File: \"" + archives [i] + "\" does not exist, ignored");
                }
                catch (ZipException ze) {
                    // O.K. "archives [i]" calls itself a JAR, but isn't.  Naughty little bugger, but we don't have to care at this point.
                }
            }
            classes = DirectoryHelper.list (directory, classFiles.toArray (new String [] {}), DirectoryHelper.RECURSE_DIRECTORIES);
            for (i = 0; i < classes.length; i++) {
                if (verbose)
                    System.out.println (i + "\t\"" + classes [i] + "\"");
                System.out.println (classes [i]);
            }
        }
    }

}
