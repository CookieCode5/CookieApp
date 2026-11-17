# CookieApp

CookieApp est une application web de recettes anti-gaspillage.
On y trouve, crée et note des recettes. Le Frigo Magique propose ce qu'on peut cuisiner avec ce qu'on a déjà, et donne le prix par personne.

Projet JEE du groupe **CookieCode** : Amina ATTAF, Amira OTHMANI, Amira TARCHOUNE, Danika THEAM, Nylan PAILLASSA.

## Prérequis

- **Java 21** : vérifier avec `java -version`
- Rien d'autre à installer : Maven est inclus (`mvnw`) et la base de données H2 est dans le projet.

## Lancer l'application

```bash
cd cookieapp
./mvnw spring-boot:run
```

Sous Windows, avec l'invite de commandes : `mvnw.cmd spring-boot:run`.

Puis ouvrir **http://localhost:8081**. Pour arrêter l'application : `Ctrl + C`.

## Se connecter

| Compte | Email | Mot de passe |
| --- | --- | --- |
| Utilisateur de démo | `chef@cookieapp.test` | `motdepasse123` |

On peut aussi créer son propre compte avec « Créer un compte ».

## Ce qu'on peut faire

- Parcourir les recettes, et les filtrer (végétarien, temps, prix).
- Créer, modifier et supprimer ses recettes : ingrédients, étapes, ustensiles.
- Noter et commenter les recettes des autres, et ajouter des favoris.
- Frigo Magique : cocher ses ingrédients et voir les recettes réalisables.
- Passer Premium (simulé, sans paiement) : recherches illimitées et recettes premium.
- S'inscrire à des ateliers de cuisine.

## Voir la base de données

http://localhost:8081/h2-console, avec :
- JDBC URL : `jdbc:h2:file:./data/cookieapp`
- utilisateur : `sa`
- mot de passe : vide

## Tester

```bash
cd cookieapp
./mvnw test
```

L'API REST (`/api/v1/recipes`) se teste avec [Bruno](https://www.usebruno.com/) : ouvrir le dossier `bruno/` du projet, l'application étant lancée.

## Documentation

`diagramme-uml.md` contient le modèle de données et les diagrammes de l'application.