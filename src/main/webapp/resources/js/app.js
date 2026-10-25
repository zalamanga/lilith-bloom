(() => {
    'use strict';
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.dataset.submitting === 'true') { event.preventDefault(); return; }
            form.dataset.submitting = 'true';
            form.setAttribute('aria-busy', 'true');
            document.getElementById('loading').hidden = false;
        });
    });
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('form').forEach(form => { delete form.dataset.submitting; form.removeAttribute('aria-busy'); });
        document.getElementById('loading').hidden = true;
    });
})();
