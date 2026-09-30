package br.andrew.sap.services.security.acesso

/** Documento ou entrada invalida: 400, com a lista de erros para a tela mostrar. */
class RegrasValidacaoException(val erros: List<String>, val avisos: List<String> = emptyList()) :
    RuntimeException(erros.joinToString("; "))

/** Outra pessoa gravou uma versao nova antes desta gravacao: 409, a tela deve recarregar. */
class RegrasConflitoException(mensagem: String) : RuntimeException(mensagem)

class RegrasNaoEncontradasException(mensagem: String) : RuntimeException(mensagem)

/** Mesma mensagem que o RoleBasedAuthorizationFilter usa. */
class RegrasAcessoNegadoException :
    RuntimeException("Você não tem permissão para acessar este recurso.")
