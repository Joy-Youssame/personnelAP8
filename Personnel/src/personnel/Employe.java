package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

/**
 * Employé d'une ligue hébergée par la M2L. Certains peuvent 
 * être administrateurs des employés de leur ligue.
 * Un seul employé, rattaché à aucune ligue, est le root.
 * Il est impossible d'instancier directement un employé, 
 * il faut passer la méthode {@link Ligue#addEmploye addEmploye}.
 */

public class Employe implements Serializable, Comparable<Employe>
{
	private static final long serialVersionUID = 4795721718037994734L;
	private String nom, prenom, password, mail;
	private Ligue ligue;
	private GestionPersonnel gestionPersonnel;
	private LocalDate dateEmbauche;
	private LocalDate dateFinContrat;
	
	Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password)
	{
		this(gestionPersonnel, ligue, nom, prenom, mail, password, LocalDate.now(), null);
	}
	
	Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, 
			LocalDate dateEmbauche, LocalDate dateFinContrat)
	{
		this.gestionPersonnel = gestionPersonnel;
		this.nom = nom;
		this.prenom = prenom;
		this.password = password;
		this.mail = mail;
		this.ligue = ligue;
		setDateEmbauche(dateEmbauche);
		setDateFinContrat(dateFinContrat);
		validerDatesContrat();
	}
	
	/**
	 * Valide la cohérence des dates de contrat
	 * @throws IllegalArgumentException si les dates sont incohérentes
	 */
	private void validerDatesContrat()
	{
		if (dateEmbauche != null && dateFinContrat != null && dateFinContrat.isBefore(dateEmbauche))
		{
			throw new IllegalArgumentException("La date de fin de contrat ne peut pas être avant la date d'embauche");
		}
		
		if (dateEmbauche != null && dateEmbauche.isAfter(LocalDate.now().plusDays(1)))
		{
			throw new IllegalArgumentException("La date d'embauche ne peut pas être dans le futur");
		}
	}
	/**
	 * Retourne vrai ssi l'employé est administrateur de la ligue 
	 * passée en paramètre.
	 * @return vrai ssi l'employé est administrateur de la ligue 
	 * passée en paramètre.
	 * @param ligue la ligue pour laquelle on souhaite vérifier si this 
	 * est l'admininstrateur.
	 */
	
	public boolean estAdmin(Ligue ligue)
	{
		return ligue.getAdministrateur() == this;
	}
	
	/**
	 * Retourne vrai ssi l'employé est le root.
	 * @return vrai ssi l'employé est le root.
	 */
	
	public boolean estRoot()
	{
		return gestionPersonnel.getRoot() == this;
	}
	
	/**
	 * Retourne le nom de l'employé.
	 * @return le nom de l'employé. 
	 */
	
	public String getNom()
	{
		return nom;
	}

	/**
	 * Change le nom de l'employé.
	 * @param nom le nouveau nom.
	 */
	
	public void setNom(String nom)
	{
		this.nom = nom;
	}

	/**
	 * Retourne le prénom de l'employé.
	 * @return le prénom de l'employé.
	 */
	
	public String getPrenom()
	{
		return prenom;
	}
	
	/**
	 * Change le prénom de l'employé.
	 * @param prenom le nouveau prénom de l'employé. 
	 */

	public void setPrenom(String prenom)
	{
		this.prenom = prenom;
	}

	/**
	 * Retourne le mail de l'employé.
	 * @return le mail de l'employé.
	 */
	
	public String getMail()
	{
		return mail;
	}
	
	/**
	 * Change le mail de l'employé.
	 * @param mail le nouveau mail de l'employé.
	 */

	public void setMail(String mail)
	{
		this.mail = mail;
	}

	/**
	 * Retourne vrai ssi le password passé en paramètre est bien celui
	 * de l'employé.
	 * @return vrai ssi le password passé en paramètre est bien celui
	 * de l'employé.
	 * @param password le password auquel comparer celui de l'employé.
	 */
	
	public boolean checkPassword(String password)
	{
		return this.password.equals(password);
	}

	/**
	 * Change le password de l'employé.
	 * @param password le nouveau password de l'employé. 
	 */
	
	public void setPassword(String password)
	{
		this.password= password;
	}

	
	
	public Ligue getLigue()
	{
		return ligue;
	}

	
	
	public LocalDate getDateEmbauche()
	{
		return dateEmbauche;
	}
	
	
	public void setDateEmbauche(LocalDate dateEmbauche)
	{
		if (dateEmbauche != null && dateEmbauche.isAfter(LocalDate.now().plusDays(1)))
		{
			throw new IllegalArgumentException("La date d'embauche ne peut pas être dans le futur");
		}
		this.dateEmbauche = dateEmbauche;
		validerDatesContrat();
	}
	
	
	public LocalDate getDateFinContrat()
	{
		return dateFinContrat;
	}
	
	
	public void setDateFinContrat(LocalDate dateFinContrat)
	{
		this.dateFinContrat = dateFinContrat;
		validerDatesContrat();
	}
	
	
	public int getAnciennete()
	{
		if (dateEmbauche == null)
			return 0;
		
		return Period.between(dateEmbauche, LocalDate.now()).getYears();
	}
	
	
	public boolean estContratTermine()
	{
		return dateFinContrat != null && dateFinContrat.isBefore(LocalDate.now());
	}
	
	/**
	 * Supprime l'employé. Si celui-ci est un administrateur, le root
	 * récupère les droits d'administration sur sa ligue.
	 */
	
	public void remove()
	{
		Employe root = gestionPersonnel.getRoot();
		if (this != root)
		{
			if (estAdmin(getLigue()))
				getLigue().setAdministrateur(root);
			getLigue().remove(this);
		}
		else
			throw new ImpossibleDeSupprimerRoot();
	}

	@Override
	public int compareTo(Employe autre)
	{
		int cmp = getNom().compareTo(autre.getNom());
		if (cmp != 0)
			return cmp;
		return getPrenom().compareTo(autre.getPrenom());
	}
	
	@Override
	public String toString()
	{
		String res = nom + " " + prenom + " " + mail + " (";
		if (estRoot())
			res += "super-utilisateur";
		else
			res += ligue.toString();
		return res + ")";
	}

	public String getPassword() {
		return this.password;
	}
}
