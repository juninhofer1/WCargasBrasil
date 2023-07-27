package ifsc.sti.tcc.service;

import java.util.ArrayList;
import java.util.List;

import ifsc.sti.tcc.resources.rest.models.usuario.login.request.LoginEmailRequest;
import ifsc.sti.tcc.resources.rest.models.usuario.login.request.LoginRequest;
import ifsc.sti.tcc.utilidades.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ifsc.sti.tcc.modelos.usuario.Imagem;
import ifsc.sti.tcc.modelos.usuario.Usuario;
import ifsc.sti.tcc.repository.ImagemRepository;
import ifsc.sti.tcc.repository.UsuarioRepository;
import ifsc.sti.tcc.resources.mappers.domaintoview.UsuarioMapper;
import ifsc.sti.tcc.resources.mappers.viewtodomain.AlterarMapper;
import ifsc.sti.tcc.resources.mappers.viewtodomain.CadastroMapper;
import ifsc.sti.tcc.resources.rest.ResponseBase;
import ifsc.sti.tcc.resources.rest.models.usuario.cadastro.UsuarioRequest;
import ifsc.sti.tcc.resources.rest.models.usuario.login.request.LoginDocumentRequest;
import ifsc.sti.tcc.resources.rest.models.usuario.login.response.UsuarioBaseResponse;
import ifsc.sti.tcc.utilidades.ValidatedField;

import static ifsc.sti.tcc.utilidades.ValidateUtil.STRING_OK;

public class UsuarioService {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(UsuarioService.class);

	private UsuarioRepository jpaRepository;
	private ImagemService imagemService;

	public static class Instance extends BaseService<UsuarioRepository> implements BaseService.BaseObject<UsuarioService> {

		public Instance(UsuarioRepository jpaRepository) {
			super(jpaRepository);
		}
		
		private ImagemRepository imagemRepository;
		
		public Instance withImagemRepository(ImagemRepository repository) {
			this.imagemRepository = repository;
			return this;
		}
		
		@Override
		public UsuarioService build() {
			UsuarioService service = new UsuarioService();
			service.setJpaRepository(jpaRepository);
			service.setImagemService(new ImagemService.Instance(imagemRepository).build());
			return service;
		}
	}

	private Usuario salvarUsuario(UsuarioRequest usuarioRequest) {
		Usuario usuarioCadastro = new CadastroMapper().transform(usuarioRequest);
		Usuario usuario = jpaRepository.save(usuarioCadastro);
		return usuario;
	}

	private Usuario alterarUsuario(Usuario usuario, UsuarioRequest usuarioRequest) {
		usuario.setNome(usuarioRequest.getNome());
		return jpaRepository.save(new AlterarMapper().transform(usuario, usuarioRequest));
	}
	
	public Usuario loadUser(long idUser) {
		return jpaRepository.findById(idUser);
	}

	private UsuarioBaseResponse converterUsuario(Usuario usuario) {
		Imagem imagem = imagemService.buscarImagem(usuario.getId());
		UsuarioBaseResponse usuarioBaseResponse = new UsuarioMapper().transform(usuario);
		if (imagem != null) {
			usuarioBaseResponse.setImagemPerfil(imagem.getPerfil());
		}
		return usuarioBaseResponse;
	}

	public ResponseEntity<ResponseBase<List<UsuarioBaseResponse>>> buscar() {
		ResponseBase<List<UsuarioBaseResponse>> baseResponse = new ResponseBase<>();
		List<Usuario> usuarios = jpaRepository.findAll();
		List<UsuarioBaseResponse> usuarioBaseResponses = new ArrayList<UsuarioBaseResponse>();
		for (Usuario usuario : usuarios) {
			usuarioBaseResponses.add(converterUsuario(usuario));
		}
		baseResponse = new ResponseBase<>(usuarioBaseResponses.size() > 0,
				usuarioBaseResponses.size() > 0 ? "Informações carredas com sucesso" : "Nenhum usuário cadastrado",
				usuarioBaseResponses);

		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> buscar(long id) {
		Usuario usuario = jpaRepository.findById(id);
		ResponseBase<UsuarioBaseResponse> baseResponse = new ResponseBase<>();
		if (usuario != null) {
			baseResponse = new ResponseBase<>(true, "Informações carredas com sucesso", converterUsuario(usuario));
		} else {
			baseResponse = new ResponseBase<UsuarioBaseResponse>(false, "Usuario não encontrado", null);
		}
		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> buscar(String cpf) {
		Usuario usuario = jpaRepository.findByCpf(cpf);
		ResponseBase<UsuarioBaseResponse> baseResponse;
		if (usuario != null) {
			baseResponse = new ResponseBase<>(true, "Informações carredas com sucesso", converterUsuario(usuario));
		} else {
			baseResponse = new ResponseBase<UsuarioBaseResponse>(false, "Usuario não encontrado", null);
		}
		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> autenticar(LoginDocumentRequest loginRequest) {
		Usuario usuario = jpaRepository.findByCpf(ValidateUtil.unmask(loginRequest.getCpf()));
		return verificarSenha(usuario, loginRequest.getSenha());
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> autenticar(LoginEmailRequest loginRequest) {
		int result = ValidateUtil.validateStringWithRegex(loginRequest.getEmail(), 200, ValidateUtil.REGEX_EMAIL);
		if (result == STRING_OK) {
			Usuario usuario = jpaRepository.findByEmail( loginRequest.getEmail());
			return verificarSenha(usuario, loginRequest.getSenha());
		} else {
			return new ResponseEntity<>(new ResponseBase<>(false, "Não foi possível carregar as informações",
					null), HttpStatus.OK);

		}
	}

	private ResponseEntity<ResponseBase<UsuarioBaseResponse>> verificarSenha(Usuario usuario, String senha) {
		ResponseBase<UsuarioBaseResponse> baseResponse;
		if (usuario != null) {
			if (Usuario.autenticarUsuario(usuario, senha)) {
				baseResponse = new ResponseBase<>(true, "Informações carredas com sucesso", converterUsuario(usuario));
			} else {
				baseResponse = new ResponseBase<>(false, "Usuário ou Senha inválida", null);
			}
		} else {
			baseResponse = new ResponseBase<>(false, "Não foi possível carregar as informações",
					null);
		}
		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> cadastrar(UsuarioRequest usuarioRequest) {
		ResponseBase<UsuarioBaseResponse> baseResponse;
		ValidatedField validatedField = usuarioRequest.validarCampos();
		if (validatedField.getSuccess()) {

			if(!usuarioRequest.docIsEmptyOrNull()) {
				if (jpaRepository.findByCpf(usuarioRequest.getCpf()) != null) {
					return new ResponseEntity<>(new ResponseBase<>(false, "Usuario já cadastrado", null), HttpStatus.OK);
				}
			}

			if(!usuarioRequest.emailIsEmptyOrNull()) {
				if (jpaRepository.findByEmail(usuarioRequest.getEmail()) != null) {
					return new ResponseEntity<>(new ResponseBase<>(false, "Email já cadastrado", null), HttpStatus.OK);
				}
			}

			if (usuarioRequest.authIsEmptyOrNull()) {
				return new ResponseEntity<>(new ResponseBase<>(false, "Informe um Email ou Documento para realizar o cadastro", null), HttpStatus.OK);
			}

			else {
				Usuario usuario = salvarUsuario(usuarioRequest);
				if (usuarioRequest.getImagemPerfil() != null) {
					imagemService.saveImage(usuario.getId(), usuarioRequest.getImagemPerfil());
				}
				baseResponse = new ResponseBase<>(true, "Usuario cadastrado com sucesso",
						converterUsuario(usuario));
			}
		} else {
			baseResponse = new ResponseBase<>(false, validatedField.getMsm(), null);
		}
		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public ResponseEntity<ResponseBase<UsuarioBaseResponse>> alterar(UsuarioRequest usuarioRequest) {
		ResponseBase<UsuarioBaseResponse> baseResponse;
		ValidatedField validatedField = usuarioRequest.validarCampos();
		if (validatedField.getSuccess()) {
			Usuario usuario = jpaRepository.findByCpf(usuarioRequest.getCpf());
			if (usuario != null) {
				if(usuarioRequest.getSenha() == null) {
					usuarioRequest.setSenha(usuario.getSenha());
				}
				Usuario usuarioAlterado = alterarUsuario(usuario, usuarioRequest);
				imagemService.alterarImagem(usuario.getId(), usuarioRequest.getImagemPerfil());
				baseResponse = new ResponseBase<>(true, "Usuario Alterado com sucesso",
						converterUsuario(usuarioAlterado));
			} else {
				baseResponse = new ResponseBase<>(false, "Usuario não encontrado", null);
			}
		} else {
			baseResponse = new ResponseBase<>(false, validatedField.getMsm(), null);
		}
		return new ResponseEntity<>(baseResponse, HttpStatus.OK);
	}

	public UsuarioRepository getJpaRepository() {
		return jpaRepository;
	}

	public void setJpaRepository(UsuarioRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}
	
	public ImagemService getImagemService() {
		return imagemService;
	}

	public void setImagemService(ImagemService imagemService) {
		this.imagemService = imagemService;
	}
}
