// Login Form Handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const submitBtn = loginForm.querySelector('.btn-login');
    
    // Ensure password input is always type="password" on load
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.type = 'password';
    }
    
    // Form Submit Handler
    loginForm.addEventListener('submit', function(e) {
        // Add loading state
        submitBtn.classList.add('loading');
        submitBtn.textContent = 'Đang xử lý...';
    });
    
    // Auto hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.animation = 'slideUp 0.3s ease-out';
            setTimeout(() => {
                alert.style.display = 'none';
            }, 300);
        }, 5000);
    });
    
    // Email validation
    const emailInput = document.getElementById('email');
    emailInput.addEventListener('blur', function() {
        const email = this.value.trim();
        if (email && !isValidEmail(email)) {
            this.style.borderColor = 'var(--error-color)';
        } else {
            this.style.borderColor = 'var(--border-color)';
        }
    });
});

// Toggle password visibility
function togglePassword(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.querySelector('.eye-icon');
    const eyeSlashIcon = document.querySelector('.eye-slash-icon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.classList.add('hidden');
        eyeSlashIcon.classList.remove('hidden');
    } else {
        passwordInput.type = 'password';
        eyeIcon.classList.remove('hidden');
        eyeSlashIcon.classList.add('hidden');
    }
}

// Google Login (placeholder)
function loginWithGoogle() {
    alert('Google login integration coming soon!');
    // TODO: Implement Google OAuth
}

// Helper function to validate email
function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Slideup animation for alerts
const style = document.createElement('style');
style.textContent = `
    @keyframes slideUp {
        from {
            opacity: 1;
            transform: translateY(0);
        }
        to {
            opacity: 0;
            transform: translateY(-10px);
        }
    }
`;
document.head.appendChild(style);
