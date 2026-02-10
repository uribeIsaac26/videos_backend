-HU-01 – Listar videos sin exponer rutas internas

-📝 HU-02 – Obtener thumbnail por ID
    🧾 Historia de Usuario
    
    Como consumidor del API
    Quiero obtener la imagen (thumbnail) de un video por su ID
    Para poder mostrar la vista previa del video en la interfaz
    
    🎯 Criterios de aceptación
    
    El endpoint debe recibir un id.
    
    Debe devolver la imagen asociada a ese video.
    
    No debe exponer rutas internas del servidor.
    
    Debe responder con el Content-Type correcto.
    
    Si el video no existe → debe devolver 404.
    
    Si el thumbnail no existe físicamente → debe manejarse de forma controlada.