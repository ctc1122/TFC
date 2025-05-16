// Formulario de eventos para el calendario
class EventForm {
    constructor() {
        this.isOpen = false;
        this.eventData = null;
        this.createFormElements();
    }

    createFormElements() {
        // Crear el contenedor del modal
        this.modalContainer = document.createElement('div');
        this.modalContainer.className = 'event-modal-overlay hidden';
        this.modalContainer.innerHTML = `
            <div class="event-modal">
                <div class="event-modal-header">
                    <h3 id="event-form-title">Nueva Cita</h3>
                    <button class="close-button">&times;</button>
                </div>
                <div class="event-modal-body">
                    <form id="event-form">
                        <div class="form-group">
                            <label for="event-title">Título</label>
                            <input type="text" id="event-title" class="form-control" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group half">
                                <label for="event-start">Fecha Inicio</label>
                                <input type="date" id="event-start-date" class="form-control" required>
                            </div>
                            <div class="form-group half">
                                <label for="event-start-time">Hora</label>
                                <input type="time" id="event-start-time" class="form-control" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group half">
                                <label for="event-end">Fecha Fin</label>
                                <input type="date" id="event-end-date" class="form-control" required>
                            </div>
                            <div class="form-group half">
                                <label for="event-end-time">Hora</label>
                                <input type="time" id="event-end-time" class="form-control" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="event-type">Tipo de Cita</label>
                            <select id="event-type" class="form-control">
                                <option value="default">Normal</option>
                                <option value="urgent">Urgente</option>
                                <option value="completed">Completada</option>
                                <option value="cancelled">Cancelada</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="event-location">Ubicación</label>
                            <input type="text" id="event-location" class="form-control">
                        </div>
                        <div class="form-group">
                            <label for="event-description">Descripción</label>
                            <textarea id="event-description" class="form-control" rows="3"></textarea>
                        </div>
                        <div class="form-actions">
                            <button type="button" id="event-delete" class="delete-button" style="display: none;">Eliminar</button>
                            <div class="spacer"></div>
                            <button type="button" id="event-cancel" class="cancel-button">Cancelar</button>
                            <button type="submit" id="event-save" class="save-button">Guardar</button>
                        </div>
                    </form>
                </div>
            </div>
        `;

        // Añadir al body
        document.body.appendChild(this.modalContainer);

        // Configurar eventos
        this.setupEventListeners();
    }

    setupEventListeners() {
        const closeBtn = this.modalContainer.querySelector('.close-button');
        const cancelBtn = this.modalContainer.querySelector('#event-cancel');
        const saveBtn = this.modalContainer.querySelector('#event-save');
        const deleteBtn = this.modalContainer.querySelector('#event-delete');
        const form = this.modalContainer.querySelector('#event-form');

        // Cerrar modal
        closeBtn.addEventListener('click', () => this.close());
        cancelBtn.addEventListener('click', () => this.close());

        // Guardar evento
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveEvent();
        });

        // Borrar evento
        deleteBtn.addEventListener('click', () => {
            if (confirm('¿Estás seguro de eliminar esta cita?')) {
                this.deleteEvent();
            }
        });

        // Cambiar color según tipo
        const typeSelect = this.modalContainer.querySelector('#event-type');
        typeSelect.addEventListener('change', () => {
            this.updateColorPreview(typeSelect.value);
        });
    }

    open(eventData = null) {
        this.eventData = eventData;
        this.updateFormFields();
        this.modalContainer.classList.remove('hidden');
        setTimeout(() => {
            this.modalContainer.classList.add('visible');
        }, 10);
        this.isOpen = true;
    }

    close() {
        this.modalContainer.classList.remove('visible');
        setTimeout(() => {
            this.modalContainer.classList.add('hidden');
        }, 300);
        this.isOpen = false;
    }

    updateFormFields() {
        const titleEl = this.modalContainer.querySelector('#event-form-title');
        const titleInput = this.modalContainer.querySelector('#event-title');
        const startDateInput = this.modalContainer.querySelector('#event-start-date');
        const startTimeInput = this.modalContainer.querySelector('#event-start-time');
        const endDateInput = this.modalContainer.querySelector('#event-end-date');
        const endTimeInput = this.modalContainer.querySelector('#event-end-time');
        const typeSelect = this.modalContainer.querySelector('#event-type');
        const locationInput = this.modalContainer.querySelector('#event-location');
        const descriptionInput = this.modalContainer.querySelector('#event-description');
        const deleteBtn = this.modalContainer.querySelector('#event-delete');

        if (this.eventData) {
            // Es una edición
            titleEl.textContent = 'Editar Cita';
            
            // Llenar los campos con los datos del evento
            let start, end;
            
            if (typeof this.eventData.start === 'string') {
                start = new Date(this.eventData.start);
            } else if (this.eventData.start instanceof Date) {
                start = this.eventData.start;
            } else if (this.eventData.start && typeof this.eventData.start.toDate === 'function') {
                start = this.eventData.start.toDate();
            }
            
            if (typeof this.eventData.end === 'string') {
                end = new Date(this.eventData.end);
            } else if (this.eventData.end instanceof Date) {
                end = this.eventData.end;
            } else if (this.eventData.end && typeof this.eventData.end.toDate === 'function') {
                end = this.eventData.end.toDate();
            } else {
                // Si no hay fecha de fin, usar fecha inicio + 1 hora
                end = new Date(start);
                end.setHours(end.getHours() + 1);
            }

            // Formatear fechas y horas para los inputs
            const formatDate = (date) => {
                return date.toISOString().split('T')[0];
            };
            
            const formatTime = (date) => {
                return date.toTimeString().substring(0, 5);
            };

            titleInput.value = this.eventData.title || '';
            startDateInput.value = formatDate(start);
            startTimeInput.value = formatTime(start);
            endDateInput.value = formatDate(end);
            endTimeInput.value = formatTime(end);
            
            // Obtener tipo del evento
            let eventType = 'default';
            if (this.eventData.extendedProps && this.eventData.extendedProps.type) {
                eventType = this.eventData.extendedProps.type;
            } else if (this.eventData.type) {
                eventType = this.eventData.type;
            }
            
            typeSelect.value = eventType;
            
            // Ubicación y descripción
            let location = '';
            if (this.eventData.extendedProps && this.eventData.extendedProps.location) {
                location = this.eventData.extendedProps.location;
            } else if (this.eventData.location) {
                location = this.eventData.location;
            }
            
            let description = '';
            if (this.eventData.extendedProps && this.eventData.extendedProps.description) {
                description = this.eventData.extendedProps.description;
            } else if (this.eventData.description) {
                description = this.eventData.description;
            }
            
            locationInput.value = location;
            descriptionInput.value = description;
            
            // Mostrar botón eliminar
            deleteBtn.style.display = 'block';
            
        } else {
            // Es una creación nueva
            titleEl.textContent = 'Nueva Cita';
            
            // Valores por defecto
            const now = new Date();
            const later = new Date(now);
            later.setHours(later.getHours() + 1);
            
            titleInput.value = '';
            startDateInput.value = now.toISOString().split('T')[0];
            startTimeInput.value = now.toTimeString().substring(0, 5);
            endDateInput.value = later.toISOString().split('T')[0];
            endTimeInput.value = later.toTimeString().substring(0, 5);
            typeSelect.value = 'default';
            locationInput.value = '';
            descriptionInput.value = '';
            
            // Ocultar botón eliminar
            deleteBtn.style.display = 'none';
        }
        
        // Actualizar visual según tipo
        this.updateColorPreview(typeSelect.value);
    }

    updateColorPreview(type) {
        const header = this.modalContainer.querySelector('.event-modal-header');
        
        // Remover clases anteriores
        header.classList.remove('default-type', 'urgent-type', 'completed-type', 'cancelled-type');
        
        // Añadir clase según tipo
        header.classList.add(`${type}-type`);
    }

    saveEvent() {
        // Obtener valores del formulario
        const titleInput = this.modalContainer.querySelector('#event-title');
        const startDateInput = this.modalContainer.querySelector('#event-start-date');
        const startTimeInput = this.modalContainer.querySelector('#event-start-time');
        const endDateInput = this.modalContainer.querySelector('#event-end-date');
        const endTimeInput = this.modalContainer.querySelector('#event-end-time');
        const typeSelect = this.modalContainer.querySelector('#event-type');
        const locationInput = this.modalContainer.querySelector('#event-location');
        const descriptionInput = this.modalContainer.querySelector('#event-description');
        
        // Crear objeto de evento
        const startDateTime = `${startDateInput.value}T${startTimeInput.value}:00`;
        const endDateTime = `${endDateInput.value}T${endTimeInput.value}:00`;
        
        const eventData = {
            title: titleInput.value,
            start: startDateTime,
            end: endDateTime,
            type: typeSelect.value,
            location: locationInput.value,
            description: descriptionInput.value
        };
        
        // Si es edición, mantener el ID
        if (this.eventData && this.eventData.id) {
            eventData.id = this.eventData.id;
        }
        
        // Personalizar color según tipo
        switch(typeSelect.value) {
            case 'urgent':
                eventData.backgroundColor = '#ff9800';
                eventData.borderColor = '#e65100';
                break;
            case 'completed':
                eventData.backgroundColor = '#4caf50';
                eventData.borderColor = '#2e7d32';
                break;
            case 'cancelled':
                eventData.backgroundColor = '#f44336';
                eventData.borderColor = '#b71c1c';
                eventData.textDecoration = 'line-through';
                break;
            default:
                eventData.backgroundColor = '#1a73e8';
                eventData.borderColor = '#1a73e8';
        }
        
        // Notificar al listener para guardar
        if (this.onSave) {
            this.onSave(eventData);
        }
        
        // Cerrar formulario
        this.close();
    }

    deleteEvent() {
        // Notificar al listener para eliminar
        if (this.onDelete && this.eventData && this.eventData.id) {
            this.onDelete(this.eventData.id);
        }
        
        // Cerrar formulario
        this.close();
    }

    // Callback que será asignado desde fuera
    onSave = null;
    onDelete = null;
}

// Exportar la clase
window.EventForm = EventForm; 