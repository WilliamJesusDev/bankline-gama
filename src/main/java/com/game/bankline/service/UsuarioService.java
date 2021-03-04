package com.game.bankline.service;

import java.util.Arrays;

import com.game.bankline.dto.CredenciaisDto;
import com.game.bankline.exceptions.SenhaInvalidaException;
import com.game.bankline.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.bankline.dto.UsuarioDto;
import com.game.bankline.entity.Conta;
import com.game.bankline.entity.PlanoConta;
import com.game.bankline.entity.Usuario;
import com.game.bankline.entity.enums.TipoConta;
import com.game.bankline.entity.enums.TipoMovimento;
import com.game.bankline.exceptions.DuplicateKeyException;
import com.game.bankline.exceptions.ObjectNotFoundException;
import com.game.bankline.exceptions.RequiredFieldsException;
import com.game.bankline.repository.ContaRepository;
import com.game.bankline.repository.PlanoContaRepository;
import com.game.bankline.repository.UsuarioRepository;

@Service
public class UsuarioService {
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ContaRepository contaRepository;
	
	@Autowired
	private PlanoContaRepository planoContaRepository;
	
	public UsuarioDto createUsuario(Usuario usuario) {
		UsuarioDto novoUsuario = new UsuarioDto();
		
		if(usuario.getCpf() == null || usuario.getLogin() == null || usuario.getNome() == null || usuario.getSenha() == null) {
			
			throw new RequiredFieldsException("Todos os campos sao obrigatorios");
			
		} else if(usuarioRepository.findByCpf(usuario.getCpf()).isPresent()){
        	
            throw new DuplicateKeyException("Usuario de cpf: "+usuario.getCpf()+" existente");
            
        } else if(usuarioRepository.findByLogin(usuario.getLogin()).isPresent()){
        	
            throw new DuplicateKeyException("Login "+usuario.getLogin()+" ja utilizado");
            
        } else {
        	usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        	Usuario usuarioCriado = usuarioRepository.save(usuario);
        	novoUsuario.setLogin(usuarioCriado.getLogin());
        	novoUsuario.setNome(usuarioCriado.getNome());
        	
        	Conta contaCredito = new Conta(null,"Conta Credito",usuario.getLogin(),0.0,TipoConta.CREDITO);
        	Conta contaDebito = new Conta(null,"Conta Debito",usuario.getLogin(),0.0,TipoConta.DEBITO);
        	
        	contaRepository.saveAll(Arrays.asList(contaCredito,contaDebito)); 
        	
        	criarPlanosContaPadrao(usuarioCriado.getLogin());
        	
        }	
		
		return novoUsuario;
	}
	
	public UsuarioDto findUsuarioById(Integer id) {
		 UsuarioDto usuarioBuscado;
		
		if(usuarioRepository.findById(id).isPresent()){
			Usuario usuario = usuarioRepository.findById(id).get();
			usuarioBuscado = new UsuarioDto(usuario.getNome(),usuario.getLogin());
    		
        }else{
            throw new ObjectNotFoundException("Usuario de id: "+id+" nao encontrado");
        }
		
		return usuarioBuscado;
	}

	public void changePassword(CredenciaisDto credenciais){
		Usuario usuario = usuarioRepository.findByLogin(credenciais.getLogin()).get();

		if(usuarioRepository.findByLogin(credenciais.getLogin()).isPresent()){
			usuario.setSenha(passwordEncoder.encode(credenciais.getSenha()));
			usuarioRepository.save(usuario);
		}else{
			throw new ObjectNotFoundException("Usuario de login: "+credenciais.getLogin()+" nao encontrado");
		}
	}
	
	private void criarPlanosContaPadrao(String login){
		
		PlanoConta planoPadraoReceita = new PlanoConta(null,"RECEITA PADRAO",login,true,TipoMovimento.RECEITA.getId());
		PlanoConta planoPadraoDespesa = new PlanoConta(null,"DESPESA PADRAO",login,true,TipoMovimento.DESPESA.getId());
		PlanoConta planoPadraoTransferencia = new PlanoConta(null,"TRANSFERENCIA PADRAO",login,true,TipoMovimento.TRANSFERENCIA.getId());
		
		planoContaRepository.saveAll(Arrays.asList(planoPadraoReceita,planoPadraoDespesa,planoPadraoTransferencia));

	}


	public String autenticar(Usuario usuario ){
		UserDetails user = userDetailsService.loadUserByUsername(usuario.getLogin());
		boolean senhasBatem = passwordEncoder.matches( usuario.getSenha(), user.getPassword() );


		if(senhasBatem){
			return jwtUtil.generateToke(usuario.getLogin());
		}

		throw new SenhaInvalidaException();
	}

}
