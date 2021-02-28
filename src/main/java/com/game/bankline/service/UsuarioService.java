package com.game.bankline.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.bankline.dto.UsuarioDto;
import com.game.bankline.entity.Usuario;
import com.game.bankline.exceptions.DuplicateKeyException;
import com.game.bankline.exceptions.ObjectNotFoundException;
import com.game.bankline.exceptions.RequiredFieldsException;
import com.game.bankline.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	public UsuarioDto createUsuario(Usuario usuario) {
		UsuarioDto novoUsuario = new UsuarioDto();
		
		if(usuario.getCpf() == null || usuario.getLogin() == null || usuario.getNome() == null || usuario.getSenha() == null) {
			
			throw new RequiredFieldsException("Todos os campos sao obrigatorios");
			
		} else if(usuarioRepository.findByCpf(usuario.getCpf()).isPresent()){
        	
            throw new DuplicateKeyException("Usuario de cpf: "+usuario.getCpf()+" existente");
            
        } else if(usuarioRepository.findByLogin(usuario.getLogin()).isPresent()){
        	
            throw new DuplicateKeyException("Login "+usuario.getLogin()+" ja utilizado");
            
        } else {
        	Usuario usuarioCriado = usuarioRepository.save(usuario);
        	novoUsuario.setLogin(usuarioCriado.getLogin());
        	novoUsuario.setNome(usuarioCriado.getNome());
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

}
