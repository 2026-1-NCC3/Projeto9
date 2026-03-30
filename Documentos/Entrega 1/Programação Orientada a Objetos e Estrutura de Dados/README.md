# 📑 Entrega 1: Estrutura de Programação Orientada a Objetos
---

### 🧬 Hierarquia de Classes e Atributos

A estrutura utiliza a relação onde um **Paciente** herda as propriedades de um **Usuário**.

#### 1. Classe: Usuario (Superclasse)
Responsável pelas informações básicas de identificação e autenticação.
* **`id` (`Int`)**: Identificador único do registro.
* **`nome` (`String`)**: Nome completo do usuário.
* **`email` (`String`)**: Endereço para login e contato.
* **`senha` (`String`)**: Credencial de acesso com proteção por hash.

#### 2. Classe: Paciente (Subclasse)
Especialização que contém os dados específicos para o tratamento de RPG.
* **Atributos Herdados**: (id: Int, nome: String, email: String e senha: String).
* **`telefone` (`String`)**: Para envio de lembretes e notificações via push ou WhatsApp.
* **`observacoesClinicas` (`String`)**: Campo para o prontuário e histórico de evolução do paciente.

---

### 🖼️ Diagrama de Classes
O diagrama abaixo representa a estrutura da função principal exigida nesta etapa, contemplando as entidades e seus respectivos tipos de dados.

![Diagrama de Classes](Documentos/Entrega-1/Programação-Orientada-a-Objetos-e-Estrutura-de-Dados/DiagramaPoo.png)

---

### 🔄 Regras de Conexão (CRUD)
A modelagem permite as operações fundamentais para a gestão da clínica:
* **Visualizar**: Listagem de pacientes ativos no sistema.
* **Adicionar**: Cadastro de novos usuários e pacientes.
* **Editar**: Atualização de informações de contato ou prontuário.
* **Deletar**: Remoção ou inativação de registros.

---
