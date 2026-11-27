## Livrable n°2 - Développement principal, utilisateurs et parties
EDT : **29 novembre 2026**

Ce deuxième livrable contient les services concernant les utilisateurs et les parties.
En se basant sur la conception (possibles modifications si besoin), le PlayerService et le GameService auront une première version.

### 1. Infrastructure
Squelette Spring Boot de chaque service (PlayerService, GameService, LadderService, MarketService), un `Dockerfile` par service et un `docker-compose.yaml` qui lance l'ensemble. Mise en place de la base de données avec persistance entre les redémarrages.

### 2. PlayerService
Le PlayerService sera implémenté, l'inscription, la connexion et la création de comptes administrateurs seront disponibles.
Les signalements pourront être exécuté par des utilisateurs, et les bannissements par des administrateurs.

### 3. GameService 
La création de parties, sélection du nombre de joueurs et d'IA, la liste des parties ouvertes et la possibilité de rejoindre ou quitté un salon.
Si possible, effectuer un squelette du code concernant la logique de jeu.

### 4. Client léger
Pages inscription, connexion, tableau de bord avec liste des parties.
## Authentification

### Inscription
POST /players
Content-Type: application/json

{
  "email": "joueur@skull.fr",
  "username": "Joueur1",
  "password": "secret123"
}
-> 201 Created

### Connexion
POST /players/login
Content-Type: application/json

{
  "email": "joueur@skull.fr",
  "password": "secret123"
}
-> 200 OK avec { id, email, username, admin, token }

### Admin par defaut
- Email : admin@skull.fr
- Mot de passe : admin123
- Cree automatiquement au demarrage s'il n'existe pas.

### Tableau de bord
GET /players/{id}/dashboard

Retourne les infos du joueur, les parties ouvertes (via game-service)
et l'historique (via ladder-service).

Note : necessite que game-service et ladder-service soient lances.
Les URLs sont configurables dans application.properties.
