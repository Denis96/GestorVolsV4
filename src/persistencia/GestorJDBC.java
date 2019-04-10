package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import model.Companyia;
import principal.GestioVolsExcepcio;

/**
 *
 * @author root
 */
public class GestorJDBC implements ProveedorPersistencia {
    private Companyia companyia;

    private Connection conn; //Connexió a la base de dades

    public Companyia getCompanyia() {
        return companyia;
    }

    public void setCompanyia(Companyia companyia) {
        this.companyia = companyia;
    }

    /*
     *TODO
     * 
     *Paràmetres: cap
     *
     *Acció:
     * - Heu d'establir la connexio JDBC amb la base de dades GestorVols
     * - Heu de fer el catch de les possibles excepcions SQL mostrant el missatge
     *   de l'excepció capturada mitjançant getMessage().
     * - Si es produeix una excepció a l'establir la connexió, assignareu el 
     *   valor null a l'atribut conn.
     *
     *Retorn: cap
     *
     */
    public void estableixConnexio() throws SQLException {
        String baseDades = "gestiovols";
        String urlBaseDades = "jdbc:mysql://localhost:3306/"+baseDades;
        String usuari = "root";
        String contrasenya =null;
        
        try{
            //Carreguem el controlador MySQL. Class.forName retorna un objecte
            //associat al paràmetre, en el nostre cas un controlador per mysql
            Class.forName("com.mysql.jdbc.Driver");
            //Connectem amb la base de dades
            conn = DriverManager.getConnection(urlBaseDades,usuari,contrasenya);
            System.out.println("Ens hem connectat");
        }catch (ClassNotFoundException e1){
           //Error si no es pot llegir el controlador 
           System.out.println("ERROR: no s'ha trobat el controlador de la BD: "+e1.getMessage());
        }catch (SQLException e2) {
           //Error SQL: de usuari o contrasenya
           System.out.println("ERROR: SQL ha fallat: "+e2.getMessage());
	}
    }

   /*
     * Heu de tancar la connexió i assignar-li el valor null a l'atribut conn, es 
     * produeixi o no una excepció.
     *
     */
    public void tancaConnexio() throws SQLException {
        if (conn!=null){ //Si existeix la connexió....
            conn.close(); //Tanquem la connexió
        }
    }

    /*
     *TODO
     * 
     *Paràmetres: el nom del fitxer i la companyia a desar
     *
     *Acció:
     * Heu de desar la companyia passada com a paràmetre en la base de dades:
     *   - S'ha de desar en la taula companyes (nomFitxer és el codi de la companyia)
     *   - Cada avió de la companyia, s'ha de desar com registre de la taula avions
     *   - Heu de tenir en compte que si la companyia ja existeix a la base de 
     *     dades, aleshores heu de fer el següent:
     *        - Actualitzar la companyia ja existent
     *        - Eliminar totes els avions d'aquesta companyia de la taula avions i 
     *          després insertar els avions de la companyia.
     *   - Si al fer qualsevol operació es produeix una excepció, llavors heu de 
     *     llançar l'excepció GestioVolsExcepcio amb codi "GestorJDBC.desar"
     *
     *Retorn: cap
     *
     */
    @Override
    public void desarDades(String nomFitxer, Companyia companyia) throws GestioVolsExcepcio {
        int codi = Integer.parseInt(nomFitxer);
        
        String queryInsertCompanyia = "INSERT INTO COMPANYIES "+"(CODI, NOM) VALUES "+"(?,?)";
        String queryComprovarCompanyia = "SELECT * FROM COMPANYIES WHERE CODI = "+companyia.getCodi();
        String queryUpdateCompanyia = "UPDATE COMPANYIES SET CODI = ? WHERE CODI = "+companyia.getCodi();
        
        
        String queryInsertAvions = "INSERT INTO AVIONS "+"(CODI, FABRICANT, MODEL, CAPACITAT, CODICOMPANYIA) VALUES "+"(?,?,?,?,?,?)";
        String queryDeleteAvions = "DELETE FROM AVIONS WHERE CODICOMPANYIA = "+companyia.getCodi();
        String queryInsertAvionsAfterDelete = "INSERT INTO AVIONS "+"(CODI, FABRICANT, MODEL, CAPACITAT, CODICOMPANYIA) VALUES "+"(?,?,?,?,?,?)";
        //companyia.getComponents().
        
        
        
        
        try {
            PreparedStatement comprovarCompanyia = conn.prepareStatement(queryComprovarCompanyia);
            comprovarCompanyia.execute();
            if (comprovarCompanyia != null){
                PreparedStatement updateCompanyia = conn.prepareStatement(queryUpdateCompanyia);
                updateCompanyia.setInt(1, codi);

                // Borra los aviones
                PreparedStatement deleteAvions = conn.prepareStatement(queryDeleteAvions);
                deleteAvions.execute();


            }
                    
            PreparedStatement preparedStatement = conn.prepareStatement(queryInsertCompanyia);

            preparedStatement.setInt(1, codi);
            preparedStatement.setString(2, companyia.getNom());


            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            System.out.println("Record is inserted into COMPANYIES table!");

            } catch (SQLException e) {

                System.out.println(e.getMessage());

        }
    }

    /*
     *TODO
     * 
     *Paràmetres: el nom del fitxer de la companyia
     *
     *Acció:
     * Heu de carregar la companyia des de la base de dades (nomFitxer és el codi 
     * de la companyia). Per fer això, heu de:
     *   - Cercar el registre companyia de la taula companyies amb codi = nomFitxer. 
     *   - Heu d'afegir els avions al vector de components de la companyia a partir de 
     *     la taula avions.
     *   - Si al fer qualsevol operació es produeix una excepció, llavors heu de 
     *     llançar l'excepció GestioVolsExcepcio amb codi "GestorJDBC.carrega"
     *   - Si el nomFitxer donat no existeix a la taula companyies (és a dir, el 
     *     codi = nomFitxer no existeix), aleshores heu de llançar l'excepció 
     *     GestioVolsExcepcio amb codi "GestorJDBC.noexist"
     *
     *Retorn: cap
     *
     */
    @Override
    public Companyia carregarDades(String nomFitxer) throws ParseException, GestioVolsExcepcio {
         int codi = Integer.parseInt(nomFitxer);
         
         String queryComprovarCompanyia = "SELECT * FROM COMPANYIES WHERE CODI = "+codi;
         
         try {
             PreparedStatement comprovarCompanyia = conn.prepareStatement(queryComprovarCompanyia);
             comprovarCompanyia.execute();
             
         } catch (SQLException e) {
             
         }
         
    
    }
}