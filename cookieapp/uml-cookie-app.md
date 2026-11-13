# Cookie App — Diagrammes UML

Version 2 (13/11/2026) : tout le code est en anglais (classes, attributs, colonnes, énumérations, routes), seuls les textes affichés à l'écran restent en français.
Elle reprend le diagramme de la branche `main` (`uml-cookie-app.md`) avec les corrections listées au paragraphe « Changements par rapport à la version de l'équipe ».
GitHub affiche les schémas directement ; pour modifier avec aperçu, collez un bloc dans [mermaid.live](https://mermaid.live).

| N° | Diagramme | Type |
| --- | --- | --- |
| 1 | Entités JPA et base de données | Classes |
| 2 | Services et DTO | Classes |
| 3 | Architecture MVC (controllers → services → repositories) | Classes |
| 4 | Inscription | Séquence |
| 5 | Connexion et page protégée | Séquence |
| 6 | Créer une recette (1-N et N-N) | Séquence |
| 7 | Frigo Magique | Séquence |
| 8 | Noter et commenter une recette | Séquence |
| 9 | S'inscrire à un atelier | Séquence |
| 10 | API REST depuis Bruno | Séquence |

## Notation

| Élément | Signification |
| --- | --- |
| `«Entity»` | Classe annotée `@Entity`, donc une table en base (nom de table = nom de classe en snake_case, sauf `User` → table `users`) |
| `«enumeration»` | Enum Java, stockée en `VARCHAR` via `@Enumerated(EnumType.STRING)` |
| `«PK»` / `«FK»` / `«UK»` | Clé primaire / clé étrangère / contrainte d'unicité |
| `«Role»`, `«Unit»`… sur une colonne | La colonne prend les valeurs de cette énumération |
| Losange plein (composition) | Les lignes filles sont supprimées avec le parent (`cascade = ALL, orphanRemoval = true`) |
| Libellé sur un lien | Nom de l'attribut Java de la relation (`author`, `favorites`…) |

## 1. Diagramme de classes des entités

```mermaid
---
config:
  layout: elk
---
classDiagram
direction TB

class User {
  <<Entity>>
  id : BIGINT «PK»
  username : VARCHAR «UK»
  email : VARCHAR «UK»
  password_hash : VARCHAR
  role : VARCHAR «Role»
  registration_date : DATE
  +isPremium() boolean
}
class Subscription {
  <<Entity>>
  id : BIGINT «PK»
  user_id : BIGINT «FK» «UK»
  type : VARCHAR «SubscriptionType»
  start_date : DATE
  end_date : DATE
  daily_searches : INT
  last_search_date : DATE
  +isActive() boolean
  +canStartSearch() boolean
}
class Recipe {
  <<Entity>>
  id : BIGINT «PK»
  author_id : BIGINT «FK»
  title : VARCHAR
  description : TEXT
  image_url : VARCHAR
  base_servings : INT
  preparation_time : INT
  cooking_time : INT
  difficulty : VARCHAR «Difficulty»
  category : VARCHAR «Category»
  vegetarian : BOOLEAN
  vegan : BOOLEAN
  premium : BOOLEAN
  average_rating : DOUBLE
  review_count : INT
  created_at : TIMESTAMP
  +totalTime() int
  +allergens() Set~Allergen~
}
class Step {
  <<Entity>>
  id : BIGINT «PK»
  recipe_id : BIGINT «FK»
  step_order : INT
  title : VARCHAR
  instruction : TEXT
  duration_minutes : INT
  tip : VARCHAR
  parallelizable : BOOLEAN
}
class Ingredient {
  <<Entity>>
  id : BIGINT «PK»
  name : VARCHAR «UK»
  reference_unit : VARCHAR «Unit»
  price_per_unit : DECIMAL
  aisle : VARCHAR «Aisle»
}
class RecipeIngredient {
  <<Entity>>
  id : BIGINT «PK»
  recipe_id : BIGINT «FK»
  ingredient_id : BIGINT «FK»
  quantity : DOUBLE
  unit : VARCHAR «Unit»
  optional : BOOLEAN
  +quantityFor(int servings) double
}
class PantryItem {
  <<Entity>>
  id : BIGINT «PK»
  user_id : BIGINT «FK»
  ingredient_id : BIGINT «FK»
  quantity : DOUBLE
  unit : VARCHAR «Unit»
  expiration_date : DATE
}
class Utensil {
  <<Entity>>
  id : BIGINT «PK»
  name : VARCHAR «UK»
  common : BOOLEAN
}
class Allergen {
  <<Entity>>
  id : BIGINT «PK»
  name : VARCHAR «UK»
}
class Review {
  <<Entity>>
  id : BIGINT «PK»
  recipe_id : BIGINT «FK»
  user_id : BIGINT «FK»
  rating : INT
  comment : TEXT
  created_at : TIMESTAMP
}
class Workshop {
  <<Entity>>
  id : BIGINT «PK»
  recipe_id : BIGINT «FK»
  organizer_id : BIGINT «FK»
  title : VARCHAR
  scheduled_at : TIMESTAMP
  location : VARCHAR
  type : VARCHAR «WorkshopType»
  max_seats : INT
  +remainingSeats() int
  +isFull() boolean
}

%% ===== 1-1 =====
User "1" -- "1" Subscription : subscription

%% ===== 1-N =====
User "1" -- "0..*" Recipe : author
Recipe "1" *-- "1..*" Step : steps
Recipe "1" *-- "1..*" RecipeIngredient : ingredients
Ingredient "1" -- "0..*" RecipeIngredient : ingredient
User "1" *-- "0..*" PantryItem : pantry
Ingredient "1" -- "0..*" PantryItem : ingredient
Recipe "1" *-- "0..*" Review : reviews
User "1" -- "0..*" Review : author
Recipe "1" -- "0..*" Workshop : recipe
User "1" -- "0..*" Workshop : organizer

%% ===== N-N =====
Recipe "0..*" -- "0..*" Utensil : utensils
User "0..*" -- "0..*" Utensil : utensils
User "0..*" -- "0..*" Allergen : allergies
Ingredient "0..*" -- "0..*" Allergen : allergens
User "0..*" -- "0..*" Recipe : favorites
Workshop "0..*" -- "0..*" User : participants

%% ===== Énumérations (colonnes VARCHAR, @Enumerated STRING) =====
class Role {
  <<enumeration>>
  USER
  ADMIN
}
class SubscriptionType {
  <<enumeration>>
  FREE
  PREMIUM
}
class Difficulty {
  <<enumeration>>
  EASY
  MEDIUM
  HARD
}
class Category {
  <<enumeration>>
  CAKE
  TART
  PASTRY
  COOKIE
  ENTREMET
  CREAM_DESSERT
  CONFECTIONERY
}
class Unit {
  <<enumeration>>
  G
  KG
  ML
  CL
  L
  PIECE
  TEASPOON
  TABLESPOON
  +toBase(double quantity) double
}
class Aisle {
  <<enumeration>>
  FRESH
  GROCERY
  PRODUCE
  FROZEN
}
class WorkshopType {
  <<enumeration>>
  HOME
  CLASS
  RESTAURANT
}
```

### Méthodes métier des entités

| Méthode | Classe | Rôle |
| --- | --- | --- |
| `isPremium()` | User | Vrai si l'abonnement est `PREMIUM` et actif : décide l'accès aux recettes `premium`. |
| `isActive()` | Subscription | Vrai entre `start_date` et `end_date` (ou sans `end_date` pour `FREE`). |
| `canStartSearch()` | Subscription | Premium actif → toujours ; `FREE` → moins de 3 recherches Frigo aujourd'hui (`daily_searches`, remis à 0 quand `last_search_date` change). |
| `totalTime()` | Recipe | `preparation_time + cooking_time`, calculé pour ne jamais être incohérent. |
| `allergens()` | Recipe | Union des allergènes des ingrédients de la recette, pour avertir et filtrer. |
| `quantityFor(servings)` | RecipeIngredient | `quantity × servings / recipe.base_servings` : ajustement des portions. |
| `remainingSeats()`, `isFull()` | Workshop | `max_seats − participants.size()` ; plein quand il ne reste aucune place. |
| `toBase(quantity)` | Unit | Convertit en unité de base (g, ml ou pièce) : KG ×1000, CL ×10, L ×1000, TEASPOON = 5 ml, TABLESPOON = 15 ml. Sert au calcul du prix. |

### Relations et côté propriétaire

| Type | Relation (attribut Java) | Clé étrangère ou table de jointure | Associer / dissocier depuis |
| --- | --- | --- | --- |
| 1-1 | User ↔ Subscription (`subscription`) | `subscription.user_id` (unique) | Page Offres : passer Premium |
| 1-N | User → Recipe (`author`) | `recipe.author_id` | Création de recette (auteur = connecté) |
| 1-N | Recipe → Step (`steps`) | `step.recipe_id` | Formulaire recette : ajouter / supprimer une étape |
| 1-N | Recipe → RecipeIngredient → Ingredient | `recipe_ingredient.recipe_id`, `.ingredient_id` | Formulaire recette : lignes d'ingrédients |
| 1-N | User → PantryItem → Ingredient (`pantry`) | `pantry_item.user_id`, `.ingredient_id` | Frigo Magique / Mon garde-manger |
| 1-N | Recipe → Review, User → Review | `review.recipe_id`, `review.user_id` | Fiche recette : noter, commenter |
| 1-N | Recipe → Workshop, User → Workshop (`organizer`) | `workshop.recipe_id`, `.organizer_id` | Création d'atelier : choisir la recette |
| N-N | Recipe ↔ Utensil (`utensils`) | `recipe_utensil` | Formulaire recette : cocher les ustensiles |
| N-N | User ↔ Utensil (`utensils`) | `user_utensil` | Mon espace : mes équipements |
| N-N | User ↔ Allergen (`allergies`) | `user_allergen` | Mon espace : mes allergies |
| N-N | Ingredient ↔ Allergen (`allergens`) | `ingredient_allergen` | Admin : fiche ingrédient |
| N-N | User ↔ Recipe (`favorites`) | `favorite` | Bouton cœur |
| N-N | Workshop ↔ User (`participants`) | `workshop_participant` | S'inscrire / se désinscrire |

`RecipeIngredient` et `PantryItem` relient deux entités comme un N-N mais portent des données (quantité, unité) : ce sont des entités avec deux `@ManyToOne`, pas des `@ManyToMany`. Pour la grille, les vrais `@ManyToMany` sont les six de la fin du tableau.

### Règles de gestion

- **RG-U1** : `email` et `username` uniques ; mot de passe ≥ 8 caractères, stocké en BCrypt ; une `Subscription` `FREE` est créée à l'inscription.
- **RG-R1** : seul l'auteur (ou un `ADMIN`) modifie ou supprime une recette.
- **RG-A1** : un seul avis par couple (user, recipe) (`@UniqueConstraint`) ; un auteur ne note pas sa propre recette ; `rating` entre 1 et 5.
- **RG-A2** : `average_rating` et `review_count` sont recalculés par `ReviewService` à chaque ajout, modification ou suppression d'avis.
- **RG-F1** : une ligne est couverte si l'utilisateur a un `PantryItem` pour cet ingrédient (quantité vide = « j'en ai » ; quantité renseignée = il en faut assez, après `toBase`) ; une ligne `optional` ne compte pas.
- **RG-F2** : score = lignes couvertes / lignes non optionnelles ; on garde score ≥ 0,7, sans allergène de l'utilisateur ni ustensile manquant non `common` ; tri par score puis coût du complément.
- **RG-P1** : coût d'une ligne = `toBase(quantityFor(n)) × price_per_unit` (prix par unité de base de l'ingrédient) ; coût du complément = somme des lignes non couvertes.
- **RG-S1** : `FREE` = 3 recherches Frigo par jour ; `PREMIUM` = illimité + recettes `premium`. Passage Premium simulé (pas de paiement).
- **RG-W1** : inscription à un atelier refusée s'il est plein, passé, ou si l'utilisateur en est l'organisateur ; `max_seats` ≥ 1.

### Changements par rapport à la version de l'équipe

| Version de l'équipe | Version 2 | Pourquoi |
| --- | --- | --- |
| Classes et énumérations en français (`Utilisateur`, `Recette`, `Difficulte`…), colonnes en anglais | Tout en anglais (`User`, `Recipe`, `Difficulty`…) | Demande du prof, et la branche `bdd` a déjà commencé en anglais (`User`, `Allergen`, `Subscription`) |
| Table `utilisateur` | Entité `User` → `@Table(name = "users")` | `USER` est un mot réservé de H2 : sans ça, la table ne se crée pas |
| `image : BLOB` | `image_url : VARCHAR` (fichier dans `uploads/`) | Des images dans H2 gonflent le fichier de base qui doit être dans Git |
| `remaining_ai_credits` | `daily_searches` + `last_search_date` | Il n'y a pas d'IA dans le périmètre ; sans date, le quota ne se remettait jamais à zéro |
| Pas de régime sur `Recette` | `vegetarian`, `vegan` | Filtres « Végétarien » et « Régime » de la maquette (accueil, liste, Frigo) |
| `Etape` sans titre ni astuce | `title`, `tip` ajoutés | La fiche recette de la maquette affiche un titre et une « Astuce du Chef » par étape |
| `StockIngredient` | `PantryItem` (même contenu, `quantity`, `unit` et `expiration_date` facultatifs) | Nom anglais ; l'écran Frigo coche des ingrédients sans quantité |
| `Avis.date`, `Atelier.date` | `created_at`, `scheduled_at` | `date` est un nom de type SQL, source d'erreurs |
| Liens sans libellé | Libellés `author`, `favorites`, `participants`… | Deux liens User–Recipe et deux liens User–Workshop étaient indiscernables |
| `Unite` sur la ligne et sur l'ingrédient, sans conversion | `Unit.toBase()` | Nécessaire au calcul du prix quand la recette dit « 200 G » et l'ingrédient est vendu au « KG » |

## 2. Diagramme de classes des services et DTO

```mermaid
classDiagram
    direction LR

    class UserService {
        +register(RegisterDTO) User
        +authenticate(String email, String password) User
        +toPublicUser(User) PublicUser
        +updateUtensils(User, Set~Long~ utensilIds)
        +updateAllergies(User, Set~Long~ allergenIds)
        +toggleFavorite(User, Long recipeId)
    }
    class SubscriptionService {
        +createFree(User) Subscription
        +checkFridgeSearch(User)
        +upgradeToPremium(User, int months)
        +canView(User, Recipe) boolean
    }
    class RecipeService {
        +create(RecipeForm, MultipartFile image, User) Recipe
        +update(Long, RecipeForm, User) Recipe
        +delete(Long, User)
        +search(RecipeSearchCriteria) List~Recipe~
        +findById(Long) Recipe
        +latest(int n) List~Recipe~
    }
    class ReviewService {
        +rate(User, Long recipeId, int rating, String comment) Review
        +delete(Long reviewId, User)
        +refreshAverage(Recipe)
        +starDistribution(Recipe) Map~Integer, Long~
        +topOfTheWeek(int n) List~Recipe~
    }
    class PantryService {
        +setFromFridge(User, Set~Long~ ingredientIds)
        +addItem(User, PantryItemForm)
        +removeItem(Long itemId, User)
    }
    class SearchService {
        +feasibleRecipes(User, FridgeCriteria) List~FeasibilityResult~
        -isCovered(RecipeIngredient, List~PantryItem~ pantry) boolean
    }
    class PriceService {
        +recipeCost(Recipe, int servings) BigDecimal
        +costPerServing(Recipe, int servings) BigDecimal
        +missingCost(Recipe, List~PantryItem~ pantry, int servings) BigDecimal
    }
    class WorkshopService {
        +create(WorkshopForm, User organizer) Workshop
        +join(Long workshopId, User)
        +leave(Long workshopId, User)
        +upcoming() List~Workshop~
    }

    class RegisterDTO {
        String username
        String email
        String password
        String confirmPassword
    }
    class PublicUser {
        Long id
        String username
        String email
        boolean premium
        boolean admin
    }
    class RecipeForm {
        String title
        String description
        Category category
        Difficulty difficulty
        int baseServings
        int preparationTime
        int cookingTime
        boolean vegetarian
        boolean vegan
        boolean premium
        List~IngredientLineForm~ ingredients
        List~StepForm~ steps
        Set~Long~ utensilIds
    }
    class FridgeCriteria {
        Set~Long~ ingredientIds
        Set~Long~ utensilIds
        int servings
        int maxTime
        boolean vegetarian
        boolean vegan
        Set~Long~ excludedAllergenIds
    }
    class FeasibilityResult {
        Recipe recipe
        double score
        List~String~ missingIngredients
        List~String~ missingUtensils
        BigDecimal missingCost
        BigDecimal costPerServing
    }
    class RecipeDTO {
        Long id
        String title
        String description
        Long authorId
        Set~Long~ utensilIds
        List~IngredientLineForm~ ingredients
        List~StepForm~ steps
    }

    UserService ..> SubscriptionService : crée l'abonnement
    UserService ..> RegisterDTO
    UserService ..> PublicUser
    RecipeService ..> RecipeForm
    RecipeService ..> RecipeDTO
    SearchService ..> PriceService
    SearchService ..> FridgeCriteria
    SearchService ..> FeasibilityResult
    ReviewService ..> RecipeService
    WorkshopService ..> RecipeService
```

## 3. Architecture MVC : controllers, services, repositories

```mermaid
classDiagram
    direction LR

    class HomeController {
        +GET /
    }
    class AuthController {
        +GET/POST /register
        +GET/POST /login
        +GET /logout
    }
    class RecipeController {
        +GET /recipes
        +GET /recipes/id
        +GET/POST /recipes/new
        +GET/POST /recipes/id/edit
        +POST /recipes/id/delete
    }
    class ReviewController {
        +POST /recipes/id/reviews
        +POST /recipes/id/favorite
    }
    class FridgeController {
        +GET/POST /fridge
    }
    class AccountController {
        +GET /account
        +POST /account/allergies
        +POST /account/utensils
        +POST /account/pantry
        +GET /offers
        +POST /offers/premium
    }
    class WorkshopController {
        +GET /workshops
        +GET/POST /workshops/new
        +POST /workshops/id/join
        +POST /workshops/id/leave
    }
    class AdminController {
        +GET/POST /admin/ingredients
        +GET/POST /admin/utensils
        +GET/POST /admin/allergens
    }
    class RecipeApiController {
        <<RestController>>
        +GET /api/v1/recipes
        +GET /api/v1/recipes/id
        +POST /api/v1/recipes
        +PUT /api/v1/recipes/id
        +DELETE /api/v1/recipes/id
    }
    class LoginInterceptor {
        +preHandle() boolean
    }

    class UserService
    class SubscriptionService
    class RecipeService
    class ReviewService
    class PantryService
    class SearchService
    class PriceService
    class WorkshopService

    class UserRepository {
        <<JpaRepository>>
        +findByEmail(String) Optional~User~
        +existsByEmail(String) boolean
        +existsByUsername(String) boolean
    }
    class RecipeRepository {
        <<JpaRepository>>
        +findAllByOrderByCreatedAtDesc() List~Recipe~
        +findByTitleContainingIgnoreCase(String) List~Recipe~
        +findCandidates(int maxTime, boolean vegetarian, boolean vegan) List~Recipe~
    }
    class ReviewRepository {
        <<JpaRepository>>
        +findByAuthorAndRecipe(User, Recipe) Optional~Review~
        +averageForRecipe(Long recipeId) Double
        +topSince(LocalDateTime since) List~Recipe~
    }
    class PantryItemRepository {
        <<JpaRepository>>
        +findByUser(User) List~PantryItem~
    }
    class WorkshopRepository {
        <<JpaRepository>>
        +findByScheduledAtAfterOrderByScheduledAt(LocalDateTime) List~Workshop~
    }
    class IngredientRepository {
        <<JpaRepository>>
    }
    class UtensilRepository {
        <<JpaRepository>>
    }
    class AllergenRepository {
        <<JpaRepository>>
    }
    class SubscriptionRepository {
        <<JpaRepository>>
    }

    HomeController --> RecipeService
    HomeController --> ReviewService
    AuthController --> UserService
    RecipeController --> RecipeService
    RecipeController --> PriceService
    RecipeController --> ReviewService
    ReviewController --> ReviewService
    ReviewController --> UserService
    FridgeController --> SubscriptionService
    FridgeController --> PantryService
    FridgeController --> SearchService
    AccountController --> UserService
    AccountController --> PantryService
    AccountController --> SubscriptionService
    WorkshopController --> WorkshopService
    AdminController --> IngredientRepository
    AdminController --> UtensilRepository
    AdminController --> AllergenRepository
    RecipeApiController --> RecipeService

    UserService --> UserRepository
    UserService --> UtensilRepository
    UserService --> AllergenRepository
    SubscriptionService --> SubscriptionRepository
    RecipeService --> RecipeRepository
    RecipeService --> IngredientRepository
    RecipeService --> UtensilRepository
    ReviewService --> ReviewRepository
    PantryService --> PantryItemRepository
    PantryService --> IngredientRepository
    SearchService --> RecipeRepository
    SearchService --> PantryItemRepository
    WorkshopService --> WorkshopRepository
```

Chaque controller renvoie une vue Thymeleaf (sauf `RecipeApiController`, qui renvoie du JSON). `LoginInterceptor` protège `/fridge`, `/account/**`, `/offers/**`, `/recipes/new`, `/recipes/*/edit`, `/workshops/new`, `/workshops/*/join`, `/admin/**` (rôle `ADMIN`).

## 4. Séquence « Inscription »

```mermaid
sequenceDiagram
    actor V as Visiteur
    participant C as AuthController
    participant US as UserService
    participant UR as UserRepository
    participant PE as PasswordEncoder
    participant SS as SubscriptionService

    V->>C: POST /register (username, email, password, confirmPassword)
    C->>C: @Valid RegisterDTO (email, 8 caractères min)
    alt erreurs de validation
        C-->>V: auth/register.html + messages d'erreur
    else formulaire valide
        C->>US: register(dto)
        US->>US: password == confirmPassword ?
        US->>UR: existsByEmail(email), existsByUsername(username)
        UR-->>US: false
        US->>PE: encode(password)
        PE-->>US: hash BCrypt
        US->>UR: save(user)
        US->>SS: createFree(user)
        US-->>C: User
        C-->>V: redirect:/login + message flash « Compte créé »
    end
```

## 5. Séquence « Connexion et page protégée »

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant C as AuthController
    participant US as UserService
    participant UR as UserRepository
    participant PE as PasswordEncoder
    participant S as HttpSession
    participant I as LoginInterceptor

    U->>C: POST /login (email, password)
    C->>US: authenticate(email, password)
    US->>UR: findByEmail(email)
    UR-->>US: Optional de User
    US->>PE: matches(password, hash)
    alt identifiants invalides
        PE-->>US: false
        US-->>C: BusinessException
        C-->>U: redirect:/login + « Email ou mot de passe incorrect »
    else identifiants valides
        PE-->>US: true
        US-->>C: User
        C->>US: toPublicUser(user)
        C->>S: setAttribute("user", publicUser)
        C-->>U: redirect:/
    end
    U->>I: GET /fridge
    I->>S: getAttribute("user")
    alt pas de session
        I-->>U: redirect:/login
    else connecté
        I-->>U: la requête continue vers FridgeController
    end
```

## 6. Séquence « Créer une recette » (associations 1-N et N-N)

```mermaid
sequenceDiagram
    actor U as Utilisateur connecté
    participant C as RecipeController
    participant RS as RecipeService
    participant IR as IngredientRepository
    participant UtR as UtensilRepository
    participant RR as RecipeRepository

    U->>C: GET /recipes/new
    C->>IR: findAll()
    C->>UtR: findAll()
    C-->>U: recipes/form.html (ingrédients, ustensiles, catégories)
    Note right of U: remplit lignes, étapes<br/>et ustensiles
    U->>C: POST /recipes/new (RecipeForm + image)
    C->>RS: create(form, image, author)
    RS->>RS: enregistre l'image dans uploads/ et garde imageUrl
    loop pour chaque IngredientLineForm
        RS->>IR: findById(ingredientId)
        RS->>RS: new RecipeIngredient(quantity, unit) liée à la recette (1-N)
    end
    loop pour chaque StepForm
        RS->>RS: new Step(order, title, instruction, tip) liée à la recette (1-N)
    end
    RS->>UtR: findAllById(utensilIds)
    RS->>RS: recipe.setUtensils(...) (N-N)
    RS->>RR: save(recipe) (cascade vers steps et ingredients)
    RR-->>RS: Recipe
    RS-->>C: Recipe
    C-->>U: redirect:/recipes/{id} + message flash
```

## 7. Séquence « Frigo Magique »

```mermaid
sequenceDiagram
    actor U as Utilisateur connecté
    participant C as FridgeController
    participant SS as SubscriptionService
    participant PS as PantryService
    participant SE as SearchService
    participant RR as RecipeRepository
    participant PR as PriceService

    U->>C: POST /fridge (ingrédients cochés, ustensiles, servings=2, maxTime=30, régime, allergènes)
    C->>SS: checkFridgeSearch(user)
    alt FREE et 3 recherches déjà faites aujourd'hui
        SS-->>C: BusinessException
        C-->>U: redirect:/offers + message flash
    else autorisé
        C->>PS: setFromFridge(user, ingredientIds)
        C->>SE: feasibleRecipes(user, criteria)
        SE->>RR: findCandidates(maxTime, vegetarian, vegan)
        RR-->>SE: List de Recipe
        loop pour chaque recette
            SE->>SE: allergènes de la recette ∩ allergies exclues → écartée
            SE->>SE: couverture de chaque ligne (RG-F1) et score (RG-F2)
            SE->>SE: ustensiles manquants
            SE->>PR: missingCost(recipe, pantry, servings)
            PR-->>SE: BigDecimal
        end
        SE->>SE: garder score >= 0,7 puis trier
        SE-->>C: List de FeasibilityResult
        C-->>U: fridge.html : « 100 % de vos ingrédients », « Manque 1 ingrédient (+0,80 €) »
    end
```

## 8. Séquence « Noter et commenter une recette »

```mermaid
sequenceDiagram
    actor U as Utilisateur connecté
    participant C as ReviewController
    participant RV as ReviewService
    participant RS as RecipeService
    participant RVR as ReviewRepository

    U->>C: POST /recipes/{id}/reviews (rating=5, comment)
    C->>RV: rate(user, id, 5, comment)
    RV->>RS: findById(id)
    RS-->>RV: Recipe
    alt l'utilisateur est l'auteur de la recette
        RV-->>C: BusinessException « Vous ne pouvez pas noter votre recette »
    else rating hors de 1 à 5
        RV-->>C: BusinessException
    else cas normal
        RV->>RVR: findByAuthorAndRecipe(user, recipe)
        alt avis déjà existant
            RVR-->>RV: Review
            RV->>RV: remplace rating et comment (RG-A1)
        else premier avis
            RVR-->>RV: vide
            RV->>RV: new Review(...)
        end
        RV->>RVR: save(review)
        RV->>RV: refreshAverage(recipe) (RG-A2)
        RV-->>C: Review
    end
    C-->>U: redirect:/recipes/{id} + message flash
```

## 9. Séquence « S'inscrire à un atelier »

```mermaid
sequenceDiagram
    actor U as Utilisateur connecté
    participant C as WorkshopController
    participant WS as WorkshopService
    participant WR as WorkshopRepository

    U->>C: POST /workshops/{id}/join
    C->>WS: join(id, user)
    WS->>WR: findById(id)
    WR-->>WS: Workshop
    alt atelier passé, plein ou organisé par l'utilisateur (RG-W1)
        WS-->>C: BusinessException
        C-->>U: redirect:/workshops + message d'erreur
    else places disponibles
        WS->>WS: workshop.getParticipants().add(user) (N-N)
        WS->>WR: save(workshop)
        WS-->>C: ok
        C-->>U: redirect:/workshops + « Inscription confirmée »
    end
```

## 10. Séquence « API REST : créer une recette depuis Bruno »

```mermaid
sequenceDiagram
    actor B as Bruno (client HTTP)
    participant A as RecipeApiController
    participant RS as RecipeService
    participant RR as RecipeRepository

    B->>A: POST /api/v1/recipes (JSON RecipeDTO)
    A->>A: @Valid RecipeDTO
    alt JSON invalide
        A-->>B: 400 Bad Request + messages de validation
    else valide
        A->>RS: create(dto)
        RS->>RR: save(recipe)
        RR-->>RS: Recipe
        RS-->>A: Recipe
        A-->>B: 201 Created + RecipeDTO
    end
    B->>A: DELETE /api/v1/recipes/42
    A->>RS: delete(42)
    alt recette introuvable
        A-->>B: 404 Not Found
    else supprimée
        A-->>B: 204 No Content
    end
```
