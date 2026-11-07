import { peticion } from './api';

// En la base de datos el producto tiene un id numérico (id_producto).
// En pantalla se muestra el id tal como está en la base de datos, sin prefijos: 1, 2, 12...
// (se guarda como texto para poder buscarlo y compararlo igual que antes).
export const codigoDeProducto = (id) => String(id);

// Backend: { idProducto, nombre, marca, precio, estado }  ->  formato que usa el React
export const desdeApiProducto = (p) => ({
  id: p.idProducto,
  codigo: codigoDeProducto(p.idProducto),
  nombre: p.nombre,
  marca: p.marca,
  precio: Number(p.precio),
  estado: p.estado,
});

export const listarProductos = async () => {
  const datos = await peticion('/productos');
  return datos.map(desdeApiProducto);
};
