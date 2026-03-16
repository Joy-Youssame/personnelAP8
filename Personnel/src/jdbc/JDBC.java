package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.Ligue;
import personnel.Passerelle;
import personnel.SauvegardeImpossible;

public class JDBC implements Passerelle
{
	private Connection connection;

	public JDBC()
	{
		try
		{
			Class.forName(Credentials.getDriverClassName());
			connection = DriverManager.getConnection(
					Credentials.getUrl(),
					Credentials.getUser(),
					Credentials.getPassword()
			);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Pilote JDBC non installé.");
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}

	@Override
	public GestionPersonnel getGestionPersonnel()
	{
	    GestionPersonnel gp = new GestionPersonnel();

	    try
	    {
	        Connection cnx = getConnection();
	        Statement st = cnx.createStatement();

	        ResultSet rs = st.executeQuery("SELECT * FROM employe WHERE nom='root'");

	        if(rs.next())
	        {
	            int id = rs.getInt("id");
	            String nom = rs.getString("nom");
	            String prenom = rs.getString("prenom");
	            String mail = rs.getString("mail");
	            String password = rs.getString("password");

	            gp.addRoot(id, nom, prenom, mail, password);
	        }

	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }

	    return gp;
	}

	@Override
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible 
	{
		close();
	}
	
	public void close() throws SauvegardeImpossible
	{
		try
		{
			if (connection != null && !connection.isClosed())
				connection.close();
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public int insert(Ligue ligue) throws SauvegardeImpossible
	{
		String requete = "insert into ligue (nom) values(?)";

		try (PreparedStatement instruction = connection.prepareStatement(
				requete, Statement.RETURN_GENERATED_KEYS))
		{
			instruction.setString(1, ligue.getNom());
			instruction.executeUpdate();

			try (ResultSet id = instruction.getGeneratedKeys())
			{
				if (id.next())
					return id.getInt(1);
				throw new SQLException("Aucun identifiant généré.");
			}
		}
		catch (SQLException exception)
		{
			throw new SauvegardeImpossible(exception);
		}
	}

	@Override
	public int insert(Employe employe) throws SauvegardeImpossible
	{
		String requete = """
				insert into employe (nom, prenom, mail, password, dateEmbauche, dateFinContrat, idLigue)
				values (?, ?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement instruction = connection.prepareStatement(
				requete, Statement.RETURN_GENERATED_KEYS))
		{
			instruction.setString(1, employe.getNom());
			instruction.setString(2, employe.getPrenom());
			instruction.setString(3, employe.getMail());
			instruction.setString(4, employe.getPassword());
			instruction.setDate(5, employe.getDateEmbauche() != null ? java.sql.Date.valueOf(employe.getDateEmbauche()) : null);
			instruction.setDate(6, employe.getDateFinContrat() != null ? java.sql.Date.valueOf(employe.getDateFinContrat()) : null);
			instruction.setInt(7, employe.getLigue().getId());

			instruction.executeUpdate();

			try (ResultSet id = instruction.getGeneratedKeys())
			{
				if (id.next())
					return id.getInt(1);
				throw new SQLException("Aucun identifiant généré pour l'employé.");
			}
		}
		catch (SQLException exception)
		{
			throw new SauvegardeImpossible(exception);
		}
	}

	@Override
	public void delete(Employe employe) throws SauvegardeImpossible
	{
		String requete = "delete from employe where id = ?";

		try (PreparedStatement instruction = connection.prepareStatement(requete))
		{
			instruction.setInt(1, employe.getId());
			instruction.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}

	@Override
	public void update(Ligue ligue) throws SauvegardeImpossible
	{
		String requete = "update ligue set nom = ? where id = ?";

		try (PreparedStatement instruction = connection.prepareStatement(requete))
		{
			instruction.setString(1, ligue.getNom());
			instruction.setInt(2, ligue.getId());
			instruction.executeUpdate();
		}
		catch (SQLException exception)
		{
			throw new SauvegardeImpossible(exception);
		}
	}
	
	public void update(Employe employe) throws SauvegardeImpossible
	{
	    try
	    {
	        Connection cnx = getConnection();

	        PreparedStatement ps = cnx.prepareStatement(
	            "UPDATE employe SET nom=?, prenom=?, mail=?, password=? WHERE id=?"
	        );

	        ps.setString(1, employe.getNom());
	        ps.setString(2, employe.getPrenom());
	        ps.setString(3, employe.getMail());
	        ps.setString(4, employe.getPassword());
	        ps.setInt(5, employe.getId());

	        ps.executeUpdate();
	    }
	    catch (SQLException e)
	    {
	        throw new SauvegardeImpossible("Modification employé impossible", e);
	    }
	}

	@Override
	public void delete(Ligue ligue) throws SauvegardeImpossible
	{
		String deleteEmployes = "delete from employe where idLigue = ?";
		String deleteLigue = "delete from ligue where id = ?";

		try
		{
			connection.setAutoCommit(false);

			try (PreparedStatement instructionEmployes = connection.prepareStatement(deleteEmployes);
				 PreparedStatement instructionLigue = connection.prepareStatement(deleteLigue))
			{
				instructionEmployes.setInt(1, ligue.getId());
				instructionEmployes.executeUpdate();

				instructionLigue.setInt(1, ligue.getId());
				instructionLigue.executeUpdate();

				connection.commit();
			}
			catch (SQLException e)
			{
				connection.rollback();
				throw e;
			}
			finally
			{
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}
}
