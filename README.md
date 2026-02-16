EL HARRAB Hiba
# Test It - Application de Gestion de Tâches

Application Spring Boot simple pour l'apprentissage des tests unitaires via une API REST de gestion de tâches.

## Spécifications Fonctionnelles

### Statuts des Tâches
Les tâches suivent ce workflow :
- OUVERT : État initial d'une tâche
- EN_COURS : Une tâche démarrée par un utilisateur
- FINI : Une tâche terminée

### Règles Métier
- Un utilisateur ne peut avoir qu'une seule tâche en cours (status EN_COURS).
- Les transitions sont soumises à des vérifications :
  - Démarrer une tâche : seulement si status = OUVERT et l'utilisateur n'a pas d'autre tâche EN_COURS.
  - Terminer une tâche : seulement si status = EN_COURS.

### API Endpoints

#### Tâches
- `GET /tasks` : Lister toutes les tâches
- `GET /tasks/{id}` : Obtenir une tâche par ID
- `GET /tasks/user/{userId}` : Lister les tâches d'un utilisateur
- `POST /tasks` : Créer une tâche (body: {title, description, userId})
- `PUT /tasks/{id}` : Modifier une tâche
- `DELETE /tasks/{id}` : Supprimer une tâche

#### Transitions
- `POST /tasks/{id}/start?userId=1` : Démarrer une tâche
- `POST /tasks/{id}/finish?userId=1` : Terminer une tâche

## Exécution

Pour lancer l'application :
```
./gradlew bootRun
```

L'API sera accessible sur http://localhost:8080

H2 Console : http://localhost:8080/h2-console

## TD

1. réaliser un test de service unitaire
   - Tester les méthodes métier de TaskService
   - Tester les règles de transition d'état des tâches
   - Tester les cas d'erreur (exceptions métier)
2. réaliser un test de repository
   - Tester les opérations CRUD du TaskRepository
   - Tester la persistance des données
   - Tester les requêtes personnalisées
3. Réaliser un test de service IT
   - Tester l'intégration du TaskService avec les repositories
   - Tester l'interaction avec les adapters (mail, utilisateur)
   - Tester avec la base de données réelle
4. Réaliser un test de controller
   - Tester les endpoints REST du TaskController
   - Tester la validation des requêtes
   - Tester les réponses HTTP et la gestion des erreurs

## Tests
Les étudiants doivent écrire des tests unitaires pour vérifier :
- Les règles métier (voir TaskServiceTest.java pour exemples)
- Les transitions d'états
- Les cas d'erreur

L'idée est de démontrer que les tests unitaires permettent de détecter rapidement les erreurs dans la logique métier.
