# WTCSync Design System

Referência do fork para manter a mesma identidade em autenticação, CRM, atendimento,
campanhas, segmentos e área do cliente. O código em `theme/` é a fonte dos tokens.

## Identidade e cores

Grafite quente, superfícies claras no tema diurno e amarelo como assinatura.
As cores são pares semânticos; nunca reutilizar o fundo de um botão para representar
uma mensagem ou um estado de erro.

| Papel | Token | Tema escuro |
|---|---|---|
| Página | BackgroundCream | #171715 |
| Card/cabeçalho/navegação | CardSurface / HeaderBg / NavBarBackground | #232320 |
| Campo/superfície elevada | InputSurface / ActionCardBg | #2D2D28 |
| Ação principal | BrandAccent / OnBrandAccent | #F5C518 / #1C1B1B |
| Seleção/avatar | YellowBadge / YellowText | #39321C / #F7D978 |
| Mensagem enviada/pendente | MessageSentBg / MessageSentText | #39321C / #F4F0E7 |
| Texto | TextPrimary / TextSecondary | #F4F0E7 / #B8B3A7 |
| Erro/sucesso/informação | Error*, Success*, Info* | Pares próprios da paleta |

Usar os tokens também para ícones e labels. Cor de estado nunca é a única informação:
manter o texto do status, recibo, validação ou pendência.
MaterialTheme recebe o mesmo mapeamento, evitando cores padrão roxas em componentes M3.

## Bordas

Todas as bordas de superfícies usam `WtcBorders`; não criar BorderStroke nas telas.

| Uso | API | Espessura / cor |
|---|---|---|
| Card, avatar, mensagem e superfície informativa | WtcBorders.subtle() | 1dp / ListBorderLight |
| Campo, busca e controle com contorno | WtcBorders.control() | 1dp / BorderColor |
| Filtro selecionado | WtcBorders.selected() | 1dp / SaveText |
| Divisor desenhado | WtcBorders.Width.toPx() | 1dp convertido para a densidade |
| Indicador de aba/foco | WtcBorders.FocusWidth | 2dp |

Bordas decorativas são discretas; campos mantêm contraste mínimo de 3:1.
OutlinedTextField usa foco M3 de 2dp e repouso de 1dp.
A navegação inferior diferencia seleção por superfície e cor, sem contorno pesado.
Sombras fortes não substituem as bordas; distinguir camadas pela superfície.

## Cantos

| Uso | Token | Raio |
|---|---|---|
| Detalhes/skeleton | WtcShapes.extraSmall | 4dp |
| Avatar retangular e elementos compactos | WtcShapes.small | 8dp |
| Cards, botões, campos e filtros | WtcShapes.medium | 12dp |
| Dialogs | WtcShapes.large | 20dp |
| Superfície ampla especial | WtcShapes.extraLarge | 28dp |
| Badge, tag e avatar circular | WtcCorners.Pill | Circular |
| Mensagem | WtcCorners.message(outgoing) | 12dp com cauda de 4dp |

Não instanciar RoundedCornerShape nas telas. Uma exceção exige novo token por papel,
com justificativa, e não um número isolado no componente.

## Tipografia e espaçamento

Nunito nos títulos e Roboto no corpo, conforme `Type.kt` e MaterialTheme.typography.
Preferir titleLarge para cabeçalhos, titleMedium para nomes, bodyMedium para descrições
e labelLarge para ações. Truncar metadados, preservando nomes e informações essenciais.

`Spacing`: xs=4, sm=8, md=12, lg=16, xl=24, xxl=32dp.
Margem de tela de 16dp, espaçamento entre cards de 8–12dp e entre seções de 24dp.
Botões e filtros interativos compartilhados têm altura mínima de 48dp.

## Componentes disponíveis

| Componente | Responsabilidade |
|---|---|
| WtcButton | Ações Primary, Secondary, Text e DangerText; loading impede reenvio |
| WtcCreateButton | Atalho de criação com ícone e texto |
| WtcCard | Contorno, superfície, raio e feedback ao toque compartilhados |
| WtcScreenHeader | Título único, ações e retorno; acento amarelo |
| WtcFilterChip | Seleção legível e cor animada |
| WtcFeedback / WtcStatePanel | Resultado, erro, estado vazio e retentativa |
| BottomNavBar | Abas por perfil, estado selecionado e animação de cor |
| ClientAvatar / StatusBadge / TagChip | Identidade, status e metadados |
| MessageBubble / PendingMessageBubble | Visual comum e estados reais do envio |

Reutilizar APIs existentes em vez de copiar componentes. Comportamento e estados
continuam nos ViewModels: o design system não inicia requests ou altera permissões.

## Movimento

| Interação | Regra |
|---|---|
| Pressionar botão/card/filtro | pressScale 1 → 0.98, SHORT=150ms |
| Cor da seleção | SHORT=150ms |
| Troca de aba | tabMotion: fade + deslocamento vertical de 8dp, 150ms |
| Reordenação de listas | wtcItemMotion, MEDIUM=250ms, chave estável por ID |
| Entrada de mensagem/item | Fade de 150ms; sem animação de saída |
| Compositor em edição / botão em loading | wtcAnimateSize, 250ms |
| Navegação entre rotas | Fade e pequeno deslocamento, LONG=350ms |
| Skeleton de campanhas | Ciclo suave de 1200ms somente enquanto carregando |

Não atrasar requests, teclado ou confirmação da ação para aguardar animações.
Atualizações de polling/recibos conservam a chave do item e não reiniciam sua entrada.
Não usar bounce, animações decorativas contínuas ou stagger em históricos extensos.

A troca de abas anima apenas o conteúdo atual: não mantém a conversa anterior
composta, seus observadores ou sua indicação de visibilidade. Preserva o
SaveableStateProvider já usado nas abas do cliente, inclusive rascunho/edição.

`LocalReduceMotion` acompanha ANIMATOR_DURATION_SCALE por ContentObserver,
inclusive mudanças com o app aberto. Quando desativadas, não há escala, deslocamento,
animação de tamanho/lista/aba ou loop de shimmer. O observer é removido ao descartar o tema.

## Exemplo

```kotlin
WtcCard(onClick = onOpen, modifier = modifier.fillMaxWidth()) {
    Column(Modifier.padding(Spacing.lg)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(description, color = TextSecondary)
    }
}
// Dentro de items(..., key = { it.id }):
ClientCard(cliente, onClick = onOpen, modifier = wtcItemMotion())
```

## Validação e evolução

- PaletteContrastTest: texto >=4.5:1 nos dois modos, incluindo mensagens/recibos e ações;
  contornos de campos >=3:1.
- Build, testes e APK pelo workflow Android Debug APK.
- Revisar no aparelho: temas claro/escuro, teclado, fonte ampliada, TalkBack,
  reduzir movimento, troca rápida de abas, envio pendente e edição/exclusão.
- Screenshots e testes no aparelho ainda precisam de execução no ambiente com ADB.
- Mudanças de token devem atualizar esta referência e conferir componentes consumidores.

As telas legadas de campanhas também consomem bordas e cantos comuns para evitar
divergência visual se forem reutilizadas. Fluxos, endpoints e regras do fork permanecem.
