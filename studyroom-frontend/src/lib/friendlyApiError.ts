/** Mensagens curtas para o público da extensão (terceira idade). */
export function friendlyBookingError(e: unknown): string {
  if (e && typeof e === 'object' && 'response' in e) {
    const status = (e as { response?: { status?: number } }).response?.status
    if (status === 400) {
      return 'Horário inválido ou sala já reservada neste período. Escolha outro horário.'
    }
    if (status === 401) {
      return 'Sessão expirada. Saia e entre novamente.'
    }
    if (status === 403) {
      return 'Você não tem permissão para fazer esta reserva.'
    }
  }
  return 'Não foi possível concluir a reserva. Tente novamente em instantes.'
}
