# Comparação: modo padrão × modo acessível (extensão)

## A versão antiga foi apagada?

**Não.** O StudyRoom continua com a **interface padrão** (como antes do trabalho de extensão). As melhorias de acessibilidade são um **modo opcional** que qualquer pessoa liga ou desliga na barra no topo da página.

| Modo | Como ativar | Para quê |
|------|-------------|----------|
| **Padrão** | Estado inicial, ou botão flutuante **Modo normal** | Screenshots “antes”; uso habitual |
| **Acessível (extensão)** | Botão flutuante **Modo fácil** (canto inferior direito) — liga texto maior **e** alto contraste juntos | Screenshots “depois”; testes com terceira idade |

A preferência fica salva no navegador (`localStorage`), até a pessoa limpar os dados do site.

---

## Roteiro para prints e apresentação

1. Abra o mesmo fluxo nas duas versões (ex.: login → salas → modal de reserva → minhas reservas).
2. **Antes:** barra mostra *Modo padrão*; capture a tela.
3. Toque em **Modo fácil** (botão flutuante); o rótulo muda para **Modo normal**.
4. **Depois:** repita o mesmo fluxo e capture de novo.
5. No relatório ou vídeo, use uma tabela ou slide:

   - **O que mudou** (ex.: botões maiores, resumo da reserva, mensagens simples).
   - **Por quê** (leitura, contraste, menos passos cognitivos para idosos).

---

## O que foi alterado no código (resumo)

- Botão flutuante: `AccessibilityToggle.vue` + `useAccessibility.ts`
- Estilos: `style.css` (classes `html.a11y-large-text`, `html.a11y-high-contrast`)
- Telas: login, salas, confirmação, minhas reservas, guia **Como usar**
- Componentes: `BookingSummaryCard`, `HowToUseGuide`

Detalhamento fase a fase: [`plano-acessibilidade-terceira-idade.md`](plano-acessibilidade-terceira-idade.md).

---

## Variáveis opcionais (suporte na extensão)

No `.env` do frontend (Vercel/local):

- `VITE_SUPPORT_PHONE` — exibe link de telefone no rodapé e na barra
- `VITE_SUPPORT_WHATSAPP` — número só com dígitos (ex.: `5585999999999`) para link WhatsApp
