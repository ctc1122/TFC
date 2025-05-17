// Inicializar la aplicación React para el calendario
document.addEventListener('DOMContentLoaded', () => {
  // Asegurarnos de que FullCalendar y sus plugins estén cargados
  console.log('Iniciando aplicación React, FullCalendar disponible:', typeof FullCalendar !== 'undefined');
  
  // Componente principal de la aplicación
  const App = () => {
    const [events, setEvents] = React.useState([]);
    const [darkMode, setDarkMode] = React.useState(false);
    const [currentViewName, setCurrentViewName] = React.useState('dayGridMonth');
    const [dialogOpen, setDialogOpen] = React.useState(false);
    const [selectedEvent, setSelectedEvent] = React.useState(null);
    const [filters, setFilters] = React.useState({
      normal: true,
      urgent: true,
      completed: true,
      cancelled: true
    });
    
    const calendarRef = React.useRef(null);
    
    // Efectos para inicializar
    React.useEffect(() => {
      document.body.className = darkMode ? 'dark-theme' : '';
      
      // Función para exponer API a Java
      window.calendarApi = {
        // Toggle del modo oscuro
        toggleDarkMode: (isDark) => {
          setDarkMode(isDark);
        },
        
        // Actualizar todos los eventos
        updateEvents: (newEventsJson) => {
          try {
            let newEvents;
            if (typeof newEventsJson === 'string') {
              newEvents = JSON.parse(newEventsJson);
            } else {
              newEvents = newEventsJson;
            }
            
            if (!Array.isArray(newEvents)) {
              newEvents = newEvents ? [newEvents] : [];
            }
            
            setEvents(newEvents);
            console.log('Eventos actualizados:', newEvents.length);
          } catch (error) {
            console.error('Error updating events:', error);
          }
        },
        
        // Ir a hoy
        goToToday: () => {
          if (calendarRef.current) {
            const calendarApi = calendarRef.current.getApi();
            calendarApi.today();
          }
        },
        
        // Ir a una fecha específica
        goToDate: (dateStr) => {
          if (calendarRef.current) {
            const calendarApi = calendarRef.current.getApi();
            calendarApi.gotoDate(dateStr);
          }
        },
        
        // Cambiar vista
        changeView: (viewName) => {
          setCurrentViewName(viewName);
          if (calendarRef.current) {
            const calendarApi = calendarRef.current.getApi();
            calendarApi.changeView(viewName);
          }
        },
        
        // Crear nuevo evento
        createNewEvent: () => {
          const now = new Date();
          const endTime = new Date(now.getTime() + 60 * 60 * 1000);
          
          setSelectedEvent({
            start: now.toISOString(),
            end: endTime.toISOString(),
            title: '',
            type: 'default'
          });
          
          setDialogOpen(true);
        },
        
        // Refrescar calendario
        refreshCalendar: () => {
          if (window.javaConnector) {
            window.javaConnector.processEvent('refresh', JSON.stringify({
              requestType: 'userAppointments',
              timestamp: new Date().getTime()
            }));
          } else {
            // Refrescar con los datos existentes
            if (calendarRef.current) {
              const calendarApi = calendarRef.current.getApi();
              calendarApi.refetchEvents();
            }
          }
        }
      };
      
      // Cargar eventos iniciales de ejemplo
      const sampleEvents = [
        {
          id: '1',
          title: 'Revisión de Max (María García)',
          start: '2023-05-16T10:00:00',
          end: '2023-05-16T10:30:00',
          location: 'Clínica Veterinaria',
          description: 'Revisión periódica para Max',
          type: 'default'
        },
        {
          id: '2',
          title: 'URGENTE: Vacunación (Carlos Pérez)',
          start: '2023-05-16T11:30:00',
          end: '2023-05-16T12:00:00',
          location: 'Clínica Veterinaria',
          description: 'Vacuna antirrábica',
          type: 'urgent'
        },
        {
          id: '3',
          title: 'Cirugía completada (Ana Martínez)',
          start: '2023-05-17T09:00:00',
          end: '2023-05-17T11:00:00',
          location: 'Clínica Veterinaria - Quirófano',
          description: 'Cirugía de fractura en pata derecha',
          type: 'completed'
        },
        {
          id: '4',
          title: 'Control cancelada (Pedro Sánchez)',
          start: '2023-05-18T15:00:00',
          end: '2023-05-18T15:30:00',
          location: 'Clínica Veterinaria',
          description: 'Control post-operatorio cancelado',
          type: 'cancelled'
        }
      ];
      
      setEvents(sampleEvents);
    }, []);

    // Renderizamos el calendario directamente utilizando FullCalendar
    React.useEffect(() => {
      if (!calendarRef.current) return;
      
      const calendarEl = calendarRef.current;
      const calendar = new FullCalendar.Calendar(calendarEl, {
        plugins: [
          FullCalendar.dayGridPlugin,
          FullCalendar.timeGridPlugin,
          FullCalendar.interactionPlugin
        ],
        initialView: 'dayGridMonth',
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        events: events,
        editable: true,
        selectable: true,
        selectMirror: true,
        dayMaxEvents: true,
        locale: 'es',
        height: 'auto',
        expandRows: true,
        nowIndicator: true,
        businessHours: {
          daysOfWeek: [1, 2, 3, 4, 5],
          startTime: '08:00',
          endTime: '20:00',
        },
        eventTimeFormat: {
          hour: '2-digit',
          minute: '2-digit',
          meridiem: false
        },
        firstDay: 1, // Lunes como primer día
        buttonText: {
          today: 'Hoy',
          month: 'Mes',
          week: 'Semana',
          day: 'Día'
        },
        eventClick: function(info) {
          const event = info.event;
          alert('Evento: ' + event.title);
          
          // Notificar a Java si está disponible
          if (window.javaConnector) {
            window.javaConnector.processEvent('click', JSON.stringify({
              id: event.id,
              title: event.title
            }));
          }
        },
        dateClick: function(info) {
          alert('Fecha: ' + info.dateStr);
        }
      });
      
      calendar.render();
      
      // Guardar referencia para poder llamar a los métodos
      window.calendar = calendar;
      
      return () => {
        calendar.destroy();
      };
    }, [events]);
    
    // Componente principal
    return (
      <div className="app-container">
        <div className="calendar-container">
          <div className="sidebar">
            <h3>Filtros</h3>
            <div className="filter-section">
              <div className="filter-title">Tipos de citas</div>
              <div className="filter-item">
                <input
                  type="checkbox"
                  id="filter-normal"
                  checked={filters.normal}
                  onChange={(e) => setFilters(prev => ({...prev, normal: e.target.checked}))}
                />
                <label htmlFor="filter-normal">Normales</label>
              </div>
              <div className="filter-item">
                <input
                  type="checkbox"
                  id="filter-urgent"
                  checked={filters.urgent}
                  onChange={(e) => setFilters(prev => ({...prev, urgent: e.target.checked}))}
                />
                <label htmlFor="filter-urgent">Urgentes</label>
              </div>
              <div className="filter-item">
                <input
                  type="checkbox"
                  id="filter-completed"
                  checked={filters.completed}
                  onChange={(e) => setFilters(prev => ({...prev, completed: e.target.checked}))}
                />
                <label htmlFor="filter-completed">Completadas</label>
              </div>
              <div className="filter-item">
                <input
                  type="checkbox"
                  id="filter-cancelled"
                  checked={filters.cancelled}
                  onChange={(e) => setFilters(prev => ({...prev, cancelled: e.target.checked}))}
                />
                <label htmlFor="filter-cancelled">Canceladas</label>
              </div>
            </div>
            
            <div className="sidebar-actions">
              <button 
                className="action-button" 
                onClick={() => window.calendarApi.goToToday()}
              >
                <i className="material-icons">today</i> Hoy
              </button>
              <button 
                className="action-button" 
                onClick={() => window.calendarApi.refreshCalendar()}
              >
                <i className="material-icons">refresh</i> Actualizar
              </button>
            </div>
          </div>
          
          <div className="calendar-main">
            <div ref={calendarRef}></div>
          </div>
        </div>
      </div>
    );
  };
  
  // Renderizar la aplicación React
  ReactDOM.render(
    <App />,
    document.getElementById('root')
  );
}); 