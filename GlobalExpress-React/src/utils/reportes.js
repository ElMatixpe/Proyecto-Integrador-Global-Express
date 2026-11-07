// Cálculos de los reportes e indicadores.
export const NOMBRES_MES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Setiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

// Redondeo y formato. Redondea. Evita el error de coma flotante
export const redondear = (n, decimales = 2) => {
  const signo = n < 0 ? -1 : 1;
  const absoluto = Math.abs(n);
  const r = Number(Math.round(Number(`${absoluto}e${decimales}`)) + `e-${decimales}`);
  return Number.isFinite(r) ? signo * r : signo * Math.round(absoluto * 10 ** decimales) / 10 ** decimales;
};

export const moneda = (n) =>
  `S/ ${redondear(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

export const porcentaje = (n) => `${redondear(n).toFixed(2)}%`;

const variacionTexto = (n) => `${redondear(n) > 0 ? '+' : ''}${redondear(n).toFixed(2)}%`;

// Fechas (siempre texto 'AAAA-MM-DD' para evitar problemas de zona horaria) 
export const aISO = (fecha) => {
  const mm = String(fecha.getMonth() + 1).padStart(2, '0');
  const dd = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mm}-${dd}`;
};

export const fechaCorta = (iso) => {
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
};

const mesDe = (iso) => iso.slice(0, 7); 

const etiquetaMes = (ym) => {
  const [a, m] = ym.split('-');
  return `${NOMBRES_MES[Number(m) - 1]} ${a}`;
};

// Primer día del mes anterior a la fecha dada: '2026-08-15' -> '2026-07-01'
export const inicioMesAnterior = (iso) => `${mesAnteriorDe(mesDe(iso))}-01`;

const aFecha = (iso) => {
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(a, m - 1, d);
};

// Suma (o resta, si es negativo) días a una fecha 'AAAA-MM-DD'
const sumarDias = (iso, dias) => {
  const f = aFecha(iso);
  f.setDate(f.getDate() + dias);
  return aISO(f);
};

// Días entre dos fechas 'AAAA-MM-DD' (la misma fecha = 0)
const diferenciaDias = (desde, hasta) => Math.round((aFecha(hasta) - aFecha(desde)) / 86400000);

// Desde qué fecha hay que pedir las ventas al backend: el mes anterior completo y, además, los 31 días
// previos a "desde" (para comparar con el período anterior de la misma duración).
export const inicioConsulta = (iso) => {
  const mesAnterior = inicioMesAnterior(iso);
  const treintaYUno = sumarDias(iso, -31);
  return mesAnterior < treintaYUno ? mesAnterior : treintaYUno;
};

const mesAnteriorDe = (ym) => {
  const [a, m] = ym.split('-').map(Number);
  return m === 1 ? `${a - 1}-12` : `${a}-${String(m - 1).padStart(2, '0')}`;
};

const mesesEntre = (desde, hasta) => {
  const lista = [];
  let actual = mesDe(desde);
  const ultimo = mesDe(hasta);
  while (actual <= ultimo) {
    lista.push(actual);
    const [a, m] = actual.split('-').map(Number);
    actual = m === 12 ? `${a + 1}-01` : `${a}-${String(m + 1).padStart(2, '0')}`;
  }
  return lista;
};

// Un mes es "parcial" si el período elegido no lo cubre completo (ej. el mes en curso)
const esMesParcial = (ym, desde, hasta) => {
  const [a, m] = ym.split('-').map(Number);
  const ultimoDia = new Date(a, m, 0).getDate();
  return desde > `${ym}-01` || hasta < `${ym}-${String(ultimoDia).padStart(2, '0')}`;
};

const diasDelMes = (ym) => {
  const [a, m] = ym.split('-').map(Number);
  return new Date(a, m, 0).getDate();
};

const dosDigitos = (n) => String(n).padStart(2, '0');

// Filtros y totales 
export const filtrarPorPeriodo = (ventas, desde, hasta) =>
  ventas.filter((v) => {
    const dia = v.fecha.slice(0, 10);
    return dia >= desde && dia <= hasta;
  });

const pagadas = (ventas) => ventas.filter((v) => v.estado === 'Pagado');
const suma = (ventas) => ventas.reduce((acc, v) => acc + Number(v.total), 0);

// Totales por mes: { '2026-07': { total, transacciones } } solo con ventas pagadas
const totalesPorMes = (ventas) => {
  const mapa = {};
  pagadas(ventas).forEach((v) => {
    const ym = mesDe(v.fecha);
    mapa[ym] = mapa[ym] || { total: 0, transacciones: 0 };
    mapa[ym].total += Number(v.total);
    mapa[ym].transacciones += 1;
  });
  return mapa;
};

// Tarjetas: ingresos, máximo, mínimo, promedio, canceladas 
export function calcularKpis(ventasPeriodo) {
  const ok = pagadas(ventasPeriodo);
  const canceladas = ventasPeriodo.filter((v) => v.estado === 'Cancelado');
  const ingresos = suma(ok);
  let maxima = null;
  let minima = null;
  ok.forEach((v) => {
    if (!maxima || Number(v.total) > Number(maxima.total)) maxima = v;
    if (!minima || Number(v.total) < Number(minima.total)) minima = v;
  });

  // Producto más vendido (en unidades) del período
  const unidades = {};
  ok.forEach((v) => {
    (v.detalles || []).forEach((d) => {
      unidades[d.codigo] = unidades[d.codigo] || { codigo: d.codigo, nombre: d.nombre, unidades: 0 };
      unidades[d.codigo].unidades += Number(d.cantidad);
    });
  });
  const productoTop =
    Object.values(unidades).sort((a, b) => b.unidades - a.unidades || a.codigo.localeCompare(b.codigo, undefined, { numeric: true }))[0] || null;

  return {
    ingresos,
    productoTop,
    cantidadPagadas: ok.length,
    maxima,
    minima,
    promedio: ok.length ? ingresos / ok.length : 0,
    cantidadCanceladas: canceladas.length,
    montoCancelado: suma(canceladas),
  };
}

// Indicadores 
const NOTA_PARCIAL = '* Mes parcial: solo incluye las ventas dentro del período seleccionado.';

export const INDICADORES = [
  {
    id: 1,
    color: 'azul',
    icono: 'tendencia',
    titulo: 'Porcentaje de Ventas por Mes (%)',
    descripcion: 'Qué parte del total vendido en el período corresponde a cada mes.',
  },
  {
    id: 2,
    color: 'naranja',
    icono: 'caja',
    titulo: 'Porcentaje de Participación del Producto (%)',
    descripcion: 'Unidades vendidas de cada producto respecto al total de unidades del período.',
  },
  {
    id: 3,
    color: 'morado',
    icono: 'barras',
    titulo: 'Porcentaje de Variación de Ventas Mensual (%)',
    descripcion: 'Cuánto subió o bajó la venta de cada mes frente al mes anterior.',
  },
  {
    id: 4,
    color: 'amarillo',
    icono: 'moneda',
    titulo: 'Promedio de Venta Mensual por Operación',
    descripcion: 'Monto promedio de cada venta realizada en el mes.',
  },
];

/* Devuelve { columnas, filas, hayMesParcial } del indicador pedido. */
export function generarReporte(idIndicador, ventasPeriodo, todasLasVentas, desde, hasta) {
  const meses = mesesEntre(desde, hasta);
  const porMes = totalesPorMes(ventasPeriodo);
  const etiqueta = (ym) => etiquetaMes(ym) + (esMesParcial(ym, desde, hasta) ? ' *' : '');
  const hayMesParcial = meses.some((ym) => esMesParcial(ym, desde, hasta));

  if (idIndicador === 1) {
    //Porcentaje de Ventas por Mes = ventas del mes / total de ventas del período x 100
    const totalPeriodo = suma(pagadas(ventasPeriodo));
    return {
      hayMesParcial,
      nota: NOTA_PARCIAL,
      columnas: ['Mes', 'Ventas del Mes (S/)', 'Total del Período (S/)', 'Participación (%)'],
      filas: meses.map((ym) => {
        const total = porMes[ym]?.total || 0;
        return [etiqueta(ym), moneda(total), moneda(totalPeriodo), porcentaje(totalPeriodo ? (total / totalPeriodo) * 100 : 0)];
      }),
    };
  }

  if (idIndicador === 2) {
    // Porcentaje de participación del producto en las ventas Mensuales = unidades del producto / total de unidades vendidas x 100
    const productos = {};
    let totalUnidades = 0;
    pagadas(ventasPeriodo).forEach((v) => {
      (v.detalles || []).forEach((d) => {
        productos[d.codigo] = productos[d.codigo] || { codigo: d.codigo, nombre: d.nombre, marca: d.marca, unidades: 0 };
        productos[d.codigo].unidades += Number(d.cantidad);
        totalUnidades += Number(d.cantidad);
      });
    });
    const lista = Object.values(productos)
      .sort((a, b) => b.unidades - a.unidades || a.codigo.localeCompare(b.codigo, undefined, { numeric: true }))
      .slice(0, 10);
    return {
      hayMesParcial: false,
      nota: '',
      columnas: ['ID', 'Producto', 'Marca', 'Unidades Vendidas', 'Participación (%)'],
      filas: lista.map((p) => [p.codigo, p.nombre, p.marca, `${p.unidades} u.`, porcentaje((p.unidades / totalUnidades) * 100)]),
    };
  }

  if (idIndicador === 3) {
    // Porcentaje de variación de ventas mensual = (venta actual - venta de comparación) / venta de comparación x 100
    // Mes completo: se compara con el mes anterior completo.
    // Mes parcial (el período Desde/Hasta no cubre el mes entero, p. ej. el mes en curso): se compara
    // con el período INMEDIATAMENTE ANTERIOR de la misma duración. Así la comparación depende siempre
    // de las fechas elegidas (Desde / Hasta): 12/10 al 17/10 (6 días) se compara con 06/10 al 11/10.
    // "Venta Mes Anterior" muestra siempre el total completo del mes anterior.
    const pagadasTodas = pagadas(todasLasVentas);

    const filas = meses.map((ym) => {
      const primeroMes = `${ym}-01`;
      const ultimoMes = `${ym}-${dosDigitos(diasDelMes(ym))}`;
      const ini = desde > primeroMes ? desde : primeroMes; // primer día del mes dentro del período
      const fin = hasta < ultimoMes ? hasta : ultimoMes; // último día del mes dentro del período

      const actual = suma(filtrarPorPeriodo(pagadasTodas, ini, fin));

      const previo = mesAnteriorDe(ym);
      const anteriorCompleto = suma(
        filtrarPorPeriodo(pagadasTodas, `${previo}-01`, `${previo}-${dosDigitos(diasDelMes(previo))}`)
      );

      const parcial = esMesParcial(ym, desde, hasta);
      let comparable = anteriorCompleto;
      let textoComparable = moneda(anteriorCompleto);
      if (parcial) {
        const dias = diferenciaDias(ini, fin) + 1;
        const finPrevio = sumarDias(ini, -1);
        const inicioPrevio = sumarDias(ini, -dias);
        comparable = suma(filtrarPorPeriodo(pagadasTodas, inicioPrevio, finPrevio));
        textoComparable = moneda(comparable);
      }

      const nombre = etiquetaMes(ym);
      const fila = [nombre, moneda(actual), moneda(anteriorCompleto)];
      if (hayMesParcial) fila.push(textoComparable);

      if (!comparable) {
        fila.push('—', 'Sin referencia');
      } else {
        const variacion = redondear(((actual - comparable) / comparable) * 100);
        const estado = variacion > 0 ? 'Crecimiento' : variacion < 0 ? 'Decrecimiento' : 'Sin cambio';
        fila.push(variacionTexto(variacion), estado);
      }
      return fila;
    });

    return {
      hayMesParcial,
      nota: '* Mes parcial: la variación se compara con el período anterior de la misma duración.',
      columnas: [
        'Período / Mes',
        'Venta Mes Actual (S/)',
        'Venta Mes Anterior (S/)',
        ...(hayMesParcial ? ['Período anterior equivalente (S/)'] : []),
        'Variación (%)',
        'Estado',
      ],
      filas,
    };
  }

  // Indicador 4: Promedio de Venta Mensual por Operación  = monto total del mes / número de ventas del mes
  return {
    hayMesParcial,
    nota: NOTA_PARCIAL,
    columnas: ['Mes', 'Monto Total Ventas (S/)', 'Número de Transacciones', 'Monto Promedio (S/)'],
    filas: meses.map((ym) => {
      const { total = 0, transacciones = 0 } = porMes[ym] || {};
      return [etiqueta(ym), moneda(total), String(transacciones), moneda(transacciones ? total / transacciones : 0)];
    }),
  };
}
