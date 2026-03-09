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
		GestionPersonnel gestionPersonnel = new GestionPersonnel();
		String requete = "select * from ligue";

		try (Statement instruction = connection.createStatement();
			 ResultSet ligues = instruction.executeQuery(requete))
		{
			while (ligues.next())
				gestionPersonnel.addLigue(ligues.getInt(1), ligues.getString(2));
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}

		return gestionPersonnel;
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
