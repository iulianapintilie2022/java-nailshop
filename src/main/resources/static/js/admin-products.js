const productDialog = document.querySelector('.product-dialog');
const productDialogContent = productDialog.querySelector('.product-dialog-content');
let productFormTrigger;
let productFormLoading = false;

document.querySelectorAll('.open-product-form').forEach(link => {
    link.addEventListener('click', async event => {
        // Keep normal links available for new tabs and browsers without dialog support.
        if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
            || typeof productDialog.showModal !== 'function') {
            return;
        }

        event.preventDefault();
        if (productFormLoading) return;
        productFormLoading = true;
        link.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(link.href);
            if (!response.ok) throw new Error('Could not load product form');
            const formPage = new DOMParser().parseFromString(await response.text(), 'text/html');
            const editor = formPage.querySelector('.product-editor');
            if (!editor) throw new Error('Product form unavailable');

            productDialogContent.replaceChildren(document.importNode(editor, true));
            productDialogContent.querySelector('.editor-cancel').addEventListener('click', event => {
                event.preventDefault();
                productDialog.close();
            });
            productFormTrigger = link;
            document.body.classList.add('product-panel-open');
            productDialog.showModal();
            productDialogContent.querySelector('input[name="name"]').focus();
        } catch {
            // The existing standalone form route remains the fallback.
            window.location.assign(link.href);
        } finally {
            productFormLoading = false;
            link.removeAttribute('aria-busy');
        }
    });
});

productDialog.querySelector('.dialog-close').addEventListener('click', () => productDialog.close());
productDialog.addEventListener('close', () => {
    document.body.classList.remove('product-panel-open');
    productDialogContent.replaceChildren();
    productFormTrigger?.focus({preventScroll: true});
});

const deleteProductDialog = document.querySelector('.delete-product-dialog');
let deleteProductTrigger;

document.querySelectorAll('[data-delete-url]').forEach(button => {
    button.addEventListener('click', () => {
        if (typeof deleteProductDialog.showModal !== 'function') {
            if (window.confirm(`Delete this product?\n${button.dataset.productName}`)) {
                window.location.assign(button.dataset.deleteUrl);
            }
            return;
        }

        deleteProductTrigger = button;
        deleteProductDialog.querySelector('#delete-product-name').textContent = button.dataset.productName;
        document.body.classList.add('product-panel-open');
        deleteProductDialog.showModal();
        deleteProductDialog.querySelector('.cancel-product-delete').focus();
    });
});

deleteProductDialog.querySelector('.cancel-product-delete').addEventListener('click', () => deleteProductDialog.close());
deleteProductDialog.querySelector('.confirm-product-delete').addEventListener('click', () => {
    if (deleteProductTrigger) window.location.assign(deleteProductTrigger.dataset.deleteUrl);
});
deleteProductDialog.addEventListener('close', () => {
    document.body.classList.remove('product-panel-open');
    deleteProductTrigger?.focus({preventScroll: true});
    deleteProductTrigger = undefined;
});
