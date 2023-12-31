package ifsc.sti.tcc.resources;

import java.util.List;

import javax.validation.Valid;

import ifsc.sti.tcc.resources.rest.models.usuario.login.request.LoginEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ifsc.sti.tcc.repository.ImagemRepository;
import ifsc.sti.tcc.repository.UsuarioRepository;
import ifsc.sti.tcc.resources.rest.ResponseBase;
import ifsc.sti.tcc.resources.rest.models.usuario.cadastro.UsuarioRequest;
import ifsc.sti.tcc.resources.rest.models.usuario.login.request.LoginDocumentRequest;
import ifsc.sti.tcc.resources.rest.models.usuario.login.response.UsuarioBaseResponse;
import ifsc.sti.tcc.service.UsuarioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api")
@Api(value = "API REST STI Usuario")
public class UsuarioApi {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ImagemRepository imagemRepository;
	
	@ApiOperation(value = "Busca a lista de usuários cadastrados")
	@GetMapping("/BuscarTodos")
	public ResponseEntity<ResponseBase<List<UsuarioBaseResponse>>> getAlunosUsers() {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.buscar();
	}
	
	@ApiOperation(value = "Busca um usuários por seu Identificador")
	@GetMapping("/BuscarUsuarioId")
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> buscarUsuarioPorId(@RequestParam Integer id) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.buscar(id);
	}
	
	@ApiOperation(value = "Busca um usuário por seu cpf")
	@GetMapping("/BuscarUsuarioCPF")
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> buscarUsuarioPorCPF(@RequestParam  String cpf) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.buscar(cpf);
	}
	
	@ApiOperation(value = "Realiza a autenticação dos usuários")
	@RequestMapping(value = "/LoginDocument", method = RequestMethod.POST)
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> login(@RequestBody @Valid LoginDocumentRequest loginRequest) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.autenticar(loginRequest);
	}

	@ApiOperation(value = "Realiza a autenticação dos usuários")
	@RequestMapping(value = "/LoginEmail", method = RequestMethod.POST)
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> login(@RequestBody @Valid LoginEmailRequest loginRequest) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.autenticar(loginRequest);
	}
	
	@ApiOperation(value = "Realiza o cadastro dos usuários")
	@RequestMapping(value = "/Cadastro", method = RequestMethod.POST)
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> cadastrar(@RequestBody @Valid UsuarioRequest usuarioRequest) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.cadastrar(usuarioRequest);
	}
	
	@ApiOperation(value = "Altera as informações do usuários")
	@RequestMapping(value = "/Alterar", method = RequestMethod.PUT)
	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> alterar(@RequestBody @Valid UsuarioRequest usuarioRequest) {
		UsuarioService lUsuarioService = new UsuarioService.Instance(usuarioRepository)
				.withImagemRepository(imagemRepository)
				.build();
		return lUsuarioService.alterar(usuarioRequest);
	}
}
