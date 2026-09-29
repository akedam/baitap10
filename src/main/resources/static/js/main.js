const tokenKey = "jwtToken";

function bearerHeaders() {
    const token = localStorage.getItem(tokenKey);
    return token ? {Authorization: `Bearer ${token}`} : {};
}

const loginForm = document.querySelector("#login-form");
if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = document.querySelector("#message");
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                email: document.querySelector("#email").value,
                password: document.querySelector("#password").value
            })
        });
        const data = await response.json();
        if (!response.ok) {
            message.textContent = data.message ?? "Đăng nhập thất bại";
            return;
        }
        localStorage.setItem(tokenKey, data.token);
        window.location.href = "/profile";
    });
}

const profile = document.querySelector("#profile");
if (profile) {
    fetch("/users/me", {headers: bearerHeaders()})
        .then(async (response) => {
            const data = await response.json();
            if (!response.ok) throw new Error(data.message ?? "Không thể tải hồ sơ");
            return data;
        })
        .then((data) => renderProfile(data))
        .catch((error) => {
            document.querySelector("#profile-status").textContent = error.message;
        });
}

function renderProfile(data) {
    const image = document.querySelector("#profile-image");
    const fallback = document.querySelector("#avatar-fallback");
    const imagePath = profileImagePath(data.image);

    fallback.textContent = (data.fullName || data.email || "U").trim().charAt(0).toUpperCase();
    image.addEventListener("error", () => {
        image.style.display = "none";
        fallback.style.display = "block";
    }, {once: true});
    image.src = imagePath;

    document.querySelector("#profile-name").textContent = data.fullName || "Chưa có họ tên";
    document.querySelector("#profile-email").textContent = data.email || "";
    document.querySelector("#profile-id").textContent = data.id ?? "—";
    document.querySelector("#profile-role").textContent = data.role || "USER";
    document.querySelector("#profile-created-at").textContent = formatDate(data.createdAt);
    document.querySelector("#profile-status").hidden = true;
    profile.hidden = false;
}

function profileImagePath(value) {
    if (!value) return "/images/avatar-default.svg";
    if (/^(https?:|data:|\/)/i.test(value)) return value;
    return `/images/${encodeURIComponent(value)}`;
}

function formatDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat("vi-VN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
}

document.querySelector("#logout")?.addEventListener("click", () => {
    localStorage.removeItem(tokenKey);
    window.location.href = "/login";
});
