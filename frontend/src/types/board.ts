export interface Board {
  id: string
  title: string
  description: string
  color: string
  ownerUserId: string
  ownerName: string
}

export type BoardMemberRole = 'VIEWER' | 'EDITOR'

export interface BoardMember {
  userId: string
  username: string
  email: string
  role: BoardMemberRole
}

