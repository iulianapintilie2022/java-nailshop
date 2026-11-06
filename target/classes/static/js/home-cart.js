// The existing cart endpoint returns text; submit in place to keep the page and scroll position.
document.querySelectorAll('.add-to-cart-form').forEach(form => {
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const button = form.querySelector('button');
        button.disabled = true;

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: new FormData(form)
            });

            if (response.status === 409) {
                button.textContent = 'Stock limit reached';
                button.classList.add('cart-error');
                return;
            }

            if (!response.ok) {
                throw new Error('Could not add product to cart');
            }

            button.textContent = 'Added ✓';
            setTimeout(() => {
                button.textContent = 'Add to Cart';
                button.disabled = false;
            }, 1000);
        } catch (error) {
            button.textContent = 'Try again';
            button.disabled = false;
            console.error(error);
        }
    });
});
