// Apenas carga la página, le preguntamos al backend si hay sesión activa
function cargarNavbarSesion() {
  fetch('api/session?t=' + new Date().getTime())
    .then(response => response.json())
    .then(data => {
      if (data.conectado) {
        const navLinks = document.querySelector('.nav-links');

        if (navLinks) {
          navLinks.style.display = 'flex';
          navLinks.style.alignItems = 'center';
          navLinks.style.flexWrap = 'wrap';
          navLinks.style.gap = '0.25rem';

          let html = '';

          if (data.esHuesped) {
            html += `<a href="index.html">Buscar</a>`;
            html += `<a href="mis-reservas.html">Mis Reservas</a>`;
            html += `<a href="mis-favoritos.html">Mis Favoritos ❤️</a>`;
          }

          if (data.esVendedor) {
            html += `<a href="panel-vendedor.html">Mi Panel</a>`;
          } else {
            html += `<a href="convertir-vendedor" style="color: #28a745; font-weight: bold;">Publicar mi propiedad</a>`;
          }

          // Acceso al Panel de Administrador para verificar y dar de alta propiedades
          html += `<a href="panel-admin.html" style="color: #0d6efd; font-weight: 700; background: rgba(13, 110, 253, 0.08); padding: 4px 10px; border-radius: 8px; border: 1px solid rgba(13, 110, 253, 0.2);">🛡️ Panel Admin</a>`;

          // Nombre limpio (evitando el texto "null" si en la BD estaba vacío)
          let nombreUsuario = (data.nombre && data.nombre !== "null" && data.nombre.trim() !== "")
            ? data.nombre.trim()
            : ((data.usuario && data.usuario.mail) ? data.usuario.mail.split('@')[0] : "Usuario");

          let inicial = nombreUsuario.charAt(0).toUpperCase();

          // Contenedor circular fijo de 34x34px para que cualquier foto se adapte chiquita sin deformar la interfaz
          const estiloCirculo = "width: 34px; height: 34px; min-width: 34px; max-width: 34px; min-height: 34px; max-height: 34px; border-radius: 50%; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; border: 2px solid #e0603a; background: #e0603a; color: #fff; font-size: 0.85rem; font-weight: 800; flex-shrink: 0;";
          const estiloImg = "width: 34px !important; height: 34px !important; min-width: 34px !important; max-width: 34px !important; min-height: 34px !important; max-height: 34px !important; object-fit: cover !important; border-radius: 50% !important; display: block;";

          let avatarHtml = '';
          if (data.fotoPerfil && data.fotoPerfil !== "null" && data.fotoPerfil.trim() !== "") {
            const fotoUrl = 'uploads/' + encodeURI(data.fotoPerfil.trim());
            avatarHtml = `
              <span style="${estiloCirculo} background: #fff;">
                <img src="${fotoUrl}" width="34" height="34" alt="Avatar" style="${estiloImg}" onerror="this.style.display='none'; this.parentElement.style.background='#e0603a'; this.nextElementSibling.style.display='inline';">
                <span style="display:none; color:#fff;">${inicial}</span>
              </span>
            `;
          } else {
            avatarHtml = `<span style="${estiloCirculo}">${inicial}</span>`;
          }

          // Botón interactivo de bienvenida que lleva a Mi Perfil
          html += `
            <a href="perfil.html" title="Ir a mi perfil y editar mis datos" style="display: inline-flex; align-items: center; gap: 8px; margin-left: 0.8rem; padding: 4px 12px 4px 5px; background: #f5f3ee; border: 1px solid #e2ddd2; border-radius: 999px; color: #1d2433; font-weight: 700; font-size: 0.9rem; text-decoration: none; line-height: 1.2;">
              ${avatarHtml}
              <span style="white-space: nowrap;">Hola, ${nombreUsuario}</span>
              <span style="font-size: 0.75rem; font-weight: 600; color: #e0603a; background: rgba(224, 96, 58, 0.12); padding: 3px 8px; border-radius: 999px; white-space: nowrap;">✏️ Mi Perfil</span>
            </a>
          `;

          html += `<a href="logout" style="color: var(--brasa); font-weight: bold;">Cerrar Sesión</a>`;

          navLinks.innerHTML = html;
        }
      }
    })
    .catch(error => console.error("Error chequeando la sesión:", error));
}

window.actualizarNavbarSesion = cargarNavbarSesion;
cargarNavbarSesion();
