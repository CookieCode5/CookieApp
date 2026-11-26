document.addEventListener('DOMContentLoaded', () => {
    // ---------- INGREDIENTS ----------
    const ingredientsBody = document.getElementById('ingredientsBody');
    const ingredientTpl = document.getElementById('ingredientRowTemplate');
    const ingredientCount = document.getElementById('ingredientCount');

    function reindexIngredients() {
        [...ingredientsBody.querySelectorAll('tr.ingredient-row')].forEach((row, i) => {
            row.querySelectorAll('[name^="ingredients["]').forEach(el => {
                el.name = el.name.replace(/ingredients\[\d+]/, `ingredients[${i}]`);
            });
        });
        ingredientCount.textContent = ingredientsBody.querySelectorAll('tr.ingredient-row').length + ' ingrédient(s)';
    }

    document.getElementById('addIngredientBtn').addEventListener('click', () => {
        const index = ingredientsBody.querySelectorAll('tr.ingredient-row').length;
        const html = ingredientTpl.innerHTML.replaceAll('__INDEX__', index);
        ingredientsBody.insertAdjacentHTML('beforeend', html);
        reindexIngredients();
    });

    ingredientsBody.addEventListener('click', (e) => {
        const btn = e.target.closest('.remove-ingredient');
        if (!btn) return;
        if (ingredientsBody.querySelectorAll('tr.ingredient-row').length > 1) {
            btn.closest('tr').remove();
            reindexIngredients();
        }
    });

    // ---------- STEPS ----------
    const stepsContainer = document.getElementById('stepsContainer');
    const stepTpl = document.getElementById('stepTemplate');
    const stepCount = document.getElementById('stepCount');

    function reindexSteps() {
        [...stepsContainer.querySelectorAll('.step-card')].forEach((card, i) => {
            card.querySelectorAll('[name^="steps["]').forEach(el => {
                el.name = el.name.replace(/steps\[\d+]/, `steps[${i}]`);
            });
            const badge = card.querySelector('.step-number');
            if (badge) badge.textContent = i + 1;
        });
        stepCount.textContent = stepsContainer.querySelectorAll('.step-card').length + ' étape(s)';
    }

    document.getElementById('addStepBtn').addEventListener('click', () => {
        const index = stepsContainer.querySelectorAll('.step-card').length;
        const html = stepTpl.innerHTML.replaceAll('__INDEX__', index);
        stepsContainer.insertAdjacentHTML('beforeend', html);
        reindexSteps();
    });

    stepsContainer.addEventListener('click', (e) => {
        const btn = e.target.closest('.remove-step');
        if (!btn) return;
        if (stepsContainer.querySelectorAll('.step-card').length > 1) {
            btn.closest('.step-card').remove();
            reindexSteps();
        }
    });
});