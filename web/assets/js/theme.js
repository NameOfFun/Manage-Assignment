
// Restore stored preference if any; otherwise let the CSS prefers-color-scheme media query decide.
const root = document.documentElement, tog = document.getElementById('tog');
const savedTheme = localStorage.getItem('ams-theme');
if (savedTheme === 'light' || savedTheme === 'dark') {
    root.dataset.theme = savedTheme;
}
const dark = () => root.dataset.theme
            ? root.dataset.theme === 'dark'
            : matchMedia('(prefers-color-scheme: dark)').matches;
if (tog) {
    tog.setAttribute('aria-label',
            dark() ? 'Switch to the light theme' : 'Switch to the dark theme');
    tog.addEventListener('click', () => {
        const next = dark() ? 'light' : 'dark';
        root.dataset.theme = next;
        localStorage.setItem('ams-theme', next);
        tog.setAttribute('aria-label',
                dark() ? 'Switch to the light theme' : 'Switch to the dark theme');
    });
}
