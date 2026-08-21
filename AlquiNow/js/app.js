// Función para mostrar/ocultar los campos adicionales según el rol en registro.html
function toggleRoleFields() {
    const chkComprador = document.getElementById('chk-comprador');
    const chkVendedor = document.getElementById('chk-vendedor');

    const compradorFields = document.getElementById('comprador-fields');
    const vendedorFields = document.getElementById('vendedor-fields');

    if (compradorFields) {
        compradorFields.style.display = chkComprador && chkComprador.checked ? 'block' : 'none';
    }

    if (vendedorFields) {
        vendedorFields.style.display = chkVendedor && chkVendedor.checked ? 'block' : 'none';
    }
}

// Función para desplegar la caja de pago de seña en reservar.html
function showSenaModal(resId) {
    const senaBox = document.getElementById('sena-box');
    const senaTitle = document.getElementById('sena-title');
    const hiddenIdInput = document.getElementById('reserva-id-hidden');

    if (senaTitle) {
        senaTitle.innerText = 'Registrar Pago de Seña para ' + resId;
    }

    if (hiddenIdInput) {
        hiddenIdInput.value = resId;
    }

    if (senaBox) {
        senaBox.style.display = 'block';
        senaBox.scrollIntoView({ behavior: 'smooth' });
    }
}