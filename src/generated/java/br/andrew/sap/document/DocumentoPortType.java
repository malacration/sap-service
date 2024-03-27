/**
 * DocumentoPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public interface DocumentoPortType extends java.rmi.Remote {

    /**
     * Dados do processo. <BR>
     *                 PROCESSID         -> Identificador do processo*;<BR>
     * WORKFLOWTITLE     -> Título;<BR>
     *                 DOCUMENTBATCHID   -> Identificador do documento;<BR>
     * USERID            -> Identificador do usuário*;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.NewCaptureInstanceResponseType newCaptureInstance(br.andrew.sap.document.NewCaptureInstanceRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancelamento de documento. <BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário;<BR>
     *                 DSCANCEL       -> Descrição do cancelamento*;<BR>
     *                 IDCATEGORY     -> Identificador da categoria;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.CancelDocumentResponseType cancelDocument(br.andrew.sap.document.CancelDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Verifica se o usuário possui permissão em um documento<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário*;<BR>
     *                 PERMISSIONSTYPE-> Tipo de permissão*(1 => Alterar;
     * 2 => Excluir;
     *                 3 => Revisar;
     *                 4 => Conhecimento;
     *                 5 => Distribuir cópia;
     *                 6 => Visualizar;
     *                 7 => Cancelar;
     *                 8 => Arquivar;
     *                 9 => Imprimir;
     *                 10 => Treinamento;
     *                 11 => Registrar treinamento;
     *                 12 => Salvar localmente;
     *                 13 => Assinar;
     *                 14 => Notificação;
     *                 15 => Incluir comentário;
     *                 16 => Avaliar aplicabilidade)<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Acesso
     * permitido´. <BR>Em caso de FALHA, retorna uma mensagem com o erro
     * ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.CheckAccessPermissionResponseType checkAccessPermission(br.andrew.sap.document.CheckAccessPermissionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Remove permissão de acesso de um determinado documento.<BR>
     * IDDOCUMENT     -> Identificador do documento*;<BR>
     *                 IDUSER         -> Identificador do usuário*;<BR>
     *                 USERTYPE       -> Tipo de usuário(1 => Usuário; 2
     * => Área; 3 => Função; 4=>Área/Função; 5 => Equipe; 6 => Todos)*;<BR>
     * Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação realizada
     * com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem com o erro
     * ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.DeleteAccessPermissionResponseType deleteAccessPermission(br.andrew.sap.document.DeleteAccessPermissionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Excluir Documento. <BR>
     *                 IDCATEGORY     -> Identificador da categoria*;<BR>
     * IDDOCUMENT     -> Identificador do documento*;<BR>
     *                 IDUSER         -> Identificador do usuário;<BR>
     *                 JUSTIFY        -> Justificativa*;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.DeleteDocumentResponseType deleteDocument(br.andrew.sap.document.DeleteDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Exclusão de revisão.<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDREVISION     -> Identificador da revisão*;<BR>
     *                 IDUSER         -> Identificador do usuário;<BR>
     *                 JUSTIFY        -> Justificativa;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´.<BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.DeleteRevisionResponseType deleteRevision(br.andrew.sap.document.DeleteRevisionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Download de arquivo eletrônico.<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDREVISION     -> Identificador da revisão;<BR>
     *                 IDUSER         -> Identificador do usuário;<BR>
     *                 FGCONVERTOPDF  -> Informar o valor 1 para baixar o
     * arquivo em PDF, se existir, caso contrário baixa o arquivo original.;<BR>
     * IDCATEGORY     -> Identificador da categoria;<BR>
     *                 FGWATERMARK    -> Marca d´água;<BR>
     *                 NMFILE         -> Nome do arquivo;<BR>
     *                 FGFILELINK     -> Informar 1 para retornar o link
     * para download do arquivo eletrônico. Caso contrário, será retornado
     * o conteúdo do arquivo;<BR>
     *                 Em caso de SUCESSO: Retorna um arquivo eletrônico.<BR>Em
     * caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.DownloadEletronicFileResponseType downloadEletronicFile(br.andrew.sap.document.DownloadEletronicFileRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Editar Documento. <BR>
     *                 IDCATEGORY     -> Identificador da categoria*;<BR>
     * IDDOCUMENT     -> Identificador do documento*;<BR>
     *                 IDUSER         -> Identificador do usuário;<BR>
     *                 IDREVISION     -> Identificador da revisão;<BR>
     *                 TITLE          -> Título do documento;<BR>
     *                 SUMMARY        -> Resumo do documento;<BR>
     *                 IDUSERRESP     -> Usuário responsável;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.EditDocumentResponseType editDocument(br.andrew.sap.document.EditDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Busca por palavras-chave.. <BR>
     *                 LETTERS     -> Matriz com as letras iniciais das palavras-chave
     * a serem buscadas, poderá ser usado o caracter ´*´ como curinga.*;<BR>
     * Em caso de sucesso, uma matriz com as palavras-chave é retornada.<BR>
     * Em caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.GetKeyWordsResponseType getKeyWords(br.andrew.sap.document.GetKeyWordsRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Busca por palavras-chave, seus significados e sinônimos.. <BR>
     * WORDS     -> Matriz com as palavras-chave a serem buscadas.*;<BR>
     *                 Em caso de sucesso, uma matriz de objetos com os seguintes
     * índices é retornada: <BR>
     *                 IDKEYWORD      -> Palavra-chave<BR>
     *                 NMKEYWORD      -> Significado da palavra-chave.<BR>
     * SYNONYMS       -> Matriz de strings com os sinônimos da palavra-chave.<BR>
     * Em caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.GetKeyWordsDataResponseType getKeyWordsData(br.andrew.sap.document.GetKeyWordsDataRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Retorna os tipos de acessos que o usuário possui em um documento<BR>
     * IDDOCUMENT     -> Identificador do documento*;<BR>
     *                 IDUSER         -> Identificador do usuário*;<BR>
     *                 IDCATEGORY     -> Identificador da categoria;<BR>
     *                 Em caso de SUCESSO: Retorna os tipos de permissões.<BR>Em
     * caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 Tipo de permissão*(1 => Alterar;
     *                 2 => Excluir;
     *                 3 => Revisar;
     *                 4 => Conhecimento;
     *                 5 => Distribuir cópia;
     *                 6 => Visualizar;
     *                 7 => Cancelar;
     *                 8 => Arquivar;
     *                 9 => Imprimir;
     *                 10 => Treinamento;
     *                 11 => Registrar treinamento;
     *                 12 => Salvar localmente;
     *                 13 => Assinar;
     *                 14 => Notificação;
     *                 15 => Incluir comentário;
     *                 16 => Avaliar aplicabilidade)<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.ListAccessPermissionResponseType listAccessPermission(br.andrew.sap.document.ListAccessPermissionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Lista documentos pendentes em elaboração, consenso, aprovação
     * ou homologação.<BR>
     *                 IDUSER         -> Identificador do usuário*;<BR>
     *                 Em caso de SUCESSO: Retorna identificador do documento,
     * revisão, tipo da tarefa.<BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.ListPendencyDocumentResponseType listPendencyDocument(br.andrew.sap.document.ListPendencyDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inclui permissão de acesso a um determinado documento.<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário*;<BR>
     *                 USERTYPE       -> Tipo de usuário(1 => Usuário; 2
     * => Área; 3 => Função; 4=>Área/Função; 5 => Equipe; 6 => Todos)*;<BR>
     * PERMISSION     -> Tipo de permissão(1 => Alterar;
     *                 2 => Excluir;
     *                 3 => Revisar;
     *                 4 => Conhecimento;
     *                 5 => Distribuir cópia;
     *                 6 => Visualizar;
     *                 7 => Cancelar;
     *                 8 => Arquivar;
     *                 9 => Imprimir;
     *                 10 => Treinamento;
     *                 11 => Registrar treinamento;
     *                 12 => Salvar localmente;
     *                 13 => Assinar;
     *                 14 => Notificação;
     *                 15 => Incluir comentário;
     *                 16 => Avaliar aplicabilidade). Ex: 1,2,3 *;<BR>
     *                 PERMISSIONSTYPE-> Tipo de ação (1 => Permitir; 2 =>
     * Negar)*;<BR>
     *                 FGADDLOWERLEVEL-> Considerar subníveis (1 => Sim;
     * 2 => Não);<BR>
     *                 IDCATEGORY     -> Identificador da categoria;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´.<BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.NewAccessPermissionResponseType newAccessPermission(br.andrew.sap.document.NewAccessPermissionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Incluir novo documento. <BR>
     *                 IDCATEGORY     -> Identificador da categoria*;<BR>
     * IDDOCUMENT     -> Identificador do documento; <BR>
     *                 TITLE          -> Título do documento*;<BR>
     *                 DSRESUME       -> Resumo do documento;<BR>
     *                 DTDOCUMENT     -> Data do documento;O formato da data
     * deve ser A-M-D (Ano-Mês-Dia);
     *                 ATTRIBUTES     -> Atributo do documento. Ex: IDENTIFICADOR_DO_ATRIBUTO1=VALOR_DO_ATRIBUTO1;IDENTIFICADOR_DO_ATRIBUTO2=VALOR_DO_ATRIBUTO2;;<BR>
     * Exemplo com atributo multivalorado (Observe que o separador para os
     * valores dos atributos multivalorados é a ´,´): IDENTIFICADOR_DO_ATRIBUTO1=VALOR1_DO_ATRIBUTO1,VALOR2_DO_ATRIBUTO1,VALOR3_DO_ATRIBUTO1.<br>
     * OBS: Para atributos com valor numérico, o separador de casas decimais
     * deve ser o ´.´. Para campos de data, o valor deve ter o formato "Y-M-D"
     * (Ano-Mês-Dia).<br>
     *                 IDUSER         -> Identificador do usuário;<BR>
     *                 PARTICIPANTS   -> Participantes da revisão.<BR>
     *                 CONTROL: Tipo de controle
     *                 1 -> Usuário;
     *                 2 -> Área;
     *                 3 -> Função;
     *                 4 -> Área/Função;
     *                 5 -> Equipe;
     *                 ENTCONTROL: Identificador da entidade de controle.
     * Ex: Identificador para controle por usuário.
     *                 STEP: Etapa Ex: 1,2,3,4
     *                 1 -> Elaboração;
     *                 2 -> Consenso;
     *                 3 -> Aprovação;
     *                 4 -> Homologação;
     *                 SEQUENCE: Sequência. Ex: 2, 2, 2, 2
     *                 QTDEADLINE: Prazo (dias). Ex: 5;
     *                 FGMODEL        -> Flag Arquivo modelo''. Ex: 1 = Marcado
     * / 2 = Desmarcado <BR>
     *                 FILE           -> Array Arquivos; <BR>
     *                 NMFILE: String; Nome do arquivo;
     *                 BINFILE: Stream; Binário do arquivo eletrônico;
     *                 CONTAINER: String; Identificador do container de arquivo
     * complexo;<BR>
     *                 KEYWORD ->    Array Palavra-chave; <BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido. <BR>
     */
    public br.andrew.sap.document.NewDocumentResponseType newDocument(br.andrew.sap.document.NewDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Incluir novo documento. <BR>
     *                 CategoryID     => Identificador da categoria*;<BR>
     * DocumentID     => Identificador do documento; <BR>
     *                 Title          => Título do documento*;<BR>
     *                 Summary        => Resumo do documento;<BR>
     *                 Date           => Data do documento;O formato da data
     * deve ser A-M-D (Ano-Mês-Dia);
     *                 Attributes     => Atributo do documento;<BR>
     *                 OBS: Para atributos com valor numérico, o separador
     * de casas decimais deve ser o ´.´. Para campos de data, o valor deve
     * ter o formato "Y-M-D" (Ano-Mês-Dia).<br>
     *                 ResponsibleUserID => Identificador do usuário;<BR>
     * RevisionMembers   => Participantes da revisão.<BR>
     *                 MemberType: Tipo de participante
     *                 1 => Usuário;
     *                 2 => Área;
     *                 3 => Função;
     *                 4 => Área/Função;
     *                 5 => Equipe;
     *                 MemberID: Identificador da entidade de controle. Ex:
     * Identificador para controle por usuário.
     *                 Step: Etapa Ex: 1,2,3,4
     *                 1 => Elaboração;
     *                 2 => Consenso;
     *                 3 => Aprovação;
     *                 4 => Homologação;
     *                 Sequence: Sequência. Ex: 2, 2, 2, 2
     *                 Deadline: Prazo (dias). Ex: 5;
     *                 TemplateID      => Flag Arquivo modelo''. Ex: 1 =
     * Marcado / 2 = Desmarcado <BR>
     *                 Files           => Array Arquivos; <BR>
     *                 Name:      String; Nome do arquivo;
     *                 Content:   Stream; Binário do arquivo eletrônico;
     *                 Container: String; Identificador do container de arquivo
     * complexo;<BR>
     *                 Keywords =>    Array Palavra-chave; <BR>
     *                 KeywordID:  String; Identificador da palavra-chave;
     * <BR>
     *                 * Requerido. <BR>
     *                 LanguageID      => Flag Idioma do documento <BR>
     *                 RevisionID      => Flag Identificador da revisão <BR>
     */
    public br.andrew.sap.document.NewDocument2ResponseType newDocument2(br.andrew.sap.document.NewDocument2RequestType parameters) throws java.rmi.RemoteException;

    /**
     * Link para compartilhamento. <BR>
     *                 DocumentID   => Identificador do documento*; <BR>
     *                 CategoryID   => Identificador da categoria; <BR>
     *                 Recreate     => Recriar link de compartilhamento (1
     * - Sim; 2 - Não)*; <BR>
     *                 Public       => Tornar público (1 - Sim; 2 - Não)*;
     * <BR>
     *                 Download     => Salvar localmente (1 - Sim; 2 - Não);
     * <BR>
     *                 Original     => Utilizar arquivo original (1 - Sim;
     * 2 - Não); <BR>
     *                 ValidityDate => Data de validade; O formato da data
     * deve ser A-M-D (Ano-Mês-Dia); <BR>
     *                 * Requerido <BR>
     */
    public br.andrew.sap.document.ShareDocumentResponseType shareDocument(br.andrew.sap.document.ShareDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Incluir nova revisão.<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário;<BR>
     *                 PARTICIPANTS   -> Participantes da revisão.<BR>
     *                 CONTROL: Tipo de controle
     *                 1 -> Usuário;
     *                 2 -> Área;
     *                 3 -> Função;
     *                 4 -> Área/Função;
     *                 5 -> Equipe;
     *                 ENTCONTROL: Identificador da entidade de controle.
     * Ex: Identificador para controle por usuário.
     *                 STEP: Etapa. Ex: 1,2,3,4
     *                 1 -> Elaboração;
     *                 2 -> Consenso;
     *                 3 -> Aprovação;
     *                 4 -> Homologação;
     *                 SEQUENCE: Sequência. Ex: 2, 2, 2, 2
     *                 QTDEADLINE: Prazo (dias). Ex: 5;<BR>
     *                 DOCUMENTDATA: Matriz; Dados do documento;
     *                 NMTITLE: String; Título do documento;
     *                 ATTRIBUTTES: String; Atributo do documento;
     *                 Ex: IDENTIFICADOR_DO_ATRIBUTO1=VALOR_DO_ATRIBUTO1;IDENTIFICADOR_DO_ATRIBUTO2=VALOR_DO_ATRIBUTO2;;
     * 
     *                 Exemplo com atributo multivalorado (Observe que o
     * separador para os valores dos atributos multivalorados é a ´,´): IDENTIFICADOR_DO_ATRIBUTO1=VALOR1_DO_ATRIBUTO1,VALOR2_DO_ATRIBUTO1,VALOR3_DO_ATRIBUTO1.
     * 
     *                 OBS: Para atributos com valor numérico, o separador
     * de casas decimais deve ser o ´.´. Para campos de data, o valor deve
     * ter o formato "Y-M-D" (Ano-Mês-Dia).<BR>
     *                 FILE: Matriz; Arquivos;
     *                 NMFILE: String; Nome do arquivo;
     *                 BINFILE: Stream; Binário do arquivo eletrônico;<BR>
     * FGSTATUS: Deseja encerrar a revisão?
     *                 0 -> Revisão;
     *                 1 -> Encerrado;<BR>
     *                 DSJUSTIFY: String; Justificativa;<BR>
     *                 IDCATEGORY     -> Identificador da categoria;<BR>
     *                 IDREVISION     -> Identificador da revisão;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´.<BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.NewRevisionResponseType newRevision(br.andrew.sap.document.NewRevisionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Libera tarefa de um usuário. <BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário*;<BR>
     *                 APPROV         -> Flag 1 = Aprovado 2 = Reprovado*;<BR>
     * JUSTIFY        -> Justificativa, Obrigatório quando reprovado;<BR>
     * Em caso de SUCESSO: Retorna identificador do próximo usuário pendente,
     * tipo de usuário.<BR>
     *                 Em caso de FALHA, retorna uma mensagem com o erro
     * ocorrido.<BR>
     *                 Retorno:
     *                 IDUSER     -> Identificador do usuário da sequência
     * USERTYPE       -> Tipo de usuário(1 => Usuário; 2 => Área; 3 => Função;
     * 4=>Área/Função; 5 => Equipe; 6 => Todos)*;<BR>
     *                 *Requerido.<BR>
     *                 IDCATEGORY     -> Identificador da categoria*;<BR>
     */
    public br.andrew.sap.document.ReleasePendencyDocumentResponseType releasePendencyDocument(br.andrew.sap.document.ReleasePendencyDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Consulta auditoria de acessos.<BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDUSER         -> Identificador do usuário*;<BR>
     *                 PERIODBEGIN    -> Data de início do período. O formato
     * da data deve ser A-M-D (Ano-Mês-Dia)*;<BR>
     *                 PERIODEND      -> Data de término do período. O formato
     * da data deve ser A-M-D (Ano-Mês-Dia)*;<BR>
     *                 Em caso de SUCESSO: Retorna dados da auditoria.<BR>
     * Em caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 Legenda: TYPEAUDIT (1 - Visualização de documento,
     * 2 - Documento cadastrado, 3 - Documento removido, 4 - Revisão cadastrada,
     * 5 - Revisão removida, 6 - Revisão encerrada, 7 - Cópia controlada
     * impressa, 8 - Cópia não controlada impressa, 9 - Quantidade de cópias
     * programadas alterada,
     *                 11 - Documento cancelado, 12 - Documento liberado,
     * 13 - Exclusão de arquivo eletrônico, 14 - Arquivamento registrado,
     * 15 - Arquivamento cancelado, 16 - Assinatura digital, 17 - Inclusão
     * de arquivo eletrônico, 18 - Inclusão de versão PDF para arquivo eletrônico,
     * 19 - Exclusão de versão PDF para arquivo eletrônico)
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.SearchAccessAuditResponseType searchAccessAudit(br.andrew.sap.document.SearchAccessAuditRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Categorias de documento.<BR>
     *                 RESULTARRAY: Matriz; Resultado da pesquisa;<BR>
     *                 CDCATEGORY: Integer; Código da categoria;<BR>
     *                 CDCATEGORYOWNER: Integer; Código da categoria;<BR>
     * IDCATEGORY: String; Identificador da categoria;<BR>
     *                 NMCATEGORY: String; Nome da categoria;
     */
    public br.andrew.sap.document.SearchCategoryResponseType searchCategory() throws java.rmi.RemoteException;

    /**
     * Consulta de documentos.<BR>
     *                 FILTERS    -> Filtros de consulta*;<BR>
     *                 IDCATEGORY: String; Identificador da categoria;<BR>
     * IDDOCUMENT: String; Identificador do documento;<BR>
     *                 NMTITLE: String; Título do documento;<BR>
     *                 NMAUTHOR: String; Autor;<BR>
     *                 CONTENT: String; Conteúdo;<BR>
     *                 SYNONYM: String; Procurar por sinônimo;<BR>
     *                 STATUS: String; Situação do documento. Ex: 1,2,3,4;<BR>
     * 1: Emissão;
     *                 2: Homologado;
     *                 3: Revisão;
     *                 4: Cancelado;<BR>
     *                 PERMISSION: String; Permissões do documento. Ex: 1,2,3,4,5;<BR>
     * 1: Incluir;
     *                 2: Alterar;
     *                 3: Excluir;
     *                 4: Revisar;
     *                 5: Conhecimento;
     *                 6: Distribuir cópia;
     *                 7: Visualizar;
     *                 8: Cancelar;
     *                 9: Arquivar;
     *                 10: Imprimir;
     *                 11: Treinamento;
     *                 12: Registrar treinamento;
     *                 13: Salvar localmente;
     *                 14: Assinar;
     *                 15: Notificação;<BR>
     *                 IDUSERPERM: String; Identificador do usuário da permissão;<BR>
     * DEPARTMENTPERM: String; Identificador da área da permissão;<BR>
     *                 IDUSERREGISTER: String; Identificador do usuário de
     * cadastro;<BR>
     *                 FUNCTIONPERM: String; Identificador da função da permissão;<BR>
     * KEYWORD: String; Palavra-chave;<BR>
     *                 OPKEYWORD: Integer; Operador da palavra chave;<BR>
     * 1: E;
     *                 2: OU;<BR>
     *                 DTDOCLASTDAY: String; Número de dias referente à data
     * do documento (últimos dias);<BR>
     *                 DTDOCBEGIN: String; Data de início para período do
     * documento;<BR>
     *                 DTDOCEND: String; Data final para período do documento;<BR>
     * DTREGLASTDAY: String; Número de dias referente à data de cadastro
     * do documento (últimos dias);<BR>
     *                 DTREGBEGIN: String; Data inicial para período da data
     * de cadastro do documento;<BR>
     *                 DTREGEND: String; Data final para período da data
     * de cadastro do documento;<BR>
     *                 DTVALIDBEGIN: String; Data de início para filtro por
     * data de validade;<BR>
     *                 DTVALIDEND: String; Data final para filtro por data
     * de validade;<BR>
     *                 DTREVLASTDAY: String; Número de dias referente à data
     * da revisão do documento (últimos dias);<BR>
     *                 DTREVBEGIN: String; Data de início para período da
     * revisão do documento;<BR>
     *                 DTREVEND: String; Data final para período da revisão
     * do documento;<BR>
     *                 IDPROJECT: String; Identificador do projeto;<BR>
     *                 NMPROJECT: String; Nome do projeto;<BR>
     *                 ATTRIBUTTES: String; Atributos do documento;<BR>
     *                 Ex: IDENTIFICADOR_DO_ATRIBUTO1=VALOR_DO_ATRIBUTO1;IDENTIFICADOR_DO_ATRIBUTO2=VALOR_DO_ATRIBUTO2;;
     * Exemplo com atributo multivalorado (Observe que o separador para os
     * valores dos atributos multivalorados é a ´,´): IDENTIFICADOR_DO_ATRIBUTO1=VALOR1_DO_ATRIBUTO1,VALOR2_DO_ATRIBUTO1,VALOR3_DO_ATRIBUTO1.
     * 
     *                 OBS: Para atributos com valor numérico, o separador
     * de casas decimais deve ser o ´.´. Para campos de data, o valor deve
     * ter o formato "Y-M-D" (Ano-Mês-Dia).<BR>
     *                 IDUSER     -> Identificador do usuário*;<BR>
     *                 Em caso de SUCESSO: Retorna uma matriz com o resultado
     * da pesquisa e o percentual de aderência à pesquisa.<BR>
     *                 RESULTS: Matriz; Resultado da pesquisa;
     *                 IDDOCUMENT: String; Identificador do documento;
     *                 NMTITLE: String; Título do documento;<BR>
     *                 ADHESION: Integer; Percentual de aderência à pesquisa;
     * Em caso de FALHA, retorna uma mensagem com o erro ocorrido.<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.SearchDocumentResponseType searchDocument(br.andrew.sap.document.SearchDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Atribui valor a um atributo de determinado documento. Caso
     * o atributo não esteja associado a um documento, este será associado.
     * <BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDREVISION     -> Identificador da revisão;<BR>
     *                 IDATTRIBUTE    -> Identificador do atributo*;<BR>
     *                 VLATTRIBUTE    -> Valor do atributo*;<BR>
     *                 SEPARATOR      -> Separador*;<BR>
     * 
     *                 Em caso de SUCESSO, retorna uma mensagem de ´1´ significando
     * que o atributo foi alterado com sucesso. Em caso de FALHA, retorna
     * um Array com a mensagem do erro ocorrido. *Parâmetro requerido.
     * 
     *                 **Caso não informado, será considerada a revisão vigente.
     * <BR>
     */
    public br.andrew.sap.document.SetAttributeValueResponseType setAttributeValue(br.andrew.sap.document.SetAttributeValueRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Upload de arquivo. <BR>
     *                 IDDOCUMENT     -> Identificador do documento*; <BR>
     * IDREVISION     -> Identificador da revisão; <BR>
     *                 IDUSER         -> Identificador do usuário; <BR>
     *                 IDCATEGORY     -> Identificador da categoria; <BR>
     * FILE           -> Array Arquivos*; <BR>
     *                 NMFILE: String; Nome do arquivo;
     *                 BINFILE: Stream; Binário do arquivo eletrônico;
     *                 CONTAINER: String; Identificador do container de arquivo
     * complexo;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido. <BR>
     */
    public br.andrew.sap.document.UploadEletronicFileResponseType uploadEletronicFile(br.andrew.sap.document.UploadEletronicFileRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Consulta de documentos. <BR>
     *                 IDDOCUMENT     -> Identificador do documento*;<BR>
     * IDREVISION     -> Identificador da revisão;<BR>
     *                 Em caso de SUCESSO: Retorna os metadados.<BR>
     *                 IDDOCUMENT: String; Identificador do documento;<BR>
     * NMTITLE: String; Título do documento;<BR>
     *                 IDCATEGORY: String; Identificador da categoria;<BR>
     * NMCATEGORY: String; Nome da categoria;<BR>
     *                 STATUS: String; Situação do documento: 1: Emissão,
     * 2: Homologado, 3: Revisão, 4: Cancelado, 5: Indexação, 6: Aprovação,
     * 7: Contrato encerrado<BR>
     *                 NRHITS: Integer; Hits;<BR>
     *                 NMAUTHOR: String; Autor;<BR>
     *                 IDREVISION: String; Identificador da revisão;<BR>
     *                 DTDOCUMENT: String; Data do documento;<BR>
     *                 QTVALIDITY: Integer; Validade;<BR>
     *                 FGMEASVALID: String; Medida para a validade;
     *                 1: Dias;
     *                 2: Meses;
     *                 3: Anos;<BR>
     *                 REVALIDATION: String; Revalidação;<BR>
     *                 DTVALIDITY: String; Data de validade;<BR>
     *                 RESUME: String; Resumo do documento;<BR>
     *                 CANCELREASON: String; Motivo do cancelamento;<BR>
     *                 URL: String;<BR>
     *                 ATTRIBUTTES: Matriz; Atributos do documento.<BR>
     *                 ATTRIBUTTENAME: String; Identificador do atributo;<BR>
     * ATTRIBUTTEVALUE: vetor; Valor do atributo;<BR>
     *                 ELECTRONICFILE: Matriz; Arquivo eletrônico.<BR>
     *                 FILENAME: String; Nome do arquivo;<BR>
     *                 Em caso de FALHA, retorna uma mensagem com o erro
     * ocorrido.<BR>
     *                 *Requerido<BR>
     */
    public br.andrew.sap.document.ViewDocumentDataResponseType viewDocumentData(br.andrew.sap.document.ViewDocumentDataRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Associação de documentos na estrutura do container. <BR>
     *                 UpperLevelCategoryID -> Identificador da categoria
     * superior*;<BR>
     *                 UpperLevelDocumentID -> Identificador do documento
     * superior*;<BR>
     *                 RevisionID           -> Identificador da revisão;<BR>
     * StructID             -> Identificador do documento superior*;<BR>
     *                 LowerLevelCategoryID -> Identificador da categoria
     * inferior*;<BR>
     *                 LowerLevelDocumentID -> Identificador do documento
     * inferior*;<BR>
     *                 *Requerido.
     */
    public br.andrew.sap.document.NewDocumentContainerAssociationResponseType newDocumentContainerAssociation(br.andrew.sap.document.NewDocumentContainerAssociationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Desassociação de documentos na estrutura do container. <BR>
     * UpperLevelCategoryID -> Identificador da categoria superior*;<BR>
     *                 UpperLevelDocumentID -> Identificador do documento
     * superior*;<BR>
     *                 RevisionID           -> Identificador da revisão;<BR>
     * StructID             -> Identificador do documento superior*;<BR>
     *                 LowerLevelCategoryID -> Identificador da categoria
     * inferior*;<BR>
     *                 LowerLevelDocumentID -> Identificador do documento
     * inferior*;<BR>
     *                 *Requerido.
     */
    public br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType deleteDocumentContainerAssociation(br.andrew.sap.document.DeleteDocumentContainerAssociationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancelamento de documento. <BR>
     *                 DocumentID -> Identificador do documento*;<BR>
     *                 CategoryID -> Identificador da categoria;<BR>
     *                 ProcessID  -> Identificador do processo*;<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.AssociateProcessResponseType associateProcess(br.andrew.sap.document.AssociateProcessRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Alterar situação. <BR>
     *                 CopyStationID -> Identificador do posto de cópias*;<BR>
     * Status        -> Status;<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.ChangeCopyStationStatusResponseType changeCopyStationStatus(br.andrew.sap.document.ChangeCopyStationStatusRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Incluir posto de cópias. <BR>
     *                 CopyStationID      -> Identificador*;<BR>
     *                 CopyStationName    -> Nome*;<BR>
     *                 CopyStationOwnerID -> Posto superior (Identificador);<BR>
     * TeamID             -> Equipe responsável pelo envio (Identificador);<BR>
     * UserID             -> Responsável recebimento (padrão) (Matrícula)*;<BR>
     * *Requerido.<BR>
     */
    public br.andrew.sap.document.NewCopyStationResponseType newCopyStation(br.andrew.sap.document.NewCopyStationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Editar posto de cópias. <BR>
     *                 CopyStationID      -> Identificador*;<BR>
     *                 CopyStationName    -> Nome*;<BR>
     *                 CopyStationOwnerID -> Posto superior (Identificador);<BR>
     * TeamID             -> Equipe responsável pelo envio (Identificador);<BR>
     * UserID             -> Responsável recebimento (padrão) (Matrícula)*;<BR>
     * *Requerido.<BR>
     */
    public br.andrew.sap.document.EditCopyStationResponseType editCopyStation(br.andrew.sap.document.EditCopyStationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancelamento de documento. <BR>
     *                 DocumentID    -> Identificador do documento*;<BR>
     *                 CategoryID    -> Identificador da categoria;<BR>
     *                 CopyStationID -> Identificador do posto de cópias*;<BR>
     * Quantity      -> Quantidade de cópias: ;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.RelateCopyStationResponseType relateCopyStation(br.andrew.sap.document.RelateCopyStationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Alterar situação. <BR>
     *                 CopyStationID -> Identificador do posto de cópias*;<BR>
     * *Requerido.<BR>
     */
    public br.andrew.sap.document.DeleteCopyStationResponseType deleteCopyStation(br.andrew.sap.document.DeleteCopyStationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inserir responsável do posto de cópias. <BR>
     *                 CopyStationID -> Identificador*;<BR>
     *                 UserID        -> Responsável (Matrícula)*;<BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.NewCopyStationRespResponseType newCopyStationResp(br.andrew.sap.document.NewCopyStationRespRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancelamento de documento. <BR>
     *                 CopyStationID -> Identificador do posto de cópias*;<BR>
     * UserID        -> Matrícula do usuário*;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.DeleteRespCopyStationResponseType deleteRespCopyStation(br.andrew.sap.document.DeleteRespCopyStationRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancelamento de documento. <BR>
     *                 ProtocolID      -> Identificador do protocolo*;<BR>
     * Copytype        -> Tipo de cópia*;<BR>
     *                 Date            -> Data*;<BR>
     *                 ReasonID        -> Motivo*;<BR>
     *                 Responsible     -> Identificador do usuário emissor*;<BR>
     * CopyStationID   -> Identificador do posto de cópias*;<BR>
     *                 Copystationresp -> Responsável pelo posto de cópias*;<BR>
     * Company         -> Empresa*;<BR>
     *                 Companyresp     -> Responsável pela empresa*;<BR>
     *                 Observation     -> Responsável recebimento (padrão)*;<BR>
     * Status          -> Status*;<BR>
     *                 Receivingresp   -> Responsável pelo recebimento*;<BR>
     * Receivingdate   -> Data de recebimento*;<BR>
     *                 DestType        -> Tipo de destinatário [1 - Posto
     * de cópias, 2 - Área, 3 - Usuário]*;<BR>
     *                 CtrlTeam        -> Equipe de controle padrão*;<BR>
     * UserId          -> Usuário*;<BR>
     *                 DepartmentId    -> Unidade organizacional*;<BR>
     *                 Em caso de SUCESSO: Retorna uma mensagem de ´1: Operação
     * realizada com sucesso.´. <BR>Em caso de FALHA, retorna uma mensagem
     * com o erro ocorrido. <BR>
     *                 *Requerido.<BR>
     */
    public br.andrew.sap.document.NewProtocolPrintedCopyResponseType newProtocolPrintedCopy(br.andrew.sap.document.NewProtocolPrintedCopyRequestType parameters) throws java.rmi.RemoteException;
}
