DROP TABLE IF EXISTS EMPLOYE;
DROP TABLE IF EXISTS LIGUE  ;

CREATE TABLE LIGUE(
   id_ligue INT AUTO_INCREMENT,
   nom_ligue VARCHAR(255) NOT NULL,
   PRIMARY KEY(id_ligue),
   UNIQUE(nom_ligue)
);

CREATE TABLE EMPLOYE(
   id_employe INT AUTO_INCREMENT,
   nom_employe VARCHAR(255) NOT NULL,
   prenom_employe VARCHAR(255) NOT NULL,
   mail_employe VARCHAR(255) NOT NULL,
   password_employe VARCHAR(255) NOT NULL,
   rôle_employe VARCHAR(50),
   date_arrivee_employe DATE,
   date_depart_employe DATE,
   id_ligue INT,
   PRIMARY KEY(id_employe),
   UNIQUE(mail_employe),
   FOREIGN KEY(id_ligue) REFERENCES LIGUE(id_ligue) 
   ON DELETE SET NULL
);
