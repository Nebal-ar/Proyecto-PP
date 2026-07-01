// ===========================================================
// AlquiNow - lógica de propiedades en el navegador
// Consume el servlet /propiedades (devuelve JSON) con fetch.
// ===========================================================

const API = "propiedades"; // ruta relativa al servlet

/** Carga todas las propiedades (sin filtros). */
function cargarPropiedades() {
  fetch(API)
    .then(r => r.json())
    .then(render)
    .catch(err => console.error("Error al cargar:", err));
}

/** Busca aplicando los filtros del formulario. */
function buscar() {
  const ciudad    = document.getElementById("f-ciudad").value.trim();
  const provincia = document.getElementById("f-provincia").value;
  const precio    = document.getElementById("f-precio").value;
  const personas  = document.getElementById("f-personas").value;

  const params = new URLSearchParams();
  if (ciudad)    params.append("ciudad", ciudad);
  if (provincia) params.append("provincia", provincia);
  if (precio)    params.append("precioMax", precio);
  if (personas)  params.append("personasMin", personas);

  fetch(API + "?" + params.toString())
    .then(r => r.json())
    .then(render)
    .catch(err => console.error("Error en la búsqueda:", err));
}

/** Dibuja las tarjetas de propiedades. */
function render(lista) {
  const cont  = document.getElementById("resultados");
  const vacio = document.getElementById("vacio");
  cont.innerHTML = "";

  if (!lista || lista.length === 0) {
    vacio.style.display = "block";
    return;
  }
  vacio.style.display = "none";

  lista.forEach(p => {
    const card = document.createElement("div");
    card.className = "card";
    card.innerHTML = `
      <div class="card-body">
        ${p.disponibilidadInmediata
            ? '<span class="badge">Disponible ya</span>' : ''}
        <div class="ubic">${escapar(p.ciudad)}, ${escapar(p.provincia)}</div>
        <div style="font-weight:700; margin-top:.25rem;">
          ${escapar(p.calle)} ${p.altura || ''}
        </div>
        <div class="precio">
          $${formato(p.precioPorNoche)}
          <small>/ noche · hasta ${p.cantPersonas || '?'} personas</small>
        </div>
        <p style="color:var(--gris); font-size:.88rem; margin-top:.6rem;">
          ${escapar((p.descripcion || '').substring(0, 90))}
        </p>
        <a class="btn btn-block" href="reservar.html?id=${p.id}">Ver y reservar</a>
      </div>`;
    cont.appendChild(card);
  });
}

/** Formatea números con separador de miles. */
function formato(n) {
  if (n == null) return "—";
  return Number(n).toLocaleString("es-AR");
}

/** Evita inyección de HTML al mostrar texto. */
function escapar(s) {
  if (!s) return "";
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}
