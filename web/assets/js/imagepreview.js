document.addEventListener("DOMContentLoaded", () => {
    const avatarInput = document.getElementById("avatarInput");
    const avatarPreview = document.getElementById("avatarPreview");

    avatarInput.addEventListener("change", (event) => {
        const file = event.target.files[0];

        if (file) {
            // Generate a local temporary URL pointing to the file in browser memory
            const tempUrl = URL.createObjectURL(file);
            
            // Instantly update the image src in the DOM
            avatarPreview.src = tempUrl;

            // Free memory when the image finishes loading
            avatarPreview.onload = () => {
                URL.revokeObjectURL(tempUrl);
            };
        }
    });
});