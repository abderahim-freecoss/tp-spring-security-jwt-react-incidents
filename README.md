# TP — Spring Security, JWT et React

## Fonctionnalités

Inscription EMPLOYEE, connexion JWT, profil et mot de passe ; création et suivi des incidents ;
affectation par l'administrateur ; mise à jour du statut et notes par le technicien affecté ;
historique ; gestion des comptes ; tableaux de bord adaptés aux rôles.
L'accès aux données est contrôlé côté serveur. Un incident clôturé ne peut plus être modifié.

## Démarrage sous Windows (PowerShell)

Prérequis : JDK 17 ou supérieur et Node.js 22. Maven est téléchargé par le wrapper.
Validé sur cette machine avec Java 19.0.1 et Node 22.15.0.

Dans un premier terminal, depuis la racine :

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Dans un deuxième terminal, depuis la racine :

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Ouvrir http://localhost:5173. API : http://localhost:8080.
Créer son compte sur « Créer un compte », puis se connecter. Aucun compte de démonstration n'est créé sans le profil `demo`. L'inscription produit toujours EMPLOYEE,
même si un client ajoute un champ `role: ADMIN` à la requête.

### Comptes de démonstration

Pour créer les comptes de test en local, lancer le backend depuis `backend` avec :

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

Les comptes `admin@example.com`, `tech@example.com`, `employee@example.com` et
`employee2@example.com` ont le mot de passe `DemoPassword123!`. Ils ne sont créés
que si leur email n'existe pas déjà. Le profil demo est réservé aux essais locaux.

## Configuration et sécurité

- Base H2 persistante : `backend/data/incidents`. La commande backend doit être exécutée depuis `backend`.
- Sans `JWT_SECRET`, une clé aléatoire est générée au démarrage : un redémarrage invalide les jetons.
- Pour conserver la clé entre démarrages, définir `JWT_SECRET` dans l'environnement (au moins 32 octets aléatoires). Ne jamais la committer.
- `DB_PASSWORD` configure le mot de passe H2 ; il est vide par défaut pour ce TP local.
- Console H2 désactivée par défaut. Pour le développement uniquement :
  `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'`.
  Puis `/h2-console`, JDBC `jdbc:h2:file:./data/incidents`, utilisateur `sa`.
- Le serveur écoute sur l'interface locale. CORS autorise `http://localhost:5173` et `http://127.0.0.1:5173` (Vite accepte les deux origines locales).
- Vite relaie `/auth` et `/api` vers le backend. Pour un autre déploiement, configurer
  `VITE_API_URL` et adapter CORS ainsi que la réécriture des routes React.
- Le JWT est stocké dans sessionStorage ; Axios l'ajoute aux appels API. Un 401 sur une API
  supprime la session et affiche un message sur Login. Un mauvais login reste sur le formulaire.
- Les rôles et l'état enabled sont relus en base à chaque requête. Le changement d'email ou
  de mot de passe invalide les jetons existants. La déconnexion efface le jeton du navigateur ;
  elle ne le révoque pas côté serveur (il expire après cinq minutes).
- La désactivation CSRF correspond à cette API sans cookie d'authentification, avec JWT explicite.
- Ne pas exposer cette configuration pédagogique telle quelle sur Internet : pas encore de limitation
  des tentatives de connexion, de migrations versionnées ni de déploiement HTTPS configuré.

## Endpoints disponibles

| Méthode | URL | Accès / résultat |
|---|---|---|
| POST | `/auth/register` | Public, crée EMPLOYEE, réponse 201 sans mot de passe |
| POST | `/auth/login` | Public, token + role + fullName |
| GET | `/api/profile` | Authentifié |
| PUT | `/api/profile` | Authentifié, fullName + email |
| PUT | `/api/profile/password` | Authentifié, oldPassword + newPassword + confirmation |
| POST | `/api/incidents` | EMPLOYEE, déclare un incident |
| GET | `/api/incidents/my` | EMPLOYEE, ses incidents |
| GET | `/api/incidents/assigned` | TECHNICIAN, incidents qui lui sont affectés |
| GET | `/api/incidents` | ADMIN, tous les incidents |
| GET | `/api/incidents/{id}` | Créateur, technicien affecté ou ADMIN, détail et historique |
| PUT | `/api/incidents/{id}/assign/{technicianId}` | ADMIN, affecte un technicien actif |
| PUT | `/api/incidents/{id}/status` | ADMIN ou technicien affecté, change le statut |
| POST | `/api/incidents/{id}/comments` | Technicien affecté, ajoute une note |
| GET | `/api/admin/users` | ADMIN, liste des comptes |
| PUT | `/api/admin/users/{id}/enable` | ADMIN, active un compte |
| PUT | `/api/admin/users/{id}/disable` | ADMIN, désactive un compte |
| GET | `/api/admin/statistics` | ADMIN, statistiques des utilisateurs et incidents |

### Rôles et fonctionnalités

- `EMPLOYEE` : inscription, connexion, profil, création d'incidents et consultation de ses incidents.
- `TECHNICIAN` : consultation des incidents affectés, mise à jour de leur statut, ajout de notes et consultation de l'historique.
- `ADMIN` : consultation globale, affectation, gestion des utilisateurs et consultation des statistiques.
- Interface React : `/login`, `/register`, `/profile`, `/dashboard`, `/incidents`, `/incidents/new`, `/incidents/:id` et `/admin/users`. Les routes et données sont filtrées selon le rôle ; le backend reste l'autorité de sécurité.

Exemple d'inscription :

```json
{"fullName":"Ahmed Benali","email":"ahmed@example.com","password":"UnMotDePasse123!"}
```

## Vérifications

```powershell
cd backend
.\mvnw.cmd test
```

```powershell
cd frontend
npm.cmd run build
```

Tests navigateur (avec backend sur 8080 et frontend sur 5173 déjà lancés) :

```powershell
cd frontend
npx.cmd playwright install chromium
npx.cmd playwright test
```

Les tests navigateur créent un compte dont l'email est unique. Utiliser une base de test si l'on souhaite éviter ces données dans la base locale.

Alternative sans télécharger Chromium si Microsoft Edge est installé :
`$env:PLAYWRIGHT_CHANNEL='msedge'; npx.cmd playwright test`.
Vérifications réalisées : 8 tests backend réussis, 2 tests navigateur réussis sur Edge,
compilation de production React réussie et captures de connexion contrôlées à 390 et 1440 px.

Le détail retourne l'historique et les notes. Tester le scénario complet en créant un incident
avec l'employé, en l'affectant avec l'administrateur, puis en ajoutant une note avec le technicien.
Les tests d'intégration vérifient notamment l'isolation entre employés, l'affectation,
les droits du technicien et le blocage d'un incident clôturé.

Les captures du parcours navigateur et les captures Postman réellement exécutées sont dans `output/captures/` et intégrées en annexe au rapport PDF. La collection Postman importable est dans `output/postman/`. Les captures suivent le parcours réellement lancé ; les inscriptions et reconnexions inutiles ont été omises. Le refresh token est facultatif et n’est pas implémenté.

## Livrables du TP

- Backend Spring Boot : `backend/`.
- Frontend React : `frontend/`.
- Rapport PDF avec annexes de captures : `output/pdf/rapport_tp_spring_security_jwt_react.pdf`.
- Diagrammes d’architecture et d’entités : `output/diagrams/diagrammes_application.pdf` ; source Mermaid : `docs/architecture.md`.
- Captures de l’application et de Postman : `output/captures/`.
- Collection Postman importable : `output/postman/`.
- Démonstration fonctionnelle : parcours de l’employé à l’administrateur et au technicien documenté dans le rapport et illustré par les captures navigateur et API.

## Choix techniques et références

Les relations Incident → User sont `ManyToOne(fetch = LAZY)` : plusieurs incidents partagent
un auteur ou un technicien. On évite de charger automatiquement les utilisateurs avec chaque incident.
Une collection inverse `OneToMany` n'est pas nécessaire pour interroger les incidents via un repository.
Les contrôleurs exposent des DTO, jamais l'entité User et son hash. Les futures conversions de relations
LAZY devront se faire dans un service transactionnel, puisque `open-in-view` est désactivé.

Le JWT porte `sub` (email), `iat`, `exp`, `iss`, `role` et `version`. Spring Security vérifie
la signature et l'expiration via son filtre Bearer natif ; aucun parseur cryptographique maison.
401 signifie authentification absente/invalide ; 403 signifie accès refusé ; 400 données invalides ;
404 ressource absente ; 409 conflit (notamment email déjà utilisé).

- [Exigences Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Support JWT officiel de Spring Security](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html)
- Voir `docs/architecture.md` pour les diagrammes de cette première étape.
