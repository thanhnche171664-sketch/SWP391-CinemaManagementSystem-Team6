// Login Form Handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const submitBtn = loginForm.querySelector('.btn-login');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');

    // Ensure password input is always type="password" on load
    if (passwordInput) {
        passwordInput.type = 'password';
    }

    // InsertAfter helper for Element
    if (!Element.prototype.insertAfter) {
        Element.prototype.insertAfter = function(newNode, referenceNode) {
            this.insertBefore(newNode, referenceNode.nextSibling);
        };
    }

    // Clear previous error states on input
    [emailInput, passwordInput].forEach(input => {
        if (!input) return;
        input.addEventListener('input', function() {
            clearFieldError(this);
        });
    });

    // Helper: set error on a field
    function setFieldError(input, message) {
        clearFieldError(input);
        const wrapper = input.closest('.input-wrapper') || input.parentElement;
        const errorSpan = document.createElement('span');
        errorSpan.className = 'field-error';
        errorSpan.textContent = message;
        wrapper.insertAfter(errorSpan, input.nextSibling);
        input.style.borderColor = 'var(--error-color)';
    }

    // Helper: clear error on a field
    function clearFieldError(input) {
        input.style.borderColor = '';
        const wrapper = input.closest('.input-wrapper') || input.parentElement;
        const existing = wrapper.querySelector('.field-error');
        if (existing) existing.remove();
    }

    // Validate a single field
    function validateField(input) {
        const value = input.value.trim();

        if (input.id === 'email') {
            if (!value) {
                setFieldError(input, 'Email is required');
                return false;
            }
            if (!isValidEmail(value)) {
                setFieldError(input, 'Invalid email format');
                return false;
            }
            clearFieldError(input);
            return true;
        }

        if (input.id === 'password') {
            if (!value) {
                setFieldError(input, 'Password is required');
                return false;
            }
            clearFieldError(input);
            return true;
        }

        return true;
    }

    // Blur validation
    emailInput.addEventListener('blur', function() {
        const value = this.value.trim();
        if (!value) return;
        if (!isValidEmail(value)) {
            this.style.borderColor = 'var(--error-color)';
        } else {
            clearFieldError(this);
        }
    });

    // Form Submit Handler
    loginForm.addEventListener('submit', function(e) {
        let valid = true;

        if (!validateField(emailInput)) valid = false;
        if (!validateField(passwordInput)) valid = false;

        if (!valid) {
            e.preventDefault();
            const firstError = loginForm.querySelector('.field-error');
            if (firstError) {
                const errorInput = firstError.previousElementSibling;
                if (errorInput && (errorInput.id === 'email' || errorInput.id === 'password')) {
                    errorInput.focus();
                }
            }
            return;
        }

        submitBtn.classList.add('loading');
        submitBtn.textContent = 'Đang xử lý...';
        submitBtn.disabled = true;
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
        if (eyeIcon) eyeIcon.classList.add('hidden');
        if (eyeSlashIcon) eyeSlashIcon.classList.remove('hidden');
    } else {
        passwordInput.type = 'password';
        if (eyeIcon) eyeIcon.classList.remove('hidden');
        if (eyeSlashIcon) eyeSlashIcon.classList.add('hidden');
    }
}

// Google Login (placeholder)
function loginWithGoogle() {
    alert('Google login integration coming soon!');
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
    .field-error {
        display: block;
        color: #f56565;
        font-size: 12px;
        margin-top: 4px;
        padding-left: 2px;
    }
`;
document.head.appendChild(style);
