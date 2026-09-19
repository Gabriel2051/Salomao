PROMPT MESTRE — SISTEMA “SALOMÃO”

Biblioteca Geral para organização de anotações, personagens, poderes, histórias e mapas mentais

Você é um engenheiro de software sênior, arquiteto de sistemas, especialista em UX/UI e segurança de aplicações web.



Sua tarefa é projetar e implementar um sistema web completo chamado “Salomão”, uma biblioteca pessoal para criação, organização e gerenciamento de conteúdos narrativos e criativos.



O sistema deve ter aparência de software profissional produzido por uma equipe de desenvolvimento, evitando completamente aparência de template genérico, dashboard de IA ou projeto experimental.



O resultado deve ser funcional, seguro, responsivo, escalável, organizado e pronto para evolução futura.



1\. OBJETIVO DO SISTEMA

O Salomão será uma biblioteca digital privada onde cada usuário poderá armazenar e organizar:



Anotações



Personagens



Poderes e habilidades



Histórias



Mapas mentais



Conteúdos relacionados entre si



Textos e documentos gerais



Cada conta deve possuir seu próprio espaço privado.



Um usuário jamais poderá visualizar, editar, excluir ou consultar dados pertencentes a outro usuário.



2\. PRINCÍPIO FUNDAMENTAL DE PRIVACIDADE

A arquitetura deve ser construída seguindo o princípio:



Todo conteúdo pertence a um usuário e somente esse usuário pode acessá-lo.



Não confiar apenas no frontend para segurança.



Toda operação no backend deve verificar a propriedade do recurso.



Exemplo:



Usuário A

&#x20;├── Personagens

&#x20;├── Poderes

&#x20;├── Histórias

&#x20;├── Anotações

&#x20;└── Mapas mentais



Usuário B

&#x20;├── Personagens

&#x20;├── Poderes

&#x20;├── Histórias

&#x20;├── Anotações

&#x20;└── Mapas mentais



Usuário A não pode acessar absolutamente nenhum recurso de Usuário B, mesmo que tente manipular:



IDs



URLs



parâmetros



requisições HTTP



payloads



endpoints



IDs de banco de dados



Implementar autorização no backend em todas as operações.



3\. AUTENTICAÇÃO

Criar:



Tela de registro

Campos:



Nome de usuário



E-mail



Senha



Confirmação de senha



Implementar:



Validação de campos



Validação de e-mail



Senha forte



Confirmação de senha



Mensagens de erro amigáveis



Hash seguro da senha



Proteção contra cadastro duplicado



Proteção contra ataques de autenticação



Sanitização das entradas



Nunca armazenar senha em texto puro.



4\. TELA DE LOGIN

Criar tela profissional contendo:



Logo/nome Salomão



Campo de usuário ou e-mail



Campo de senha



Mostrar/ocultar senha



Botão Entrar



Link para criar conta



Mensagens de erro



Estado de carregamento



Tratamento de credenciais inválidas



A interface deve transmitir sensação de:



biblioteca privada



organização



poder



tecnologia



mistério



5\. AUTENTICAÇÃO E SESSÃO

Implementar autenticação segura.



Considerar:



Sessões seguras



Cookies HttpOnly quando apropriado



Secure cookies em produção



SameSite adequado



Expiração de sessão



Logout seguro



Proteção contra CSRF quando aplicável



Rate limiting



Proteção contra brute force



Validação server-side



Não armazenar tokens sensíveis de maneira insegura no navegador.



6\. DASHBOARD PRINCIPAL

Após login, o usuário deverá visualizar seu painel principal.



Criar um dashboard elegante contendo:



Saudação ao usuário



Resumo da biblioteca



Quantidade de personagens



Quantidade de poderes



Quantidade de histórias



Quantidade de anotações



Quantidade de mapas mentais



Atividades recentes



Botões para criação rápida



Exemplo:



SALOMÃO



Olá, \[Usuário]



┌────────────┐ ┌────────────┐ ┌────────────┐

│ Personagens│ │   Poderes  │ │  Histórias │

│     24     │ │     18     │ │      7     │

└────────────┘ └────────────┘ └────────────┘



┌──────────────────────────────────────────┐

│ Atividades recentes                     │

│ • Personagem criado                     │

│ • Poder atualizado                      │

│ • História editada                      │

└──────────────────────────────────────────┘



Não copiar literalmente esse layout. Criar uma interface visual profissional.



7\. SISTEMA DE ANOTAÇÕES

Criar módulo:



Anotações

O usuário poderá:



Criar anotação



Editar anotação



Excluir anotação



Visualizar anotação



Pesquisar



Filtrar



Organizar



Adicionar título



Adicionar conteúdo



Adicionar tags



Criar categorias



Cada anotação deve possuir:



ID



Usuário proprietário



Título



Conteúdo



Tags



Data de criação



Data de atualização



8\. SISTEMA DE PERSONAGENS

Criar módulo completo:



Personagens

Cada personagem poderá possuir:



Informações básicas

Nome completo



Nome alternativo



Apelido



Idade



Data de nascimento



Gênero



Espécie



Nacionalidade



Ocupação



Status



Aparência

Descrição física



Altura



Peso



Cor dos olhos



Cor dos cabelos



Características especiais



Vestimentas



Marcas



Cicatrizes



Outros detalhes



Personalidade

Personalidade



Gostos



Desgostos



Medos



Objetivos



Motivações



Fraquezas



Virtudes



Defeitos



História

Campo de texto rico para:



Origem



Infância



Família



Eventos importantes



Relações



Conflitos



Desenvolvimento



Curiosidades



Habilidades

Permitir registrar habilidades específicas.



Exemplo:



Habilidades



• Combate corpo a corpo

• Estratégia

• Manipulação de energia

• Conhecimento mágico



Poderes

O personagem poderá receber poderes cadastrados no sistema.



Um personagem pode possuir:



nenhum poder



um poder



vários poderes



E o mesmo poder pode pertencer a vários personagens.



9\. SISTEMA DE PODERES

Criar módulo:



Poderes

Cada poder deve possuir:



Nome



Descrição



Categoria



Nível



Limitações



Fraquezas



Origem



Observações



Data de criação



Data de atualização



Os poderes devem aparecer visualmente como tokens/chips/cards compactos.



Exemplo:



┌─────────────────────────┐

│ 🔥 Manipulação Ígnea    │

└─────────────────────────┘



Ao clicar no poder:



Abrir modal ou painel lateral



Exibir explicação completa



Mostrar informações



Mostrar personagens associados



Permitir edição quando autorizado



10\. RELAÇÃO ENTRE PODERES E PERSONAGENS

Implementar relacionamento muitos-para-muitos:



Personagem

&#x20;     │

&#x20;     ├──── Poder A

&#x20;     ├──── Poder B

&#x20;     └──── Poder C



Outro Personagem

&#x20;     │

&#x20;     ├──── Poder A

&#x20;     └──── Poder D



O mesmo poder pode ser associado a vários personagens.



Na tela de personagem:



Poderes



\[Manipulação Ígnea] \[Telepatia] \[Regeneração]



Clicar em um token abre os detalhes do poder.



Também deve ser possível:



Associar poder



Remover associação



Criar novo poder



Pesquisar poderes



Filtrar poderes



11\. SISTEMA DE HISTÓRIAS

Criar módulo:



Histórias

O usuário poderá:



Criar histórias



Editar histórias



Salvar histórias



Excluir histórias



Pesquisar histórias



Organizar histórias



Categorizar histórias



Adicionar tags



Cada história deve possuir:



Título



Subtítulo opcional



Conteúdo



Tags



Status



Data de criação



Data de atualização



12\. MENÇÕES DE PERSONAGENS

Dentro do editor de histórias, implementar sistema de menções.



O usuário poderá escrever:



@Nome do Personagem



ou utilizar um autocomplete.



Ao digitar:



@Arth



o sistema deve sugerir:



Arthas

Arthur Black

Arthena



Ao selecionar um personagem:



Criar uma referência interna



Destacar visualmente a menção



Permitir clicar nela



Abrir o personagem relacionado



Não depender apenas do texto literal para relacionamento.



Criar uma estrutura de referência adequada no banco.



13\. EDITOR DE TEXTO INTEGRADO

Criar um editor de texto rico e profissional para uso em:



Anotações



Personagens



Histórias



Descrições



Poderes



Outros registros



O editor deve oferecer, quando aplicável:



Negrito



Itálico



Sublinhado



Títulos



Subtítulos



Listas



Listas numeradas



Citações



Links



Separadores



Código quando necessário



Undo/Redo



Atalhos de teclado



Seleção de texto



Busca dentro do documento



O conteúdo deve ser salvo de maneira estruturada e segura.



Evitar armazenar HTML arbitrário sem sanitização.



14\. AUTOSAVE

Implementar salvamento automático.



Exemplo:



Salvando...

Salvo agora



O sistema deve:



Evitar perda de conteúdo



Detectar alterações



Salvar após determinado intervalo



Evitar requisições excessivas



Informar visualmente o estado do salvamento



Recuperar alterações quando apropriado



Não sobrescrever silenciosamente alterações mais recentes.



15\. MAPA MENTAL

Criar um módulo completo:



Mapa Mental

O usuário poderá criar mapas mentais interativos.



Funcionalidades:



Criar mapa



Renomear mapa



Excluir mapa



Criar nós



Excluir nós



Editar nós



Conectar nós



Desconectar nós



Mover nós



Zoom



Pan



Centralizar mapa



Selecionar nós



Expandir/recolher quando aplicável



Salvar automaticamente



Persistir no banco



Tipos de nós:



Texto



Personagem



Poder



História



Anotação



Categoria



Nó livre



Os nós relacionados a entidades existentes devem apontar para o registro correspondente.



Exemplo:



&#x20;                \[MUNDO]

&#x20;                   │

&#x20;         ┌─────────┴─────────┐

&#x20;         │                   │

&#x20;   \[Personagem A]       \[Personagem B]

&#x20;         │

&#x20;    \[Poder X]

&#x20;         │

&#x20;     \[História]



O mapa deve ser realmente funcional, e não apenas uma imagem estática.



16\. PESQUISA GLOBAL

Criar pesquisa global.



O usuário poderá pesquisar:



Personagens



Poderes



Histórias



Anotações



Mapas mentais



Exemplo:



🔎 Pesquisar na biblioteca...



Personagens

&#x20; Arthur Black



Poderes

&#x20; Manipulação Sombria



Histórias

&#x20; O Retorno do Rei



Anotações

&#x20; Ideias para capítulo 4



A pesquisa deve retornar apenas conteúdo pertencente ao usuário autenticado.



17\. ORGANIZAÇÃO

Implementar:



Tags



Categorias



Favoritos



Arquivamento



Ordenação



Filtros



Busca



Recentemente editados



Recentemente criados



Permitir diferentes modos de visualização quando fizer sentido:



Lista



Cards



Grid



18\. DESIGN UX/UI

O design deve ser premium, autoral e profissional.



Evitar:



aparência genérica de IA



excesso de cards



gradientes genéricos



dashboards clonados



excesso de sombras



componentes visualmente infantis



excesso de arredondamento



cores aleatórias



Paleta

Priorizar:



Preto



Preto profundo



Vermelho



Vermelho escuro



Branco/cinza para texto



Exemplo conceitual:



Background: #080808

Surface:    #111111

Surface 2:  #171717

Red:        #B5121B

Red Dark:   #6E0B10

Text:       #F5F5F5

Muted:      #8A8A8A

Border:     #292929



Essas cores são referência; ajuste conforme necessário para obter excelente contraste e acessibilidade.



19\. IDENTIDADE VISUAL

Criar uma identidade visual própria para:



SALOMÃO

O sistema deve transmitir:



biblioteca



conhecimento



organização



poder



mistério



sofisticação



Criar logotipo/marca tipográfica simples e elegante.



Evitar depender de imagens genéricas.



20\. NAVEGAÇÃO

Criar sidebar ou sistema de navegação profissional contendo:



SALOMÃO



Dashboard



Biblioteca

&#x20;├── Anotações

&#x20;├── Personagens

&#x20;├── Poderes

&#x20;├── Histórias

&#x20;└── Mapas Mentais



Pesquisa



Configurações



Sair



A navegação deve funcionar perfeitamente em desktop e dispositivos menores.



21\. RESPONSIVIDADE

O sistema deve funcionar em:



Desktop



Notebook



Tablet



Celular



Não simplesmente diminuir elementos.



Adaptar:



Sidebar



Editor



Cards



Formulários



Modais



Mapas mentais



Pesquisa



Menus



22\. BANCO DE DADOS

Utilizar obrigatoriamente:



PostgreSQL

Nome do banco:



salomao



Projetar schema relacional adequado.



Estrutura conceitual mínima:



users

notes

characters

powers

character\_powers

stories

story\_character\_mentions

mental\_maps

mental\_map\_nodes

mental\_map\_edges

tags

entity\_tags



Adicionar outras tabelas quando necessário.



23\. MULTI-TENANCY

Todos os registros de usuário devem possuir referência ao proprietário.



Exemplo:



users

&#x20; id



characters

&#x20; id

&#x20; user\_id



powers

&#x20; id

&#x20; user\_id



stories

&#x20; id

&#x20; user\_id



Toda consulta deve respeitar:



WHERE user\_id = authenticated\_user\_id



Para relacionamentos indiretos, garantir a mesma proteção.



Exemplo:



Um usuário não pode associar um poder pertencente a outro usuário ao próprio personagem.



24\. INTEGRIDADE DOS DADOS

Implementar:



Foreign Keys



Índices



Unique constraints



Check constraints quando apropriado



Transações



Cascade rules cuidadosamente definidas



Integridade referencial



Evitar IDs previsíveis quando isso representar risco.



Mesmo utilizando UUIDs, nunca considerar UUID como mecanismo de autorização.



25\. SEGURANÇA

Aplicar práticas modernas de segurança.



Proteger contra:



SQL Injection



XSS



CSRF



Session fixation



Brute force



Credential stuffing



IDOR



Mass assignment



Path traversal



Upload malicioso



HTML malicioso



Manipulação de payload



Acesso horizontal entre usuários



Acesso vertical indevido



Implementar:



Validação server-side



Sanitização



Rate limiting



Headers de segurança



CSP quando aplicável



Controle de sessão



Logs de segurança



Princípio do menor privilégio



26\. PROTEÇÃO DE API

Se o sistema utilizar API:



Todos os endpoints devem validar:



Usuário autenticado?



Recurso existe?



Recurso pertence ao usuário?



Operação é permitida?



Payload é válido?



Nunca retornar informações de recursos que o usuário não possui.



Não confiar em:



user\_id



enviado pelo frontend.



O usuário autenticado deve ser obtido através da sessão/token validado no backend.



27\. VALIDAÇÃO DO PROGRESSO

O desenvolvimento deve seguir uma metodologia incremental.



Não criar todo o projeto de uma vez sem validação.



Após cada módulo importante:



Implementar



Executar



Testar



Corrigir



Verificar integração



Prosseguir



Criar checkpoints:



CHECKPOINT 01

Autenticação



CHECKPOINT 02

Banco de dados



CHECKPOINT 03

Dashboard



CHECKPOINT 04

Anotações



CHECKPOINT 05

Personagens



CHECKPOINT 06

Poderes



CHECKPOINT 07

Relacionamentos



CHECKPOINT 08

Histórias



CHECKPOINT 09

Editor



CHECKPOINT 10

Mapas mentais



CHECKPOINT 11

Pesquisa



CHECKPOINT 12

Segurança



CHECKPOINT 13

Responsividade



CHECKPOINT 14

Testes finais



Se algum checkpoint apresentar erro, corrija antes de continuar.



28\. TESTES

Criar testes para:



Autenticação

Registro



Login



Logout



Senha inválida



Usuário inexistente



Sessão expirada



Autorização

Testar explicitamente:



Usuário A tenta acessar recurso de Usuário B

→ DEVE SER BLOQUEADO



Testar:



GET



POST



PUT/PATCH



DELETE



Personagens

Criar



Editar



Excluir



Associar poder



Remover poder



Poderes

Criar



Editar



Excluir



Associar múltiplos personagens



Histórias

Criar



Editar



Salvar



Menções



Mapas

Criar nó



Mover nó



Criar conexão



Salvar



Reabrir mapa



29\. TRATAMENTO DE ERROS

Criar tratamento consistente.



Nunca mostrar:



500 Internal Server Error



como única informação ao usuário.



Utilizar mensagens como:



Não foi possível salvar suas alterações.

Tente novamente.



Registrar detalhes técnicos nos logs, não na interface pública.



30\. ESTADOS DE INTERFACE

Toda interface que realiza operações assíncronas deve possuir:



Loading



Success



Error



Empty state



Disabled state



Exemplo:



Salvando...



✓ Alterações salvas



Não foi possível salvar.

\[Tentar novamente]



31\. CONFIRMAÇÕES

Operações destrutivas devem exigir confirmação.



Exemplo:



Excluir personagem?



Esta ação removerá o personagem e seus relacionamentos.



\[Cancelar] \[Excluir]



Não excluir dados importantes acidentalmente.



32\. ACESSIBILIDADE

Aplicar boas práticas de acessibilidade:



Contraste adequado



Navegação por teclado



Focus states



Labels



ARIA quando necessário



Botões semanticamente corretos



Feedback para leitores de tela



Não depender exclusivamente de cor



33\. PERFORMANCE

O sistema deve ser desenvolvido pensando em crescimento.



Implementar quando apropriado:



Paginação



Lazy loading



Debounce na pesquisa



Cache controlado



Índices PostgreSQL



Queries eficientes



Evitar N+1 queries



Otimização do editor



Autosave eficiente



34\. ARQUITETURA

Utilizar arquitetura limpa e organizada.



Separar claramente:



Frontend

Backend

Database

Authentication

Components

Services

Repositories

Validation

Security

Tests



Evitar colocar toda a lógica em um único arquivo.



O código deve ser:



modular



reutilizável



tipado quando possível



documentado quando necessário



fácil de manter



35\. TECNOLOGIAS

Caso nenhuma stack tenha sido previamente definida, escolha uma stack moderna e estável.



Preferência:



Frontend

React



TypeScript



Framework moderno adequado, como Next.js



CSS moderno / Tailwind ou solução equivalente



Editor rich-text profissional



Backend

Pode utilizar:



API do próprio framework



Node.js/TypeScript



ORM moderno compatível com PostgreSQL



Banco

PostgreSQL



Banco chamado salomao



Mapas mentais

Utilizar uma biblioteca madura para diagramas/nodes, caso necessário.



Não reinventar todo o mecanismo de canvas sem necessidade.



36\. ESTRUTURA DE PROJETO

Criar estrutura organizada semelhante a:



salomao/

│

├── app/

├── components/

├── features/

│   ├── auth/

│   ├── notes/

│   ├── characters/

│   ├── powers/

│   ├── stories/

│   └── mindmaps/

│

├── lib/

│   ├── auth/

│   ├── database/

│   ├── security/

│   └── validation/

│

├── server/

├── database/

│   ├── migrations/

│   └── seed/

│

├── tests/

│

├── public/

│

└── README.md



Adapte a estrutura à stack escolhida.



37\. CONFIGURAÇÃO

Utilizar variáveis de ambiente.



Exemplo conceitual:



DATABASE\_URL=

AUTH\_SECRET=

APP\_URL=



Nunca colocar:



senha do banco



secrets



tokens



chaves privadas



diretamente no código-fonte.



Criar:



.env.example



sem valores secretos reais.



38\. DOCUMENTAÇÃO

Criar README completo contendo:



Descrição



Tecnologias



Pré-requisitos



Instalação



Configuração



Variáveis de ambiente



Configuração PostgreSQL



Migrações



Execução local



Testes



Build



Deploy



Arquitetura



Segurança



39\. EXPERIÊNCIA DO USUÁRIO

A experiência deve ser fluida.



Exemplo de fluxo:



Registro

&#x20;  ↓

Login

&#x20;  ↓

Dashboard

&#x20;  ↓

Criar personagem

&#x20;  ↓

Criar poderes

&#x20;  ↓

Associar poderes

&#x20;  ↓

Criar história

&#x20;  ↓

Mencionar personagem

&#x20;  ↓

Criar mapa mental

&#x20;  ↓

Relacionar entidades



Tudo deve funcionar como um ecossistema integrado.



40\. INTEGRAÇÃO ENTRE MÓDULOS

Os módulos não devem parecer sistemas independentes.



Exemplo:



Na página do personagem:



Arthur Black



Informações

Aparência

História

Habilidades



Poderes

\[Manipulação Sombria]

\[Regeneração]



Histórias relacionadas

\[O Retorno]



Mapas mentais

\[Conflito dos Reinos]



Na página de um poder:



Manipulação Sombria



Descrição...



Personagens que possuem este poder:

\[Arthur Black]

\[Kael]



Na história:



O Retorno



Arthur Black caminhou...



Arthur Black deve ser uma referência clicável ao personagem.



41\. EXPERIÊNCIA DE PESQUISA

Criar pesquisa rápida e intuitiva.



Atalho sugerido:



Ctrl + K



Abrir pesquisa global.



Permitir navegar pelos resultados usando teclado.



42\. DESIGN DE DETALHES

Utilizar animações discretas:



Hover



Transições



Modal



Drawer



Feedback de salvamento



Evitar animações excessivas.



O sistema deve parecer rápido e sólido.



43\. DARK MODE

Como o projeto já possui identidade baseada em preto/vermelho, criar inicialmente uma experiência dark premium.



Priorizar legibilidade.



O vermelho deve ser utilizado principalmente como:



destaque



ação primária



estado ativo



detalhes de identidade



Não utilizar vermelho excessivamente em grandes áreas.



44\. DADOS DE EXEMPLO

Criar seed opcional para desenvolvimento.



Exemplo:



Usuário demo

Personagem demo

Poder demo

História demo

Anotação demo

Mapa mental demo



Deixar claramente identificado como dados de desenvolvimento.



Nunca incluir credenciais reais.



45\. CRITÉRIOS DE ACEITAÇÃO

O projeto somente será considerado concluído quando:



&#x20;Registro funciona



&#x20;Login funciona



&#x20;Logout funciona



&#x20;Sessões funcionam



&#x20;PostgreSQL funciona



&#x20;Usuários possuem dados isolados



&#x20;Anotações funcionam



&#x20;Personagens funcionam



&#x20;Poderes funcionam



&#x20;Poderes podem ser associados a vários personagens



&#x20;Histórias funcionam



&#x20;Menções funcionam



&#x20;Editor funciona



&#x20;Autosave funciona



&#x20;Mapas mentais funcionam



&#x20;Pesquisa funciona



&#x20;Tags funcionam



&#x20;Filtros funcionam



&#x20;Interface é responsiva



&#x20;Segurança foi testada



&#x20;Testes foram executados



&#x20;Não existem erros críticos conhecidos



&#x20;README está atualizado



&#x20;Variáveis de ambiente estão documentadas



46\. REGRA FUNDAMENTAL DE IMPLEMENTAÇÃO

Não criar funcionalidades falsas ou simuladas.



Não utilizar:



botões que não fazem nada



dados estáticos fingindo ser banco



telas falsas



APIs simuladas na versão final



mapas mentais apenas visuais



autenticação falsa



relacionamentos apenas no frontend



Tudo que aparecer como funcional na interface deve estar conectado à implementação real.



47\. PROCESSO DE DESENVOLVIMENTO OBRIGATÓRIO

Antes de começar:



Analisar os requisitos.



Definir arquitetura.



Definir schema PostgreSQL.



Definir modelo de autenticação.



Definir estratégia de autorização.



Definir componentes principais.



Definir estrutura de pastas.



Implementar por etapas.



Depois:



Implementar banco.



Implementar autenticação.



Implementar autorização.



Implementar dashboard.



Implementar módulos.



Implementar relacionamentos.



Implementar editor.



Implementar mapas.



Implementar pesquisa.



Implementar segurança.



Testar.



Corrigir.



Testar novamente.



Fazer revisão final.



48\. REGRA DE QUALIDADE

Sempre que encontrar uma decisão arquitetural importante, priorizar:



Segurança



Integridade dos dados



Funcionalidade



Manutenibilidade



Performance



UX



Estética



Não sacrificar segurança para facilitar implementação.



Não sacrificar integridade dos dados por conveniência.



49\. ENTREGA FINAL

Ao finalizar, apresentar:



1\. Resumo da arquitetura

2\. Stack utilizada

3\. Estrutura do projeto

4\. Schema do PostgreSQL

5\. Estratégia de autenticação

6\. Estratégia de isolamento entre usuários

7\. Funcionalidades implementadas

8\. Testes realizados

9\. Problemas encontrados e corrigidos

10\. Como executar o projeto

11\. Como configurar PostgreSQL

12\. Variáveis de ambiente necessárias

13\. Próximas melhorias recomendadas

50\. RESULTADO ESPERADO

O resultado final deve ser um sistema chamado:



SALOMÃO

Uma biblioteca pessoal digital completa, com:



&#x20;                SALOMÃO

&#x20;                   │

&#x20;      ┌────────────┼────────────┐

&#x20;      │            │            │

&#x20;  ANOTAÇÕES   PERSONAGENS    PODERES

&#x20;      │            │            │

&#x20;      │            └─────┬──────┘

&#x20;      │                  │

&#x20;      └────────────┐     │

&#x20;                   │     │

&#x20;                HISTÓRIAS

&#x20;                   │

&#x20;                   │

&#x20;              MAPAS MENTAIS



Todos esses módulos devem estar integrados em uma única experiência.



O sistema deve parecer um produto comercial profissional, e não um protótipo.



Prioridade máxima: segurança, isolamento de dados por usuário, funcionamento real, qualidade de código, UX/UI profissional e estabilidade.

