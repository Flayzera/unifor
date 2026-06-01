# Plano de implementação — Acessibilidade (terceira idade)

Extensão sobre o **StudyRoom**: melhorias incrementais no **frontend** (`studyroom-frontend/`), sem mudar contrato da API na primeira fase.

**Princípio:** uma base global (tema + preferências) e depois ajustes tela a tela.

**Modo padrão × acessível:** a interface original **não foi removida**. Use a barra no topo para comparar antes/depois — ver [`comparacao-acessibilidade.md`](comparacao-acessibilidade.md).

---

## Checklist de implementação (código)

| Fase | Descrição | Status |
|------|-----------|--------|
| 0 | Alinhamento (público, testes manuais — equipe) | ⬜ Fora do repositório |
| 1 | Base global (`useAccessibility`, `AccessibilityBar`, `style.css`, `App.vue`, `main.ts`) | ✅ Concluído |
| 2 | Shell + `/como-usar` (`AppShell`, `HowToUseView`, `HowToUseGuide`, rota) | ✅ Concluído |
| 3 | Login (`LoginView` — Google, erros, link ajuda) | ✅ Concluído |
| 4 | Reserva (`RoomsView`, `BookingConfirmView`, `BookingSummaryCard`, erros amigáveis) | ✅ Concluído |
| 5 | Minhas reservas (`BookingsView`) | ✅ Concluído |
| 6 | Admin (herda estilos globais; sem refatoração dedicada) | ✅ Parcial (modo global) |
| 7 | Teste `useAccessibility.spec.ts` + docs comparação | ✅ Concluído (teste com usuários: ⬜ equipe) |

---

## Fase 0 — Alinhamento (antes de codar)

| Item                                                                            | Responsável     | Entrega                                      |
| ------------------------------------------------------------------------------- | --------------- | -------------------------------------------- |
| Definir público-alvo (ex.: idosos em espaço de estudo da universidade)          | Equipe extensão | 1 parágrafo no relatório                     |
| Lista de 3–5 tarefas críticas no app (entrar, reservar, ver reservas, cancelar) | Equipe          | Checklist de teste manual                    |
| Critério mínimo de sucesso                                                      | Equipe          | Ex.: “reservar sala em &lt; 5 min sem ajuda” |

---

## Fase 1 — Base global (fazer primeiro)

Objetivo: **modo acessível** ligável em qualquer tela (fonte maior, contraste, alvos de toque).

### Arquivos novos

| Arquivo                               | Função                                                                                                 |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `src/composables/useAccessibility.ts` | Estado `largeText`, `highContrast`; persistir em `localStorage`; aplicar classes no `<html>` ou `#app` |
| `src/components/AccessibilityBar.vue` | Barra fixa ou botão “A+ / Contraste” visível em todas as telas autenticadas (e opcional no login)      |

### Arquivos a alterar

| Arquivo         | O que fazer                                                                                                                                                                                          |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/style.css` | Variáveis CSS `--text-scale`, `--touch-min-height`; classes `.a11y-large-text` (ex.: `font-size: 1.125rem` base → `1.25rem`), `.a11y-high-contrast` (fundo branco, texto `#000`, bordas mais fortes) |
| `src/App.vue`   | Envolver `RouterView` com `AccessibilityBar` + `class` reativa do composable no wrapper                                                                                                              |
| `src/main.ts`   | Chamar `initAccessibility()` na subida do app (ler `localStorage` antes do primeiro paint, se possível)                                                                                              |

### Critério de pronto

- Alternar modo em `/rooms` e ver mudança em **nav + títulos + botões** sem recarregar.
- Preferência mantida após F5.

---

## Fase 2 — Navegação e shell

| Arquivo                       | O que fazer                                                                                                                                                                                                          |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/components/AppShell.vue` | Botões do menu com `min-h-12`, texto sempre visível (já tem rótulos — revisar tamanho); link **“Como usar”**; rodapé com **contato de suporte** (telefone/WhatsApp da extensão); `aria-current="page"` no item ativo |
| `src/router/index.ts`         | Nova rota `/como-usar` → `HowToUseView.vue` (lazy ou estática como login)                                                                                                                                            |
| `src/views/HowToUseView.vue`  | **Novo:** 4 passos em português simples + fonte grande; sem jargão                                                                                                                                                   |

### Critério de pronto

- Usuário encontra ajuda sem sair do app.
- Foco visível ao tabular (outline em links/botões).

---

## Fase 3 — Login (barreira comum em idosos)

| Arquivo                   | O que fazer                                                                                                                                                       |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/views/LoginView.vue` | Bloco **“Como entrar com Google”** (lista numerada); botão Google maior (`min-h-14`, texto `text-lg`); mensagens de erro em frase simples; link para `/como-usar` |

### Critério de pronto

- Leitura clara sem zoom do navegador no modo padrão + modo acessível.

---

## Fase 4 — Reserva de sala (fluxo principal)

| Arquivo                            | O que fazer                                                                                                                                                                                                                                              |
| ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/views/RoomsView.vue`          | Labels explícitos nos campos de data/hora; botão **“Reservar”** grande; antes de enviar, abrir modal ou ir para confirmação com **resumo legível** (sala, data, horário, pessoas); mensagens de erro humanizadas (mapear textos do `err` / `bookingErr`) |
| `src/views/BookingConfirmView.vue` | Tela de confirmação com **dois botões grandes**: “Confirmar reserva” / “Voltar e corrigir”; título `h1` único por página                                                                                                                                 |
| `src/components/StatusBadge.vue`   | Já tem texto + cor — garantir contraste no modo alto contraste                                                                                                                                                                                           |

### Opcional (mesma fase)

| Arquivo                                 | O que fazer                                                            |
| --------------------------------------- | ---------------------------------------------------------------------- |
| `src/components/BookingSummaryCard.vue` | **Novo:** componente reutilizável do resumo (usado em Rooms + Confirm) |

### Critério de pronto

- Fluxo: escolher intervalo → ver resumo → confirmar → feedback claro de sucesso.

---

## Fase 5 — Minhas reservas

| Arquivo                      | O que fazer                                                                                                                                                                                   |
| ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/views/BookingsView.vue` | Cards com mais padding; `StatusBadge` já integrado — aumentar fonte no modo acessível; confirmação de cancelamento com texto explícito; estado vazio amigável (“Você ainda não tem reservas”) |

---

## Fase 6 — Admin (prioridade baixa para extensão)

| Arquivo                   | O que fazer                                                                                                                                |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `src/views/AdminView.vue` | Só se o público idoso incluir gestores: mesmas classes globais da Fase 1; tabelas com fonte maior — **não bloquear** entrega das fases 1–5 |

---

## Fase 7 — Qualidade e evidência (extensão)

| Item                        | Onde                                                                                  |
| --------------------------- | ------------------------------------------------------------------------------------- |
| Teste do toggle acessível   | `src/composables/useAccessibility.spec.ts` (opcional) ou teste e2e manual documentado |
| Checklist WCAG rápido       | Marcar no [`scope-checklist.md`](scope-checklist.md) ou relatório                     |
| Teste com 2 usuários idosos | Registrar no relatório técnico: o que confundiu, o que melhorou                       |
| Atualizar README extensão   | 1 parágrafo em [`README.md`](../README.md) apontando para este plano                  |

---

## Ordem sugerida de PRs (equipe)

1. `feature/a11y-base` — Fase 1 (`useAccessibility`, `style.css`, `App.vue`)
2. `feature/a11y-shell-help` — Fase 2 (`AppShell`, `HowToUseView`, rota)
3. `feature/a11y-login` — Fase 3
4. `feature/a11y-booking-flow` — Fase 4 (+ `BookingSummaryCard` se fizer sentido)
5. `feature/a11y-bookings` — Fase 5

---

## O que **não** entra na 1ª leva (backlog)

- Login só por e-mail (exige decisão de produto + Firebase)
- Leitor de tela customizado / TTS nativo
- Idioma além de pt-BR
- Mudanças no backend (só se precisarem mensagens de erro traduzidas no `GlobalExceptionHandler`)

---

## Referência rápida — mapa de arquivos

```
studyroom-frontend/src/
├── composables/useAccessibility.ts     ← NOVO (Fase 1)
├── components/
│   ├── AccessibilityBar.vue            ← NOVO (Fase 1)
│   ├── AppShell.vue                    ← Fase 2
│   ├── StatusBadge.vue                 ← Fase 4 (contraste)
│   └── BookingSummaryCard.vue          ← NOVO opcional (Fase 4)
├── views/
│   ├── LoginView.vue                   ← Fase 3
│   ├── HowToUseView.vue                ← NOVO (Fase 2)
│   ├── RoomsView.vue                   ← Fase 4
│   ├── BookingConfirmView.vue          ← Fase 4
│   └── BookingsView.vue                ← Fase 5
├── App.vue                             ← Fase 1
├── main.ts                             ← Fase 1
├── style.css                           ← Fase 1
└── router/index.ts                     ← Fase 2
```

---

_Arquivos extras:_ `src/lib/friendlyApiError.ts`, `docs/comparacao-acessibilidade.md`, `studyroom-frontend/.env.example` (suporte opcional)._
