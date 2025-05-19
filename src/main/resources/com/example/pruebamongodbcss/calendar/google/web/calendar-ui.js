/**
 * Funciones para la interfaz de usuario del calendario
 * Maneja la interacción con los modales, botones y eventos
 */
document.addEventListener('DOMContentLoaded', () => {
    // Referencias a elementos de interfaz
    const newEventBtn = document.getElementById('newEventBtn');
    const editEventBtn = document.getElementById('editEventBtn');
    const deleteEventBtn = document.getElementById('deleteEventBtn');
    const eventModal = document.getElementById('eventModal');
    const confirmModal = document.getElementById('confirmModal');
    const eventForm = document.getElementById('eventForm');
    const modalTitle = document.getElementById('modalTitle');
    const closeButtons = document.querySelectorAll('.close');
    const cancelEventBtn = document.getElementById('cancelEventBtn');
    const saveEventBtn = document.getElementById('saveEventBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const todayBtn = document.getElementById('todayBtn');
    const monthViewBtn = document.getElementById('monthViewBtn');
    const weekViewBtn = document.getElementById('weekViewBtn');
    const dayViewBtn = document.getElementById('dayViewBtn');
    const listViewBtn = document.getElementById('listViewBtn');
    
    // Almacena el evento seleccionado actualmente
    let selectedEvent = null;
    
    // Filtros de eventos
    const filterCitas = document.getElementById('filterCitas');
    const filterReuniones = document.getElementById('filterReuniones');
    const filterRecordatorios = document.getElementById('filterRecordatorios');
    
    // Inicializar
    initUI();
    
    /**
     * Inicializa la interfaz de usuario
     */
    function initUI() {
        // Configurar botones de acción
        newEventBtn.addEventListener('click', handleNewEvent);
        editEventBtn.addEventListener('click', handleEditEvent);
        deleteEventBtn.addEventListener('click', handleDeleteEvent);
        
        // Configurar botones de modal
        cancelEventBtn.addEventListener('click', closeModal);
        saveEventBtn.addEventListener('click', saveEvent);
        cancelDeleteBtn.addEventListener('click', () => closeModal(confirmModal));
        confirmDeleteBtn.addEventListener('click', deleteEvent);
        
        // Cerrar modales con el botón X
        closeButtons.forEach(button => {
            button.addEventListener('click', function() {
                const modal = this.closest('.modal');
                closeModal(modal);
            });
        });
        
        // Botones de navegación del calendario
        todayBtn.addEventListener('click', () => {
            window.calendarApi.calendar.today();
        });
        
        // Botones de vista del calendario
        monthViewBtn.addEventListener('click', () => {
            setActiveView('dayGridMonth');
            monthViewBtn.classList.add('active');
        });
        
        weekViewBtn.addEventListener('click', () => {
            setActiveView('timeGridWeek');
            weekViewBtn.classList.add('active');
        });
        
        dayViewBtn.addEventListener('click', () => {
            setActiveView('timeGridDay');
            dayViewBtn.classList.add('active');
        });
        
        listViewBtn.addEventListener('click', () => {
            setActiveView('listWeek');
            listViewBtn.classList.add('active');
        });
        
        // Escuchar cambios en los filtros
        filterCitas.addEventListener('change', applyFilters);
        filterReuniones.addEventListener('change', applyFilters);
        filterRecordatorios.addEventListener('change', applyFilters);
        
        // Configurar event handlers para el calendario
        configureCalendarHandlers();
        
        // Añadir handler para el campo allDay que actualice los campos de fecha
        const allDayCheckbox = document.getElementById('eventAllDay');
        if (allDayCheckbox) {
            allDayCheckbox.addEventListener('change', function() {
                const startField = document.getElementById('eventStart');
                const endField = document.getElementById('eventEnd');
                
                if (this.checked) {
                    // Guardar los valores originales como atributos de datos
                    if (startField.value) {
                        startField.setAttribute('data-original', startField.value);
                        // Convertir a todo el día (solo fecha, sin tiempo)
                        startField.value = startField.value.split('T')[0] + 'T00:00';
                    }
                    
                    if (endField.value) {
                        endField.setAttribute('data-original', endField.value);
                        const endDate = new Date(endField.value);
                        endDate.setHours(23, 59);
                        endField.value = endField.value.split('T')[0] + 'T23:59';
                    }
                } else {
                    // Restaurar valores originales si existen
                    if (startField.hasAttribute('data-original')) {
                        startField.value = startField.getAttribute('data-original');
                    }
                    
                    if (endField.hasAttribute('data-original')) {
                        endField.value = endField.getAttribute('data-original');
                    }
                }
            });
        }
    }
    
    /**
     * Configura los manejadores de eventos del calendario
     */
    function configureCalendarHandlers() {
        // Sobreescribir algunos manejadores de eventos del calendario
        const calendarApi = window.calendarApi;
        
        // Guardar las funciones originales
        const originalHandleEventClick = calendarApi.handleEventClickDirect;
        
        // Reemplazar con nuestras funciones mejoradas
        calendarApi.handleEventClickDirect = function(eventData) {
            // Guardar el evento seleccionado
            selectedEvent = eventData;
            
            // Habilitar botones de edición y eliminación
            editEventBtn.disabled = false;
            deleteEventBtn.disabled = false;
            
            // Destacar visualmente el evento seleccionado
            highlightSelectedEvent(eventData.id);
            
            // También podemos mostrar un tooltip o una vista detallada
            // En lugar del alert por defecto
            showEventDetails(eventData);
        };
        
        // Hacer disponible la función para abrir el modal de creación
        window.openCreateEventModal = openCreateEventModal;
    }
    
    /**
     * Abre el modal para crear un nuevo evento
     */
    function handleNewEvent() {
        // Resetear formulario
        eventForm.reset();
        document.getElementById('eventId').value = '';
        
        // Establecer fecha actual
        const now = new Date();
        const later = new Date(now.getTime() + 3600000); // +1 hora
        
        // Formatear fecha para input datetime-local (YYYY-MM-DDThh:mm)
        const startStr = formatDateTimeLocal(now);
        const endStr = formatDateTimeLocal(later);
        
        document.getElementById('eventStart').value = startStr;
        document.getElementById('eventEnd').value = endStr;
        
        // Cambiar título del modal
        modalTitle.textContent = 'Nueva Cita';
        
        // Mostrar modal
        openModal(eventModal);
    }
    
    /**
     * Abre el modal para crear un evento en una fecha específica
     */
    function openCreateEventModal(dateStr) {
        // Resetear formulario
        eventForm.reset();
        document.getElementById('eventId').value = '';
        
        // Convertir la fecha ISO a objeto Date
        const startDate = new Date(dateStr);
        
        // Redondear a la hora más cercana
        startDate.setMinutes(0, 0, 0);
        
        // Fecha fin = fecha inicio + 1 hora
        const endDate = new Date(startDate.getTime() + 3600000);
        
        // Formatear fechas para el input
        document.getElementById('eventStart').value = formatDateTimeLocal(startDate);
        document.getElementById('eventEnd').value = formatDateTimeLocal(endDate);
        
        // Cambiar título del modal
        modalTitle.textContent = 'Nueva Cita';
        
        // Mostrar modal
        openModal(eventModal);
    }
    
    /**
     * Abre el modal para editar un evento existente
     */
    function handleEditEvent() {
        if (!selectedEvent) return;
        
        // Llenar el formulario con los datos del evento
        document.getElementById('eventId').value = selectedEvent.id;
        document.getElementById('eventTitle').value = selectedEvent.title;
        
        // Convertir fechas ISO a formato input
        const startDate = new Date(selectedEvent.start);
        const endDate = selectedEvent.end ? new Date(selectedEvent.end) : new Date(startDate.getTime() + 3600000);
        
        document.getElementById('eventStart').value = formatDateTimeLocal(startDate);
        document.getElementById('eventEnd').value = formatDateTimeLocal(endDate);
        document.getElementById('eventAllDay').checked = selectedEvent.allDay || false;
        
        // Datos opcionales
        if (selectedEvent.eventType) {
            document.getElementById('eventType').value = selectedEvent.eventType;
        }
        
        if (selectedEvent.location) {
            document.getElementById('eventLocation').value = selectedEvent.location;
        }
        
        if (selectedEvent.description) {
            document.getElementById('eventDescription').value = selectedEvent.description;
        }
        
        // Cambiar título del modal
        modalTitle.textContent = 'Editar Cita';
        
        // Mostrar modal
        openModal(eventModal);
    }
    
    /**
     * Abre el modal de confirmación para eliminar un evento
     */
    function handleDeleteEvent() {
        if (!selectedEvent) return;
        
        // Mostrar título del evento a eliminar
        document.getElementById('deleteEventTitle').textContent = selectedEvent.title;
        
        // Mostrar modal de confirmación
        openModal(confirmModal);
    }
    
    /**
     * Guarda un evento (nuevo o existente)
     */
    function saveEvent() {
        // Validar formulario
        if (!eventForm.checkValidity()) {
            eventForm.reportValidity();
            return;
        }
        
        // Recopilar datos del formulario
        const eventId = document.getElementById('eventId').value;
        const title = document.getElementById('eventTitle').value;
        const startStr = document.getElementById('eventStart').value;
        const endStr = document.getElementById('eventEnd').value;
        const allDay = document.getElementById('eventAllDay').checked;
        const eventType = document.getElementById('eventType').value;
        const location = document.getElementById('eventLocation').value;
        const description = document.getElementById('eventDescription').value;
        
        // Convertir fechas
        const start = new Date(startStr).toISOString();
        const end = new Date(endStr).toISOString();
        
        // Crear objeto de evento
        const eventData = {
            title,
            start,
            end,
            allDay,
            eventType,
            location: location || null,
            description: description || null
        };
        
        // Si hay ID, es una actualización
        if (eventId) {
            eventData.id = eventId;
            
            // Usar el API para actualizar
            if (window.calendarApi.useDirectApi && typeof CalendarAPI !== 'undefined') {
                CalendarAPI.updateEvent(eventId, eventData)
                    .then(updatedEvent => {
                        window.calendarApi.handleEventChangeDirect(eventData);
                        closeModal(eventModal);
                        deselectEvent();
                    })
                    .catch(error => {
                        alert('Error al actualizar el evento: ' + error.message);
                    });
            } else if (window.javaConnector) {
                // Usar JavaConnector
                window.javaConnector.processEvent('update', JSON.stringify(eventData));
                closeModal(eventModal);
                deselectEvent();
            }
        } else {
            // Es un nuevo evento
            if (window.calendarApi.useDirectApi && typeof CalendarAPI !== 'undefined') {
                CalendarAPI.createEvent(eventData)
                    .then(savedEvent => {
                        window.calendarApi.events.push(savedEvent);
                        window.calendarApi.updateEvents(window.calendarApi.events);
                        closeModal(eventModal);
                    })
                    .catch(error => {
                        alert('Error al crear el evento: ' + error.message);
                    });
            } else if (window.javaConnector) {
                // Usar JavaConnector
                window.javaConnector.processEvent('create', JSON.stringify(eventData));
                closeModal(eventModal);
            }
        }
    }
    
    /**
     * Elimina el evento seleccionado
     */
    function deleteEvent() {
        if (!selectedEvent || !selectedEvent.id) return;
        
        const eventId = selectedEvent.id;
        
        if (window.calendarApi.useDirectApi && typeof CalendarAPI !== 'undefined') {
            CalendarAPI.deleteEvent(eventId)
                .then(success => {
                    if (success) {
                        // Eliminar de la lista local
                        window.calendarApi.events = window.calendarApi.events.filter(e => e.id !== eventId);
                        window.calendarApi.updateEvents(window.calendarApi.events);
                        closeModal(confirmModal);
                        deselectEvent();
                    } else {
                        alert('No se pudo eliminar el evento.');
                    }
                })
                .catch(error => {
                    alert('Error al eliminar el evento: ' + error.message);
                });
        } else if (window.javaConnector) {
            // Usar JavaConnector
            window.javaConnector.processEvent('delete', JSON.stringify({ id: eventId }));
            closeModal(confirmModal);
            deselectEvent();
        }
    }
    
    /**
     * Aplica los filtros de tipo de evento
     */
    function applyFilters() {
        const showCitas = document.getElementById('filterCitas').checked;
        const showReuniones = document.getElementById('filterReuniones').checked;
        const showRecordatorios = document.getElementById('filterRecordatorios').checked;
        
        console.log(`Aplicando filtros: Citas=${showCitas}, Reuniones=${showReuniones}, Recordatorios=${showRecordatorios}`);
        
        // Método 1: Mejor método usando la API de FullCalendar
        if (window.calendarApi && window.calendarApi.calendar) {
            const calendar = window.calendarApi.calendar;
            const events = calendar.getEvents();
            
            events.forEach(event => {
                const eventType = event.extendedProps.eventType;
                let visible = true;
                
                // Por defecto mostrar solo citas médicas
                if (eventType === 'CITA_MEDICA' && !showCitas) {
                    visible = false;
                } else if (eventType === 'REUNION' && !showReuniones) {
                    visible = false;
                } else if (eventType === 'RECORDATORIO' && !showRecordatorios) {
                    visible = false;
                }
                
                // Mostrar u ocultar usando la API de FullCalendar
                if (visible) {
                    event.setProp('display', 'auto');
                } else {
                    event.setProp('display', 'none');
                }
            });
            
            // Forzar renderizado para aplicar cambios
            calendar.render();
            return;
        }
        
        // Método 2 (fallback): Alternativa directa con DOM
        const eventElements = document.querySelectorAll('.fc-event');
        
        eventElements.forEach(eventEl => {
            const eventType = eventEl.getAttribute('data-event-type');
            let show = true;
            
            if (eventType === 'CITA_MEDICA' && !showCitas) {
                show = false;
            } else if (eventType === 'REUNION' && !showReuniones) {
                show = false;
            } else if (eventType === 'RECORDATORIO' && !showRecordatorios) {
                show = false;
            }
            
            // Mostrar u ocultar el evento
            eventEl.style.display = show ? '' : 'none';
        });
    }
    
    /**
     * Muestra los detalles de un evento
     */
    function showEventDetails(event) {
        // Podríamos mostrar más información en una tarjeta o tooltip
        console.log('Evento seleccionado:', event);
    }
    
    /**
     * Resalta visualmente el evento seleccionado
     */
    function highlightSelectedEvent(eventId) {
        // Quitar resaltado anterior
        document.querySelectorAll('.fc-event.selected').forEach(el => {
            el.classList.remove('selected');
        });
        
        // Agregar clase para resaltar
        const eventElements = document.querySelectorAll(`.fc-event[data-event-id="${eventId}"]`);
        eventElements.forEach(el => {
            el.classList.add('selected');
        });
    }
    
    /**
     * Deselecciona el evento actual
     */
    function deselectEvent() {
        selectedEvent = null;
        editEventBtn.disabled = true;
        deleteEventBtn.disabled = true;
        
        // Quitar resaltado
        document.querySelectorAll('.fc-event.selected').forEach(el => {
            el.classList.remove('selected');
        });
        
        // Informar al objeto calendarApi
        if (window.calendarApi) {
            window.calendarApi.deselectEvent();
        }
    }
    
    /**
     * Cambia la vista activa del calendario
     */
    function setActiveView(viewName) {
        // Cambiar vista en el calendario
        window.calendarApi.calendar.changeView(viewName);
        
        // Actualizar estados de los botones
        monthViewBtn.classList.remove('active');
        weekViewBtn.classList.remove('active');
        dayViewBtn.classList.remove('active');
        listViewBtn.classList.remove('active');
    }
    
    /**
     * Abre un modal
     */
    function openModal(modal) {
        modal.classList.add('show');
    }
    
    /**
     * Cierra un modal
     */
    function closeModal(modal = eventModal) {
        modal.classList.remove('show');
    }
    
    /**
     * Formatea una fecha para input datetime-local
     * Formato: YYYY-MM-DDThh:mm
     */
    function formatDateTimeLocal(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    }
    
    // Hacer accesibles algunas funciones
    window.openCreateEventModal = openCreateEventModal;
}); 