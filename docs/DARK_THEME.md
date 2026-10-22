# Tema escuro: grafite e amarelo

A versão 1.12 preserva a identidade amarela do WTCSync com superfícies neutras quentes.

| Papel | Escuro |
|---|---|
| Fundo | #171715 |
| Cards, cabeçalho e navegação | #232320 |
| Campos e superfície elevada | #2D2D28 |
| Ação primária e contador de não lidas | #F5C518 / texto #1C1B1B |
| Seleções e avatares | #39321C / texto #F7D978 |
| Mensagem enviada | #39321C / texto #F4F0E7 |
| Texto secundário | #B8B3A7 |
| Contorno decorativo | #44443C |
| Contorno de campo | #858578 |

`BrandAccent` e `OnBrandAccent` são reservados para ações e notificações.
`YellowBadge` e `YellowText` representam seleções e destaques suaves.
`MessageSentBg` e `MessageSentText` representam mensagens enviadas e pendentes.
`InputSurface` diferencia campos dos cards. Foco usa o acento legível `SaveText`.

Campos mantêm contorno com contraste mínimo de 3:1; bordas decorativas são mais discretas.
Textos e recibos de leitura têm pares verificados com contraste mínimo de 4.5:1.
A suíte PaletteContrastTest cobre os dois modos, campos, ações, seleções e mensagens.

O escopo é visual: nenhum endpoint, ViewModel, fluxo de autenticação, atendimento,
edição/exclusão, entrega de mensagens ou notificações foi substituído.
Conferência visual no dispositivo continua necessária após instalar o APK.
