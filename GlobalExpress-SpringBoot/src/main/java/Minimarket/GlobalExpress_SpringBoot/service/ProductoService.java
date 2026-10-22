package Minimarket.GlobalExpress_SpringBoot.service;

import Minimarket.GlobalExpress_SpringBoot.exception.RecursoNoEncontradoException;
import Minimarket.GlobalExpress_SpringBoot.model.Producto;
import Minimarket.GlobalExpress_SpringBoot.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private static final Set<String> ESTADOS = Set.of("Disponible", "No Disponible");

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtener(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }

    @Transactional
    public Producto crear(Producto datos) {
        validar(datos);
        Producto nuevo = new Producto();
        copiar(datos, nuevo);
        return productoRepository.save(nuevo);
    }

    @Transactional
    public Producto actualizar(Integer id, Producto datos) {
        validar(datos);
        Producto existente = obtener(id);
        copiar(datos, existente);
        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Integer id) {
        productoRepository.delete(obtener(id));
    }

    private void copiar(Producto origen, Producto destino) {
        destino.setNombre(origen.getNombre().trim());
        destino.setMarca(origen.getMarca().trim());
        destino.setPrecio(origen.getPrecio());
        destino.setEstado(origen.getEstado());
    }

    private void validar(Producto p) {
        if (p == null || vacio(p.getNombre()) || vacio(p.getMarca())) {
            throw new IllegalArgumentException("El nombre y la marca son obligatorios.");
        }
        if (p.getNombre().trim().length() > 150 || p.getMarca().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre o la marca superan la longitud permitida.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        if (p.getPrecio().scale() > 2) {
            throw new IllegalArgumentException("El precio admite como máximo 2 decimales.");
        }
        if (p.getEstado() == null || !ESTADOS.contains(p.getEstado())) {
            throw new IllegalArgumentException("El estado debe ser Disponible o No Disponible.");
        }
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
