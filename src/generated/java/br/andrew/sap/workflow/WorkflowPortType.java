/**
 * WorkflowPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public interface WorkflowPortType extends java.rmi.Remote {

    /**
     * Inicia um workflow;
     * 
     *                 Parâmetros:
     *                 ProcessID*: Identificador do processo;   WorkflowTitle*:
     * Título do workflow;   UserID: Matrícula do usuário;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordKey: Código do registro
     * incluído pelo método;   RecordID: Identificador do registro incluído
     * pelo método;
     */
    public br.andrew.sap.workflow.NewWorkflowResponseType newWorkflow(br.andrew.sap.workflow.NewWorkflowRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inicia um workflow e altera seus dados;
     * 
     *                 Parâmetros:
     *                 ProcessID*: Identificador do processo;   WorkflowTitle*:
     * Título do workflow;   UserID: Matrícula do usuário;   <Requester>
     * <User>   UserID: Matrícula do usuário solicitante;   </User>  <Customer>
     * CustomerID: Identificador do cliente;    Contact: Contato solicitante;
     * </Customer>  </Requester>  <EntityList>  <Entity>   EntityID: Identificador
     * da tabela de formulário;    <EntityAttributeList>   <EntityAttribute>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * EntityAttributeValue: Valor do campo da tabela de formulário**;  
     * </EntityAttribute>   </EntityAttributeList>   <RelationshipList> 
     * <Relationship>     RelationshipID: Identificador do relacionamento;
     * <RelationshipAttributeList>      <RelationshipAttribute>       RelationshipAttributeID:
     * Identificador do campo da tabela de formulário relacionada;      
     * RelationshipAttributeValue: Valor do campo da tabela de formulário
     * relacionada**;       </RelationshipAttribute>     </RelationshipAttributeList>
     * </Relationship>   </RelationshipList>   <EntityAttributeFileList>
     * <EntityAttributeFile>     EntityAttributeID: Identificador do campo
     * da tabela de formulário;      FileName: Nome do arquivo (inclui sua
     * extensão);      FileContent: Conteúdo binário do arquivo;     </EntityAttributeFile>
     * </EntityAttributeFileList>  </Entity>  </EntityList>  <AttributeList>
     * <Attribute>   AttributeID: Identificador do atributo;    <AttributeValueList>
     * AttributeValue: Valor do atributo*** ;    </AttributeValueList>  </Attribute>
     * </AttributeList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do campo da tabela
     * de formulário:   Número: dígitos numéricos sem separador de milhar
     * e decimal;    Decimal: dígitos numéricos sem separador de milhar e
     * com ponto (.) como separador decimal;    Data: YYYY-MM-DD   Hora:
     * HH:MM   Boolean: 0 ou 1;   *** Observações de acordo com o tipo do
     * atributo:   Numérico: dígitos numéricos sem separador de milhar e
     * com ponto (.) como separador decimal;    Moeda: dígitos numéricos
     * sem separador de milhar e com ponto (.) como separador decimal;  
     * Data: YYYY-MM-DD   Hora: HHHH:MM
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordKey: Código do registro
     * incluído pelo método;   RecordID: Identificador do registro incluído
     * pelo método;
     */
    public br.andrew.sap.workflow.NewWorkflowEditDataResponseType newWorkflowEditData(br.andrew.sap.workflow.NewWorkflowEditDataRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inicia um workflow e altera seus dados;
     * 
     *                 Parâmetros:
     *                 WorkflowTypeID*: Identificador do tipo de workflow;
     * WorkflowTitle*: Título do workflow;   UserID: Matrícula do usuário;
     * <Requester>  <User>   UserID: Matrícula do usuário solicitante;  
     * </User>  <Customer>   CustomerID: Identificador do cliente;    Contact:
     * Contato solicitante;   </Customer>  </Requester>  <EntityList>  <Entity>
     * EntityID: Identificador da tabela de formulário;    <EntityAttributeList>
     * <EntityAttribute>     EntityAttributeID: Identificador do campo da
     * tabela de formulário;      EntityAttributeValue: Valor do campo da
     * tabela de formulário**;     </EntityAttribute>   </EntityAttributeList>
     * <RelationshipList>   <Relationship>     RelationshipID: Identificador
     * do relacionamento;      <RelationshipAttributeList>      <RelationshipAttribute>
     * RelationshipAttributeID: Identificador do campo da tabela de formulário
     * relacionada;        RelationshipAttributeValue: Valor do campo da
     * tabela de formulário relacionada**;       </RelationshipAttribute>
     * </RelationshipAttributeList>    </Relationship>   </RelationshipList>
     * <EntityAttributeFileList>   <EntityAttributeFile>     EntityAttributeID:
     * Identificador do campo da tabela de formulário;      FileName: Nome
     * do arquivo (inclui sua extensão);      FileContent: Conteúdo binário
     * do arquivo;     </EntityAttributeFile>   </EntityAttributeFileList>
     * </Entity>  </EntityList>  <AttributeList>  <Attribute>   AttributeID*:
     * Identificador do atributo;    <AttributeValueList>    AttributeValue:
     * Valor do atributo ;    </AttributeValueList>  </Attribute>  </AttributeList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Numérico: dígitos numéricos sem separador de milhar e com ponto (.)
     * como separador decimal;    Moeda: dígitos numéricos sem separador
     * de milhar e com ponto (.) como separador decimal;    Data: YYYY-MM-DD
     * Hora: HHHH:MM
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordKey: Código do registro
     * incluído pelo método;   RecordID: Identificador do registro incluído
     * pelo método;
     */
    public br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType newWorkflowTypeEditData(br.andrew.sap.workflow.NewWorkflowTypeEditDataRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Altera os dados do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   UserID:
     * Matrícula do usuário;   <Requester>  <User>   UserID: Matrícula do
     * usuário solicitante;   </User>  <Customer>   CustomerID: Identificador
     * do cliente;    Contact: Contato solicitante;   </Customer>  </Requester>
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.EditWorkflowDataResponseType editWorkflowData(br.andrew.sap.workflow.EditWorkflowDataRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Altera um registro da tabela de formulário do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   EntityID*:
     * Identificador da tabela de formulário;   <EntityAttributeList>  <EntityAttribute>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * EntityAttributeValue: Valor do campo da tabela de formulário**;  
     * </EntityAttribute>  </EntityAttributeList>  <RelationshipList>  <Relationship>
     * RelationshipID: Identificador do relacionamento;    <RelationshipAttribute>
     * RelationshipAttributeID: Identificador do campo da tabela de formulário
     * relacionada;     RelationshipAttributeValue: Valor do campo da tabela
     * de formulário relacionada**;    </RelationshipAttribute>  </Relationship>
     * </RelationshipList>  <EntityAttributeFileList>  <EntityAttributeFile>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * FileName: Nome do arquivo (inclui sua extensão);    FileContent: Conteúdo
     * binário do arquivo;   </EntityAttributeFile>  </EntityAttributeFileList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Número: dígitos numéricos sem separador de milhar e decimal;    Decimal:
     * dígitos numéricos sem separador de milhar e com ponto (.) como separador
     * decimal;    Data: YYYY-MM-DD   Hora: HH:MM   Boolean: 0 ou 1;
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.EditEntityRecordResponseType editEntityRecord(br.andrew.sap.workflow.EditEntityRecordRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inclui um registro na tabela de formulário descendente do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   MainEntityID*:
     * Identificador da tabela de formulário principal;   ChildRelationshipID*:
     * Identificador do relacionamento da tabela de formulário descendente;
     * <EntityAttributeList>  <EntityAttribute>   EntityAttributeID: Identificador
     * do campo da tabela de formulário;    EntityAttributeValue: Valor do
     * campo da tabela de formulário**;   </EntityAttribute>  </EntityAttributeList>
     * <RelationshipList>  <Relationship>   RelationshipID: Identificador
     * do relacionamento;    <RelationshipAttribute>    RelationshipAttributeID:
     * Identificador do campo da tabela de formulário relacionada;     RelationshipAttributeValue:
     * Valor do campo da tabela de formulário relacionada**;    </RelationshipAttribute>
     * </Relationship>  </RelationshipList>  <EntityAttributeFileList>  <EntityAttributeFile>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * FileName: Nome do arquivo (inclui sua extensão);    FileContent: Conteúdo
     * binário do arquivo;   </EntityAttributeFile>  </EntityAttributeFileList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Número: dígitos numéricos sem separador de milhar e decimal;    Decimal:
     * dígitos numéricos sem separador de milhar e com ponto (.) como separador
     * decimal;    Data: YYYY-MM-DD   Hora: HH:MM   Boolean: 0 ou 1;
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordKey: Código do registro
     * incluído pelo método;
     */
    public br.andrew.sap.workflow.NewChildEntityRecordResponseType newChildEntityRecord(br.andrew.sap.workflow.NewChildEntityRecordRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inclui um ou mais registros na tabela de formulário descendente
     * do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   MainEntityID*:
     * Identificador da tabela de formulário principal;   ChildRelationshipID*:
     * Identificador do relacionamento da tabela de formulário descendente;
     * <EntityRecordList>  <EntityRecord>   <EntityAttributeList>   <EntityAttribute>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * EntityAttributeValue: Valor do campo da tabela de formulário**;  
     * </EntityAttribute>   </EntityAttributeList>   <RelationshipList> 
     * <Relationship>     RelationshipID: Identificador do relacionamento;
     * <RelationshipAttribute>      RelationshipAttributeID: Identificador
     * do campo da tabela de formulário relacionada;       RelationshipAttributeValue:
     * Valor do campo da tabela de formulário relacionada**;      </RelationshipAttribute>
     * </Relationship>   </RelationshipList>   <EntityAttributeFileListList>
     * <EntityAttributeFile>     EntityAttributeID: Identificador do campo
     * da tabela de formulário;      FileName: Nome do arquivo (inclui sua
     * extensão);      FileContent: Conteúdo binário do arquivo;     </EntityAttributeFile>
     * </EntityAttributeFileListList>  </EntityRecord>  </EntityRecordList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Número: dígitos numéricos sem separador de milhar e decimal;    Decimal:
     * dígitos numéricos sem separador de milhar e com ponto (.) como separador
     * decimal;    Data: YYYY-MM-DD   Hora: HH:MM   Boolean: 0 ou 1;
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   <RecordList>  <Record>   RecordKey:
     * Código do registro incluído pelo método;   </Record>  </RecordList>
     */
    public br.andrew.sap.workflow.NewChildEntityRecordListResponseType newChildEntityRecordList(br.andrew.sap.workflow.NewChildEntityRecordListRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Altera um registro da tabela de formulário descendente do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   MainEntityID*:
     * Identificador da tabela de formulário principal;   ChildRelationshipID*:
     * Identificador do relacionamento da tabela de formulário descendente;
     * ChildRecordOID*: Código do registro relacionado da tabela de formulário
     * descendente;   <EntityAttributeList>  <EntityAttribute>   EntityAttributeID:
     * Identificador do campo da tabela de formulário;    EntityAttributeValue:
     * Valor do campo da tabela de formulário**;   </EntityAttribute>  </EntityAttributeList>
     * <RelationshipList>  <Relationship>   RelationshipID: Identificador
     * do relacionamento;    <RelationshipAttribute>    RelationshipAttributeID:
     * Identificador do campo da tabela de formulário relacionada;     RelationshipAttributeValue:
     * Valor do campo da tabela de formulário relacionada**;    </RelationshipAttribute>
     * </Relationship>  </RelationshipList>  <EntityAttributeFileList>  <EntityAttributeFile>
     * EntityAttributeID: Identificador do campo da tabela de formulário;
     * FileName: Nome do arquivo (inclui sua extensão);    FileContent: Conteúdo
     * binário do arquivo;   </EntityAttributeFile>  </EntityAttributeFileList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Número: dígitos numéricos sem separador de milhar e decimal;    Decimal:
     * dígitos numéricos sem separador de milhar e com ponto (.) como separador
     * decimal;    Data: YYYY-MM-DD   Hora: HH:MM   Boolean: 0 ou 1;
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.EditChildEntityRecordResponseType editChildEntityRecord(br.andrew.sap.workflow.EditChildEntityRecordRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Exclui um registro da tabela de formulário descendente do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   MainEntityID*:
     * Identificador da tabela de formulário principal;   ChildRelationshipID*:
     * Identificador do relacionamento da tabela de formulário descendente;
     * ChildRecordOID*: Código do registro relacionado da tabela de formulário
     * descendente;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.DeleteChildEntityRecordResponseType deleteChildEntityRecord(br.andrew.sap.workflow.DeleteChildEntityRecordRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Exclui todos registros relacionados da tabela de formulário
     * descendente do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   MainEntityID*:
     * Identificador da tabela de formulário principal;   ChildRelationshipID*:
     * Identificador do relacionamento da tabela de formulário descendente;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.ClearChildEntityRecordResponseType clearChildEntityRecord(br.andrew.sap.workflow.ClearChildEntityRecordRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Altera o valor dos atributos de um workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   <AttributeList>
     * <Attribute>   AttributeID*: Identificador do atributo;    <AttributeValueList>
     * AttributeValue: Valor do atributo ;    </AttributeValueList>  </Attribute>
     * </AttributeList>
     * 
     *                 * Requerido;
     *                 ** Observações de acordo com o tipo do atributo: 
     * Numérico: dígitos numéricos sem separador de milhar e com ponto (.)
     * como separador decimal;    Moeda: dígitos numéricos sem separador
     * de milhar e com ponto (.) como separador decimal;    Data: YYYY-MM-DD
     * Hora: HHHH:MM
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.EditAttributeValueResponseType editAttributeValue(br.andrew.sap.workflow.EditAttributeValueRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Anexa um arquivo a uma atividade do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;   FileName*: Nome do arquivo (inclui sua
     * extensão);   FileContent*: Conteúdo binário do arquivo;   UserID:
     * Matrícula do usuário;   AttachmentID: Desde a versão 2.0.10 este parâmetro
     * tornou-se obsoleto. Na importação, deixe-o em branco.;   AttachmentName:
     * Nome do anexo;   Summary: Resumo;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordKey: Código do registro
     * incluído pelo método;   RecordID: Identificador do registro incluído
     * pelo método;
     */
    public br.andrew.sap.workflow.NewAttachmentResponseType newAttachment(br.andrew.sap.workflow.NewAttachmentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Associa um documento do SE Documento a uma atividade do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;   DocumentID*: Identificador do documento;
     * ActivityOrder: Número da ordem da atividade;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.NewAssocDocumentResponseType newAssocDocument(br.andrew.sap.workflow.NewAssocDocumentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Associa um ou mais workflows a um outro workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   <AssocWorkflowList>
     * <AssocWorkflow>   AssocWorkflowID*: Identificador do workflow associado;
     * AssocWorkflowType: Tipo da associação**;   </AssocWorkflow>  </AssocWorkflowList>
     * ActivityID: Identificador da atividade;   ActivityOrder: Número da
     * ordem da atividade;
     * 
     *                 * Requerido;
     *                 ** Tipo da associação:   1: Bloqueante;    Qualquer
     * outro valor: Não bloqueante;
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   <RecordList>  <Record>   RecordKey:
     * Código do registro incluído pelo método;   </Record>  </RecordList>
     */
    public br.andrew.sap.workflow.NewAssocWorkflowResponseType newAssocWorkflow(br.andrew.sap.workflow.NewAssocWorkflowRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Executa uma atividade de sistema assíncrona do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;   ActivityOrder: Número da ordem da atividade;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.ExecuteSystemActivityResponseType executeSystemActivity(br.andrew.sap.workflow.ExecuteSystemActivityRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Executa uma atividade de usuário ou decisão do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;   ActionSequence*: Número de sequência
     * da ação;   UserID: Matrícula do usuário;   ActivityOrder: Número da
     * ordem da atividade;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.ExecuteActivityResponseType executeActivity(br.andrew.sap.workflow.ExecuteActivityRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Inclui uma ou mais exceções de acesso do workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   <AccessExceptionList>
     * <AccessException>   UserID: Matrícula do usuário;   </AccessException>
     * </AccessExceptionList>
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   <RecordDetailList>  <RecordDetail>
     * Status: Situação do processamento do registro. Seu valor pode ser:;
     * SUCCESS: Registro processado com sucesso;     FAILURE: Ocorreu uma
     * falha no processamento do registro;    Code: Código do processamento
     * do registro;    Detail: Detalhe do processamento do registro;    RecordKey:
     * Código do registro incluído;   </RecordDetail>  </RecordDetailList>
     */
    public br.andrew.sap.workflow.NewAccessExceptionResponseType newAccessException(br.andrew.sap.workflow.NewAccessExceptionRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Cancela um workflow;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   Explanation*:
     * Justificativa;   UserID: Matrícula do usuário;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.CancelWorkflowResponseType cancelWorkflow(br.andrew.sap.workflow.CancelWorkflowRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Dados da instância;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   Title: Título da instância;
     * ProcessID: Identificador do processo;   InstanceStatus: Situação da
     * instância;   WorkflowStatus: Situação dinâmica do workflow (Identificador
     * - Nome);   StarterUser: Matrícula do usuário iniciador da instância;
     * StarterExternalUser: E-mail do usuário externo iniciador da instância;
     * StartDate: Data e hora de início da instância (formato: AAAA-MM-DD
     * hh:mm:ss);
     */
    public br.andrew.sap.workflow.GetWorkflowResponseType getWorkflow(br.andrew.sap.workflow.GetWorkflowRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Desassociar atividade do usuário;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.UnlinkActivityFromUserResponseType unlinkActivityFromUser(br.andrew.sap.workflow.UnlinkActivityFromUserRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Adiciona um comentário na instância do processo.;
     * 
     *                 Parâmetros:
     *                 WorkflowID*: Identificador do workflow;   ActivityID*:
     * Identificador da atividade;   Text*: Texto;   Private: Privado;  
     * UserID: Matrícula do usuário;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;
     */
    public br.andrew.sap.workflow.NewCommentResponseType newComment(br.andrew.sap.workflow.NewCommentRequestType parameters) throws java.rmi.RemoteException;

    /**
     * Reativa um ou mais workflows;
     * 
     *                 Parâmetros:
     *                 <ReactivateWorkflowList>  <WorkflowID>   Identificador
     * do workflow;   </WorkflowID>  </ReactivateWorkflowList>  Explanation*:
     * Justificativa;   UserID: Matrícula do usuário;
     * 
     *                 * Requerido;
     * 
     * 
     *                 Retorno:
     *                 Status: Situação da execução do método. Seu valor
     * pode ser:   SUCCESS: Método executado com sucesso;    FAILURE: Ocorreu
     * uma falha na execução do método;   Code: Código do retorno do método;
     * Detail: Detalhe do retorno do método;   RecordListResponse: Lista
     * com a situação dos registros executados pelo método;   <}WorkflowID>
     * Situação do registro;   <}/WorkflowID>
     */
    public br.andrew.sap.workflow.ReactivateWorkflowResponseType reactivateWorkflow(br.andrew.sap.workflow.ReactivateWorkflowRequestType parameters) throws java.rmi.RemoteException;
}
