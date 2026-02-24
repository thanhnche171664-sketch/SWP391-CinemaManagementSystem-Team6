document.addEventListener('DOMContentLoaded', function () {
    const seatButtons = document.querySelectorAll('.seat-button:not(.disabled)');
    const selectedSeatsContainer = document.getElementById('selectedSeatsContainer');
    const selectedSeatCount = document.getElementById('selectedSeatCount');
    const selectedSeatLabels = document.getElementById('selectedSeatLabels');

    const selected = new Map();

    function renderSelectedSeats() {
        selectedSeatsContainer.innerHTML = '';
        const labels = [];

        selected.forEach((label, seatId) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'seatIds';
            input.value = seatId;
            selectedSeatsContainer.appendChild(input);
            labels.push(label);
        });

        selectedSeatCount.textContent = String(selected.size);
        selectedSeatLabels.textContent = labels.length > 0 ? labels.join(', ') : 'None';
    }

    seatButtons.forEach(button => {
        button.addEventListener('click', function () {
            const seatId = this.getAttribute('data-seat-id');
            const seatLabel = this.getAttribute('data-seat-label');

            if (selected.has(seatId)) {
                selected.delete(seatId);
                this.classList.remove('btn-success');
                this.classList.add('btn-outline-secondary');
            } else {
                selected.set(seatId, seatLabel);
                this.classList.remove('btn-outline-secondary');
                this.classList.add('btn-success');
            }

            renderSelectedSeats();
        });
    });

    renderSelectedSeats();
});

