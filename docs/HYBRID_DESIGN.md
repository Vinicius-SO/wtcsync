# Design integrado do fork

Base funcional: `Vinicius-SO/wtcsync`, commit `f7650bea85e4e463dfd8099e43ce97f4ddc33876`.
Referência visual: `CauaLc/wtcsync`, commit `935e68855832707ab42256f1c2f12c1581e67a7d`.

## Integração

- Reaproveita tokens Motion, Shape e Spacing, feedback pressScale e tipografia Nunito/Roboto do principal.
- Preserva a paleta clara/escura do fork, acento amarelo, abas de cada perfil e APIs dos componentes existentes.
- Unifica cards de clientes, campanhas, segmentos e conversas com contorno, cantos arredondados e resposta ao toque.
- Mantém ações internas dos cards, inclusive exclusão de campanhas.
- Anima cor de filtros e navegação, transições entre rotas e movimentação das conversas na lista.
- Usa a contagem real de não lidas no badge; nunca representa presença online sem evidência.
- Respeita a preferência de reduzir movimento, sem escala ou deslocamento quando habilitada.

## Preservação funcional

A integração não substitui ViewModels, repositórios, DTOs, serviços, workers ou endpoints.
Os ajustes de timeouts do principal não foram importados: a configuração do fork contempla
a retomada do Render gratuito. A autenticação continua obrigatória em todas as rotas protegidas.

Permanecem os logins por senha/Google, restauração de sessão, separação de perfis,
fila de atendimento e responsável, ordenação recente, WebSocket autenticado, histórico,
envio persistente e idempotente, retentativas, recibos, edição no compositor,
exclusão de mensagens, notificações duráveis, perfis, campanhas e menu Outras com saída confirmada.

O principal também possui uma mudança para conversas por remetente. O fork mantém seu
isolamento autorizado por cliente e atendimento, com histórico compartilhado entre os
atendentes autorizados e transferência de responsável; os arquivos antigos de chat
não são copiados sobre essa implementação.

## Validação

- Comparação com o checkpoint anterior: arquivos de dados, ViewModels, serviços,
  workers, manifesto e testes permanecem idênticos.
- O fluxo de edição/exclusão dentro de MessageScreen permanece idêntico.
- Navigation mantém destinos, ações e controle de acesso; acrescenta somente transições.
- Executar a suíte de testes existente e assembleDebug no workflow Android Debug APK.
- A aparência final, teclado, TalkBack e preferência de movimento ainda precisam
  de validação no aparelho; CI não substitui essa avaliação.
