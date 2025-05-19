/**
 * Cliente API para comunicarse con el servidor Java
 * Proporciona métodos para operaciones CRUD en eventos del calendario
 */
const CalendarAPI = {
    baseUrl: '/api/calendar',
    
    /**
     * Obtiene todos los eventos
     * @returns {Promise<Array>} Promise que resolverá a un array de eventos
     */
    getAllEvents: async function() {
        try {
            const response = await fetch(this.baseUrl);
            if (!response.ok) {
                throw new Error(`Error al obtener eventos: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error en getAllEvents:', error);
            throw error;
        }
    },
    
    /**
     * Obtiene eventos en un rango de fechas
     * @param {Date} start Fecha de inicio
     * @param {Date} end Fecha de fin
     * @returns {Promise<Array>} Promise que resolverá a un array de eventos
     */
    getEventsInRange: async function(start, end) {
        try {
            const startStr = start.toISOString();
            const endStr = end.toISOString();
            
            const response = await fetch(`${this.baseUrl}/range?start=${encodeURIComponent(startStr)}&end=${encodeURIComponent(endStr)}`);
            if (!response.ok) {
                throw new Error(`Error al obtener eventos por rango: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error en getEventsInRange:', error);
            throw error;
        }
    },
    
    /**
     * Obtiene un evento por su ID
     * @param {string} id ID del evento
     * @returns {Promise<Object>} Promise que resolverá al evento
     */
    getEventById: async function(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`);
            if (response.status === 404) {
                return null;
            }
            if (!response.ok) {
                throw new Error(`Error al obtener evento: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error en getEventById:', error);
            throw error;
        }
    },
    
    /**
     * Crea un nuevo evento
     * @param {Object} event Datos del evento
     * @returns {Promise<Object>} Promise que resolverá al evento creado
     */
    createEvent: async function(event) {
        try {
            const response = await fetch(this.baseUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(event)
            });
            
            if (!response.ok) {
                throw new Error(`Error al crear evento: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error en createEvent:', error);
            throw error;
        }
    },
    
    /**
     * Actualiza un evento existente
     * @param {string} id ID del evento
     * @param {Object} event Datos del evento
     * @returns {Promise<Object>} Promise que resolverá al evento actualizado
     */
    updateEvent: async function(id, event) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(event)
            });
            
            if (response.status === 404) {
                return null;
            }
            
            if (!response.ok) {
                throw new Error(`Error al actualizar evento: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error en updateEvent:', error);
            throw error;
        }
    },
    
    /**
     * Elimina un evento
     * @param {string} id ID del evento
     * @returns {Promise<boolean>} Promise que resolverá a true si se eliminó correctamente
     */
    deleteEvent: async function(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'DELETE'
            });
            
            if (response.status === 404) {
                return false;
            }
            
            return response.status === 204;
        } catch (error) {
            console.error('Error en deleteEvent:', error);
            throw error;
        }
    }
};

// API para manejo de eventos
window.calendarApi = window.calendarApi || {};

// Extender la API con métodos de comunicación
Object.assign(window.calendarApi, {
    // Método para enviar un evento a Java
    sendEventToJava: function(action, eventData) {
        try {
            if (window.javaConnector) {
                console.log('Enviando evento a Java:', action, eventData);
                window.javaConnector.processEvent(action, JSON.stringify(eventData));
                return true;
            } else {
                console.error('Java connector no está disponible');
                return false;
            }
        } catch (e) {
            console.error('Error al enviar evento a Java:', e);
            return false;
        }
    },

    // Método para crear un nuevo evento
    createEvent: function(eventData) {
        return this.sendEventToJava('create', eventData);
    },

    // Método para actualizar un evento
    updateEvent: function(eventData) {
        return this.sendEventToJava('update', eventData);
    },

    // Método para eliminar un evento
    deleteEvent: function(eventId) {
        return this.sendEventToJava('delete', { id: eventId });
    },

    // Método para solicitar todos los eventos
    requestEvents: function() {
        return this.sendEventToJava('refresh', { requestType: 'userAppointments' });
    },

    // Método para solicitar cambio de tema
    toggleTheme: function() {
        return this.sendEventToJava('toggleTheme', {});
    }
}); 