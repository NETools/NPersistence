// BismIllah
package org.ns.test;

import org.ns.npersistence.NPersistenceSession;
import org.ns.npersistence.NPersistenceSession.RetrieveMode;
import org.ns.npersistence.NEntityUpdateHandlerPolicy;
import org.ns.npersistence.NDebugOutputHandler;
import org.ns.test.blob.KlasseA;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class Program {

    public static void main(String[] args) throws Exception {
        NDebugOutputHandler.getDefault().setDebugOutputMode(NDebugOutputHandler.DebugOutputMode.Console);
        NPersistenceSession.setRootFolder("databases");

        NEntityUpdateHandlerPolicy.setForceDeleteTable(true);
        NEntityUpdateHandlerPolicy.setAutoSync(true);

        NPersistenceSession persistence = NPersistenceSession.getDefault();
        persistence.openSession(NPersistenceSession.SQLDriver.SQLite,
                "",
                "test_database_SQLITE.db",
                "",
                "");
        
        /*
         * Beispielhafte Persistierung eines Java-Objekts:
         * 
         *         
         *         Buch buch = new Buch();
         *         buch.setTitel("Buch");
         *         buch.setAuthor("Test");
         *         buch.setIsbn("0-0000-000-0");
         *         
         *         Nutzer maxMustermann = new Nutzer();
         *         maxMustermann.setName("Max");
         *         maxMustermann.AddBuch(buch);
         *         
         *         persistence.persistObject(maxMustermann);
         */
        
        
        var nsql = "SELECT %new FROM $org.ns.test.Nutzer :a WHERE !a.buecher.titel = 'Buch';";
        var resultSet = persistence.getByNSQL(nsql, RetrieveMode.Attached);
        
        var user = resultSet.get(Nutzer.class, 0);
        
        System.out.println("Nutzername: " + user.getName() + " verfügt über das Buch mit dem Titel 'Buch'!");
        
        // Das Objekt "user" ist dabei "Attached", sämtliche Änderungen auf diesem Objekt werden mit der Datenbank direkt synchronisiert!
        
        user.setName("Maxi"); // ohne das wir das explizit persistieren müssen, ist die Änderung in der Datenbank übernommen worden.
        
        
        persistence.closeSession();
    }
}