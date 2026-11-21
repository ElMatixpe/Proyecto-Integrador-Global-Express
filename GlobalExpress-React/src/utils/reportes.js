// Cálculos de los reportes e indicadores. 

export const NOMBRES_MES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Setiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

//Redondeo y formato 
// Redondea (9.225 -> 9.23). Evita el error de coma flotante
// de Math.round(n * 100) / 100 y de toFixed (que daban 9.22).
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

export const rangoConsulta = (desde, hasta) => {
  const inicioAnio = `${desde.slice(0, 4)}-01-01`;
  const base = inicioConsulta(desde);
  return {
    inicio: base < inicioAnio ? base : inicioAnio,
    fin: `${hasta.slice(0, 4)}-12-31`,
  };
};

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

// Un mes es "parcial" si el período elegido no lo cubre completo 
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
    descripcion: 'Total de ventas del mes ÷ total de ventas del año × 100.',
  },
  {
    id: 2,
    color: 'naranja',
    icono: 'caja',
    titulo: 'Porcentaje de Participación del Producto en las ventas Mensuales (%)',
    descripcion: 'Unidades vendidas del producto en el mes ÷ total de unidades vendidas en el mes × 100.',
  },
  {
    id: 3,
    color: 'morado',
    icono: 'barras',
    titulo: 'Porcentaje de Variación de Ventas Mensual (%)',
    descripcion: '(Ventas del mes actual − ventas del mes anterior) ÷ ventas del mes anterior × 100.',
  },
  {
    id: 4,
    color: 'amarillo',
    icono: 'moneda',
    titulo: 'Promedio de Venta Mensual por Operación',
    descripcion: 'Total de ventas realizadas en el mes ÷ número de ventas realizadas en el mes.',
  },
  {
    id: 5,
    color: 'verde',
    icono: 'lista',
    titulo: 'Registro de Ventas por Rango de Fechas',
    descripcion: 'Lista todas las ventas realizadas entre las fechas Desde y Hasta, con su detalle y total.',
  },
];

/** Devuelve { columnas, filas, hayMesParcial } del indicador pedido.
 * - ventasPeriodo: ventas dentro del rango de fechas elegido.
 * - todasLasVentas: todas las ventas (sirve para tomar el mes anterior completo en la variación) */
export function generarReporte(idIndicador, ventasPeriodo, todasLasVentas, desde, hasta) {
  const meses = mesesEntre(desde, hasta);
  const porMes = totalesPorMes(ventasPeriodo);
  const etiqueta = (ym) => etiquetaMes(ym) + (esMesParcial(ym, desde, hasta) ? ' *' : '');
  const hayMesParcial = meses.some((ym) => esMesParcial(ym, desde, hasta));

  if (idIndicador === 1) {
    // % Ventas por mes = total de ventas del mes / total de ventas al año x 100
    const ventasPagadas = pagadas(todasLasVentas);
    const totalAnio = (anio) => suma(ventasPagadas.filter((v) => v.fecha.slice(0, 4) === anio));
    return {
      hayMesParcial,
      nota: NOTA_PARCIAL,
      columnas: ['Mes', 'Total de Ventas del Mes (S/)', 'Total de Ventas del Año (S/)', 'Ventas por Mes (%)'],
      filas: meses.map((ym) => {
        const total = porMes[ym]?.total || 0;
        const anual = totalAnio(ym.slice(0, 4));
        return [etiqueta(ym), moneda(total), moneda(anual), porcentaje(anual ? (total / anual) * 100 : 0)];
      }),
    };
  }

  if (idIndicador === 2) {
    // % Participación del producto = unidades vendidas del producto en el mes / total de unidades vendidas en el mes x 100
    const porMesProducto = {};
    pagadas(ventasPeriodo).forEach((v) => {
      const ym = mesDe(v.fecha);
      if (!porMesProducto[ym]) porMesProducto[ym] = { totalUnidades: 0, productos: {} };
      const mes = porMesProducto[ym];
      (v.detalles || []).forEach((d) => {
        if (!mes.productos[d.codigo]) {
          mes.productos[d.codigo] = { codigo: d.codigo, nombre: d.nombre, marca: d.marca, unidades: 0 };
        }
        mes.productos[d.codigo].unidades += Number(d.cantidad);
        mes.totalUnidades += Number(d.cantidad);
      });
    });

    const filas = [];
    meses.forEach((ym) => {
      const mes = porMesProducto[ym];
      if (!mes) return;
      Object.values(mes.productos)
        .sort((a, b) => b.unidades - a.unidades || a.codigo.localeCompare(b.codigo, undefined, { numeric: true }))
        .slice(0, 10)
        .forEach((p) =>
          filas.push([
            etiqueta(ym),
            p.codigo,
            p.nombre,
            p.marca,
            `${p.unidades} u.`,
            `${mes.totalUnidades} u.`,
            porcentaje((p.unidades / mes.totalUnidades) * 100),
          ])
        );
    });

    return {
      hayMesParcial,
      nota: NOTA_PARCIAL,
      columnas: ['Mes', 'ID', 'Producto', 'Marca', 'Unidades Vendidas', 'Total Unidades del Mes', 'Participación (%)'],
      filas,
    };
  }

  if (idIndicador === 5) {
    // Registro de ventas: todas las ventas (pagadas y canceladas) entre Desde y Hasta, en orden cronológico
    const lista = [...ventasPeriodo].sort((a, b) => a.fecha.localeCompare(b.fecha) || a.id - b.id);
    const ok = calcularKpis(ventasPeriodo);
    return {
      hayMesParcial: false,
      nota: '',
      pie:
        `${lista.length} ${lista.length === 1 ? 'venta' : 'ventas'} en el período: ` +
        `${ok.cantidadPagadas} ${ok.cantidadPagadas === 1 ? 'pagada' : 'pagadas'} por ${moneda(ok.ingresos)} en total y ` +
        `${ok.cantidadCanceladas} ${ok.cantidadCanceladas === 1 ? 'cancelada' : 'canceladas'} (no suman a los ingresos).`,
      columnas: ['N°', 'Fecha', 'Hora', 'Cliente', 'Vendedor', 'Método de Pago', 'Estado', 'Total (S/)'],
      filas: lista.map((v) => [
        String(v.id),
        fechaCorta(v.fecha),
        v.fecha.slice(11, 16),
        v.cliente || '—',
        v.vendedor || '—',
        v.metodoPago || '—',
        v.estado,
        moneda(v.total),
      ]),
    };
  }

  if (idIndicador === 3) {
    // Variación = (venta actual - venta de comparación) / venta de comparación x 100
    const pagadasTodas = pagadas(todasLasVentas);

    const filas = meses.map((ym) => {
      const primeroMes = `${ym}-01`;
      const ultimoMes = `${ym}-${dosDigitos(diasDelMes(ym))}`;
      const ini = desde > primeroMes ? desde : primeroMes; 
      const fin = hasta < ultimoMes ? hasta : ultimoMes; 

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

      const nombre = parcial ? etiqueta(ym) : etiquetaMes(ym);
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

  // Indicador 4: venta promedio = monto total del mes / número de ventas del mes
  return {
    hayMesParcial,
    nota: NOTA_PARCIAL,
    columnas: ['Mes', 'Monto Total Ventas (S/)', 'Número de Transacciones', 'Promedio de venta mensual por operación (S/)'],
    filas: meses.map((ym) => {
      const { total = 0, transacciones = 0 } = porMes[ym] || {};
      return [etiqueta(ym), moneda(total), String(transacciones), moneda(transacciones ? total / transacciones : 0)];
    }),
  };
}
