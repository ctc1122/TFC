// Menú contextual para el calendario
class CalendarContextMenu {
    constructor() {
        this.menuElement = null;
        this.visible = false;
        this.contextData = null;
        
        this.createMenuElement();
        this.setupGlobalListeners();
    }
    
    createMenuElement() {
        // Crear el elemento del menú
        this.menuElement = document.createElement('div');
        this.menuElement.className = 'calendar-context-menu';
        this.menuElement.style.display = 'none';
        
        // Añadir al DOM
        document.body.appendChild(this.menuElement);
    }
    
    setupGlobalListeners() {
        // Ocultar menú al hacer clic en cualquier parte
        document.addEventListener('click', () => {
            this.hide();
        });
        
        // Ocultar menú al hacer scroll
        document.addEventListener('scroll', () => {
            this.hide();
        });
        
        // Prevenir menú contextual por defecto
        document.addEventListener('contextmenu', (e) => {
            if (this.visible) {
                e.preventDefault();
                this.hide();
            }
        });
    }
    
    // Mostrar menú para un día
    showForDay(e, date) {
        e.preventDefault();
        this.contextData = { type: 'day', date };
        this.show(e, this.generateDayMenu(date));
    }
    
    // Mostrar menú para un evento
    showForEvent(e, event) {
        e.preventDefault();
        this.contextData = { type: 'event', event };
        this.show(e, this.generateEventMenu(event));
    }
    
    // Mostrar el menú en una posición
    show(e, menuContent) {
        // Actualizar contenido
        this.menuElement.innerHTML = menuContent;
        
        // Calcular posición
        const x = e.clientX;
        const y = e.clientY;
        
        // Posicionar menú
        this.menuElement.style.left = `${x}px`;
        this.menuElement.style.top = `${y}px`;
        
        // Mostrar con animación
        this.menuElement.style.display = 'block';
        this.menuElement.style.opacity = '0';
        this.menuElement.style.transform = 'scale(0.95)';
        
        // Forzar reflow para que la animación funcione
        this.menuElement.getBoundingClientRect();
        
        // Aplicar animación
        this.menuElement.style.opacity = '1';
        this.menuElement.style.transform = 'scale(1)';
        
        // Verificar si el menú se sale de la pantalla
        const menuRect = this.menuElement.getBoundingClientRect();
        const viewportWidth = window.innerWidth;
        const viewportHeight = window.innerHeight;
        
        if (menuRect.right > viewportWidth) {
            this.menuElement.style.left = `${x - menuRect.width}px`;
        }
        
        if (menuRect.bottom > viewportHeight) {
            this.menuElement.style.top = `${y - menuRect.height}px`;
        }
        
        this.visible = true;
        
        // Configurar eventos de los elementos del menú
        this.setupMenuItemListeners();
    }
    
    // Ocultar el menú
    hide() {
        if (!this.visible) return;
        
        // Animar salida
        this.menuElement.style.opacity = '0';
        this.menuElement.style.transform = 'scale(0.95)';
        
        // Ocultar después de la animación
        setTimeout(() => {
            this.menuElement.style.display = 'none';
        }, 200);
        
        this.visible = false;
    }
    
    // Generar HTML para menú de día
    generateDayMenu(date) {
        const formattedDate = date.toLocaleDateString('es-ES', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        
        return `
            <div class="menu-header">${formattedDate}</div>
            <div class="menu-item" data-action="new-event">
                <i class="menu-icon">add</i>
                <span class="menu-text">Nueva cita</span>
            </div>
            <div class="menu-item" data-action="day-view">
                <i class="menu-icon">calendar_view_day</i>
                <span class="menu-text">Ver día</span>
            </div>
        `;
    }
    
    // Generar HTML para menú de evento
    generateEventMenu(event) {
        return `
            <div class="menu-header">${event.title}</div>
            <div class="menu-item" data-action="edit-event">
                <i class="menu-icon">edit</i>
                <span class="menu-text">Editar cita</span>
            </div>
            <div class="menu-item" data-action="delete-event">
                <i class="menu-icon">delete</i>
                <span class="menu-text">Eliminar cita</span>
            </div>
            <div class="menu-separator"></div>
            <div class="menu-item" data-action="day-view">
                <i class="menu-icon">calendar_view_day</i>
                <span class="menu-text">Ver día</span>
            </div>
        `;
    }
    
    // Configurar eventos para elementos del menú
    setupMenuItemListeners() {
        const menuItems = this.menuElement.querySelectorAll('.menu-item');
        
        menuItems.forEach(item => {
            item.addEventListener('click', (e) => {
                e.stopPropagation();
                const action = item.getAttribute('data-action');
                this.handleMenuAction(action);
                this.hide();
            });
        });
    }
    
    // Manejar acciones del menú
    handleMenuAction(action) {
        switch (action) {
            case 'new-event':
                if (this.onNewEvent && this.contextData.type === 'day') {
                    this.onNewEvent(this.contextData.date);
                }
                break;
                
            case 'edit-event':
                if (this.onEditEvent && this.contextData.type === 'event') {
                    this.onEditEvent(this.contextData.event);
                }
                break;
                
            case 'delete-event':
                if (this.onDeleteEvent && this.contextData.type === 'event') {
                    if (confirm('¿Estás seguro de eliminar esta cita?')) {
                        this.onDeleteEvent(this.contextData.event);
                    }
                }
                break;
                
            case 'day-view':
                if (this.onDayView) {
                    let date;
                    if (this.contextData.type === 'day') {
                        date = this.contextData.date;
                    } else if (this.contextData.type === 'event') {
                        date = this.contextData.event.start;
                    }
                    
                    if (date) {
                        this.onDayView(date);
                    }
                }
                break;
        }
    }
    
    // Callbacks que serán asignados desde fuera
    onNewEvent = null;
    onEditEvent = null;
    onDeleteEvent = null;
    onDayView = null;
}

// Exportar la clase
window.CalendarContextMenu = CalendarContextMenu; 