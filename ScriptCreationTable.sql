DROP TABLE EMPLOYE IF EXISTS;
DROP TABLE LIGUE IF EXISTS;

CREATE TABLE EMPLOYE
(
    NumEmployé INT AUTO_INCREMENT NOT NULL,
    PrenomEmployé VARCHAR(25),
    NomEmployé VARCHAR(25),
    MDPEmployé VARCHAR(25),
    Rôle VARCHAR(25),
    EmailEmployé VARCHAR(25),
    DateArrivée DATE,
    DateDepart DATE,
    Login VARCHAR(25),
    CONSTRAINT PK_EMPLOYE PRIMARY KEY (NumEmployé),
    CONSTRAINT UK_LIGUE foreign KEY(NumLigue) REFERENCES(NumEmployé)
)
    engine=innodb;

desc EMPLOYE;

CREATE TABLE LIGUE
(
    NumLigue INT AUTO_INCREMENT NOT NULL,
    NomLigue VARCHAR(25),
    NumEmployé INT,
    CONSTRAINT PK_LIGUE PRIMARY KEY (NumLigue),
    CONSTRAINT UK_LIGUE_NOM UNIQUE(NomLigue)
)
    engine=innodb;

desc LIGUE;
