package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.UsuarioAdDTO;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    public LdapService(LdapTemplate ldapTemp) {
        this.ldapTemplate = ldapTemp;
    }

    public Optional<UsuarioAdDTO> buscarUsuario(String login) {

        List<UsuarioAdDTO> usuarios = ldapTemplate.search(
                query().where("sAMAccountName").is(login),
                (AttributesMapper<UsuarioAdDTO>) attrs -> new UsuarioAdDTO(
                        getAttribute(attrs, "sAMAccountName"),
                        getAttribute(attrs, "displayName"),
                        getAttribute(attrs, "mail")
                )
        );

        return usuarios.stream().findFirst();
    }

    private String getAttribute(Attributes attrs, String nome) throws NamingException {
        Attribute attribute = attrs.get(nome);
        return attribute != null ? attribute.get().toString() : null;
    }

    public UsuarioAdDTO search(String login) {

        List<UsuarioAdDTO> usuarios = ldapTemplate.search(
                query().where("sAMAccountName").is(login),
                (AttributesMapper<UsuarioAdDTO>) attrs -> new UsuarioAdDTO(
                        getAttribute(attrs, "sAMAccountName"),
                        getAttribute(attrs, "displayName"),
                        getAttribute(attrs, "mail")
                )
        );
        return usuarios.stream().findFirst().orElseThrow(() -> new BusinessException("Login inexistente no AD"));
    }
}

/*

List<String> dns = ldapTemplate.search("","(sAMAccountName=" + usuario + ")",
        (AttributesMapper<String>) attrs -> {
            return attrs.get("distinguishedName") != null ? attrs.get("distinguishedName").get().toString() : null;
        }
);

public Optional<UsuarioAdDTO> buscarUsuario(String login) {
    List<UsuarioAdDTO> usuariosold = ldapTemplate.search("","(sAMAccountName=" + login + ")",
            (AttributesMapper<UsuarioAdDTO>) attrs -> new UsuarioAdDTO(
                    getAttribute(attrs, "sAMAccountName"),
                    getAttribute(attrs, "displayName"),
                    getAttribute(attrs, "mail")
            )
    );
    return usuarios.stream().findFirst();
}

*/