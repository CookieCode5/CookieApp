package com.cookiecode.cookieapp.config;   // adapt to your package

import com.cookiecode.cookieapp.model.*;
import com.cookiecode.cookieapp.repository.*;
import com.cookiecode.cookieapp.model.enums.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("!test")
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private final RecipeRepository recipes;
    private final IngredientRepository ingredients;
    private final UtensilRepository utensils;
    private final AllergenRepository allergens;
    private final WorkshopRepository workshops;
    private final UserRepository users;

    private final Map<String, Ingredient> ing = new HashMap<>();
    private final Map<String, Utensil> ut = new HashMap<>();
    private final Map<String, Allergen> al = new HashMap<>();

    public DataSeeder(RecipeRepository recipes, IngredientRepository ingredients,
                      UtensilRepository utensils, AllergenRepository allergens,
                      WorkshopRepository workshops, UserRepository users) {
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.utensils = utensils;
        this.allergens = allergens;
        this.workshops = workshops;
        this.users = users;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();

        if (recipes.count() > 0) return;   // already seeded
        User author = users.findAll().get(0);

        seedAllergens();
        seedIngredients();
        seedUtensils();
        seedRecipes(author);
        seedWorkshops(author);
    }

    // ================= Allergens =================
    private void seedAllergens() {
        for (String n : List.of("Gluten", "Milk", "Eggs", "Tree nuts", "Soy")) {
            Allergen a = new Allergen();
            a.setName(n);
            al.put(n, allergens.save(a));
        }
    }

    // ================= Ingredients (30) =================
    private void i(String name, Unit u, String price, Aisle aisle, String... allergenNames) {
        Ingredient x = new Ingredient();
        x.setName(name);
        x.setReferenceUnit(u);
        x.setPricePerUnit(new BigDecimal(price));   // price per reference unit
        x.setAisle(aisle);
        for (String a : allergenNames) x.getAllergens().add(al.get(a));
        ing.put(name, ingredients.save(x));
    }

    private void seedIngredients() {
        // GROCERY
        i("Flour",             Unit.G,        "0.0012", Aisle.GROCERY, "Gluten");
        i("Sugar",             Unit.G,        "0.0015", Aisle.GROCERY);
        i("Powdered sugar",    Unit.G,        "0.0020", Aisle.GROCERY);
        i("Brown sugar",       Unit.G,        "0.0020", Aisle.GROCERY);
        i("Baking powder",     Unit.G,        "0.0100", Aisle.GROCERY);
        i("Baker's yeast",     Unit.G,        "0.0150", Aisle.GROCERY);
        i("Salt",              Unit.G,        "0.0008", Aisle.GROCERY);
        i("Dark chocolate",    Unit.G,        "0.0100", Aisle.GROCERY, "Milk", "Soy");
        i("Cocoa powder",      Unit.G,        "0.0150", Aisle.GROCERY);
        i("Almond flour",      Unit.G,        "0.0120", Aisle.GROCERY, "Tree nuts");
        i("Hazelnuts",         Unit.G,        "0.0130", Aisle.GROCERY, "Tree nuts");
        i("Vanilla",           Unit.TEASPOON, "0.60",   Aisle.GROCERY);
        i("Honey",             Unit.G,        "0.0090", Aisle.GROCERY);
        i("Sunflower oil",     Unit.ML,       "0.0030", Aisle.GROCERY);
        i("Cornstarch",        Unit.G,        "0.0030", Aisle.GROCERY);
        i("Gelatin",           Unit.G,        "0.0400", Aisle.GROCERY);
        // FRESH
        i("Eggs",              Unit.PIECE,    "0.30",   Aisle.FRESH, "Eggs");
        i("Butter",            Unit.G,        "0.0100", Aisle.FRESH, "Milk");
        i("Milk",              Unit.ML,       "0.0012", Aisle.FRESH, "Milk");
        i("Plant-based milk",  Unit.ML,       "0.0018", Aisle.FRESH);
        i("Heavy cream",       Unit.ML,       "0.0050", Aisle.FRESH, "Milk");
        i("Mascarpone",        Unit.G,        "0.0100", Aisle.FRESH, "Milk");
        i("Shortcrust dough",  Unit.PIECE,    "1.50",   Aisle.FRESH, "Gluten", "Milk");
        // PRODUCE
        i("Lemon",             Unit.PIECE,    "0.50",   Aisle.PRODUCE);
        i("Apples",            Unit.PIECE,    "0.40",   Aisle.PRODUCE);
        i("Bananas",           Unit.PIECE,    "0.30",   Aisle.PRODUCE);
        i("Strawberries",      Unit.G,        "0.0080", Aisle.PRODUCE);
        // FROZEN
        i("Frozen raspberries", Unit.G,       "0.0100", Aisle.FROZEN);
        i("Frozen blueberries", Unit.G,       "0.0110", Aisle.FROZEN);
        i("Frozen puff pastry", Unit.PIECE,   "1.80",   Aisle.FROZEN, "Gluten", "Milk");
    }

    // ================= Utensils (8) =================
    private void seedUtensils() {
        Object[][] data = {
            {"Mixing bowl", true}, {"Whisk", true}, {"Oven", true}, {"Cake pan", true},
            {"Tart pan", false}, {"Saucepan", true}, {"Spatula", true}, {"Stand mixer", false}
        };
        for (Object[] d : data) {
            Utensil u = new Utensil();
            u.setName((String) d[0]);
            u.setCommon((Boolean) d[1]);
            ut.put(u.getName(), utensils.save(u));
        }
    }

    // ================= Recipe helpers =================
    private Recipe recipe(User author, String title, String desc, Difficulty d,
                          int servings, int prep, int cook,
                          boolean vegetarian, boolean vegan, boolean premium) {
        Recipe r = new Recipe();
        r.setAuthor(author);
        r.setTitle(title);
        r.setDescription(desc);
        r.setDifficulty(d);
        r.setBaseServings(servings);
        r.setPreparationTime(prep);
        r.setCookingTime(cook);
        r.setVegetarian(vegetarian);
        r.setVegan(vegan);
        r.setPremium(premium);
        r.setAverageRating(0);
        r.setReviewCount(0);
        return r;
    }

    private void step(Recipe r, int order, String title, String text, int min, String tip, boolean parallelizable) {
        Step s = new Step();
        s.setRecipe(r);
        s.setStepOrder(order);
        s.setTitle(title);
        s.setInstruction(text);
        s.setDurationMinutes(min);
        s.setTip(tip);
        s.setParallelizable(parallelizable);
        r.getSteps().add(s);
    }

    private void line(Recipe r, String name, double qty, Unit u, boolean optional) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setRecipe(r);
        ri.setIngredient(ing.get(name));
        ri.setQuantity(qty);
        ri.setUnit(u);
        ri.setOptional(optional);
        r.getIngredients().add(ri);
    }

    private void uses(Recipe r, String... names) {
        for (String n : names) r.getUtensils().add(ut.get(n));
    }

    // ================= Recipes (8) =================
    private void seedRecipes(User a) {

        // 1. Cake
        Recipe r1 = recipe(a, "Moist Lemon Cake", "The classic afternoon treat, scented with fresh zest.",
                Difficulty.EASY, 6, 15, 35, true, false, false);
        line(r1, "Flour", 250, Unit.G, false);
        line(r1, "Sugar", 150, Unit.G, false);
        line(r1, "Eggs", 3, Unit.PIECE, false);
        line(r1, "Butter", 120, Unit.G, false);
        line(r1, "Milk", 100, Unit.ML, false);
        line(r1, "Baking powder", 10, Unit.G, false);
        line(r1, "Lemon", 2, Unit.PIECE, false);
        step(r1, 1, "Preheat", "Preheat the oven to 180 °C, then butter and flour the pan.", 5, "Flouring the pan stops the cake from sticking.", true);
        step(r1, 2, "Mix", "Whisk eggs and sugar, then add melted butter, milk and zest.", 10, "Zest the lemons before juicing them.", false);
        step(r1, 3, "Fold in", "Add the sifted flour and baking powder.", 5, "Do not overmix the batter.", false);
        step(r1, 4, "Bake", "Bake for 35 minutes and check with a knife tip.", 35, "Do not open the oven before 25 minutes.", false);
        uses(r1, "Mixing bowl", "Whisk", "Oven", "Cake pan");
        recipes.save(r1);

        // 2. Cookies
        Recipe r2 = recipe(a, "Chocolate Chip Cookies", "Crispy outside, soft and gooey inside.",
                Difficulty.EASY, 12, 15, 12, true, false, false);
        line(r2, "Flour", 250, Unit.G, false);
        line(r2, "Butter", 125, Unit.G, false);
        line(r2, "Brown sugar", 120, Unit.G, false);
        line(r2, "Eggs", 1, Unit.PIECE, false);
        line(r2, "Dark chocolate", 150, Unit.G, false);
        line(r2, "Baking powder", 5, Unit.G, false);
        step(r2, 1, "Cream", "Beat the softened butter with the brown sugar.", 5, "Use butter at room temperature.", false);
        step(r2, 2, "Assemble", "Add the egg, the flour, then the chopped chocolate.", 5, "Chop the chocolate into big chunks.", false);
        step(r2, 3, "Bake", "Place spaced-out dough balls on a tray, bake 12 minutes at 180 °C.", 12, "Take them out as soon as the edges turn golden.", false);
        uses(r2, "Mixing bowl", "Spatula", "Oven");
        recipes.save(r2);

        // 3. Apple tart
        Recipe r3 = recipe(a, "Apple Tart", "Rustic tart with thinly sliced apples.",
                Difficulty.MEDIUM, 8, 20, 35, true, false, false);
        line(r3, "Shortcrust dough", 1, Unit.PIECE, false);
        line(r3, "Apples", 5, Unit.PIECE, false);
        line(r3, "Sugar", 60, Unit.G, false);
        line(r3, "Butter", 30, Unit.G, false);
        line(r3, "Vanilla", 1, Unit.TEASPOON, true);
        step(r3, 1, "Line the pan", "Roll the dough into the pan and prick it with a fork.", 5, "Pricking prevents air bubbles.", false);
        step(r3, 2, "Arrange", "Lay the thin apple slices on the dough.", 10, "Keep the slices thin and even.", true);
        step(r3, 3, "Bake", "Bake 35 minutes at 180 °C with butter dots and sugar.", 35, "Sprinkle sugar at the end for a caramelised top.", false);
        uses(r3, "Tart pan", "Oven");
        recipes.save(r3);

        // 4. Strawberry tart
        Recipe r4 = recipe(a, "Strawberry Mascarpone Tart", "Light cream and fresh strawberries.",
                Difficulty.MEDIUM, 8, 40, 25, true, false, false);
        line(r4, "Shortcrust dough", 1, Unit.PIECE, false);
        line(r4, "Mascarpone", 250, Unit.G, false);
        line(r4, "Strawberries", 400, Unit.G, false);
        line(r4, "Powdered sugar", 50, Unit.G, false);
        line(r4, "Vanilla", 1, Unit.TEASPOON, true);
        step(r4, 1, "Blind bake", "Bake 20 minutes at 180 °C with baking beans.", 20, "Weigh the dough down so it does not puff up.", false);
        step(r4, 2, "Cream", "Whisk mascarpone, powdered sugar and vanilla.", 10, "Keep the mascarpone very cold.", true);
        step(r4, 3, "Assemble", "Fill the shell with cream, then top with strawberries.", 10, "Serve chilled.", false);
        uses(r4, "Tart pan", "Oven", "Whisk");
        recipes.save(r4);

        // 5. Brownie
        Recipe r5 = recipe(a, "Fudgy Brownie", "Intense chocolate flavour.",
                Difficulty.EASY, 9, 15, 25, true, false, false);
        line(r5, "Dark chocolate", 200, Unit.G, false);
        line(r5, "Butter", 150, Unit.G, false);
        line(r5, "Sugar", 150, Unit.G, false);
        line(r5, "Eggs", 3, Unit.PIECE, false);
        line(r5, "Flour", 80, Unit.G, false);
        line(r5, "Hazelnuts", 50, Unit.G, true);
        step(r5, 1, "Melt", "Melt chocolate and butter in a double boiler.", 8, "Use low heat so the chocolate does not burn.", false);
        step(r5, 2, "Mix", "Add eggs, sugar, then flour.", 7, "Do not beat too much.", false);
        step(r5, 3, "Bake", "Bake 25 minutes at 180 °C.", 25, "The centre should stay slightly wobbly.", false);
        uses(r5, "Saucepan", "Mixing bowl", "Cake pan", "Oven");
        recipes.save(r5);

        // 6. Vegan banana bread
        Recipe r6 = recipe(a, "Vegan Banana Bread", "No eggs and no dairy.",
                Difficulty.EASY, 6, 10, 40, true, true, false);
        line(r6, "Bananas", 3, Unit.PIECE, false);
        line(r6, "Flour", 200, Unit.G, false);
        line(r6, "Sugar", 80, Unit.G, false);
        line(r6, "Sunflower oil", 60, Unit.ML, false);
        line(r6, "Plant-based milk", 80, Unit.ML, false);
        line(r6, "Baking powder", 8, Unit.G, false);
        step(r6, 1, "Mash", "Mash the bananas with a fork.", 5, "The riper the bananas, the sweeter the bread.", false);
        step(r6, 2, "Mix", "Add oil, plant-based milk, sugar, flour and baking powder.", 5, "Mix only until combined.", false);
        step(r6, 3, "Bake", "Bake 40 minutes at 180 °C.", 40, "Cover with foil if the top browns too fast.", false);
        uses(r6, "Mixing bowl", "Cake pan", "Oven");
        recipes.save(r6);

        // 7. Croissants (premium)
        Recipe r7 = recipe(a, "Butter Croissants", "Flaky bakery-style viennoiserie.",
                Difficulty.HARD, 8, 90, 20, true, false, true);
        line(r7, "Flour", 500, Unit.G, false);
        line(r7, "Sugar", 60, Unit.G, false);
        line(r7, "Salt", 10, Unit.G, false);
        line(r7, "Baker's yeast", 10, Unit.G, false);
        line(r7, "Milk", 250, Unit.ML, false);
        line(r7, "Butter", 280, Unit.G, false);
        line(r7, "Eggs", 1, Unit.PIECE, false);
        step(r7, 1, "Base dough", "Knead flour, sugar, salt, yeast and milk, then rest in the fridge.", 30, "Rest for 1 hour in the fridge.", true);
        step(r7, 2, "Laminate", "Enclose the butter and do 3 simple folds.", 40, "Butter and dough should be at the same temperature.", false);
        step(r7, 3, "Shape", "Cut triangles and roll them up.", 20, "Roll without squeezing too tightly.", false);
        step(r7, 4, "Proof and bake", "Egg-wash, proof 2 hours, bake 20 minutes at 190 °C.", 20, "The croissants should double in volume.", false);
        uses(r7, "Stand mixer", "Oven", "Mixing bowl");
        recipes.save(r7);

        // 8. Raspberry bavarois (gelatin -> not vegetarian)
        Recipe r8 = recipe(a, "Raspberry Bavarois", "Light and tangy chilled dessert.",
                Difficulty.MEDIUM, 8, 40, 0, false, false, false);
        line(r8, "Frozen raspberries", 300, Unit.G, false);
        line(r8, "Heavy cream", 300, Unit.ML, false);
        line(r8, "Sugar", 80, Unit.G, false);
        line(r8, "Gelatin", 8, Unit.G, false);
        line(r8, "Eggs", 2, Unit.PIECE, false);
        line(r8, "Lemon", 1, Unit.PIECE, true);
        step(r8, 1, "Coulis", "Blend the raspberries with the sugar, then strain.", 10, "Strain to remove the seeds.", true);
        step(r8, 2, "Gelatin", "Soften the gelatin and dissolve it in the warm coulis.", 10, "Never let it boil.", false);
        step(r8, 3, "Cream", "Whip the cream and gently fold it in.", 10, "The cream must be very cold.", false);
        step(r8, 4, "Set", "Pour into the mould and chill for 4 hours.", 240, "Make it the day before.", false);
        uses(r8, "Saucepan", "Whisk", "Mixing bowl", "Cake pan");
        recipes.save(r8);
    }

    // ================= Workshops (2) =================
    private void seedWorkshops(User organizer) {
        List<Recipe> all = recipes.findAll();
        workshops.save(workshop(organizer, all.get(6), "Croissants à plusieurs"));
        workshops.save(workshop(organizer, all.get(3), "Tarte aux fraises entre amis"));
    }

    private Workshop workshop(User organizer, Recipe recipe, String title) {
        Workshop w = new Workshop();
        w.setOrganizer(organizer);
        w.setRecipe(recipe);
        w.setTitle(title);
        return w;
    }
    private void seedUsers(){
        if (users.count() > 0) return;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        createAccount(
            "free@mail.com",
            "FreeUser",
            encoder.encode("FreePassword"),
            Role.USER,
            SubscriptionType.FREE,
            Set.of("Gluten")
        );

        createAccount(
            "premium@mail.com",
            "PremiumUser",
            encoder.encode("PremiumPassword"),
            Role.USER,
            SubscriptionType.PREMIUM,
            Set.of("Lactose")
        );

        createAccount(
            "admin@mail.com",
            "AdminUser",
            encoder.encode("AdminPassword"),
            Role.ADMIN,
            SubscriptionType.PREMIUM,
            Set.of()
        );
    }

    private void createAccount(String email, String username, String password_hash, Role role, SubscriptionType subType, Set<String> allergies){

        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setPassword_hash(password_hash);
        u.setRole(role);

        for (String name : allergies) {
            Allergen allergen = al.get(name);
            if (allergen != null){
                u.getAllergies().add(allergen);
            }
        }

        User saved_users = users.save(u);

        Subscription sub = new Subscription();
        sub.setUser(saved_users);
        

    }
}