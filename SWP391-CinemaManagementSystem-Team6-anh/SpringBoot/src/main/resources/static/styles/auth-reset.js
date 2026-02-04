// Reset Password Page Script
document.addEventListener('DOMContentLoaded', function() {
    const resetForm = document.getElementById('resetForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Form submit
    resetForm.addEventListener('submit', function(e) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            e.preventDefault();
            showModal('Error', 'Passwords do not match!', 'error');
            confirmPasswordInput.focus();
            return false;
        }

        if (passwordInput.value.length < 6) {
            e.preventDefault();
            showModal('Error', 'Password must be at least 6 characters', 'error');
            passwordInput.focus();
            return false;
        }
    });

    // Real-time password match validation
    confirmPasswordInput.addEventListener('input', function() {
        if (this.value && passwordInput.value !== this.value) {
            this.style.borderColor = 'var(--error-color)';
        } else {
            this.style.borderColor = 'var(--border-color)';
        }
    });

    // Auto hide alerts
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
function togglePassword(inputId) {
    const passwordInput = document.getElementById(inputId);
    const toggleButton = passwordInput.parentElement.querySelector('.toggle-password');
    const eyeIcon = toggleButton.querySelector('.eye-icon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>';
    } else {
        passwordInput.type = 'password';
        eyeIcon.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
    }
}

// Modal Functions
function showModal(title, message, type = 'info') {
    const modal = document.getElementById('messageModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const modalIcon = document.getElementById('modalIcon');
    const modalContent = modal.querySelector('.modal-content');
    
    modalTitle.textContent = title;
    modalMessage.textContent = message;
    
    modalContent.classList.remove('modal-success', 'modal-error');
    
    if (type === 'error') {
        modalContent.classList.add('modal-error');
        modalIcon.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#f56565" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';
    } else if (type === 'success') {
        modalContent.classList.add('modal-success');
        modalIcon.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#48bb78" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="8 12 11 15 16 9"/></svg>';
    } else {
        modalIcon.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#00A9FF" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><circle cx="12" cy="8" r="0.5" fill="#00A9FF"/></svg>';
    }
    
    modal.style.display = 'flex';
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
}

function closeModal() {
    const modal = document.getElementById('messageModal');
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

window.onclick = function(event) {
    const modal = document.getElementById('messageModal');
    if (event.target === modal) {
        closeModal();
    }
};

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
    }
});

// Slideup animation
const style = document.createElement('style');
style.textContent = `
    @keyframes slideUp {
        from { opacity: 1; transform: translateY(0); }
        to { opacity: 0; transform: translateY(-10px); }
    }
`;
document.head.appendChild(style);
