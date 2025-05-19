// Objeto global para la API del calendario
window.calendarApi = {
    calendar: null,
    isDarkMode: false,

    // Inicializar el calendario
    init() {
        const calendarEl = document.getElementById('calendar');
        this.calendar = new FullCalendar.Calendar(calendarEl, {
            plugins: ['dayGrid', 'timeGrid', 'list', 'interaction'],
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
            },
            initialView: 'dayGridMonth',
            editable: true,
            selectable: true,
            selectMirror: true,
            dayMaxEvents: true,
            locale: 'es',
            buttonText: {
                today: 'Hoy',
                month: 'Mes',
                week: 'Semana',
                day: 'Día',
                list: 'Lista'
            },
            eventClick: this.handleEventClick.bind(this),
            select: this.handleDateSelect.bind(this),
            eventDrop: this.handleEventDrop.bind(this),
            eventResize: this.handleEventResize.bind(this)
        });
        
        this.calendar.render();
    },

    // Actualizar eventos
    updateEvents(events) {
        if (!this.calendar) return;
        
        this.calendar.removeAllEvents();
        this.calendar.addEventSource(events);
    },

    // Cambiar tema
    toggleDarkMode(isDark) {
        this.isDarkMode = isDark;
        document.body.classList.toggle('dark-theme', isDark);
    },

    // Manejadores de eventos
    handleEventClick(info) {
        if (window.javaConnector) {
            window.javaConnector.processEvent('click', JSON.stringify({
                id: info.event.id,
                title: info.event.title,
                start: info.event.start,
                end: info.event.end,
                allDay: info.event.allDay
            }));
        }
    },

    handleDateSelect(info) {
        if (window.javaConnector) {
            window.javaConnector.processEvent('openClinicaForm', JSON.stringify({
                date: info.start.toISOString()
            }));
        }
    },

    handleEventDrop(info) {
        if (window.javaConnector) {
            window.javaConnector.processEvent('update', JSON.stringify({
                id: info.event.id,
                title: info.event.title,
                start: info.event.start.toISOString(),
                end: info.event.end ? info.event.end.toISOString() : null,
                allDay: info.event.allDay
            }));
        }
    },

    handleEventResize(info) {
        if (window.javaConnector) {
            window.javaConnector.processEvent('update', JSON.stringify({
                id: info.event.id,
                title: info.event.title,
                start: info.event.start.toISOString(),
                end: info.event.end.toISOString(),
                allDay: info.event.allDay
            }));
        }
    }
};

// Inicializar cuando el documento esté listo
document.addEventListener('DOMContentLoaded', () => {
    window.calendarApi.init();
}); 