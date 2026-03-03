// OTP Verification Page Script
document.addEventListener('DOMContentLoaded', function() {
    console.log('[OTP] Script loaded and initialized');
    
    const otpInputs = document.querySelectorAll('.otp-input');
    const otpHiddenInput = document.getElementById('otp');
    const otpForm = document.getElementById('otpForm');
    const resendBtn = document.getElementById('resendBtn');
    const timerElement = document.getElementById('timer');
    
    console.log('[OTP] Found', otpInputs.length, 'input fields');
    
    let timeLeft = 300; // 5 minutes
    let resendCooldown = 60;
    let timerInterval;
    let resendInterval;

    // Start countdown timer
    startTimer();

    // OTP Input handling
    otpInputs.forEach((input, index) => {
        console.log('[OTP] Setting up input', index);
        
        input.addEventListener('input', function(e) {
            console.log('[OTP] Input event on index', index, 'value:', e.target.value);
            
            let value = e.target.value;
            
            // Only keep last character if multiple entered
            if (value.length > 1) {
                value = value.slice(-1);
                e.target.value = value;
            }
            
            // Only allow numbers
            if (value && !/^\d$/.test(value)) {
                e.target.value = '';
                console.log('[OTP] Non-numeric value rejected');
                return;
            }

            // Mark as filled
            if (value) {
                e.target.classList.add('filled');
                console.log('[OTP] Input filled, moving to next...');
                
                // Auto focus next input immediately
                if (index < otpInputs.length - 1) {
                    const nextInput = otpInputs[index + 1];
                    nextInput.focus();
                    nextInput.select();
                    console.log('[OTP] Focused input', index + 1);
                }
            } else {
                e.target.classList.remove('filled');
            }

            // Update hidden input
            updateOtpValue();
        });
        
        // Prevent non-numeric input
        input.addEventListener('keypress', function(e) {
            if (!/^\d$/.test(e.key)) {
                e.preventDefault();
                console.log('[OTP] Non-numeric key rejected:', e.key);
            }
        });

        input.addEventListener('keydown', function(e) {
            // Backspace handling
            if (e.key === 'Backspace' && !e.target.value && index > 0) {
                otpInputs[index - 1].focus();
                otpInputs[index - 1].value = '';
                otpInputs[index - 1].classList.remove('filled');
                updateOtpValue();
            }

            // Arrow key navigation
            if (e.key === 'ArrowLeft' && index > 0) {
                otpInputs[index - 1].focus();
            }
            if (e.key === 'ArrowRight' && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
        });

        // Paste handling
        input.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');
            
            if (pastedData.length === 6) {
                // Clear all inputs first
                otpInputs.forEach(inp => {
                    inp.value = '';
                    inp.classList.remove('filled');
                });
                
                // Fill with pasted data
                otpInputs.forEach((inp, i) => {
                    if (pastedData[i]) {
                        inp.value = pastedData[i];
                        inp.classList.add('filled');
                    }
                });
                updateOtpValue();
                
                // Focus last input
                setTimeout(() => {
                    otpInputs[5].focus();
                }, 50);
            } else if (pastedData.length > 0) {
                // Partial paste - fill from current position
                const startIndex = parseInt(e.target.dataset.index);
                for (let i = 0; i < pastedData.length && (startIndex + i) < 6; i++) {
                    otpInputs[startIndex + i].value = pastedData[i];
                    otpInputs[startIndex + i].classList.add('filled');
                }
                updateOtpValue();
                
                // Focus next empty or last
                const nextIndex = Math.min(startIndex + pastedData.length, 5);
                setTimeout(() => {
                    otpInputs[nextIndex].focus();
                }, 50);
            }
        });
    });

    // Update hidden input value
    function updateOtpValue() {
        const otpValue = Array.from(otpInputs).map(input => input.value).join('');
        otpHiddenInput.value = otpValue;
        console.log('[OTP] Updated hidden value:', otpValue);
    }

    // Countdown timer
    function startTimer() {
        timeLeft = 300; // 5 minutes
        console.log('[OTP] Timer started: 5 minutes');
        updateTimerDisplay();
        
        if (timerInterval) {
            clearInterval(timerInterval);
        }

        timerInterval = setInterval(() => {
            timeLeft--;
            updateTimerDisplay();

            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                timerElement.classList.add('expired');
                console.log('[OTP] Timer expired');
                showModal('OTP Expired', 'Your OTP has expired. Please request a new one.', 'error');
            }
        }, 1000);
    }

    function updateTimerDisplay() {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
        
        if (timeLeft <= 30) {
            timerElement.style.color = '#f56565'; // Red color
        } else {
            timerElement.style.color = '#d97706'; // Orange color
        }
    }

    // Resend button cooldown
    function startResendCooldown() {
        resendCooldown = 60;
        resendBtn.disabled = true;
        document.getElementById('resendTimer').textContent = `(${resendCooldown}s)`;

        if (resendInterval) {
            clearInterval(resendInterval);
        }

        resendInterval = setInterval(() => {
            resendCooldown--;
            document.getElementById('resendTimer').textContent = `(${resendCooldown}s)`;

            if (resendCooldown <= 0) {
                clearInterval(resendInterval);
                resendBtn.disabled = false;
                document.getElementById('resendTimer').textContent = '';
            }
        }, 1000);
    }

    // Start resend cooldown on page load
    startResendCooldown();

    // Form submit
    otpForm.addEventListener('submit', function(e) {
        const otpValue = otpHiddenInput.value;
        console.log('[OTP] Form submit - OTP value:', otpValue);
        if (otpValue.length !== 6) {
            e.preventDefault();
            showModal('Incomplete OTP', 'Please enter all 6 digits', 'error');
        }
    });

    // Auto focus first input
    console.log('[OTP] Auto-focusing first input');
    if (otpInputs.length > 0) {
        otpInputs[0].focus();
        console.log('[OTP] First input focused');
    }

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

// Modal Functions (same as auth-forgot.js)
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
