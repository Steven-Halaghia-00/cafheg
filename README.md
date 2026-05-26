# Cafheg - Family Allowance Management System

## Travail réalisé

### Exercice 1 - Décision du parent ayant droit aux allocations

- Prise en main de l'application et test du endpoint REST `/droits/quel-parent`.
- Ajout de tests unitaires sur `AllocationService#getParentDroitAllocation`.
- Refactoring du retour initial vers un type dédié.
- Implémentation en TDD des règles métier de décision du parent ayant droit.
- Vérification du service et du endpoint REST après refactoring.

### Exercice 2 - Gestion des allocataires

- Ajout des services de suppression et de modification d'un allocataire.
- Interdiction de suppression lorsqu'un versement existe déjà.
- Protection du numéro AVS lors des modifications.
- Refus des modifications sans changement utile.
- Exposition via API REST avec codes HTTP adaptés.

### Exercice 3 - Logging

- Remplacement des sorties console et `printStackTrace` par des loggers SLF4J.
- Utilisation de niveaux de logs adaptés : `error`, `warn`, `info`, `debug` et `trace`.
- Configuration Logback avec fichier `err.log`, journal quotidien `cafheg_{date}.log` et sortie console `debug`.
- Exclusion des fichiers de logs générés du versionnement Git.

### Exercice 4 - Tests d'intégration

- Ajout d'une structure dédiée `src/integration-test/java` et `src/integration-test/resources`.
- Ajout d'un premier test d'intégration minimal `MyTestsIT`.
- Configuration de l'exécution séparée des tests unitaires et des tests d'intégration.
- Ajout de DBUnit et de datasets XML pour initialiser la base.
- Ajout de tests d'intégration pour la suppression et la modification d'un allocataire.

### Corrections d'audit

- Sécurisation de la recherche d'un allocataire inexistant dans le mapper.
- Nettoyage de la gestion des exceptions dans les mappers afin d'éviter le log-and-throw inutile.
- Conservation de la cause des exceptions lors de leur transformation.
- Vérification que les tests unitaires et les tests d'intégration restent fonctionnels.

## Pratiques appliquées

- Développement par branches Git.
- Commits structurés par fonctionnalité.
- Tests unitaires avec JUnit 5.
- Tests d'intégration séparés des tests unitaires.
- Utilisation de DBUnit pour préparer un état connu de la base.
- Refactoring vers des types explicites.
- Séparation entre contrôleurs REST, services métier et accès aux données.
- Logging structuré avec SLF4J et Logback.

## Prerequisites

- **Java 25**
- **IntelliJ IDEA** (Community or Ultimate)
- **Docker Desktop** (or Docker + Docker Compose)
- **Maven 3.8+** (bundled with the project as `mvnw`)

## Getting Started with IntelliJ

### 1. Open the Project

1. Launch IntelliJ IDEA
2. Select **File** → **Open** and choose the `cafheg` folder
3. IntelliJ will recognize the Maven project automatically

### 2. Configure Java Version

1. Go to **File** → **Project Structure** → **Project**
2. Set **SDK** to Java 25
    - If Java 25 is not available, click the dropdown and select **Download JDK**
    - Choose **Version 25** and a vendor (e.g., Eclipse Temurin)
    - Click **Download** and wait for completion
3. Set **Language level** to 25
4. Click **Apply** and **OK**

### 3. PostgreSQL Database

The application is configured to automatically start PostgreSQL via Docker Compose on startup thanks to the
`spring-boot-docker-compose` dependency.

**No manual setup required** - PostgreSQL will start automatically when you run the application.

#### Manual Database Startup (Optional)

If you prefer to manage the database separately, run:

```bash
docker compose up -d
```

This will start a PostgreSQL 18.1 container with:

- Database: `cafheg`
- User: `cafheg`
- Password: `secret`
- Published port: `5432`

### 4. Run the Application

#### Option A: From IntelliJ

1. Open `CafhegApplication.java` (in `src/main/java/ch/hearc/cafheg/`)
2. Click the green **Run** button next to the `main` method
3. The application will start on `http://localhost:8080/api`

#### Option B: From Terminal

```bash
./mvnw clean spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd clean spring-boot:run
```

### 5. Access the Application

- **API Documentation**: http://localhost:8080/api/swagger-ui/index.html
- **API Base URL**: http://localhost:8080/api

## Database Migrations

Database schema and migrations are managed by Flyway and located in:

- `src/main/resources/db/migration/ddl/` - Schema definitions
- `src/main/resources/db/migration/dml/` - Data initialization

Migrations run automatically on application startup.

## Troubleshooting

- **Port 8080 already in use**: Change `server.port` in `application.yaml`
- **Database connection failed**: Ensure PostgreSQL is running via Docker Compose and environment variables are set
  correctly
- **Maven sync issues**: Right-click on `pom.xml` and select **Maven** → **Reload projects**


