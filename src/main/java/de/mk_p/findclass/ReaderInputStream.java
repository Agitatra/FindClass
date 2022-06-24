package de.mk_p.findclass;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Konvertiert einen  {@linkplain java.io.Reader Reader} in einen {@linkplain java.io.InputStream InputStream}.
 * &Uuml;bernommen aus {@linkplain org.apache.tools.ant.util.ReaderInputStream ReaderInputStream}.
 *
 */
public class ReaderInputStream extends InputStream {

    private Reader  in;
    private String  encoding = System.getProperty ("file.encoding");
    private byte [] slack;
    private int     begin;

    /**
     * <p>
     * Der Konstruktor erzeugt eine Instanz mit dem Standard-Zeichenenkoding des aktuellen Systems.
     * </p>
     *
     * @param reader    Der {@linkplain java.io.Reader Reader} der als {@linkplain java.io.InputStream InputStream}
     *                  gelesen werden soll.
     */
    public ReaderInputStream (Reader reader) {
        in = reader;
    }

    /**
     * <p>
     * Der Konstruktor erzeugt eine Instanz mit einem speziellen Zeichenenkoding.
     * </p>
     *
     * @param reader    Der {@linkplain java.io.Reader Reader} der als {@linkplain java.io.InputStream InputStream}
     *                  gelesen werden soll.
     * @param encoding  Eine {@linkplain java.lang.String Zeichenkette} die das zu verwendende Enkoding definiert.
     */
    public ReaderInputStream (Reader reader, String encoding) {
        this (reader);

        if (encoding == null)
            throw new IllegalArgumentException ("encoding may not be null");
        else
            this.encoding = encoding;
    }

    /**
     * <p>
     * Die Methode {@linkplain java.io.Reader#read() liest} ein Zeichen aus der Quelle und gibt es zu&uuml;ck.
     * </p>
     *
     * @return      Das {@linkplain java.io.Reader#read() n&auml;chste} Zeichen.
     *
     * @throws      {@linkplain java.io.IOException IOException} falls beim Lesen aus der
     *              {@linkplain java.io.Reader Quelle} ein Problem auftritt.
     */
    public synchronized int read () throws IOException {
        byte    retval;
        byte [] buffer;
            
        if (in == null)
            throw new IOException ("Stream Closed");
        if ((slack != null) && (begin < slack.length)) {
            retval = slack [begin];
            if (++begin == slack.length)
                slack = null;
        }
        else {
            buffer = new byte [1];
            if (read (buffer, 0, 1) <= 0)
                retval = -1;
            retval = buffer [0];
        }
        if (retval < -1)
            retval += 0xFF;
        return (retval);
    }

    /**
     * <p>
     * Die Methode {@linkplain java.io.Reader#read(java.nio.CharBuffer) f&uuml;llt} ein <q>Byte-Feld</q> aus der Quelle.
     * </p>
     *
     * @param   buffer  Das <q>Byte-Feld</q> das gef&uuml;llt werden soll.
     * @param   offset  Die Position im <q>Byte-Feld</q> ab der das F&uuml;llen beginnen soll.
     * @param   length  Die Anzahl der Zeichen die in das <q>Byte-Feld</q> gelesen werden sollen.
     * @return  Die Anzahl der gelesenen Zeichen, <q>-1</q> falls das Ende der Quelle erreicht wurde.
     *
     * @throws  {@linkplain java.io.IOException IOException} falls beim Lesen aus der
     *          {@linkplain java.io.Reader Quelle} ein Problem auftritt.
     */

    public synchronized int read (byte [] buffer, int offset, int length) throws IOException {
        int     i;
        char [] buf;

        if (in == null)
            throw new IOException ("Stream Closed");
        while (slack == null) {
            buf = new char [length];    // might read too much
            i = in.read (buf);
            if (i == -1)
                return (-1);
            if (i > 0) {
                slack = new String (buf, 0, i).getBytes (encoding);
                begin = 0;
            }
        }
        if (length > slack.length - begin)
            length = slack.length - begin;
        System.arraycopy (slack, begin, buffer, offset, length);
        if ((begin += length) >= slack.length)
            slack = null;
        return (length);
    }


    /**
     * <p>
     * Setzt die aktuelle Position der {@linkplain java.io.Reader Quelle}.
     * Nachfolgende Aufrufe von {@linkplain #reset() reset} versuchen zu dieser Stelle zur&uuml;ckzukehren.
     * Nicht alle {@linkplain java.io.Reader Quellen} unterst&uuml;tzen diesen Aufruf.
     * </p>
     *
     * @param   limit   Die maximale Anzahl Zeichen die vorausgelesen werden darf, bevor die gesetzte
     *                  Markierung ung&uuml;ltig wird.
     *
     * @throws  {@linkplain java.io.IOException IOException} falls beim setzen der Markierung in der
     *          {@linkplain java.io.Reader Quelle} ein Problem auftritt.
     */

    public synchronized void mark (final int limit) {
        try {
            in.mark (limit);
        }
        catch (IOException ioe) {
            throw new RuntimeException (ioe);
        }
    }


    /**
     * <p>
     * Liefert die Anzahl der Zeichen die ohne einen neuen {@linkplain #read(byte[],int,int) Lesevorgang} aus der
     * {@linkplain java.io.Reader Quelle} zur Verf&uuml;gung stehen.
     * </p>
     *
     * @return  Die Anzal der verf&uuml;gbaren Zeichen.
     * @throws  {@linkplain java.io.IOException IOException} falls bei der Abfrage ein Problem auftritt.
     */

    public synchronized int available () throws IOException {
        if (in == null)
            throw new IOException ("Stream Closed");
        if (slack != null)
            return (slack.length - begin);
        else if (in.ready ())
            return (1);
        else
            return (0);
    }


    /**
     * <p>
     * &Uuml;berpr&uuml;ft ob die Funktionalit&auml;t der Methoden {@linkplain #mark(int) mark} und {@linkplain #reset() reset}
     * tats&auml;chlich unterst&uuml;tzt wird oder nicht.
     * </p>
     *
     * @return  <q>false</q>.  Die Funktionalit&auml;t wird nicht unterst&uuml;tzt.
     */

    public boolean markSupported () {
        return (false);     // would be imprecise
    }


    /**
     * <p>
     * Setzt die {@linkplain java.io.Reader Quelle} zur&uuml;ck.
     * </p>
     *
     * @throws  {@linkplain java.io.IOException IOException} falls beim Zur&uuml;cksetzen ein Problem auftritt.
     */

    public synchronized void reset () throws IOException {
        if (in == null)
            throw new IOException ("Stream Closed");
        slack = null;
        in.reset ();
    }


    /**
     * <p>
     * Schlie&szlig;t die {@linkplain java.io.Reader#close() Quelle}.
     * </p>
     *
     * @throws  {@linkplain java.io.IOException IOException} falls beim Schlie&szlig;en ein Problem auftritt.
     */

    public synchronized void close () throws IOException {
        if (in != null) {
            in.close ();
            slack =  null;
            in =     null;
        }
    }
}
