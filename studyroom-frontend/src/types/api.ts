export interface Room {
  id: string
  name: string
  capacity: number
  resources?: string[]
  active?: boolean
}

export interface Reservation {
  id?: string
  roomId: string
  roomName?: string
  userId?: string
  userName?: string
  startTime: string
  endTime: string
  status?: string
  createdAt?: string
  /** Quantidade de pessoas informada na reserva. */
  partySize?: number
}

/** Alinhado ao JSON do backend (Jackson usa `occupied` a partir de isOccupied). */
export interface RoomStatus {
  roomId: string
  roomName: string
  occupied: boolean
  currentOccupant?: string
  until?: string
  /** Quando livre agora: início da próxima reserva que bloqueia (ISO-8601). */
  nextReservationStart?: string
  nextReservationEnd?: string
}
