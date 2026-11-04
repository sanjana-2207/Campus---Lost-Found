(function () {
    const root = document.documentElement;

    function currentTheme() {
        return root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
    }

    function syncThemeButton() {
        const button = document.getElementById('themeToggle');
        if (!button) {
            return;
        }
        button.textContent = currentTheme() === 'dark' ? 'Light' : 'Dark';
        button.setAttribute('title', currentTheme() === 'dark'
            ? 'Switch to light theme' : 'Switch to dark theme');
    }

    function toggleTheme() {
        const next = currentTheme() === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        syncThemeButton();
    }

    document.addEventListener('DOMContentLoaded', function () {
        syncThemeButton();
        const themeButton = document.getElementById('themeToggle');
        if (themeButton) {
            themeButton.addEventListener('click', toggleTheme);
        }

        const input = document.getElementById('photoInput');
        const preview = document.getElementById('photoPreview');
        const clear = document.getElementById('clearPhoto');

        if (input && preview) {
            input.addEventListener('change', function () {
                const file = input.files && input.files[0];
                if (!file) {
                    return;
                }
                const reader = new FileReader();
                reader.onload = function (event) {
                    preview.innerHTML = '<img alt="Selected photo" src="' + event.target.result + '">';
                };
                reader.readAsDataURL(file);
            });
        }

        if (clear && input && preview) {
            clear.addEventListener('click', function () {
                input.value = '';
                preview.innerHTML = '<span>No photo selected</span>';
            });
        }
    });
})();
