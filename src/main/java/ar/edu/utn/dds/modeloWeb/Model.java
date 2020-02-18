package ar.edu.utn.dds.modeloWeb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;
import javax.script.ScriptException;

import ar.edu.utn.dds.entidades.Empresas;
import ar.edu.utn.dds.entidades.Indicadores;
import ar.edu.utn.dds.entidades.Metodologias;
import ar.edu.utn.dds.entidades.Usuarios;
import ar.edu.utn.dds.excepciones.MetodologiaYaExisteException;
import ar.edu.utn.dds.excepciones.NoHayEmpresasQueCumplanLaCondicionException;
import ar.edu.utn.dds.excepciones.NoSeEncuentraElIndicadorException;
import ar.edu.utn.dds.excepciones.NoSeEncuentraLaCuentaEnElPeriodoException;
import ar.edu.utn.dds.excepciones.NoSeEncuentraLaCuentaException;
import ar.edu.utn.dds.excepciones.NoSeEncuentraLaEmpresaException;
import ar.edu.utn.dds.excepciones.NoSePudoOrdenarLaCondicionException;
import ar.edu.utn.dds.modelo.Condicion;
import ar.edu.utn.dds.modelo.Creciente;
import ar.edu.utn.dds.modelo.Decreciente;
import ar.edu.utn.dds.modelo.Empresa;
import ar.edu.utn.dds.modelo.Filtro;
import ar.edu.utn.dds.modelo.FiltroSegunEcuacion;
import ar.edu.utn.dds.modelo.Indicador;
import ar.edu.utn.dds.modelo.Longevidad;
import ar.edu.utn.dds.modelo.Mediana;
import ar.edu.utn.dds.modelo.Metodologia;
import ar.edu.utn.dds.modelo.OrdenaAplicandoCriterioOrdenamiento;
import ar.edu.utn.dds.modelo.Periodo;
import ar.edu.utn.dds.modelo.Promedio;
import ar.edu.utn.dds.modelo.PuntajeEmpresa;
import ar.edu.utn.dds.modelo.Sumatoria;
import ar.edu.utn.dds.modelo.Traductor;
import ar.edu.utn.dds.modelo.Usuario;

public class Model {

	private Map<String, Object> empresas;
	private Map<String, Object> metodologias;
	private Map<String, Object> cuenta;
	private Map<String, Object> indicadores;

	private Metodologia meto;
	private Usuario usuario = new Usuario();
	private Traductor t = new Traductor();

	/**
	 * Constructor
	 */
	public Model() {
		Empresas.setEmpresas();
		Indicadores.setIndicadores();
		Metodologias.setMetodologias();
		Usuarios.setUsuarios();
		t.cargarTraductor();
		this.empresas = new HashMap<>();
		this.cuenta = new HashMap<>();
		this.indicadores = new HashMap<>();
		this.metodologias = new HashMap<>();

		Indicadores.getIndicadores().stream().forEach(unI -> indicadores.put(String.valueOf(unI.getId()), unI));
		Metodologias.getMetodologias().stream().forEach(unaM -> metodologias.put(String.valueOf(unaM.getId()), unaM));

		this.meto = new Metodologia();
	}

	/// CREATES

	public int createIndicador(String nombre, String expresion) {

		Indicador indicadorApersistir = new Indicador(nombre, expresion);
		Indicadores.persistirIndicador(indicadorApersistir);
		indicadores.put(String.valueOf(indicadorApersistir.getId()), indicadorApersistir);
		usuario.agregarIndicador(indicadorApersistir);
		
		Usuarios.actualizarUsuario( usuario,  indicadorApersistir);
	
		return 1;
	}

	public int createMetodologia(String nombre) throws MetodologiaYaExisteException, PersistenceException {
		meto.setNombre(nombre);
		
		Metodologias.persistirMetodologia(meto);
		return 1;

	}

	public int createCondicionLongevidad(String anios) {
		Longevidad lon = new Longevidad(t);
		Condicion cond = new Filtro(lon, Integer.parseInt(anios));
		meto.agregarCondicion(cond);
		Metodologias.persistirCrecienteoDecrecienteoLongevidad(meto, lon, cond);
		return 1;
	}

	public int createCondicionCreciente(String nombre, String anios) throws NoSeEncuentraElIndicadorException {
		Creciente cre;
		cre = new Creciente(t.buscarIndicador(nombre), t);
		Condicion condcre = new Filtro(cre, Integer.valueOf(anios));
		meto.agregarCondicion(condcre);
		Metodologias.persistirCrecienteoDecrecienteoLongevidad(meto, cre, condcre);

		return 1;
	}

	public int createCondicionDecreciente(String nombreInd, String anios) throws NoSeEncuentraElIndicadorException {
		Decreciente decre;

		decre = new Decreciente(t.buscarIndicador(nombreInd), t);

		Condicion condDecre = new Filtro(decre, Integer.valueOf(anios));
		meto.agregarCondicion(condDecre);
		Metodologias.persistirCrecienteoDecrecienteoLongevidad(meto, decre, condDecre);
		return 1;
	}

	public int createCondicionSumaPromeMediana(String tipo, String nombreInd, String comparador, String valorAcomparar,
			String ordenamiento, Periodo periodo) throws NoSeEncuentraElIndicadorException {
		Condicion cond1;
		Condicion cond2;
		if (tipo.equals("sumatoria")) {
			Sumatoria sum = new Sumatoria(t.buscarIndicador(nombreInd), t);
			cond1 = new FiltroSegunEcuacion(sum, Integer.valueOf(valorAcomparar), comparador, periodo);
			cond2 = new OrdenaAplicandoCriterioOrdenamiento(sum, periodo, ordenamiento);
			Metodologias.persistirMedianaOsumatoriaOPromedio(meto, sum, valorAcomparar, comparador, periodo,
					ordenamiento);
		} else {
			if (tipo.equals("promedio")) {
				Promedio promedio = new Promedio(t.buscarIndicador(nombreInd), t);
				cond1 = new FiltroSegunEcuacion(promedio, Integer.valueOf(valorAcomparar), comparador, periodo);
				cond2 = new OrdenaAplicandoCriterioOrdenamiento(promedio, periodo, ordenamiento);
				Metodologias.persistirMedianaOsumatoriaOPromedio(meto, promedio, valorAcomparar, comparador, periodo,
						ordenamiento);
			} else {
				Mediana mediana = new Mediana(t.buscarIndicador(nombreInd), t);
				cond1 = new FiltroSegunEcuacion(mediana, Integer.valueOf(valorAcomparar), comparador, periodo);
				cond2 = new OrdenaAplicandoCriterioOrdenamiento(mediana, periodo, ordenamiento);
				Metodologias.persistirMedianaOsumatoriaOPromedio(meto, mediana, valorAcomparar, comparador, periodo,
						ordenamiento);
			}
		}

		meto.agregarCondicion(cond1);
		meto.agregarCondicion(cond2);
		return 1;
	}

	public ArrayList<PuntajeEmpresa> aplicarMetodologia(String nombre, List<Empresa> empresas)
			throws NoHayEmpresasQueCumplanLaCondicionException, NoSeEncuentraLaEmpresaException, ScriptException,
			NoSePudoOrdenarLaCondicionException, NoSeEncuentraLaCuentaException,
			NoSeEncuentraLaCuentaEnElPeriodoException, NoSeEncuentraElIndicadorException {
		Metodologia metodologiaAaplicar = Metodologias.getMetodologias().stream()
				.filter(unaM -> unaM.getNombre().equals(nombre)).findFirst().get();
		metodologiaAaplicar.getCondicionesDeMetodologia().stream().forEach(unC -> {
			unC.getLadoIzq().setTraductor(t);
		});

		return metodologiaAaplicar.aplicarMetodologia(empresas);

	}

	/////// GETS
	public void getEmpresas() {

		Empresas.getEmpresas().forEach(unaE -> {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
			TablaEmpresa te = new TablaEmpresa();
			String id = String.valueOf(unaE.getId());
			te.setId(id);
			te.setNombre(unaE.getNombre());
			te.setFechaInscripcion(unaE.getFechaInscripcion().format(formatter));

			empresas.put(id, te);

		});

	}

	public void getCuentas(String id) {
		this.cuenta.clear();
		Empresas.getEmpresas().forEach(unaE -> {
			if (String.valueOf(unaE.getId()).equals(id)) {
				unaE.getCuentas().forEach(unaC -> {
					TablaCuenta tc = new TablaCuenta();
					tc.setNombre(unaC.getNombre());
					tc.setValor(unaC.getValor());
					cuenta.put(String.valueOf(unaC.getId()), tc);
				});
			}
		});
	}

	public List<Empresa> getEmpHandle() {

		return Empresas.getEmpresas();
	}

	////// CHECKS

	public boolean checkIndicadorCalculable(IndicadorCalculable i) {
		if (Empresas.getEmpresas().stream().noneMatch(unaE -> unaE.getNombre().equals(i.getNombreEmpresa()))) {
			return false;
		}

		return true;
	}

	// FUNCIONES

	public String calcularIndicador(IndicadorCalculable i, Periodo p)
			throws NoSeEncuentraLaEmpresaException, NoSeEncuentraLaCuentaException,
			NoSeEncuentraLaCuentaEnElPeriodoException, NoSeEncuentraElIndicadorException {
		return String.valueOf(t.calcular(i.getNombreEmpresa(), p, i.getNombreIndicador()));
	}

	public Periodo armarPeriodo(String fechaInicio, String fechaFin) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
		LocalDate fechaI = LocalDate.parse(fechaInicio, formatter);
		LocalDate fechaF = LocalDate.parse(fechaFin, formatter);
		Periodo p = new Periodo(fechaI, fechaF);
		return p;
	}

	public void inicializarMetodologia() {
		meto = new Metodologia();
	}
	//// SENDS

	public List<Object> sendEmpresas() {
		List<Object> ret = new ArrayList<>(empresas.values());
		return ret;
	}

	public List<Object> sendEmpresasID() {
		List<Object> ret = new ArrayList<>(empresas.keySet());
		return ret;
	}

	public List<Object> sendCuentas() {

		List<Object> ret = new ArrayList<>(cuenta.values());
		return ret;
	}

	public List<Object> sendIndicadores() {

		List<Object> ret = new ArrayList<>(indicadores.values());
		List<Object> indicadoresUsuario=new ArrayList<>();
		indicadoresUsuario=ret.stream().filter(unInd-> ((Indicador) unInd).getNombre().equals("i_NivelDeuda") ||((Indicador) unInd).getNombre().equals("i_MargenVentas")
				||usuario.getIndicadores().contains(unInd)
				
		).collect(Collectors.toList());
		
		
		return indicadoresUsuario;

	}

	public List<Object> sendMetodologias() {
		Metodologias.getMetodologias().stream().forEach(unaM -> {
			if (!metodologias.containsValue(unaM)) {
				metodologias.put(String.valueOf(unaM.getId()), unaM);
			}
		});
		List<Object> ret = new ArrayList<>(metodologias.values());
		return ret;
	}

	public Boolean checkUsuario(LoginWeb usuario) {

		return Usuarios.getUsuarios().stream().anyMatch(
				unU -> unU.getUsuario().equals(usuario.getNombre()) && unU.getPass().equals(usuario.getContrasenia()));

	}

	public void setUsuario(LoginWeb user) {

		usuario = Usuarios.getUsuarios().stream()
				.filter(unU -> unU.getUsuario().equals(user.getNombre()) && unU.getPass().equals(user.getContrasenia()))
				.findFirst().get();

	}

	public Usuario getUsuario() {
		return usuario;
	}

}
