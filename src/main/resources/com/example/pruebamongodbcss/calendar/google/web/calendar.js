// Objeto global para la API del calendario
window.calendarApi = {
    calendar: null,
    isDarkMode: false,
    events: [],
    useDirectApi: false, // Usar API REST directa o JavaConnector
    selectedEventId: null, // ID del evento seleccionado actualmente

    // Inicializar el calendario
    init() {
        const calendarEl = document.getElementById('calendar');
        
        if (!calendarEl) {
            console.error('El elemento #calendar no existe en el DOM');
            return;
        }
        
        // Comprobar si la API directa está disponible
        this.checkApiAvailability();
        
        // Verificar si FullCalendar está disponible
        if (typeof FullCalendar === 'undefined') {
            console.error('FullCalendar no está disponible. Asegúrate de que la librería está cargada correctamente.');
            this.showError(calendarEl, 'Error: No se pudo cargar el calendario (FullCalendar no está disponible)');
            return;
        }
        
        try {
            // Crear instancia de calendario con todos los plugins disponibles
            this.calendar = new FullCalendar.Calendar(calendarEl, {
                headerToolbar: false, // No usar la barra de herramientas predeterminada
                height: '100%',
                initialView: 'dayGridMonth',
                editable: true,
                selectable: true,
                selectMirror: true,
                dayMaxEvents: true,
                locale: 'es',
                displayEventTime: true, // Mostrar hora del evento
                forceEventDuration: true, // Forzar duración en eventos
                eventDisplay: 'block', // Mostrar como bloques
                nowIndicator: true, // Mostrar indicador de ahora
                navLinks: true, // Hacer que los días sean clickeables
                buttonText: {
                    today: 'Hoy',
                    month: 'Mes',
                    week: 'Semana',
                    day: 'Día',
                    list: 'Lista'
                },
                firstDay: 1, // Lunes como primer día
                eventTimeFormat: {
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                },
                eventClick: this.handleEventClick.bind(this),
                select: this.handleDateSelect.bind(this),
                eventDrop: this.handleEventDrop.bind(this),
                eventResize: this.handleEventResize.bind(this),
                // Event handler para actualizar etiqueta de periodo
                datesSet: (info) => {
                    this.updateCurrentPeriodLabel(info);
                },
                // Mejorar los estilos y la visualización
                eventDidMount: function(info) {
                    // Añadir clases según el tipo de evento
                    if (info.event.extendedProps.eventType) {
                        info.el.setAttribute('data-event-type', info.event.extendedProps.eventType);
                        info.el.classList.add(info.event.extendedProps.eventType.toLowerCase());
                    }
                    
                    // Añadir el ID como atributo de datos
                    if (info.event.id) {
                        info.el.setAttribute('data-event-id', info.event.id);
                    }
                    
                    // Añadir tooltip
                    if (info.event.extendedProps.description || info.event.extendedProps.location) {
                        const tooltip = document.createElement('div');
                        tooltip.className = 'event-tooltip';
                        tooltip.innerHTML = `
                            <strong>${info.event.title}</strong>
                            ${info.event.extendedProps.description ? `<p>${info.event.extendedProps.description}</p>` : ''}
                            ${info.event.extendedProps.location ? `<p><i>📍 ${info.event.extendedProps.location}</i></p>` : ''}
                        `;
                        info.el.appendChild(tooltip);
                    }
                    
                    // Añadir iconos basados en el tipo de evento
                    if (info.event.extendedProps.eventType) {
                        const icon = document.createElement('span');
                        icon.className = 'event-icon';
                        
                        switch(info.event.extendedProps.eventType) {
                            case 'CITA_MEDICA':
                                icon.innerHTML = '🩺';
                                break;
                            case 'REUNION':
                                icon.innerHTML = '👥';
                                break;
                            case 'RECORDATORIO':
                                icon.innerHTML = '⏰';
                                break;
                            default:
                                icon.innerHTML = '📅';
                        }
                        
                        const titleEl = info.el.querySelector('.fc-event-title');
                        if (titleEl) {
                            titleEl.prepend(icon);
                        }
                    }
                    
                    // Si este evento está seleccionado, aplicar clase destacada
                    if (info.event.id === window.calendarApi.selectedEventId) {
                        info.el.classList.add('selected');
                    }
                }
            });
            
            // Renderizar el calendario
            this.calendar.render();
            console.log('Calendario renderizado correctamente');
            
            // Actualizar etiqueta de periodo actual
            this.updateCurrentPeriodLabel({
                view: this.calendar.view,
                start: this.calendar.view.activeStart,
                end: this.calendar.view.activeEnd
            });
            
            // Conectar botones de vista al calendario
            this.connectViewButtons();
            
            // Cargar eventos
            this.loadEvents();
            
            // Forzar renderizado tras un breve retraso para asegurar que todos los eventos son visibles
            setTimeout(() => {
                console.log('Forzando renderizado completo del calendario...');
                this.calendar.render();
            }, 1000);
            
            // Añadir manejador para deseleccionar eventos al hacer clic fuera
            document.addEventListener('click', (e) => {
                if (!e.target.closest('.fc-event') && !e.target.closest('#eventModal') && !e.target.closest('.sidebar')) {
                    this.deselectEvent();
                }
            });
        } catch (error) {
            console.error('Error al inicializar el calendario:', error);
            this.showError(calendarEl, 'Error al inicializar el calendario: ' + error.message);
        }
    },
    
    // Método para actualizar la etiqueta del período actual
    updateCurrentPeriodLabel(info) {
        if (!info || !info.view) return;
        
        const currentPeriodEl = document.getElementById('currentPeriod');
        if (!currentPeriodEl) return;
        
        const view = info.view;
        const start = info.start || view.activeStart;
        const end = info.end || view.activeEnd;
        
        if (!start || !end) return;
        
        const formatOptions = { month: 'long', year: 'numeric' };
        const dayFormatOptions = { day: 'numeric', month: 'long', year: 'numeric' };
        let formattedPeriod = '';
        
        // Formatea según la vista actual
        switch (view.type) {
            case 'dayGridMonth':
                // Mostrar mes y año (ej: "Mayo 2023")
                formattedPeriod = new Intl.DateTimeFormat('es-ES', formatOptions).format(start);
                break;
            
            case 'timeGridWeek':
                // Mostrar rango de fechas (ej: "1 - 7 Mayo 2023")
                const weekStart = start.getDate();
                const weekEnd = end.getDate() - 1; // -1 porque el end es exclusivo
                const weekMonth = new Intl.DateTimeFormat('es-ES', { month: 'long' }).format(start);
                const weekYear = start.getFullYear();
                formattedPeriod = `${weekStart} - ${weekEnd} ${weekMonth} ${weekYear}`;
                break;
            
            case 'timeGridDay':
                // Mostrar día completo (ej: "1 Mayo 2023")
                formattedPeriod = new Intl.DateTimeFormat('es-ES', dayFormatOptions).format(start);
                break;
            
            case 'listWeek':
                // Similar a la vista semanal
                const listStart = start.getDate();
                const listEnd = end.getDate() - 1;
                const listMonth = new Intl.DateTimeFormat('es-ES', { month: 'long' }).format(start);
                const listYear = start.getFullYear();
                formattedPeriod = `${listStart} - ${listEnd} ${listMonth} ${listYear}`;
                break;
            
            default:
                // Para otras vistas
                formattedPeriod = new Intl.DateTimeFormat('es-ES', formatOptions).format(start);
        }
        
        currentPeriodEl.textContent = formattedPeriod;
    },
    
    // Conectar botones de vista al calendario
    connectViewButtons() {
        try {
            // Botón de hoy
            const todayBtn = document.getElementById('todayBtn');
            if (todayBtn) {
                todayBtn.addEventListener('click', () => {
                    this.calendar.today();
                    this.calendar.render(); // Forzar renderizado completo
                    
                    // Actualizar etiqueta de periodo
                    this.updateCurrentPeriodLabel({
                        view: this.calendar.view,
                        start: this.calendar.view.activeStart,
                        end: this.calendar.view.activeEnd
                    });
                });
            }
            
            // Botones de navegación
            const prevBtn = document.getElementById('prevBtn');
            if (prevBtn) {
                prevBtn.addEventListener('click', () => {
                    this.calendar.prev();
                    this.calendar.render(); // Forzar renderizado completo
                    
                    // Actualizar etiqueta de periodo
                    this.updateCurrentPeriodLabel({
                        view: this.calendar.view,
                        start: this.calendar.view.activeStart,
                        end: this.calendar.view.activeEnd
                    });
                });
            }
            
            const nextBtn = document.getElementById('nextBtn');
            if (nextBtn) {
                nextBtn.addEventListener('click', () => {
                    this.calendar.next();
                    this.calendar.render(); // Forzar renderizado completo
                    
                    // Actualizar etiqueta de periodo
                    this.updateCurrentPeriodLabel({
                        view: this.calendar.view,
                        start: this.calendar.view.activeStart,
                        end: this.calendar.view.activeEnd
                    });
                });
            }
            
            // Botones de vista
            const setView = (viewName) => {
                if (this.calendar) {
                    this.calendar.changeView(viewName);
                    
                    // Forzar renderizado completo después de cambiar vista
                    setTimeout(() => {
                        this.calendar.render();
                        
                        // Actualizar etiqueta de periodo
                        this.updateCurrentPeriodLabel({
                            view: this.calendar.view,
                            start: this.calendar.view.activeStart,
                            end: this.calendar.view.activeEnd
                        });
                    }, 50);
                    
                    // Actualizar clases active
                    document.querySelectorAll('.view-controls .control-button').forEach(btn => {
                        btn.classList.remove('active');
                    });
                    
                    // Añadir clase active al botón correspondiente
                    const buttonMap = {
                        'dayGridMonth': 'monthViewBtn',
                        'timeGridWeek': 'weekViewBtn',
                        'timeGridDay': 'dayViewBtn',
                        'listWeek': 'listViewBtn'
                    };
                    
                    const activeBtn = document.getElementById(buttonMap[viewName]);
                    if (activeBtn) {
                        activeBtn.classList.add('active');
                    }
                }
            };
            
            // Asignar handlers a los botones de vista
            const viewButtons = {
                'monthViewBtn': 'dayGridMonth',
                'weekViewBtn': 'timeGridWeek',
                'dayViewBtn': 'timeGridDay',
                'listViewBtn': 'listWeek'
            };
            
            Object.entries(viewButtons).forEach(([btnId, viewName]) => {
                const btn = document.getElementById(btnId);
                if (btn) {
                    btn.addEventListener('click', () => {
                        setView(viewName);
                        // Re-aplicar filtros después de cambiar vista
                        if (typeof applyFilters === 'function') {
                            setTimeout(applyFilters, 100);
                        }
                    });
                }
            });
        } catch (error) {
            console.error('Error al conectar botones de vista:', error);
        }
    },
    
    // Mostrar mensaje de error
    showError(element, message) {
        if (element) {
            element.innerHTML = `
                <div class="calendar-error">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>${message}</p>
                    <button onclick="window.calendarApi.init()">Reintentar</button>
                </div>
            `;
        }
    },
    
    // Comprobar si la API directa está disponible
    checkApiAvailability() {
        try {
            // Si tenemos el objeto CalendarAPI y no tenemos javaConnector, usar API directa
            if (typeof CalendarAPI !== 'undefined' && (typeof window.javaConnector === 'undefined' || !window.javaConnector)) {
                console.log('Usando API REST directa para eventos');
                this.useDirectApi = true;
            } else {
                console.log('Usando puente Java para eventos');
                this.useDirectApi = false;
            }
        } catch (e) {
            console.error('Error al comprobar disponibilidad de API:', e);
            this.useDirectApi = false;
        }
    },
    
    // Cargar eventos
    loadEvents() {
        if (this.useDirectApi) {
            this.loadEventsFromApi();
        } else if (window.javaConnector) {
            // No hacemos nada, los eventos se cargarán a través de javaConnector
            console.log('Esperando a recibir eventos a través de JavaConnector...');
        } else {
            // Si no hay JavaConnector ni API directa, cargar eventos de ejemplo
            console.log('Cargando eventos de ejemplo...');
            this.loadSampleEvents();
        }
    },
    
    // Cargar eventos desde la API REST
    loadEventsFromApi() {
        try {
            console.log('Cargando eventos desde API REST...');
            
            // Si no tenemos API, salir
            if (typeof CalendarAPI === 'undefined') {
                console.error('CalendarAPI no está disponible');
                return;
            }
            
            // Obtener eventos para los próximos 6 meses
            const start = new Date();
            const end = new Date();
            end.setMonth(end.getMonth() + 6);
            
            // Intentar obtener todos los eventos
            CalendarAPI.getAllEvents()
                .then(events => {
                    this.events = events;
                    this.updateEvents(events);
                    console.log(`Cargados ${events.length} eventos desde API`);
                })
                .catch(error => {
                    console.error('Error al cargar eventos:', error);
                    // Intentar con el método de rango como fallback
                    return CalendarAPI.getEventsInRange(start, end);
                })
                .then(events => {
                    if (events) {
                        this.events = events;
                        this.updateEvents(events);
                        console.log(`Cargados ${events.length} eventos por rango desde API`);
                    }
                })
                .catch(error => {
                    console.error('Error al cargar eventos por rango:', error);
                });
        } catch (e) {
            console.error('Error al cargar eventos desde API:', e);
        }
    },

    // Actualizar eventos
    updateEvents(events) {
        if (!this.calendar) return;
        
        // Primero eliminar todos los eventos existentes
        this.calendar.removeAllEvents();
        
        // Luego añadir los nuevos eventos
        this.calendar.addEventSource(events);
        
        // Forzar una actualización completa del calendario
        setTimeout(() => {
            console.log('Forzando renderizado después de actualizar eventos...');
            this.calendar.render();
            
            // Aplicar filtros si la función existe
            if (typeof applyFilters === 'function') {
                setTimeout(applyFilters, 100);
            }
        }, 200);
    },

    // Cambiar tema
    toggleDarkMode(isDark) {
        this.isDarkMode = isDark;
        document.body.classList.toggle('dark-theme', isDark);
        
        // Cambiar el icono del botón de tema
        const themeIcon = document.querySelector('#themeToggle i');
        if (themeIcon) {
            if (isDark) {
                themeIcon.classList.remove('fa-moon');
                themeIcon.classList.add('fa-sun');
            } else {
                themeIcon.classList.remove('fa-sun');
                themeIcon.classList.add('fa-moon');
            }
        }
    },
    
    // Deseleccionar el evento actual
    deselectEvent() {
        this.selectedEventId = null;
        
        // Desactivar botones en la UI
        const editBtn = document.getElementById('editEventBtn');
        const deleteBtn = document.getElementById('deleteEventBtn');
        
        if (editBtn) editBtn.disabled = true;
        if (deleteBtn) deleteBtn.disabled = true;
        
        // Quitar clase selected de todos los eventos
        document.querySelectorAll('.fc-event.selected').forEach(el => {
            el.classList.remove('selected');
        });
    },

    // Manejadores de eventos
    handleEventClick(info) {
        const eventData = {
            id: info.event.id,
            title: info.event.title,
            start: info.event.start,
            end: info.event.end,
            allDay: info.event.allDay,
            description: info.event.extendedProps.description,
            location: info.event.extendedProps.location,
            eventType: info.event.extendedProps.eventType
        };
        
        // Guardar ID del evento seleccionado
        this.selectedEventId = info.event.id;
        
        if (this.useDirectApi) {
            this.handleEventClickDirect(eventData);
        } else if (window.javaConnector) {
            window.javaConnector.processEvent('click', JSON.stringify(eventData));
        }
    },
    
    // Manejar clic en evento directamente con JS
    handleEventClickDirect(eventData) {
        // Habilitar botones de acción
        const editBtn = document.getElementById('editEventBtn');
        const deleteBtn = document.getElementById('deleteEventBtn');
        
        if (editBtn) editBtn.disabled = false;
        if (deleteBtn) deleteBtn.disabled = false;
        
        // Resaltar evento seleccionado
        document.querySelectorAll('.fc-event.selected').forEach(el => {
            el.classList.remove('selected');
        });
        
        document.querySelectorAll(`.fc-event[data-event-id="${eventData.id}"]`).forEach(el => {
            el.classList.add('selected');
        });
        
        // Mostrar detalles del evento (en consola por ahora)
        console.log('Evento seleccionado:', eventData);
    },

    handleDateSelect(info) {
        const selectData = {
            date: info.start.toISOString()
        };
        
        if (this.useDirectApi) {
            this.handleDateSelectDirect(selectData);
        } else if (window.javaConnector) {
            window.javaConnector.processEvent('openClinicaForm', JSON.stringify(selectData));
        }
    },
    
    // Manejar selección de fecha directamente con JS
    handleDateSelectDirect(selectData) {
        // Esta función está ahora en calendar-ui.js
        // Abre el modal para crear un nuevo evento
        if (typeof openCreateEventModal === 'function') {
            openCreateEventModal(selectData.date);
        } else {
            // Comportamiento anterior como fallback
            const title = prompt('Título del evento:');
            if (title) {
                const eventType = prompt('Tipo de evento (CITA_MEDICA, REUNION, RECORDATORIO):').toUpperCase();
                const description = prompt('Descripción:');
                
                // Crear nuevo evento
                const newEvent = {
                    title: title,
                    start: selectData.date,
                    end: new Date(new Date(selectData.date).getTime() + 3600000).toISOString(), // +1 hora
                    allDay: false,
                    description: description,
                    eventType: eventType
                };
                
                // Guardar en la API
                CalendarAPI.createEvent(newEvent)
                    .then(savedEvent => {
                        // Actualizar calendario
                        this.events.push(savedEvent);
                        this.updateEvents(this.events);
                    })
                    .catch(error => {
                        console.error('Error al crear evento:', error);
                        alert('Error al guardar el evento: ' + error.message);
                    });
            }
        }
    },

    handleEventDrop(info) {
        const eventData = {
            id: info.event.id,
            title: info.event.title,
            start: info.event.start.toISOString(),
            end: info.event.end ? info.event.end.toISOString() : null,
            allDay: info.event.allDay,
            description: info.event.extendedProps.description,
            location: info.event.extendedProps.location,
            eventType: info.event.extendedProps.eventType
        };
        
        if (this.useDirectApi) {
            this.handleEventChangeDirect(eventData);
        } else if (window.javaConnector) {
            window.javaConnector.processEvent('update', JSON.stringify(eventData));
        }
    },

    handleEventResize(info) {
        const eventData = {
            id: info.event.id,
            title: info.event.title,
            start: info.event.start.toISOString(),
            end: info.event.end.toISOString(),
            allDay: info.event.allDay,
            description: info.event.extendedProps.description,
            location: info.event.extendedProps.location,
            eventType: info.event.extendedProps.eventType
        };
        
        if (this.useDirectApi) {
            this.handleEventChangeDirect(eventData);
        } else if (window.javaConnector) {
            window.javaConnector.processEvent('update', JSON.stringify(eventData));
        }
    },
    
    // Manejar cambios de eventos directamente con la API
    handleEventChangeDirect(eventData) {
        if (!eventData.id) return;
        
        CalendarAPI.updateEvent(eventData.id, eventData)
            .then(updatedEvent => {
                if (updatedEvent) {
                    // Actualizar evento en la lista local
                    for (let i = 0; i < this.events.length; i++) {
                        if (this.events[i].id === eventData.id) {
                            this.events[i] = updatedEvent;
                            break;
                        }
                    }
                    
                    console.log('Evento actualizado correctamente');
                } else {
                    console.warn('El evento no se pudo actualizar');
                    // Recargar eventos
                    this.loadEventsFromApi();
                }
            })
            .catch(error => {
                console.error('Error al actualizar evento:', error);
                alert('Error al actualizar el evento: ' + error.message);
                // Recargar eventos
                this.loadEventsFromApi();
            });
    },
    
    // Recibe eventos desde el puente Java
    receiveEventsFromJava(eventsJson) {
        try {
            console.log('Recibiendo eventos desde Java...');
            
            // Si estamos recibiendo una cadena, parseamos el JSON
            let events;
            if (typeof eventsJson === 'string') {
                events = JSON.parse(eventsJson);
            } else {
                events = eventsJson;
            }
            
            if (!Array.isArray(events)) {
                console.error('Los datos recibidos no son un array:', events);
                return;
            }
            
            // Procesar y almacenar eventos
            this.events = events;
            
            // Asegurarse de que todos los eventos tienen los campos necesarios
            this.events = this.events.map(event => {
                // Asegurarse que tenemos fechas válidas
                if (!event.start) {
                    event.start = new Date().toISOString();
                }
                
                if (!event.end) {
                    // Si no hay fecha fin, añadir 1 hora
                    const startDate = new Date(event.start);
                    event.end = new Date(startDate.getTime() + 60 * 60 * 1000).toISOString();
                }
                
                // Asegurar que tenemos ID
                if (!event.id) {
                    event.id = 'temp_' + Math.floor(Math.random() * 1000000);
                }
                
                // Asegurar que tenemos título
                if (!event.title) {
                    event.title = 'Sin título';
                }
                
                return event;
            });
            
            // Actualizar calendario
            this.updateEvents(this.events);
            console.log(`Recibidos y procesados ${events.length} eventos desde Java`);
            
            // Actualizar filtros si están configurados
            if (typeof applyFilters === 'function') {
                setTimeout(applyFilters, 100);
            }
        } catch (e) {
            console.error('Error al procesar eventos recibidos desde Java:', e);
            console.error('JSON recibido:', eventsJson);
        }
    },
    
    // Llamado después de que un evento ha sido creado o actualizado desde Java
    refreshCalendar() {
        if (this.useDirectApi) {
            this.loadEventsFromApi();
        }
    },

    // Cargar eventos de ejemplo para demostración
    loadSampleEvents() {
        // Fecha actual para generar eventos alrededor de esta
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth();
        const currentDate = now.getDate();
        
        // Crear eventos de ejemplo
        const events = [
            {
                id: '1',
                title: 'Cita Dr. García',
                start: new Date(currentYear, currentMonth, currentDate, 10, 0).toISOString(),
                end: new Date(currentYear, currentMonth, currentDate, 11, 0).toISOString(),
                description: 'Revisión anual con el Dr. García',
                location: 'Consulta 3, Planta 2',
                eventType: 'CITA_MEDICA',
                allDay: false
            },
            {
                id: '2',
                title: 'Reunión de equipo',
                start: new Date(currentYear, currentMonth, currentDate + 1, 14, 30).toISOString(),
                end: new Date(currentYear, currentMonth, currentDate + 1, 16, 0).toISOString(),
                description: 'Revisión de avances del proyecto',
                location: 'Sala de juntas',
                eventType: 'REUNION',
                allDay: false
            },
            {
                id: '3',
                title: 'Recordatorio: Entregar informe',
                start: new Date(currentYear, currentMonth, currentDate + 2).toISOString(),
                end: new Date(currentYear, currentMonth, currentDate + 2).toISOString(),
                description: 'Informe mensual de actividades',
                eventType: 'RECORDATORIO',
                allDay: true
            },
            {
                id: '4',
                title: 'Cita fisioterapia',
                start: new Date(currentYear, currentMonth, currentDate - 1, 9, 0).toISOString(),
                end: new Date(currentYear, currentMonth, currentDate - 1, 10, 0).toISOString(),
                description: 'Sesión de fisioterapia',
                location: 'Centro médico',
                eventType: 'CITA_MEDICA',
                allDay: false
            },
            {
                id: '5',
                title: 'Vacaciones',
                start: new Date(currentYear, currentMonth, currentDate + 10).toISOString(),
                end: new Date(currentYear, currentMonth, currentDate + 15).toISOString(),
                description: 'Vacaciones de verano',
                eventType: 'OTRO',
                allDay: true
            }
        ];
        
        this.events = events;
        this.updateEvents(events);
        console.log(`Cargados ${events.length} eventos de ejemplo`);
    }
};

// Inicializar cuando el documento esté listo
document.addEventListener('DOMContentLoaded', () => {
    window.calendarApi.init();
    
    // Eliminar el script antiguo de cambio de tema
    const oldScript = document.querySelector('script:not([src])');
    if (oldScript) {
        oldScript.remove();
    }
}); 