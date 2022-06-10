package de.mk_p.findclass;

/**
 * <p>
 * Ein Interface zur Einflu&szlig;nahme auf die Arbeit der Methoden der Klasse
 * {@linkplain com.vdek.dafin.utils.io.file.DirectoryHelper DirectoryHelper}.
 * </p>
 *
 * @author Mark.Kahl.extern@vdek.com
 *
 */

public interface IOHelperCallback {
	/**
	 * <p>
	 * Die Methode bestimmt ob die sie rufende Methode beendet werden soll.
	 * </p>
	 *
	 * @return <q>true</q> wenn die aufrufende Methode beendet werden soll,
	 *         <q>false</q> wenn nicht.
	 */
	public boolean terminate ();
}
