const categoryDialog = document.querySelector('.category-dialog');
const categoryDialogContent = categoryDialog.querySelector('.category-dialog-content');
let categoryFormTrigger;
let categoryFormLoading = false;

document.querySelectorAll('.open-category-form').forEach(link => {
    link.addEventListener('click', async event => {
        if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
            || typeof categoryDialog.showModal !== 'function') return;

        event.preventDefault();
        if (categoryFormLoading) return;
        categoryFormLoading = true;
        link.setAttribute('aria-busy', 'true');
        try {
            const response = await fetch(link.href);
            if (!response.ok) throw new Error('Could not load category form');
            const formPage = new DOMParser().parseFromString(await response.text(), 'text/html');
            const editor = formPage.querySelector('.category-editor');
            if (!editor) throw new Error('Category form unavailable');

            categoryDialogContent.replaceChildren(document.importNode(editor, true));
            categoryDialogContent.querySelector('.category-editor-cancel').addEventListener('click', event => {
                event.preventDefault();
                categoryDialog.close();
            });
            categoryFormTrigger = link;
            document.body.classList.add('product-panel-open');
            categoryDialog.showModal();
            categoryDialogContent.querySelector('input[name="name"]').focus();
        } catch {
            window.location.assign(link.href);
        } finally {
            categoryFormLoading = false;
            link.removeAttribute('aria-busy');
        }
    });
});

categoryDialog.querySelector('.dialog-close').addEventListener('click', () => categoryDialog.close());
categoryDialog.addEventListener('close', () => {
    document.body.classList.remove('product-panel-open');
    categoryDialogContent.replaceChildren();
    categoryFormTrigger?.focus({preventScroll: true});
});

const deleteCategoryDialog = document.querySelector('.delete-category-dialog');
let categoryDeleteForm;
let categoryDeleteTrigger;

document.querySelectorAll('.open-category-delete').forEach(button => {
    button.addEventListener('click', () => {
        const form = button.closest('form');
        if (typeof deleteCategoryDialog.showModal !== 'function') {
            if (window.confirm(`Delete this category?\n${form.dataset.categoryName}`)) form.requestSubmit();
            return;
        }

        categoryDeleteForm = form;
        categoryDeleteTrigger = button;
        deleteCategoryDialog.querySelector('#delete-category-name').textContent = form.dataset.categoryName;
        document.body.classList.add('product-panel-open');
        deleteCategoryDialog.showModal();
        deleteCategoryDialog.querySelector('.cancel-category-delete').focus();
    });
});

deleteCategoryDialog.querySelector('.cancel-category-delete').addEventListener('click', () => deleteCategoryDialog.close());
deleteCategoryDialog.querySelector('.confirm-category-delete').addEventListener('click', () => {
    categoryDeleteForm?.requestSubmit();
});
deleteCategoryDialog.addEventListener('close', () => {
    document.body.classList.remove('product-panel-open');
    categoryDeleteTrigger?.focus({preventScroll: true});
    categoryDeleteForm = undefined;
    categoryDeleteTrigger = undefined;
});
