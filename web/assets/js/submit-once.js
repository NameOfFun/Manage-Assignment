// Prevents double submits (double click / Enter twice): once a form marked data-submit-once is sent,
// its submit buttons are disabled and show "Saving...". Load with "defer" so it runs after
// page scripts; a submit those scripts cancelled (preventDefault) is ignored.
document.querySelectorAll('form[data-submit-once]').forEach(form => {
    form.addEventListener('submit', event => {
        if (event.defaultPrevented) {
            return;
        }
        if (form.dataset.submitting) {
            event.preventDefault();
            return;
        }
        form.dataset.submitting = '1';
        form.querySelectorAll('button[type="submit"], button:not([type])').forEach(button => {
            button.disabled = true;
            button.dataset.label = button.textContent;
            button.textContent = 'Saving...';
        });
    });
});

// Back/forward cache: re-enable the buttons if the user comes back to this page.
window.addEventListener('pageshow', event => {
    if (!event.persisted) {
        return;
    }
    document.querySelectorAll('form[data-submitting]').forEach(form => {
        delete form.dataset.submitting;
        form.querySelectorAll('button[disabled][data-label]').forEach(button => {
            button.disabled = false;
            button.textContent = button.dataset.label;
        });
    });
});
