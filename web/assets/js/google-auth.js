// google-auth.js

// Callback triggered when Google OAuth succeeds
function handleCredentialResponse(response) {
    const credentialInput = document.getElementById('google_credential');
    const googleForm = document.getElementById('google-form');

    if (credentialInput && googleForm) {
        credentialInput.value = response.credential;
        googleForm.submit();
    } else {
        console.error("Google login form elements missing from DOM.");
    }
}

// Global initialization function invoked when Google SDK finishes loading
window.initGoogleAuth = function () {
    // Verify the global 'google' object exists on window
    if (typeof google === 'undefined' || !google.accounts || !google.accounts.id) {
        console.error("Google Identity Services SDK not loaded.");
        return;
    }

    const btnContainer = document.getElementById("google-btn-container");
    if (!btnContainer) {
        console.error("Container #google-btn-container not found in DOM.");
        return;
    }

    // Initialize Google Identity
    google.accounts.id.initialize({
        client_id: "1085471992590-m6fvbptmmv7q5kuft7bmn3dfrrbfaud4.apps.googleusercontent.com",
        callback: handleCredentialResponse
    });

    // Render the button
    google.accounts.id.renderButton(btnContainer, {
        type: "standard",
        theme: "outline",
        size: "large",
        text: "signin_with",
        locale: "en",
        shape: "rectangular",
        logo_alignment: "left",
        width: 336
    });
};

// Fallback: If google SDK loaded BEFORE initGoogleAuth was attached during a forward,
// trigger it manually once DOM is ready.
document.addEventListener("DOMContentLoaded", function () {
    if (typeof google !== 'undefined' && google.accounts) {
        window.initGoogleAuth();
    }
});