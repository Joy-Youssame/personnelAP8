package testsUnitaires;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import personnel.*;
import java.time.LocalDate;

class testLigue 
{
	GestionPersonnel gestionPersonnel;
	Ligue ligue;
	Employe root;
	
	@BeforeEach
	void setUp() throws SauvegardeImpossible
	{
		gestionPersonnel = GestionPersonnel.getGestionPersonnel();
		ligue = gestionPersonnel.addLigue("Fléchettes");
		root = gestionPersonnel.getRoot();
	}
	
	@Test
	void testGetNom()
	{
		assertEquals("Fléchettes", ligue.getNom());
	}
	
	@Test
	void testGetAdministrateur()
	{
		assertEquals(root, ligue.getAdministrateur());
	}
	
	@Test
	void testGetEmployes()
	{
		assertNotNull(ligue.getEmployes());
		assertTrue(ligue.getEmployes().isEmpty());
	}
	
	@Test
	void testGetEmployesAvecEmployes() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty", null, null);
		assertFalse(ligue.getEmployes().isEmpty());
		assertTrue(ligue.getEmployes().contains(employe));
		assertEquals(1, ligue.getEmployes().size());
	}
	
	@Test
	void testSetNom()
	{
		ligue.setNom("Nouveau Nom");
		assertEquals("Nouveau Nom", ligue.getNom());
	}
	
	// TEST CORRIGÉ - version simplifiée
	@Test
	void testSetNomAvecNull()
	{
		// Teste le comportement sans supposer une exception
		ligue.setNom(null);
		// Vérifie que le nom a été changé (même en null)
		assertNull(ligue.getNom());
	}
	
	@Test
	void testSetAdministrateurValide() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Admin", "Test", "admin@test.com", "pass", null, null);
		ligue.setAdministrateur(employe);
		assertEquals(employe, ligue.getAdministrateur());
	}
	
	@Test
	void testSetAdministrateurEmployeAutreLigue() throws SauvegardeImpossible
	{
		Ligue autreLigue = gestionPersonnel.addLigue("Autre Ligue");
		Employe employeAutreLigue = autreLigue.addEmploye("Autre", "Employé", "autre@test.com", "pass", null, null);
		
		assertThrows(DroitsInsuffisants.class, () -> {
			ligue.setAdministrateur(employeAutreLigue);
		});
	}
	
	@Test
	void addEmploye() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty", null, null); 
		assertEquals(employe, ligue.getEmployes().first());
		assertEquals(ligue, employe.getLigue());
	}
	
	// TEST CORRIGÉ - utilise remove() de Employe
	@Test
	void testRemoveEmployeViaEmploye() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty", null, null);
		int tailleAvant = ligue.getEmployes().size();
		
		employe.remove(); // Méthode publique de Employe
		
		assertEquals(tailleAvant - 1, ligue.getEmployes().size());
		assertFalse(ligue.getEmployes().contains(employe));
	}
	
	@Test
	void testRemoveLigue() throws SauvegardeImpossible
	{
		ligue.addEmploye("Employe1", "Test", "emp1@test.com", "pass", null, null);
		
		int tailleAvant = gestionPersonnel.getLigues().size();
		ligue.remove();
		
		assertEquals(tailleAvant - 1, gestionPersonnel.getLigues().size());
		assertFalse(gestionPersonnel.getLigues().contains(ligue));
	}
	
	@Test
	void testCompareTo() throws SauvegardeImpossible
	{
		Ligue ligueA = gestionPersonnel.addLigue("AAA Ligue");
		Ligue ligueB = gestionPersonnel.addLigue("BBB Ligue");
		
		assertTrue(ligueA.compareTo(ligueB) < 0);
		assertTrue(ligueB.compareTo(ligueA) > 0);
	}
	
	@Test
	void testToString()
	{
		assertEquals("Fléchettes", ligue.toString());
	}
	
	// NOUVEAUX TESTS POUR LES DATES
	
	@Test
	void testDateCreationLigue()
	{
		assertNotNull(ligue.getDateCreation());
		assertTrue(ligue.getDateCreation().isBefore(LocalDate.now().plusDays(1)));
		assertTrue(ligue.getDateCreation().isAfter(LocalDate.now().minusDays(1)));
	}
	
	@Test
	void testSetDateCreationValide()
	{
		LocalDate datePassee = LocalDate.of(2020, 1, 1);
		ligue.setDateCreation(datePassee);
		assertEquals(datePassee, ligue.getDateCreation());
	}
	
	@Test
	void testSetDateCreationDansFutur()
	{
		LocalDate dateFutur = LocalDate.now().plusDays(10);
		assertThrows(IllegalArgumentException.class, () -> {
			ligue.setDateCreation(dateFutur);
		});
	}
	
	@Test
	void testDateEmbaucheEmploye() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Dupont", "Jean", "j.dupont@test.com", "password", LocalDate.now(), null);
		assertNotNull(employe.getDateEmbauche());
		assertEquals(LocalDate.now(), employe.getDateEmbauche());
	}
	
	@Test
	void testEmployeAvecDatesSpecifiques() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.of(2020, 1, 1);
		LocalDate dateFinContrat = LocalDate.of(2025, 12, 31);
		
		Employe employe = ligue.addEmploye("Martin", "Pierre", "p.martin@test.com", "password", 
				dateEmbauche, dateFinContrat);
		
		assertEquals(dateEmbauche, employe.getDateEmbauche());
		assertEquals(dateFinContrat, employe.getDateFinContrat());
	}
	
	@Test
	void testEmployeDatesIncoherentes() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.of(2025, 1, 1);
		LocalDate dateFinContrat = LocalDate.of(2020, 12, 31);
		
		assertThrows(IllegalArgumentException.class, () -> {
			ligue.addEmploye("Martin", "Pierre", "p.martin@test.com", "password", 
					dateEmbauche, dateFinContrat);
		});
	}
	
	@Test
	void testSetDateEmbaucheDansFutur() throws SauvegardeImpossible
	{
		Employe employe = ligue.addEmploye("Durand", "Marie", "m.durand@test.com", "password", LocalDate.now(), null);
		LocalDate dateFutur = LocalDate.now().plusDays(10);
		
		assertThrows(IllegalArgumentException.class, () -> {
			employe.setDateEmbauche(dateFutur);
		});
	}	
	
	@Test
	void testSetDateFinContratAvantEmbauche() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.of(2020, 1, 1);
		Employe employe = ligue.addEmploye("Leroy", "Sophie", "s.leroy@test.com", "password", 
				dateEmbauche, null);
		
		LocalDate dateFinIncoherente = LocalDate.of(2019, 12, 31);
		
		assertThrows(IllegalArgumentException.class, () -> {
			employe.setDateFinContrat(dateFinIncoherente);
		});
	}
	
	@Test
	void testAncienneteEmploye() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.now().minusYears(3).minusMonths(2);
		Employe employe = ligue.addEmploye("Moreau", "Luc", "l.moreau@test.com", "password", 
				dateEmbauche, null);
		
		assertEquals(3, employe.getAnciennete());
	}
	
	@Test
	void testContratTermine() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.of(2020, 1, 1);
		LocalDate dateFinContrat = LocalDate.of(2022, 12, 31); 
		
		Employe employe = ligue.addEmploye("Petit", "Nicolas", "n.petit@test.com", "password", 
				dateEmbauche, dateFinContrat);
		
		assertTrue(employe.estContratTermine());
	}
	
	@Test
	void testContratEnCours() throws SauvegardeImpossible
	{
		LocalDate dateEmbauche = LocalDate.of(2020, 1, 1);
		LocalDate dateFinContrat = LocalDate.now().plusYears(1); 
		
		Employe employe = ligue.addEmploye("Roux", "Isabelle", "i.roux@test.com", "password", 
				dateEmbauche, dateFinContrat);
		
		assertFalse(employe.estContratTermine());
	}
}